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

package cs.karibu.main; 

import java.net.UnknownHostException;
import java.util.*;

import cs.karibu.backend.*;
import cs.karibu.backend.standard.*;
import cs.karibu.utilities.PropertyReader;

/** Main program for the default Karibu backend
 * storage daemon. 
 * 
 * Responsible for fetching all messages on
 * the 'storage-queue' and storing them
 * in the Mongo database.
 *  
 * To be run in a 'nohup' or 'screen' as a daemon 
 * on the MQ tier machines. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * @author Peter Urbak, Aarhus University
 * @version 2013-06-10
 */ 
public class StorageDaemon {
  
	/**
	 * @param args 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException, UnknownHostException { 
    if (args.length != 2) {
      System.out.println("usage: StorageDaemon " +
          "(mongo|monmongo)" +
          "<resource root folder> " ); 
      System.out.println(
          "1st parameter defines if output is to MongoDB, or monitored to MongoDB." +
          "The <resource root folder> should point to the " +
          "root folder containing three properties file to be used " +
          "for configuring the exchange, queue, and mongo properties." +
          " Example: StorageDaemon mongo resource/hbc-lab." +
          " NB: Use the unix convention of using /."
          );
      System.exit(-1);
    }
    String backendType = args[0];
    String resourceFolderRoot = args[1];
    
    // determine type of backend
    boolean useMongoDB = 
        backendType.equalsIgnoreCase("mongo") 
        ||
        backendType.equalsIgnoreCase("monmongo");
    boolean useMonitoring =
        backendType.equalsIgnoreCase("monmongo");
    
    if ( ! useMongoDB ) {
      System.out.println("ERROR: StorageDaemon only support mongo db storage.");
      System.exit(-1);
    }
    
    // read in the property files
    PropertyReader rr = new PropertyReader(resourceFolderRoot);
    Properties exchangeProperties = rr.readPropertiesFailFast("exchange");
    Properties queueProperties = rr.readPropertiesFailFast("queue");

    Properties mongoProperties = rr.readPropertiesFailFast("mongo");
    
    // Create the builder object and configure it according to
    // the given parameters on the command line
    MessageReceiverEndpointFactory.Builder theBuilder = new MessageReceiverEndpointFactory.Builder();
    
    // Configure for RabbitMQ on given exchange and queue; and
    // configure for MongoDB storage on given mongo
    theBuilder = theBuilder.
        exhangeAndQueueProperties(exchangeProperties, queueProperties).
        mongoDBProperties( mongoProperties );
    if ( useMonitoring ) {
      theBuilder = theBuilder.monitorAndOutputStatisticsInInterval(50);
    }

    // Let the builder create the message end point
    MessageReceiverEndpoint messageReceiverEndpoint;
    messageReceiverEndpoint = theBuilder.   
        build();
    
    // Create the timer for pushing stats into the DB
    Timer statTimer = new Timer();
    statTimer.schedule( new FlushStatisticsTask(messageReceiverEndpoint), 
        3600 * 1000L, 3600 * 1000L);
    // No need to install shutdown hook, as the timer
    // will be removed when JVM is shut down.
        
    // and finally the daemon to handle polling from the
    // queue and processing it.
    StandardDaemon daemon = 
        new StandardDaemon("StorageDaemon", messageReceiverEndpoint);
	  
	  daemon.startAndJoin();
	} 

} 

class FlushStatisticsTask extends TimerTask {
  
  private MessageReceiverEndpoint mre;
  public FlushStatisticsTask(MessageReceiverEndpoint mre) {
    this.mre = mre;
  }
  public void run() {
    mre.getStatistic().flushToStorage();
  }
}
