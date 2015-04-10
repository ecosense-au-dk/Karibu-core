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
 
import java.util.*; 

import org.apache.commons.lang.exception.ExceptionUtils; 
import org.slf4j.*; 

import com.mongodb.*;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.serialization.Deserializer;
import dk.au.cs.karibu.testdoubles.*;
import dk.au.cs.karibu.utilities.DeadLetterDeserializer;
 
/** Standard implementation of the server request handler. 
 *  
 * Responsibility: decode the message into producer code and payload;
 * fetch the appropriate deserializer and use it to deserialize the
 * payload into a MongoDB document (BSON); and finally process/store it in the
 * injected storage in the collection named as given by the producer
 * code.
 *  
 * You must inject the storage as well as the factory object that can
 * map producer codes to deserializers and thus provide the receive
 * method with the proper deserialization method for the given
 * received payload.
 *  
 * Will handle format errors of the received payload by accepting the
 * message but storing it as binary data (the raw payload) in a
 * collection named by the constant
 * WRONG_FORMAT_COLLECTION_NAME_PREFIX. Each document in the storage
 * only has a single property whose key is 'payload' and the raw
 * binary message as value.
 * 
 * Will handle missing deserializers for a given producer code by
 * accepting the message and storing it as binary data in a collection
 * name DEADLETTER_COLLECTION_NAME_PREFIX+producer code (the producer
 * code prefixed with DEADLETTER_COLLECTION_NAME_PREFIX) and otherwise
 * identical to how wrong formatted messages are treated.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public final class StandardServerRequestHandler implements ServerRequestHandler { 
   
  public static final String WRONG_FORMAT_COLLECTION_NAME_PREFIX = 
    "WRONGFORMAT"; 
  public static final String DEADLETTER_COLLECTION_NAME_PREFIX = 
    "DEADLETTER"; 
 
  private ProcessingStrategy storage; 
  private DeserializerFactory factory; 
  private Map<String,Deserializer> mapCode2Deserializer;  
  private Logger log; 
   
  
  private String collectionName, producerCode;
  private StatisticHandler statisticHandler;
 
  /** Create the server side request handler based on the
   * injected deserializer factory and the injected
   * backend processing/storage. You may provide
   * your own logger, or leave it to null in which case
   * a SL4J logger will be used.
   * 
   * @param backendProcesser the delegate to process the
   * BSON document
   * @param factory the abstract factory that the server
   * request handler uses to get a deserializer for the
   * message
   * @param statisticsHandler the strategy for handling
   * statistics gathering
   * @param logger if null a SL4J logger will be created, 
   * otherwise the injected one is used.
   */
  public StandardServerRequestHandler(ProcessingStrategy backendProcesser, 
                                      DeserializerFactory factory,
                                      StatisticHandler statistic,
                                      Logger logger) { 
    this.storage = backendProcesser; 
    this.factory = factory; 
    this.statisticHandler = statistic;
    
    if ( logger == null ) {
      log = LoggerFactory.getLogger(StandardServerRequestHandler.class);
    } else {
      log = logger;
    }
    mapCode2Deserializer =  
        new HashMap<String,Deserializer>(); 
    log.info("Request handler initialized (storage: "+storage.getClass().getSimpleName() 
        +", factory: "+factory.getClass().getSimpleName() 
        +", statistics: "+statisticHandler.getClass().getSimpleName() 
        +", log: "+log.getClass().getSimpleName()+")..."); 
  } 

  /** Convenience constructor, for making learning curve of Karibu
   * lower - make a server request handler with standard logging and
   * no statistics collection enabled.
   * @param backendProcesser the processing strategy for messages
   * @param factory the factory to create the deserializer for a
   * given producer code.
   */
  public StandardServerRequestHandler(ProcessingStrategy backendProcesser, 
      DeserializerFactory factory) {
    this( backendProcesser, factory, new NullStatisticHandler(), null);
  }

  @Override 
  public boolean receive(byte[] bytes) {
    // result of the processing - assumed to succeed
    boolean processingSuccess = true;
    
    // Retrieve producer code 
    producerCode =  
        new String(Arrays.copyOfRange(bytes, 0, PRODUCER_CODE_LENGTH)); 
    // assume that the collectionName will be identical to the producer code
    collectionName = producerCode;
     
    byte payload[] =  
        Arrays.copyOfRange(bytes, PRODUCER_CODE_LENGTH, bytes.length); 
    
    // Collect statistics
    statisticHandler.notifyReceive(producerCode, bytes.length);
     
    Deserializer deserializer = null; 
    // Get the deserializer, optimize by caching the reference 
    deserializer = mapCode2Deserializer.get(producerCode); 
    if ( deserializer == null ) { 
      deserializer = factory.createDeserializer(producerCode); 
      if ( deserializer != null ) { 
        mapCode2Deserializer.put(producerCode, deserializer); 
        log.info("Caching the deserializer ("+deserializer+")"); 
      } 
    } 
     
    BasicDBObject dbo = null; 
    // If the deserializer is null, then we have encountered 
    // an unknown producer - store the raw payload in 
    // a collection named DEADLETTER_CODE and suffixed with
    // the producer code.
    if ( deserializer == null ){
      collectionName = StandardServerRequestHandler.DEADLETTER_COLLECTION_NAME_PREFIX + producerCode;
      log.info("DeadLetter: Unknown producer code ("+ producerCode+"),"+ 
          " stored binary in collection "+collectionName); 
      deserializer = new DeadLetterDeserializer(); 
    } 
     
    // Try to create the DBO. Our Mongo JSON parser may throw 
    // a runtime exception if the format is wrong! If the 
    // format is wrong we will store the message in a 
    // special collection 
    try { 
      dbo = deserializer.buildDocumentFromByteArray(payload); 
    } catch ( com.mongodb.util.JSONParseException parseException ){ 
      String theTrace = ExceptionUtils.getStackTrace(parseException);
      collectionName = WRONG_FORMAT_COLLECTION_NAME_PREFIX + producerCode;
      log.info("Illformed JSON received from producer "+producerCode+ 
          ", will store in collection "+collectionName+". "+theTrace); 
      // we can reuse the serializer used for dead letters 
      deserializer = new DeadLetterDeserializer(); 
      dbo = deserializer.buildDocumentFromByteArray(payload); 
    } catch( RuntimeException otherException ) { 
      String theTrace = ExceptionUtils.getStackTrace(otherException); 
      log.error("Unhandled runtime exception during deserialization. "+theTrace); 
    } 
        
    // if another runtime exception happened, the dbo may still be null 
    if ( dbo != null ) {  
      try {
        storage.process(collectionName, dbo );
      } catch ( MongoInternalException mie ) {
        processingSuccess = false;
        String theTrace = ExceptionUtils.getStackTrace(mie);
        String theMessage = mie.getMessage();
        if ( theMessage != null &&
            theMessage.contains("is over Max BSON size")) {
          log.error("Mongo Internal exception during storage on producer code: "+
              producerCode+ " / Message size is over MongoDB limit - the message will be dropped!");
          processingSuccess = true;
        } else {
          log.error("Mongo Internal exception during storage on producer code: "+
              producerCode+ " / "+theTrace);
        }
      } catch ( MongoException mongoexc ) {
        processingSuccess = false;
        String theTrace = ExceptionUtils.getStackTrace(mongoexc);
        log.error("Mongo exception during storage on producer code: "+
            producerCode+ " / "+theTrace);

      } catch ( Exception me ) {
        processingSuccess = false;
        String theTrace = ExceptionUtils.getStackTrace(me);
        log.error("Unhandled runtime exception during storage on producer code: "+
            producerCode+ " / "+theTrace);
      }
    } else { // dbo == null thus deserialization failed...
      // WHY SETTTING IT TO TRUE?
      processingSuccess = true;
      // EXPLANATION: Otherwise the message will not
      // be acknowledged to the MQ and thus pushed back 
      // which means we end in an infinte loop trying
      // to process it...
      
      // If deserialization fails it is programmers mistake
      // that must be caught during early testing, not caught
      // in production!
    }

    return processingSuccess;
  } 
 
  public StatisticHandler getStatistic() {
    return statisticHandler;
  }
} 
