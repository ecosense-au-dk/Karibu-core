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

package dk.au.cs.karibu.backend.standard;

import java.net.*;
import java.util.*;

import com.mongodb.BasicDBObject;

import dk.au.cs.karibu.backend.*;

/* The standard implementation of the StatisticsHandler
 * role. Must be configured with proper storage and
 * strategy to create time stamps.
 */

public class StandardStatisticHandler implements StatisticHandler {

  private class ProducerCodeStat {
    public int countMsg;
    public long totalSize;
  }

  private StatisticStorageStrategy storage;
  private TimestampStrategy timestampStrategy;

  private long maxChunkSize;
  private long totalBytesSent;
  private long totalCountMsg;
  private String maxChunkProducerCode;
  
  private Date startTimestamp, endTimestamp;

  private Map<String,ProducerCodeStat> codeMap;
  
  private String daemonIP;
  
  public StandardStatisticHandler(StatisticStorageStrategy storage, TimestampStrategy timestampStrategy) {
    this.storage = storage;
    this.timestampStrategy = timestampStrategy;
    
    startNewRecording();
  }

  @Override
  public void notifyReceive(String producerCode, long countOfBytes) {
    totalCountMsg++;
    totalBytesSent += countOfBytes;
    if ( countOfBytes >= maxChunkSize ) {
      maxChunkProducerCode = producerCode;
      maxChunkSize = countOfBytes;
    }
    ProducerCodeStat pcs = codeMap.get(producerCode);
    if ( pcs == null ) {
      pcs = new ProducerCodeStat();
      codeMap.put(producerCode, pcs);
    }
    pcs.countMsg++;
    pcs.totalSize += countOfBytes;
  }

  @Override
  public void flushToStorage() {
    // Create total statistics
    BasicDBObject dbo;
    
    dbo = buildBSONRepresentation();
    
    // and reset the timestamp
    startNewRecording();
    
    storage.store( dbo );  
  }

  public String toString() {
    return buildBSONRepresentation().toString();
  }
  
  private BasicDBObject buildBSONRepresentation() {
    BasicDBObject dbo;
    dbo = new BasicDBObject();
    
    dbo.put("DaemonIP", daemonIP);
    
    dbo.put("MaxChunkProducerCode", maxChunkProducerCode);
    dbo.put("MaxChunkSize",  maxChunkSize);
    
    dbo.put("TotalBytesSent", totalBytesSent);
    dbo.put("TotalCountMsg", totalCountMsg);
    
    // Statistics on each producer code
    ArrayList<BasicDBObject> listStatOnEachCode = new ArrayList<BasicDBObject>();
    for( String key : codeMap.keySet() ) {
      ProducerCodeStat pcs = codeMap.get(key);
      BasicDBObject item = fillProducerStats( key, pcs.countMsg, pcs.totalSize );
      listStatOnEachCode.add(item);
    }
    dbo.put("CodeStatList", listStatOnEachCode);
    // time stamps
    dbo.put("StartTimestamp", startTimestamp);
    endTimestamp = timestampStrategy.getNow();
    dbo.put("EndTimestamp", endTimestamp);
    
    dbo.put("ChunkTimeInMs", endTimestamp.getTime() - startTimestamp.getTime());
    return dbo;
  }

  private BasicDBObject fillProducerStats(String producerCode, 
      long countOfMsg, long totalSizeBytes) {
    BasicDBObject valueItem;
    valueItem = new BasicDBObject();
    valueItem.put("ProducerCode", producerCode);
    valueItem.put("CountMsg", countOfMsg);
    valueItem.put("TotalSizeBytes", totalSizeBytes);
    return valueItem;
  }

  private void startNewRecording() {
    maxChunkSize = 0L;
    maxChunkProducerCode = "";
    
    totalBytesSent = 0L;
    totalCountMsg = 0L;
    
    try {
      daemonIP = Inet4Address.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      daemonIP = "Undefined";
    }
    
    codeMap = new HashMap<String,ProducerCodeStat>(50);
    startTimestamp = timestampStrategy.getNow();
  }

  @Override
  public long getMaxChunkSize() {
    return maxChunkSize;
  }

  @Override
  public long getTotalBytesSent() {
    return totalBytesSent;
  }

  @Override
  public long getTotalCountMsg() {
    return totalCountMsg;
  }

  @Override
  public String getMaxChunkProducerCode() {
    return maxChunkProducerCode;
  }

  @Override
  public Date getStartTimestamp() {
    return startTimestamp;
  }

  @Override
  public Date getEndTimestamp() {
    return endTimestamp;
  }

  @Override
  public String getDaemonIP() {
    return daemonIP;
  }

  /** return a human readable string containing information 
   * on count of all received messages. 
   */ 
  @Override
  public String getStatusAsString() { 
    String result = new String(); 
    result += "Total count: "+ getTotalCountMsg()+""; 
    result += " Total KB: "+ (getTotalBytesSent()/1024)+""; 
    for ( String key : codeMap.keySet() ) { 
      result += "  "+ key+ " : "+ codeMap.get(key).countMsg; 
    } 
    return result; 
  }

}
