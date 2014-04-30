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
 
/** Interface encapsulating the connector to the 
 * message channel, responsible for the actual transmission 
 * over the wire of a payload of bytes from the client 
 * to the MQ. 
 *  
 * Any implementing class should ONLY implement  
 * 'raw' sending without trying to reconnect in 
 * case of failure etc. The ClientRequestHandler 
 * is responsible for reconnection behaviour! 
 *  
 * Also any implementation must implement 
 * efficient multiple invocations of 'openConnection' 
 * as the client request handler may call this 
 * repeatedly even thought the connection 
 * is already open. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * @author Peter Urbak, Aarhus University 
 */ 
public interface ChannelConnector { 
 
  /** Send a payload of bytes from the producer 
   * to the message channel/exchange, labeled with the given topic. 
   * Implementing class shall assume that the client 
   * request handler has already openend the connection. 
   *  
   * @param bytes the chunk of bytes being the payload. 
   * @param topic the message topic that determines the 
   * queue(s) the message will be flowed to.  
   */ 
  public void send(byte[] bytes, String topic) throws IOException; 
 
  /** 
   * Open a connection. Any implementation of this 
   * method MUST trash an old connection and establish 
   * a new one from scratch if this method is called; 
   * as it will be in case the StandardClientRequestHandler 
   * catches a failed 'send' and need to failover 
   * to another MQ server or try reconnecting the same. 
   */ 
  public void openConnection() throws IOException; 
 
  /** 
   * Closes a correctly working connection. 
   */ 
  public void closeConnection() throws IOException; 
   
  /** Return true iff the connection is already 
   * open.  
   * @return true if the connection is open 
   */ 
  public boolean isOpen(); 
} 
