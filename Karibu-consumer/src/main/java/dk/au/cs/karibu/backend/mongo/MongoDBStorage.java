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

package cs.karibu.backend.mongo; 
 
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils; 
import org.slf4j.*; 

import com.mongodb.*; 

import cs.karibu.backend.*;

/** MongoDB implementation of the Storage interface. 
 *  
 * Regarding connecting to replica sets, tutorial at
 *  
 * http://dev.af83.com/2010/11/02/mini-howto-using-the-replica-set-of-mongodb-in-java.html 
 *  
 * is a good source. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class MongoDBStorage 
  implements ProcessingStrategy, StatisticStorageStrategy { 

  // --*-- Fields --*--

  private static final String KARIBU_STATISTIC_COLLECTION_NAME = "karibu.statistic";
  
  private Mongo mongoDB; 
  private DB database; 
  private DBCollection coll; 
  private Logger log; 

  // --*-- Constructors --*--
	
  /** Connect the storage to a MongoDB replica set 
   *  
   * @param configuration properties of the configuration 
   */ 
  public MongoDBStorage(MongoConfiguration configuration) { 
    log = LoggerFactory.getLogger(MongoDBStorage.class);
    log.info("MongoDB initializing with configuration: "+configuration.toString());
	  
    createConnection(configuration);
  } 
	
  // --*-- Methods --*--

  private void createConnection(MongoConfiguration conf) { 

    String databaseName = conf.getDatabaseName();
    List<ServerAddress> addr = conf.getServerAddressList();
    
    // connect to the database server 
    mongoDB = null; 
    try { 
      mongoDB = new MongoClient(addr); 
    } catch (MongoException e) { 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("MongoException. "+theTrace); 
      System.exit(-1); 
    } 
    // get handle to the required database 
    database= mongoDB.getDB( databaseName ); 
    // authenticate in case there is a username in the config
    if ( conf.getUsername() != null ) {
      boolean auth = 
            database.authenticate(
                conf.getUsername(), 
                conf.getPassword().toCharArray());
      if ( ! auth ) {
        log.error("Mongo authentication failed for username: "+
            conf.getUsername() +" on DB: "+conf.getDatabaseName());
        System.out.println( "MongoDB authentication failed. Review log.");
        // Fail fast!
        System.exit(-1);
      }
    }

    // From javadoc:  
    // By default, all read and write operations will be made on the primary,  
    // but it's possible to read from secondaries by changing the read preference 
    mongoDB.setReadPreference(ReadPreference.secondaryPreferred()); 

    // From javadoc: Exceptions are raised for network issues, and server errors;  
    // waits on a server for the write operation 
    mongoDB.setWriteConcern(WriteConcern.SAFE); 

    log.info("MongoDB connected..."); 

    // Register a shutdown hook to close the mongo  
    // connection 
    Runtime.getRuntime().addShutdownHook(new Thread() { 
        public void run() { 
          log.info("MongoDB disconnected..."); 
          mongoDB.close(); 
        }}); 

  } 
	
  /**
   * {@inheritDoc}
   */
  @Override
    public void process(String producerCode, BasicDBObject dbo) {   
    // get a collection object to work with 
    coll = database.getCollection(producerCode); 
    // Insert it into Mongo 
    WriteResult writeResult = coll.insert(dbo); 
    CommandResult err = writeResult.getCachedLastError();
    if ( ! err.ok() ) {
      log.error("MongoDB insert failed with result: "+err.toString());
    }
  } 

  // Temporary methods - should not hit production code 
  @Deprecated
  public DBCollection getCollection(String collectionName) { 
    return database.getCollection(collectionName); 
  }

  @Override
  public void store(BasicDBObject dbo) {
    // get the karibu statistics collection object
    coll = database.getCollection(KARIBU_STATISTIC_COLLECTION_NAME);
    // Insert it into Mongo 
    WriteResult writeResult = coll.insert(dbo); 
    CommandResult err = writeResult.getCachedLastError();
    if ( ! err.ok() ) {
      log.error("MongoDB insert failed with result: "+err.toString());
    }
   
    System.err.println("Will store "+dbo);
  } 

} 
