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
/** Configuration object defining the 
 * characteristics of a rabbit queue. 
 *  
 * For documentation of the accessors 
 * methods, please refer to RabbitMQ 
 * documentation. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
 
public interface RabbitQueueConfiguration { 
 
  /**
   * Returns the name of the queue.
   *   
   *  @return the name of the queue.
   */  
  String getQueueName(); 
 
  /** Returns true if queue is durable,
   * i.e the messages are persisted until
   * consumed.
   * @return true if queue is durable
   */
  boolean isQueueDurable(); 
 
  /** Returns true if queue is exclusive,
   * review RabbitMQ documentation for info
   * @return true if queue is exclusive
   */
  boolean isQueueExclusive(); 
 
  /** Returns true if queue auto deletes,
   * review RabbitMQ documentation for info
   * @return true if queue auto deletes
   */
  boolean isQueueAutoDelete(); 
 
  /** Return the routing key this
   * queue uses to select messages from
   * the exchange.
   * @return the routing key
   */
  String getRoutingKey(); 
}
