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
 
package dk.au.cs.karibu.producer.rabbitmq; 
 
import java.io.IOException; 
import java.security.*; 

import org.slf4j.LoggerFactory; 
import org.slf4j.Logger; 
 

import com.rabbitmq.client.*; 

import dk.au.cs.karibu.producer.ChannelConnector;
 
/** A rabbit mq based implementation of the connection 
 * from the client to MQ channel. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class RabbitChannelConnector implements ChannelConnector { 
 
  private Channel channel; 
  private Connection connection; 
  private RabbitExchangeConfiguration exchangeConfiguration; 
  private Logger theLogger;
  
  /** Declare a message producer 
   *  
   * @param exchangeConfiguration configuration of the exchange to send to 
   * @throws IOException 
   */ 
  public RabbitChannelConnector(RabbitExchangeConfiguration exchangeConfiguration) throws IOException { 
    theLogger = LoggerFactory.getLogger(RabbitChannelConnector.class); 
    theLogger.info("I am starting..."); 
    this.exchangeConfiguration = exchangeConfiguration; 
    connection = null; channel = null; 
  } 
     
  public void send(byte[] payload, String topic) throws IOException { 
    channel.basicPublish(exchangeConfiguration.getExchangeName(), topic,  
        MessageProperties.PERSISTENT_BASIC, payload);  
  } 
 
  @Override 
  public void openConnection() throws IOException {     
    theLogger.info("openConnection: "+ exchangeConfiguration); 
    ConnectionFactory factory = new ConnectionFactory(); 
    factory.setUsername( exchangeConfiguration.getUsername() ); 
    factory.setPassword( exchangeConfiguration.getPassword() ); 
    if ( exchangeConfiguration.isSSLConnection() ) { 
      try { 
        factory.useSslProtocol(); 
      } catch (KeyManagementException e) { 
        theLogger.error("KeyManagementException: "+e.getLocalizedMessage()); 
      } catch (NoSuchAlgorithmException e) { 
        theLogger.error("NoSuchAlgorithmException: "+e.getLocalizedMessage()); 
      } 
    } 
    connection = factory.newConnection( exchangeConfiguration.getServerAddressList() ); 
 
    channel = connection.createChannel(); 
    channel.exchangeDeclare( 
        exchangeConfiguration.getExchangeName(), 
        exchangeConfiguration.getExchangeType(), 
        exchangeConfiguration.isExchangeDurable(), 
        exchangeConfiguration.isExchangeAutoDelete(), null);

    // The queue and the binding between queue and exchange is defined by the server side! 
  } 
 
  @Override 
  public void closeConnection() throws IOException { 
    // TODO - catch AlreadyClosedExceptions and just log them, otherwise ignore.
    channel.close(); 
    connection.close(); 
    channel = null; connection = null; 
  } 
 
  @Override 
  public boolean isOpen() { 
    return (connection != null); 
  } 
} 
