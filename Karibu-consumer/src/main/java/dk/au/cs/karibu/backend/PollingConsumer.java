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
 
import com.rabbitmq.client.*; 
 
/** A Polling Consumer interface (Hohpe and Woolf, 2003). 
 *  
 * Used by the MessageReceiverEndpoint process, an instance of 
 * this interface must play the role of polling consumer: the 
 * central method is 'nextDelivery' that must block until 
 * a message is ready for consumption. 
 *  
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public interface PollingConsumer { 
 
  /** Open the channel to the MQ server and define the routing of
   * messages. In the RabbitMQ case, this means setting the routing
   * key so the proper topic marked messages are received.
   * @throws IOException  
   */ 
  public void openChannelAndSetRouting() throws IOException; 
 
  /** Close the channel to the MQ server again.
   * @throws IOException */ 
  public void closeChannel() throws IOException; 
 
  /** Block until a message is available from the MQ server and then
   * return the payload as a byte array
   * @return the delivered message, including
   * the byte array that constitutes the payload 
   * of the message (including the project code / 
   * Format Indicator (Hohpe and Woolf, 2003)). 
   * @throws ShutdownSignalException in case the 
   * MQ server connection is shut down this 
   * exception is cast 
   * @throws InterruptedException  
   * @throws ConsumerCancelledException  
   */ 
  public Delivery nextDelivery() throws ShutdownSignalException, 
                                      ConsumerCancelledException, 
                                      InterruptedException;

  /** Acknowledge to the MQ that the delivery
   * was successfully processed and should be
   * removed from the queue. ONLY invoke (of course)
   * if the message was indeed processed.
   * @param delivery the delivery to acknowledge
   * @throws IOException in case the socket to the MQ
   * has errors in sending the acknowledge.
   */
  public void acknowledge( Delivery delivery ) throws IOException;

  /** Request the MQ to recover unacknowledged
   * messages and requeue them for 'nextDelivery'
   * to be able to fetch them.
   * @throws IOException well the RabbitMQ may
   * throw it - probably from the underlying
   * socket layer.
   */
  public void recover() throws IOException; 
}