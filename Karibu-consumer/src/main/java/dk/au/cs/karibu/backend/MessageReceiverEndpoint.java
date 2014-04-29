/*
 * Copyright 2013 Henrik Baerbak Christensen, Aarhus University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
 
package cs.karibu.backend; 
 
import java.io.IOException; 
import java.net.ConnectException;

import org.apache.commons.lang.exception.ExceptionUtils; 
import org.slf4j.*; 
 
import com.rabbitmq.client.ShutdownSignalException; 

/** This is a Message Endpoint (Hohpe and Woolf, 2003) on the 
 * message receiver side; also called MessageReceiver in the 
 * JMS terminology. It is the top level abstraction in the
 * backtier Karibu framework and must be configured with
 * proper delegates to server the roles of message polling
 * and message processing. As it is highly configurable
 * the complexity of setting it up is also high. 
 * Therefore:
 * 
 * DO NOT INSTANTIATE IT DIRECTLY. INSTEAD USE THE BUILDER IN
 * MessageRecieverEndpointFactory.
 *  
 * It implements runnable and is required to run as a thread. 
 * Once started, the receiver endpoint will continously 
 * poll for messages using a PollingConsumer instance 
 * (Polling Consumer pattern, Hohpe and Woolf, 2003) 
 * that you provide. For every message received, 
 * it will be forwarded to the ServerRequestHandler 
 * instance that you provide for final processing.
 *  
 * If a connection failure occurs to the polling consumer
 * (in production, this is the RabbitMQ), the endpoint
 * will make an exponential backoff delay and then
 * retry the connection. The default initial wait time
 * is 500ms (increasing to 1000,2000,4000, etc.) up
 * until 10 reconnects have failed. You can specify
 * other "base delay" settings.
 * 
 * If ServerRequestHandler processing fails (in
 * production, this is typically MongoDB primary
 * failing and thus waiting for the replica set
 * to elect a new primary), then the message
 * pulled from the polling consumer is NOT
 * acknowledged (thus MQ will keep it in the queue), and
 * further processing is paused for a default
 * value of 30 seconds, after which the
 * normal operation is continued.
 *  
 * The process can be terminated by invoking 
 * 'stopReceiving', but as it is a polling consumer 
 * the receive loop will only terminate once the 
 * next message has been received. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public final class MessageReceiverEndpoint implements Runnable { 
   
  private ServerRequestHandler srh; 
  private PollingConsumer consumer; 
  private boolean isRunning; 
  private Logger log; 
  /** base delay in ms between attempting reconnect to MQ,
   * this is the value used for the first reconnection try,
   * the next ones are multiplied by a an exponential backoff
   * multiplier (1x, 2x, 4x, etc.) */
  private int baseDelayMQReconnectInMs; 
  final public static int defaultBaseDelayInMS = 500;
  
  /** delay in ms of processing when server request handler
   * did not successfully process payload. Typical
   * mongo primary election routine takes around 1 minute
   * so this delay should be pretty long.
   */
  private int notProcessedDelayMs;
  final public static int defaultNotProcessedDelayInMs = 30000;
  
  /** Package visible constructor for the MessageReceiverEndpoint.
   * Use the MessageReceiverEndpointFactory to construct a
   * correctly configured instance.
   */ 
  MessageReceiverEndpoint(PollingConsumer mc,  
      ServerRequestHandler serverRequestHandler, 
      int baseReconnectionDelayInMS, 
      Logger theLogger,
      int mongoElectDelayInMS) { 
    consumer = mc; 
    srh = serverRequestHandler; 
    baseDelayMQReconnectInMs = baseReconnectionDelayInMS; 
    log = theLogger; 
    notProcessedDelayMs = mongoElectDelayInMS;
    log.info("MessageReceiverEndpoint configured ( PollingConsumer: "+
        consumer.toString()+", MQReconnect delay: "+
        baseDelayMQReconnectInMs+", MongoElect delay: "+
        notProcessedDelayMs+" )");
  } 

  /** the number of retries made since last successful
   * receptions of messages.
   */
  private int retryCount; 

  public void startReceiving() {   
    Delivery karibuDelivery;
    retryCount = 0;
    while ( isRunning ) { 
      byte[] payload; 
 
      try { 
        // Establish the channel and set routing in MQ 
        consumer.openChannelAndSetRouting();
         
        while ( isRunning ) { 
          // fetch the payload
          karibuDelivery = consumer.nextDelivery();
          payload = karibuDelivery.getPayload();
          // let the request handler process it
          boolean processingSuccess = srh.receive(payload); 
          // reset the retry count as we have succesfully
          // received something from the consumer
          retryCount = 0;
          // and acknowledge that the request handler
          // processed the payload correctly.
          if ( processingSuccess ) {
            consumer.acknowledge(karibuDelivery);
          } else {
            // if processing failed (typical situation is
            // mongoDB not responding as replica set
            // is choosing new master) then we
            // do not acknowledge the message and sleep
            // for a while. The missing acknowledge will
            // ensure the MQ does NOT remove the msg from
            // the queue - and the delay will give Mongo
            // time to elect a new primary.
            log.info("Request handler flagged message as 'not processed', will sleep ("+notProcessedDelayMs+") ms." );
            Thread.sleep(notProcessedDelayMs);
            log.info("Will ask consumer to recover.");
            consumer.recover();
            log.info("Will continue receiving now.");
          }
        } 
        // TODO: Unfortunate coupling to RabbitMQ specific exception type here :(
      } catch ( ShutdownSignalException sse ) {
        // Happens when rabbitmq shut downs more or less gracefully
        incrementRetryCountAndWaitBeforeProceeding("MQ shutdown signal"); 
      } catch ( ConnectException connectException ) { 
        // Happens in case we cannot connect to ANY of the MQs
        // The origin is the openChannelAndSetRouting method.
        incrementRetryCountAndWaitBeforeProceeding("MQ connection exception"); 
      } catch ( Exception otherExc ) { 
        retryCount++; 
        String theTrace = ExceptionUtils.getStackTrace(otherExc); 
        log.error(theTrace); 
      } 
    } 
  }
  
  private void incrementRetryCountAndWaitBeforeProceeding(String exceptionDescription) {
    retryCount++; 
    long delay = this.calculateBackoffDelayInMs();
    log.info( exceptionDescription + ", will backoff for "+
        delay+"ms and retry (Retry #"+retryCount+")"); 
    try { 
      // wait a bit to if things get better 
      Thread.sleep( calculateBackoffDelayInMs() ); 
    } catch ( InterruptedException interExc ) { 
      String theTrace = ExceptionUtils.getStackTrace(interExc); 
      log.error(theTrace); 
    }
  } 
 
  @Override 
  public void run() { 
    isRunning = true; 
    log.info("Entering startReceiving"); 
    startReceiving(); 
    log.info("startReceiving left.");  
    try { 
      consumer.closeChannel(); 
    } catch (IOException e) { 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("IOException during closeChannel(): "+theTrace ); 
    } 
    log.info("PollingConsumer closed.");  
  } 
   
  /** To stop consuming messages, invoke this method. Note 
   * that as the thread is blocking on delivery it will 
   * not take effect until AFTER the next message has 
   * been received. 
   */ 
  public synchronized void stopReceiving() { 
    isRunning = false; 
  } 
  
  /** Get the statistics object that collect
   * on going statistics on all processed messages.
   */
  public synchronized StatisticHandler getStatistic() {
    return srh.getStatistic();
  }
 
  /** given the retry count, calculate an
   * exponential backoff delay. Note that
   * the delay is just exponentially
   * increased (1,2,4,8,16,...), there
   * is no random selection of these values.
   * There is a cut off at 10 retries.
   * 
   * @return the delay in milli seconds
   */
  private long calculateBackoffDelayInMs() {
    assert retryCount > 0;
    int capedAt10RetryCount = retryCount > 10 ? 10 : retryCount;
    long multiplier = 1 << (capedAt10RetryCount-1); 
    return baseDelayMQReconnectInMs * multiplier;
  }
} 
