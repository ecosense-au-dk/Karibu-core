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
 
package dk.au.cs.karibu.monitor; 
 
import dk.au.cs.karibu.backend.MessageReceiverEndpoint;
 
/** Implementation of the JMX storage daemon monitoring.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
 
public class Monitoring implements MonitoringMBean { 
 
  private MessageReceiverEndpoint messageReceiverEndpoint; 
   
  public Monitoring(MessageReceiverEndpoint receiverEndpoint) { 
    this.messageReceiverEndpoint = receiverEndpoint; 
  } 
 
  @Override 
  public long getProcessedMessageCount() { 
    long count = messageReceiverEndpoint.getStatistic().getTotalCountMsg();
    return count; 
  } 
 
  @Override 
  public void doShutDown() { 
    messageReceiverEndpoint.stopReceiving(); 
  } 
 
  @Override 
  public String getRequestHandlerStatus() { 
    String status = messageReceiverEndpoint.getStatistic().getStatusAsString();
    return status; 
  } 
 
} 
