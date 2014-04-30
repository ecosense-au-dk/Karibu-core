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
 
package dk.au.cs.karibu.producer; 
 
import java.io.IOException; 
import java.net.ConnectException; 
 
import org.slf4j.*; 
 
import com.rabbitmq.client.AlreadyClosedException; 
 
/** A standard implementation of the client request 
 * handler. Individual clients parameterize this with 
 * the generic type of data chunks that they send. 
 *  
 * The default behavior of (represented by the 
 * minimal constructor) will provide fail-over 
 * semantics in the send method: this means 
 * that ConnectExceptions and AlreadyClosedExeptions 
 * (RabbitMQ exception) will not make the send fail 
 * but instead make the request handler try to 
 * reconnect after a delay (default 5 secs). This 
 * is ideal when using the CS cluster as there 
 * is a high probability that if one Rabbit is 
 * down, the other one will be available.
 * 
 * A given client request handler instance is bound
 * to a specific 'producer code' (which is
 * identical to the "format indicator" pattern in 
 * Hohpe and Woolf 2003) that uniquely define the
 * sender of the message and thus indicates to
 * the backtier processing what to do with the
 * sent data.
 *  
 * Karibu uses a 8 character (ASCII / only use 
 * US characters) string as producer code: 
 * Format of codes: PPPTTVVV 
 * PPP = Three letter identifying the sub project, GFK for grundfos dorm, etc. 
 * TT = Two letter identifying the type of produced data, RE for reading, SC for sensor characteristica 
 * VVV = Three digit version identifier
 * 
 * If a client sends multiple types of messages 
 * (identified by multiple 'producer codes') then 
 * one instance of this class must be defined for each message type 
 * 
 *  
 * Review the other constructors for more specialized 
 * behavior (no fail-over, different time-out). 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 * @param <T> generic type of the data chunk to send
 */ 
public class StandardClientRequestHandler<T> implements ClientRequestHandler<T> { 
 
  private ChannelConnector connector; 
  private String dataTypeID; 
  private Serializer<T> serializer; 
  private Logger theLogger; 
  /** the delay in ms before a retry is made to the server side */ 
  private long retryDelayInMs; 
   
  /** determine whether the send will fail immediately or try 
   * to reconnect 
   */ 
  private boolean failFast = false; 
  private int reconnectTriesBeforeFailing; 
   
  /** Configure a standard client request handler with reconnect 
   * fail-over code and 5 sec reconnection attempt delay. 
   * Configured to try 5 reconnects before 'send' fails. 
   *  
   * @param producerCode Identity of the sender encoded as a character string 
   * @param connector the message producer to use 
   * @param theSerializer the serializer that can create the on the wire format 
   */ 
  public StandardClientRequestHandler(String producerCode, ChannelConnector connector, 
      Serializer<T> theSerializer) { 
    this(producerCode, connector, theSerializer, 5000, 5,  
        LoggerFactory.getLogger(StandardClientRequestHandler.class) ); 
  } 
   
  /** Configure a standard client request handler while setting advanced 
   * options like number of reconnection attempts and timeout between 
   * reconnection attempts. 
   *  
   * @param producerCode Identity of the sender encoded as a character string 
   * @param connector the message producer to use 
   * @param theSerializer the serializer that can create the on the wire format 
   * @param timeoutMSBeforeReconnect the number of ms before a reconnect is attempted again 
   * after a send failed 
   * @param noReconnectsBeforeFailing the number of times a reconnect is attempted 
   * before giving up. If it is set to 0 then no reconnects are attempted. 
   */ 
  public StandardClientRequestHandler(String producerCode, ChannelConnector connector, 
      Serializer<T> theSerializer, int timeoutMSBeforeReconnect, int noReconnectsBeforeFailing) { 
    this(producerCode, connector, theSerializer, timeoutMSBeforeReconnect, 
        noReconnectsBeforeFailing, LoggerFactory.getLogger(StandardClientRequestHandler.class) ); 
  } 
   
 
  /** Configure the standard request handler, here additionally 
   * the logging facility is externally defined - used for testing. 
   * @param producerCode Identity of the sender encoded as a character string 
   * @param connector the message producer to use 
   * @param theSerializer the serializer that can create the on the wire format 
   * @param timeoutMSBeforeReconnect time in ms before trying to reconnect and send 
   * @param noReconnectsBeforeFailing the number of times reconnections are attempted in 'send' 
   * before giving up. 
   * @param theLogger the logging mechanism to use  
   */ 
  public StandardClientRequestHandler(String producerCode, 
      ChannelConnector connector, 
      Serializer<T> theSerializer, int timeoutMSBeforeReconnect,  
      int noReconnectsBeforeFailing, Logger theLogger) { 
    this.theLogger = theLogger; 
    this.dataTypeID = producerCode; 
    this.connector = connector; 
    serializer = theSerializer; 
    retryDelayInMs = timeoutMSBeforeReconnect; 
    failFast = (noReconnectsBeforeFailing == 0); 
    this.reconnectTriesBeforeFailing = noReconnectsBeforeFailing; 
  } 
 
 
  public void send(T dataObjectToSend, String topic) throws IOException { 
    byte[] payloadHeader = dataTypeID.getBytes(); 
        
    byte[] payloadBody = serializer.serialize( dataObjectToSend ); 
     
    byte[] payload = new byte[ payloadHeader.length + payloadBody.length]; 
     
    System.arraycopy(payloadHeader, 0, payload, 0, payloadHeader.length); 
    System.arraycopy(payloadBody, 0, payload, payloadHeader.length, payloadBody.length); 
     
    sendOverConnector(topic, payload);  
  } 
 
  /** send the payload over the connector, potentially with 
   * reconnection attempts. 
   */ 
  private void sendOverConnector(String topic, byte[] payload) 
      throws IOException { 
    // Ensure the ChannelConnector is open 
    if ( !connector.isOpen() ) { connector.openConnection(); } 
     
    if ( failFast ) { 
      connector.send(payload, topic); 
    } else { 
      boolean sendSucceeded = false; 
      int retry = 0; 
      while ( ! sendSucceeded ) { 
        try { 
          connector.send(payload, topic);  
          sendSucceeded = true; 
          // we reset the retry after each successful send 
          // so we not over time stop just because some 
          // historical retries. 
          retry = 0;   
        } catch ( AlreadyClosedException exc ) { 
          if ( retry >= reconnectTriesBeforeFailing ) { 
            theLogger.info(exc.getClass().getSimpleName()+"/No more retry attempts permitted/Msg="+exc.getMessage()); 
            throw exc; 
          } 
          retry++;    
          theLogger.info(exc.getClass().getSimpleName()+"/retry="+retry+"/Msg="+exc.getMessage()); 
          try { 
            Thread.sleep(retryDelayInMs); 
          } catch (InterruptedException e) { 
            theLogger.error("InterruptedException 1 in retry sleep."); 
          } // wait a bit to see if things get better 
          connector.openConnection(); 
        } catch ( ConnectException exc ) { 
          if ( retry >= reconnectTriesBeforeFailing ) { 
            theLogger.info(exc.getClass().getSimpleName()+"/No more retry attempts permitted/Msg="+exc.getMessage()); 
            throw exc; 
          } 
          retry++;    
          theLogger.info(exc.getClass().getSimpleName()+"/retry="+retry+"/Msg="+exc.getMessage()); 
          try { 
            Thread.sleep(retryDelayInMs); 
          } catch (InterruptedException e) { 
            theLogger.error("InterruptedException 2 in retry sleep."); 
          } // wait a bit to see if things get better 
          connector.openConnection(); 
        }  
      } 
    } 
  } 
} 
