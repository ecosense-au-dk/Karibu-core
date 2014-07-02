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

import dk.au.cs.karibu.producer.ChannelConnector;

/** A channel connector that uses a blocking queue to provide 
 * in memory channel connection in a threaded environment. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class BlockingQueueBasedChannelConnector implements ChannelConnector {

  BlockingQueuePollingConsumer blockingQueuePollingConsumer; 
  public BlockingQueueBasedChannelConnector(BlockingQueuePollingConsumer mc) { 
    this.blockingQueuePollingConsumer = mc; 
  } 
 
  @Override 
  public void send(byte[] bytes, String topic) throws IOException { 
    blockingQueuePollingConsumer.pushAMessage( bytes ); 
  } 
 
  int openCloseCount = 0;
  @Override 
  public void openConnection() throws IOException {
    openCloseCount++;
  } 
 
  @Override 
  public void closeConnection() throws IOException {
    openCloseCount--;
  } 
 
  @Override 
  public boolean isOpen() { 
    return openCloseCount == 0; 
  } 
}
