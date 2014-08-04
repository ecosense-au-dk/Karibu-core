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
 
package dk.au.cs.karibu.testdoubles;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.*;

import org.slf4j.Logger;

import com.rabbitmq.client.*;

import dk.au.cs.karibu.backend.*;

/** An in-single-VM polling consumer, used for  
 * running JUnit test of the MessageReceiverEndpoint. 
 *  
 * The Testing API includes methods to 
 * insert messages that appears to come from 
 * the MQ server (pushAMessage) as well as 
 * a method to simulate a shutdown exception 
 * occuring. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class BlockingQueuePollingConsumer implements PollingConsumer {

  BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(20); 
  // every open increase and every close decrease the
  // channelOpenCloseCount for test code to ensure
  // every open is properly matched by close.
  private int channelOpenCloseCount;
  private Logger logger;
  
  /** simulate RabbitMQ deliveryTag */
  private long tagCount = 0; 
   
  public BlockingQueuePollingConsumer() {
    channelOpenCloseCount = 0;
  } 

  public BlockingQueuePollingConsumer(Logger logger) {
    super();
    this.logger = logger;
  } 
 
  /** Test inspection method, should be 0
   * for a closed channel and 1 for an open one.
   * @return the sum of all open (+1) and close (-1)
   * operations over the course of the lifetime
   * of this polling consumer.
   */
  public int getOpenCloseCount() {
    return channelOpenCloseCount;
  }
  
  /* (non-Javadoc) 
   * @see cs.ecosense.incubator.PollingConsumer#openChannelAndSetRouting() 
   */ 
  @Override 
  public void openChannelAndSetRouting() {
    channelOpenCloseCount++;
    logger.info("OpenChannel called/Count = "+channelOpenCloseCount);
  } 
   
  /* (non-Javadoc) 
   * @see cs.ecosense.incubator.PollingConsumer#closeChannel() 
   */ 
  @Override 
  public void closeChannel() { 
    channelOpenCloseCount--; 
    logger.info("CloseChannel called/Count = "+channelOpenCloseCount);
  } 
 
  /* (non-Javadoc) 
   * @see cs.ecosense.incubator.PollingConsumer#nextDelivery() 
   */ 
  @Override 
  public Delivery nextDelivery() throws ShutdownSignalException { 
    Delivery delivery = null;
    byte[] payload = null; 
     
    if ( channelOpenCloseCount <= 0) { 
      throw new RuntimeException("Channel is not open"); 
    } 
     
    try { 
      payload =  queue.take();
      // simulate RabbitMQ retrieval without acknowledge by
      // immediately pushing back the payload
      // NOTE - CANNOT BE DONE WITH PEEK as it does not block!
      queue.put(payload); 
      delivery = new Delivery(tagCount ++, payload);
    } catch (InterruptedException e) { 
      e.printStackTrace(); 
    } 
    if ( ! exceptionToThrowStack.isEmpty() ) { 
      channelOpenCloseCount = 0;
      Exception toBeThrown = exceptionToThrowStack.pop();
      ShutdownSignalException sse = (ShutdownSignalException) toBeThrown;
      throw sse;// new ShutdownSignalException(false, true, theExceptionMessage, null); 
    } 
    return delivery; 
  } 
 
  /** Testing API method, allowing you to insert 
   * a message into the internal queue which will 
   * appear to come from the MQ server from the 
   * MessageEndpointReceiver's perspective. 
   * @param bytes the message payload 
   */ 
  public void pushAMessage(byte[] bytes) { 
    try { 
      queue.put(bytes); 
    } catch (InterruptedException e) { 
      e.printStackTrace(); 
    } 
     
  } 
   
  @Override
  public void acknowledge(Delivery delivery) throws IOException {
    try {
      queue.take();
    } catch (InterruptedException e) {
      // Failures just output to console
      e.printStackTrace();
    }
  }

  @Override
  public void recover() {
    // No op in this test spy
  }

  
  private Stack<Exception> exceptionToThrowStack =
      new Stack<Exception>();  

  /** push an exception to the stack of exceptions to
   * throw next. Used by the test suite to ensure
   * correct retry semantics when MQ is down.
   * @param theException
   */
  public void pushExceptionToBeThrownNext(
      Exception theException) {
    exceptionToThrowStack.push(theException);
  } 
}
