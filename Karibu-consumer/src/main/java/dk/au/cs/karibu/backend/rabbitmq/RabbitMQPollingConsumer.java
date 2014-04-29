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
 
package cs.karibu.backend.rabbitmq; 
 
import java.io.IOException; 
import java.security.*; 
import java.util.*; 

import org.apache.commons.lang.exception.ExceptionUtils; 
import org.slf4j.*; 

import com.rabbitmq.client.*; 

import cs.karibu.backend.*;
import cs.karibu.producer.rabbitmq.*;
 
/** A polling consumer implementation that uses RabbitMQ as 
 * message system. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class RabbitMQPollingConsumer implements PollingConsumer { 
  
  private RabbitExchangeConfiguration exchangeConfiguration; 
  private RabbitQueueConfiguration queueConfiguration; 
  private Logger theLogger; 
  private Connection connection; 
  private Channel channel; 
  private QueueingConsumer consumer; 
   
  public RabbitMQPollingConsumer(RabbitExchangeConfiguration rec, 
      RabbitQueueConfiguration rqc) { 
    this.exchangeConfiguration = rec; 
    this.queueConfiguration = rqc; 
    this.theLogger = LoggerFactory.getLogger(RabbitMQPollingConsumer.class); 
  } 
 
  @Override 
  public void openChannelAndSetRouting() throws IOException { 
    theLogger.info("openChannelAndSetRouting: Exchange:"+ exchangeConfiguration + " Queue: "+queueConfiguration ); 
    ConnectionFactory factory = new ConnectionFactory(); 
    factory.setUsername( exchangeConfiguration.getUsername() ); 
    factory.setPassword( exchangeConfiguration.getPassword() ); 
    if ( exchangeConfiguration.isSSLConnection() ) { 
      try { 
        factory.useSslProtocol(); 
      } catch (KeyManagementException e) { 
        String trace = ExceptionUtils.getStackTrace(e); 
        theLogger.error("KeyMgtException: "+trace); 
      } catch (NoSuchAlgorithmException e) { 
        String trace = ExceptionUtils.getStackTrace(e); 
        theLogger.error("NoSuchAlgoritmException: "+trace); 
      } 
    } 
    connection = factory.newConnection( exchangeConfiguration.getServerAddressList() ); 
     
    channel = connection.createChannel(); 
    channel.exchangeDeclare( 
        exchangeConfiguration.getExchangeName(), 
        exchangeConfiguration.getExchangeType(), 
        exchangeConfiguration.isExchangeDurable() ); 
     
    // 'RabbitMQ in Action' p 102 
    Map<String,Object> moreArguments = 
        new HashMap<String, Object>(); 
    moreArguments.put("ha-mode", "all"); 
    moreArguments.put("x-ha-policy", "all"); 
    // TODO: find out why this does not work! 
    channel.queueDeclare( 
        queueConfiguration.getQueueName(), 
        queueConfiguration.isQueueDurable(), 
        queueConfiguration.isQueueExclusive(), 
        queueConfiguration.isQueueAutoDelete(), moreArguments); 
    channel.queueBind( 
        queueConfiguration.getQueueName(), 
        exchangeConfiguration.getExchangeName(), 
        queueConfiguration.getRoutingKey() ); 
     
    consumer = new QueueingConsumer(channel); 
    // Tell RabbitMQ to await acknowledgement before removing
    // msg from the queue. See http://www.rabbitmq.com/tutorials/tutorial-two-java.html
    boolean autoAck = false;
    channel.basicConsume(queueConfiguration.getQueueName(), autoAck, consumer); 
    // Set the prefetch count to limit the amount of msg sent
    // to the daemons before they are acknowledged. Fixes a
    // bug that would induce an out-of-memory error in the
    // daemons during high transfer rates.
    // See http://www.rabbitmq.com/tutorials/tutorial-two-java.html 
    // in the 'fair dispatch' section
    int prefetchCount = 100; // ISSUE: what is the 'right' value here?
    channel.basicQos(prefetchCount);
  } 
 
  @Override 
  public void closeChannel() throws IOException { 
    theLogger.info("closeChannel: Exchange:"+ exchangeConfiguration + " Queue: "+queueConfiguration ); 
    channel.close(); 
    connection.close(); 
  } 
 
  private QueueingConsumer.Delivery rabbitDelivery; 
  @Override 
  public Delivery nextDelivery() throws ShutdownSignalException,  
    ConsumerCancelledException, InterruptedException { 
    rabbitDelivery = consumer.nextDelivery(); 
    byte[] payload = rabbitDelivery.getBody();
    Delivery karibuDelivery = 
        new Delivery( rabbitDelivery.getEnvelope().getDeliveryTag(), payload);
    return karibuDelivery; 
  }

  @Override
  public void acknowledge(Delivery delivery) throws IOException {
    channel.basicAck(delivery.getDeliveryTag(), false);
  }

  @Override
  public void recover() throws IOException {
    channel.basicRecover();
  }  
  
  public String toString() {
    return "RabbitMQPollingConsumer";
  }
} 
