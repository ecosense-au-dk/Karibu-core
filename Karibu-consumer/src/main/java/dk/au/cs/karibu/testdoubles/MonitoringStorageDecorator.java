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

import com.mongodb.BasicDBObject;

import dk.au.cs.karibu.backend.ProcessingStrategy;

/** A decorator just for stress testing, as does some
 * special process of the stress testing documents from
 * the 'stressit' program.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class MonitoringStorageDecorator implements ProcessingStrategy {

  private ProcessingStrategy decoratee;
  private long myCount;
  private int intervalBetweenOutput;
  
  public MonitoringStorageDecorator(ProcessingStrategy inner, int outputInterval) {
    intervalBetweenOutput = outputInterval;
    System.out.println( "*** Monitoring Storage Started with interval "+
        intervalBetweenOutput+" ***");
    decoratee = inner;
    myCount = 0L;
  }

  @Override
  public void process(String collectionName, BasicDBObject dbo) {
    decoratee.process(collectionName, dbo);
    myCount++;
    if ( myCount % intervalBetweenOutput == 0 ) {
      System.out.println("Msg # "+myCount+ " [for collection "+collectionName+"]");
    }
  }  
  public String toString() {
    return( "MonitoringStorageDecorated (interval: "+intervalBetweenOutput+
        ") on ("+decoratee.toString()+")");
  }

}
