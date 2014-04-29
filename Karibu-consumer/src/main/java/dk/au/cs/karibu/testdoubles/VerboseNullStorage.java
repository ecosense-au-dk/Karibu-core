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
 
package cs.karibu.testdoubles; 
 
import java.util.*; 

import com.mongodb.BasicDBObject; 
 

import cs.karibu.backend.ProcessingStrategy;
 
/** This null storage does nothing expect write the
 * stored element to standard output. For historical
 * reasons it however also understand a few Grundfos
 * related producer codes, retained for testing
 * purposes.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class VerboseNullStorage implements ProcessingStrategy { 

  // Local constants for some externally defined producer codes.
  static final String GRUNDFOS_KOLLEGIE_READING_V03 =  
      "GFKRE003"; // Proposal for format as described in May 2013 (version 2) document 
  static final String GRUNDFOS_KOLLEGIE_SENSORCHARACTERISTICS_V02 =  
      "GFKSC002"; // Proposal for format as described in May 2013 (version 2) document
  
  static final String GRUNDFOS_KOLLEGIE_READING_V02 =  
      "GFKRE002"; // version based upon agreed format for measurements early 2013 


  static final String GRUNDFOS_READING_STORE_TOPIC =  
      "grundfos.reading.store";  
  static final String GRUNDFOS_SENSOR_CHARACTERISTICS_STORE_TOPIC =  
      "grundfos.sensorcharacteristics.store";  

  @SuppressWarnings("deprecation") 
  @Override 
  public void process(String producerCode, BasicDBObject dbo) { 
    Date ts, serverts;
    ArrayList<BasicDBObject> reading;
    // VERSION 3 OF READINGS
    if ( producerCode.equals(GRUNDFOS_KOLLEGIE_READING_V03)) {
      ts = dbo.getDate("timestamp"); 
      serverts = dbo.getDate("servertimestamp"); 
      System.out.println("Readings: "+producerCode+" at Time: "+ts.toGMTString()); 
      //@SuppressWarnings("unchecked") 
      reading = (ArrayList<BasicDBObject>) dbo.get("reading"); 
      // Just output the first two elements
      System.out.println( " Contains: "+reading.size()+" readings.");
      if ( reading.size() >= 2 ) {
        for ( int i = 0; i < 2; i++ ) {
          BasicDBObject oneReading = reading.get(i);
          System.out.println( " "+i+":"+ oneReading.toString());
        }
      }
      // VERSION 2 OF SENSOR CHARACTERISTICS
    } else if ( producerCode.equals(GRUNDFOS_KOLLEGIE_SENSORCHARACTERISTICS_V02)) { 
      ts = dbo.getDate("timestamp"); 
      serverts = dbo.getDate("servertimestamp"); 
      System.out.println("Characteristics: "+producerCode+" at Time: "+ts.toGMTString()); 
      //@SuppressWarnings("unchecked") 
      BasicDBObject entry;
      ArrayList<BasicDBObject> sensors = (ArrayList<BasicDBObject>) dbo.get("sensorCharacteristic");
      System.out.println(" Sensor characteristics has "+ sensors.size()+ " elements.");
      if ( sensors.size() > 1 ) {
        entry = sensors.get(0);
        System.out.println(" 0: sensorId="+ entry.getInt("sensorId")+ " description="+ entry.getString("description"));
      }
      ArrayList<BasicDBObject> appartments = (ArrayList<BasicDBObject>) dbo.get("appartmentCharacteristic");
      System.out.println(" Appartment characteristics has "+ appartments.size()+ " elements.");
      if ( sensors.size() > 1 ) {
        entry = appartments.get(0);
        System.out.println(" 0: appartmentId="+ entry.getInt("appartmentId")+ 
            " floor="+ entry.getInt("floor") +
            " no="+ entry.getInt("no")
            );
      }      
      // DEPRECATED VERSION 2 OF READINGS
    } else if ( producerCode.equals(GRUNDFOS_KOLLEGIE_READING_V02)) { 
      ts = dbo.getDate("timestamp"); 
 
      //@SuppressWarnings("unchecked") 
      reading = (ArrayList<BasicDBObject>) dbo.get("reading"); 
      System.out.println(producerCode+" data: (ts: "+ts.toGMTString()+", #readings: "+reading.size()+")"); 
      if (reading.size() > 1235) { 
        System.out.println("   Sensor (2: " + reading.get(2).getDouble("value") 
            + ", 11: " + reading.get(11).getDouble("value") + ", 14: " 
            + reading.get(14).getDouble("value") + ", 23: " 
            + reading.get(23).getDouble("value") + ", 1234: " 
            + reading.get(1234).getDouble("value") + ")"); 
      } else { 
        System.out.println("   GFK Data with "+ reading.size() + " readings."); 
      } 
    } else { 
      System.out.println( producerCode + dbo.toString() ); 
    } 
  } 
 
} 
