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

import java.util.Date;

import dk.au.cs.karibu.backend.StatisticHandler;

public class NullStatisticHandler implements StatisticHandler {

  @Override
  public void notifyReceive(String producerCode, long countOfBytes) {
  }

  @Override
  public void flushToStorage() {
  }

  public Date getEndTimestamp() {
    return null;
  }

  public Date getStartTimestamp() {
    return null;
  }

  public String getMaxChunkProducerCode() {
    return null;
  }

  public long getTotalCountMsg() {
    return 0;
  }

  public long getTotalBytesSent() {
    return 0;
  }

  public long getMaxChunkSize() {
    return 0;
  }

  @Override
  public String getStatusAsString() {
    return "NullStatisticsHandler does not provide any real data";
  }

  @Override
  public String getDaemonIP() {
    return "NullStatisticsHandler has no IP";
  }

}
