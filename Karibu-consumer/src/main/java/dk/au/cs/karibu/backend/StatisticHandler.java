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

package dk.au.cs.karibu.backend;

import java.util.Date;

/* The StatisticsHandler defines the role of an
 * object for collection, caching and querying
 * statictics on the backend processing.
 * 
 * The role must be updated by the notify- methods
 * by the processing, and central statistics can
 * be queried from the accessor methods.
 * 
 * Statistics is cached over a timeinterval in
 * memory. A timer must invoke the flushToStorage
 * method at regular intervals, like e.g. hourly.
 * For every call to flushToStorage, the cached
 * statistics is written to a permanent storage
 * according to whatever StatisticStorage is
 * selected. Then the cached statistics is reset.
 * 
 * Thus statistics is collected in independent
 * chunks. To calculate accumulated statistics
 * you have to sum in the storage system.
 */

public interface StatisticHandler {

  /** notify the statistic handler that a message has been received
   * 
   * @param producerCode the code of the message received
   * @param countOfBytes the number of bytes in the message (incl.
   * the producer code).
   */
  public void notifyReceive(String producerCode, long countOfBytes);

  /** flush the current cached statistics to permanent storage */
  public void flushToStorage();

  public String getDaemonIP();
  
  public Date getEndTimestamp();

  public Date getStartTimestamp();

  public String getMaxChunkProducerCode();

  public long getTotalCountMsg();

  public long getTotalBytesSent();

  public long getMaxChunkSize();

  public String getStatusAsString();
}
