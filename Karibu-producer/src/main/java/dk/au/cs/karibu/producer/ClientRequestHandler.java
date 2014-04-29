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
 
package cs.karibu.producer; 
 
import java.io.IOException; 
 
/** A ClientRequestHandler (Patterns of Software Architecture, Vol 4) 
 * has the responsibility of sending a data object from a producer 
 * to the backend for storage and other processing. 
 *  
 * It is advised to use the standard implementation, see 
 * StandardClientRequestHandler, as it contains 
 * reconnection behaviour in case the ChannelConnector 
 * looses connection. 
 *   
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 * @param <T> the data type to send. 
 */ 
public interface ClientRequestHandler<T> { 
 
  /** Send one piece of data of type T to the receiver 
   * and label it with the given topic. The send is 
   * not threaded. Depending upon semantics of the 
   * implementing class, it may retry the send in 
   * case it fails on common connection errors, see the documentation for the 
   * StandardClientRequestHandler. 
   *  
   * @param dataObjectToSend the client side object to send 
   * @param topic the topic the data is published on. 
   * Topics are strings on the form "A.B.C", that is, 
   * three parts separated by dot. 
   * A = name of experiment/client/project; example "grundfos". 
   * B = type of the data; example "reading". 
   * C = type of backend processing; example "store". 
   *  
   * Important: Only data send on topic whose C part is "store"  
   * are stored in the backend storage in the standard  
   * EcoSense cluster setup. 
   *  
   * @throws IOException thrown immediately if the 
   * client request handler is configured NOT to try 
   * reconnections if a send fails; or thrown after the 
   * configured number of reconnection attempts have 
   * been tried without success. 
   */ 
  void send(T dataObjectToSend, String topic) throws IOException; 
} 
