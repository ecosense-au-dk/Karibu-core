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

package dk.au.cs.karibu.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.bson.types.Binary;
import org.junit.*; 

import com.mongodb.BasicDBObject; 
import com.rabbitmq.client.ShutdownSignalException;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.backend.standard.*;
import dk.au.cs.karibu.hobbydomain.*;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.testdoubles.*;

/** 
 * Integration testing of the MessageReceiverEndpoint implementation. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
public class TestMessageReceiverEndpoint {

  private static final String EXAMPLE_TOPIC = "example.reading.store"; 
  
  private static final String PRODUCER_CODE = "EXMTT001"; 
  private static final String PRODUCER_CODE_WITH_JSON_DESERIALIZER = "EXMTT002"; 
 
  private FakeObjectStorage storage; 
  private ChannelConnector connector; 
 
  private ClientRequestHandler<GameFavorite> crh; 
 
  private GameFavorite p1, p2; 
 
  private BlockingQueuePollingConsumer pollingConsumer; 
  private MessageReceiverEndpoint messageReceiver;
  
  private Thread receiverThread; 
  private SpyLogger spyLogger; 
  
  // Statistics roles
  private StatisticHandler statHandler;
  private SpyStatisticStorage spyStorage;
  private StubTimestampStrategy timestampStrategy;

 
  @Before 
  public void setup() { 
    // Override the logger so we can trace what is going on in 
    // the error handling code. 
    spyLogger = new SpyLogger(); 

    // 1) SETUP THE SERVER SIDE
    // Configure the request handler instances 
    storage = new FakeObjectStorage(); 
    DeserializerFactory factory = new DeserializerFactory() { 
      @Override 
      public Deserializer createDeserializer(String producerCode) { 
        Deserializer deserializer = null; 
        if (producerCode.equals(PRODUCER_CODE)) { 
          deserializer = new GameFavoriteDeserializer(); 
        } else if (producerCode.equals(PRODUCER_CODE_WITH_JSON_DESERIALIZER)) { 
          deserializer = new JSONDeserializer(); 
        } 
        return deserializer; 
      } 
    }; 
    
    // Set up a statistics handler for spying
    spyStorage = new SpyStatisticStorage();
    final Calendar cal = Calendar.getInstance(); 
    cal.set(2012,5,1,7,30,00);  
    timestampStrategy = new StubTimestampStrategy();
    timestampStrategy.setTime( cal );

    statHandler =
        new StandardStatisticHandler(spyStorage, timestampStrategy);
 
    // Set up the testing variants of the configuration objects 
    // for the MessageReceiverEndpoint 
    pollingConsumer = new BlockingQueuePollingConsumer(spyLogger); 
    // .... and configure it and spawn the thread 
    messageReceiver = new MessageReceiverEndpointFactory.Builder().
        pollingConsumer(pollingConsumer).
        processingStrategy(storage).
        deserializerFactory(factory).
        logger(spyLogger).
        statisticsHandler(statHandler).
        baseReconnectionDelayMs(2).
        mongoElectDelayMs(10).
        build();
        
    receiverThread = new Thread(messageReceiver, "ReceiverThread"); 
 
    // 2) SETUP THE CLIENT SIDE
    // Create a Producer Message Endpoint 
    connector = new BlockingQueueBasedChannelConnector(pollingConsumer);
    crh = new StandardClientRequestHandler<GameFavorite>(PRODUCER_CODE, 
        connector, new GameFavoriteSerializer()); 
 
    // And some simple objects to produce. 
    p1 = new GameFavorite("Henrik", "SCII"); 
    p2 = new GameFavorite("Mathilde", "MovieStar Planet"); 
  } 
  
  /** 
   * Test receiver end point in the case of no connection problems. 
   */ 
  @Test 
  public void shouldTestHappyPath() throws IOException, InterruptedException { 
    receiverThread.start(); 
 
    crh.send(p1, EXAMPLE_TOPIC); 
 
    Thread.sleep(100); // need to suspend this thread for the receiver to 
                       // consume 
 
    BasicDBObject dbo = storage.getCollectionNamed(PRODUCER_CODE).get(0); 
    assertNotNull(dbo); 
    assertEquals("Henrik", dbo.getString("name")); 
 
    messageReceiver.stopReceiving(); 
 
    crh.send(p2, EXAMPLE_TOPIC); 
    Thread.sleep(100); 
 
    dbo = storage.getCollectionNamed(PRODUCER_CODE).get(1); 
    assertNotNull(dbo); 
    assertEquals("Mathilde", dbo.getString("name"));
    
    assertEquals( 0, pollingConsumer.getOpenCloseCount() );
  } 

  private void dumpFullLog() {
    System.out.println("**** Dump full log ***");
    for ( String logentry: spyLogger.getFullLog() ) {
      System.out.println("-> "+logentry);
    }
  }
 
  /** Test the receiver end point in case connection problems occur. */ 
  @Test 
  public void shouldTestFailoverWhenMQFails() throws InterruptedException, IOException { 
    receiverThread.start(); 
 
    // this should succeed 
    crh.send(p1, EXAMPLE_TOPIC); 
    
    // wait for the threads to settle (so the consumer thread has pulled 
    // the message from the queue). 
    Thread.sleep(100); 
     
    // Validate that the consumer has indeed looked up the deserializer 
    assertEquals("INFO:Caching the deserializer (GameFavoriteDeserializer)", spyLogger.getLastLog()); 
 
    // Validate that the storage has stored the item 
    BasicDBObject dbo = storage.getCollectionNamed(PRODUCER_CODE).get(0); 
    assertNotNull(dbo); 
 
    // tell the polling consumer to throw an exception on the next call to a fecth from the queue.
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP001", null) );
    
    // this should succeed but the log must show a reconnect was initiated 
    crh.send(p2, EXAMPLE_TOPIC); 
    
    Thread.sleep(100); 

    // dumpFullLog();

    // assert that a reconnect was made 
    assertEquals("INFO:MQ shutdown signal, will backoff for 2ms and retry (Retry #1)", 
        spyLogger.getLastLog(2)); 
    assertEquals("INFO:OpenChannel called/Count = 1", spyLogger.getLastLog());
 
    
    dbo = storage.getCollectionNamed(PRODUCER_CODE).get(1); 
    assertNotNull(dbo); 
    assertEquals("Mathilde", dbo.getString("name")); 

    // dumpFullLog();

    pollingConsumer.closeChannel();
    assertEquals( 0, pollingConsumer.getOpenCloseCount() );
  } 

  /** Test exponential backup in case of MQ failure. */ 
  @Test 
  public void shouldTestExpBackoffWhenMQFails() throws InterruptedException, IOException { 
    receiverThread.start(); 
    // 'warm up' the receiver so the deserializer has been cached.
    crh.send(p2, EXAMPLE_TOPIC);     
    Thread.sleep(100); 

    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP001", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP002", null) );

    // this should succeed, but the log must contain the full log exponential backup 
    crh.send(p1, EXAMPLE_TOPIC);     
    Thread.sleep(100); 

    //dumpFullLog();

    // Assert that the object was stored (as item #1, #0 is Mathilde)
    BasicDBObject dbo = storage.getCollectionNamed(PRODUCER_CODE).get(1); 
    assertNotNull(dbo); 
    assertEquals("Henrik", dbo.getString("name")); 

    // Assert from the log that exponential backup was made during the reconnect tries
    assertEquals("INFO:MQ shutdown signal, will backoff for 2ms and retry (Retry #1)", 
        spyLogger.getLastLog(4)); 
    assertEquals("INFO:MQ shutdown signal, will backoff for 4ms and retry (Retry #2)", 
        spyLogger.getLastLog(2)); 
    assertEquals("INFO:OpenChannel called/Count = 1", spyLogger.getLastLog());
    
    // Next, ensure that the exp backoff counter is reset after a successful send, AND the
    // dublication of timing
    
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP003", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP004", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP005", null) );

    // this should succeed, but the log must contain the full log exponential backup 
    crh.send(p1, EXAMPLE_TOPIC);     
    Thread.sleep(100); 

    // Assert from the log that exponential backup was made during the five reconnect tries
    assertEquals("INFO:MQ shutdown signal, will backoff for 8ms and retry (Retry #3)", 
        spyLogger.getLastLog(2)); 
    assertEquals("INFO:OpenChannel called/Count = 1", spyLogger.getLastLog());
    
    //dumpFullLog();
  }
  
  @Test 
  public void shouldTestExpBackoffCapingAfter10RetriesWhenMQFails() throws InterruptedException, IOException { 
    receiverThread.start(); 
    // 'warm up' the receiver so the deserializer has been cached.
    crh.send(p2, EXAMPLE_TOPIC);     
    Thread.sleep(100); 

    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP001", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP002", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP003", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP004", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP005", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP006", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP007", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP008", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP009", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP010", null) );
    pollingConsumer.pushExceptionToBeThrownNext( new ShutdownSignalException(false, true, "EXP011", null) );

    crh.send(p1, EXAMPLE_TOPIC);     
    Thread.sleep(4500);
    
    // dumpFullLog();
    
    // Assert from the log that the exp backoff stops after 10 retries.
    assertEquals("INFO:MQ shutdown signal, will backoff for 1024ms and retry (Retry #10)", 
        spyLogger.getLastLog(4)); 
    assertEquals("INFO:MQ shutdown signal, will backoff for 1024ms and retry (Retry #11)", 
        spyLogger.getLastLog(2)); 
  }
 
  /** Test the receiver end point in case connection problems occur */ 
  @Test 
  public void shouldTestDelayWhenMongoElectsNewPrimary() throws InterruptedException, IOException { 
    receiverThread.start(); 
    storage.setExceptionTrigger("com.mongodb.MongoException$Network:"+
        "Write operation to server ecosensedb01.cs.au.dk failed", 1);

    // this should succeed 
    crh.send(p1, EXAMPLE_TOPIC); 
    Thread.sleep(100); 

    // this should experience a mongo election exception.
    crh.send(p2, EXAMPLE_TOPIC); 
    Thread.sleep(100); 

    assertEquals("INFO:Request handler flagged message as 'not processed', will sleep (10) ms.", 
        spyLogger.getLastLog(3)); 
    assertEquals("INFO:Will ask consumer to recover.", 
        spyLogger.getLastLog(2)); 
    assertEquals("INFO:Will continue receiving now.", 
        spyLogger.getLastLog(1)); 
 
    //dumpFullLog();
    // Validate both items are indeed stored
    BasicDBObject dbo;
    dbo = storage.getCollectionNamed(PRODUCER_CODE).get(0); 
    assertEquals("Henrik", dbo.getString("name")); 
    dbo = storage.getCollectionNamed(PRODUCER_CODE).get(1); 
    assertEquals("Mathilde", dbo.getString("name")); 
  }
 
  /** 
   * Test what happens in case the deserialization fails due to a JSON format problem 
   * in the sent payload 
   *  
   * @throws InterruptedException 
   */ 
  @Test 
  public void shouldStoreIncorrectPayloadFormatInWRONGFORMATCollection() 
      throws InterruptedException { 
  
    /*  Reenable this code to get the proper logging back! 
    messageReceiver = new MessageReceiverEndpoint(pollingConsumer, 
        requestHandler); 
    receiverThread = new Thread(messageReceiver); 
    */ 
    receiverThread.start(); 
 
    // push a proper formatted JSON message 
    String incorrectJSON = "{ \"version\": 17 }"; 
    String msg = PRODUCER_CODE_WITH_JSON_DESERIALIZER + incorrectJSON; 
    byte[] payload = msg.getBytes(); 
    pollingConsumer.pushAMessage(payload); 
 
    // give time to settle 
    Thread.sleep(100); 
 
    List<BasicDBObject> collection = storage 
        .getCollectionNamed(PRODUCER_CODE_WITH_JSON_DESERIALIZER); 
    assertNotNull("No collection found under the name " 
        + PRODUCER_CODE_WITH_JSON_DESERIALIZER, collection); 
    BasicDBObject dbo = storage.getCollectionNamed( 
        PRODUCER_CODE_WITH_JSON_DESERIALIZER).get(0); 
    assertNotNull(dbo); 
    assertEquals(17, dbo.getInt("version")); 
 
    // What happens if the format is wrong? 
    incorrectJSON = "{ \"version\": 17 {"; 
    msg = PRODUCER_CODE_WITH_JSON_DESERIALIZER + incorrectJSON; 
    payload = msg.getBytes(); 
    pollingConsumer.pushAMessage(payload); 
 
    // give time to settle 
    Thread.sleep(100); 
     
    // Validate that the wrongly formatted payload is stored in the 'wrong format' collection 
    String collectionName = 
        StandardServerRequestHandler.WRONG_FORMAT_COLLECTION_NAME_PREFIX+PRODUCER_CODE_WITH_JSON_DESERIALIZER;
    collection = storage.getCollectionNamed(collectionName); 
    assertNotNull("No collection found under the wrong format name: "+collectionName, collection); 
     
    dbo = collection.get(0); 
    assertNotNull(dbo); 
    Binary thePayload = (Binary) dbo.get("payload"); 
    String asString = new String(thePayload.getData()); 
    assertEquals("{ \"version\": 17 {", asString); 
     
    // Validate that the internal logging has noted the event 

    // dumpFullLog();
    assertTrue( "The log does not contain a record of the wrong format event",  
        spyLogger.getLastLog().contains("INFO:Illformed JSON received from producer EXMTT002, will store in collection WRONGFORMAT") );
    
  }  
  
  @Test
  public void shouldValidateStatisticsCollection() throws IOException, InterruptedException {
    // Simulate 2 uploads
    shouldTestHappyPath();
    
    StatisticHandler theStats = messageReceiver.getStatistic();
    
    // validate that the statistics have collected the proper data
    assertEquals( 2, theStats.getTotalCountMsg() );
    assertEquals( 52, theStats.getTotalBytesSent() );
    
    assertEquals( PRODUCER_CODE, theStats.getMaxChunkProducerCode() );
    assertEquals( 33, theStats.getMaxChunkSize() );
    
    assertEquals( "Total count: 2 Total KB: 0  EXMTT001 : 2", theStats.getStatusAsString());
       
    // System.out.println( statHandler.getStatusAsString() );
  }
}

class JSONDeserializer implements Deserializer { 
  
  @Override 
  public BasicDBObject buildDocumentFromByteArray(byte[] payload) { 
    // The binary payload is actually a string in JSON format 
    String asJSON = new String(payload); 
     
    // and Mongo has utils to convert that :) 
    BasicDBObject dbo = null; 
     dbo = (BasicDBObject) com.mongodb.util.JSON.parse(asJSON); 
  
    return dbo; 
  } 
  public String toString() {
    return "Local JSONDeserializer";
  }
} 

