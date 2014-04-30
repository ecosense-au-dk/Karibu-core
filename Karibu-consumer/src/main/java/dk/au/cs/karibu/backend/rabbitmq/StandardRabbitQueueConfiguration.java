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

package dk.au.cs.karibu.backend.rabbitmq; 

import java.util.Properties;

import dk.au.cs.karibu.common.FailFast;

/**
 * Standard implementation of the <code>RabbitQueueConfiguration</code>
 * interface.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University
 * @author Peter Urbak, Aarhus University 
 * @version 2013-06-10
 */ 
public class StandardRabbitQueueConfiguration
  implements RabbitQueueConfiguration {

  // --*-- Fields --*--

  private String queueName, routingKey;
  private boolean queueDurable, queueExclusive, queueAutoDelete; 

  private final String QUEUE_NAME = "name";
  private final String QUEUE_DURABLE = "durable";
  private final String QUEUE_EXCLUSIVE = "exclusive";
  private final String QUEUE_AUTO_DELETE = "autoDelete";
  private final String ROUTING_KEY = "routingKey";
	
  // --*-- Constructors --*--
	
  /**
   * Constructs a <code>StandardStorageQueueConfiguration</code>.
   * 
   * @param queueName
   * @param queueDurable
   * @param queueExclusive
   * @param queueAutoDelete
   * @param routingKey
   */
  public StandardRabbitQueueConfiguration(String queueName,
                                          boolean queueDurable, boolean queueExclusive,
                                          boolean queueAutoDelete, String routingKey) {
    init(queueName, queueDurable, queueExclusive,
         queueAutoDelete, routingKey);
  }
	
  /**
   * Constructs a <code>StandardStorageQueueConfiguration</code>.
   * 
   * @param queueProperties the properties of the queue.
   */
  public StandardRabbitQueueConfiguration(Properties queueProperties) {
    String queueName = 
      FailFast.readProperty(queueProperties, QUEUE_NAME);
    boolean queueDurable = 
      FailFast.readProperty(queueProperties,QUEUE_DURABLE).equalsIgnoreCase("true");
    boolean queueExclusive = 
      FailFast.readProperty(queueProperties,QUEUE_EXCLUSIVE).equalsIgnoreCase("true");
    boolean queueAutoDelete = 
      FailFast.readProperty(queueProperties,QUEUE_AUTO_DELETE).equalsIgnoreCase("true");
    String routingKey = 
      FailFast.readProperty(queueProperties,ROUTING_KEY);
    init(queueName, queueDurable, queueExclusive,
         queueAutoDelete, routingKey);
  }
	
  // --*-- Methods --*--
	
  /**
   * Initializes the <code>StandardRabbitQueueConfiguration</code> object.
   * 
   * @param queueName -
   * @param queueDurable -
   * @param queueExclusive -
   * @param queueAutoDelete -
   * @param routingKey -
   */
  private void init(String queueName, boolean queueDurable,
                    boolean queueExclusive, boolean queueAutoDelete,
                    String routingKey) {
    this.queueName = queueName;
    this.queueDurable = queueDurable;
    this.queueExclusive = queueExclusive;
    this.queueAutoDelete = queueAutoDelete;
    this.routingKey = routingKey;
  }
	
  /**
   * {@inheritDoc}
   */
  @Override
    public String toString() {
    return "StandardStorageQueueConfiguration" +
      " (queueName : " + getQueueName() +
      ", queueDurable : " + isQueueDurable() +
      ", queueExclusive : " + isQueueExclusive() +
      ", queueAutoDelete : " + isQueueAutoDelete() +
      ", routingKey : " + getRoutingKey() + 
      ")"; 
  }
	
  // -*- Getters/Setters -*-

  /**
   * {@inheritDoc}
   */
  public String getQueueName() {
    return queueName;
  }

  /**
   * {@inheritDoc}
   */
  public String getRoutingKey() {
    return routingKey;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isQueueDurable() {
    return queueDurable;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isQueueExclusive() {
    return queueExclusive;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isQueueAutoDelete() {
    return queueAutoDelete;
  }
 
} 
