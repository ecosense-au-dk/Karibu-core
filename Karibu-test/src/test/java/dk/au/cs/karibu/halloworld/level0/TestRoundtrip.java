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
 
package dk.au.cs.karibu.halloworld.level0; 
 
import static org.junit.Assert.*; 

import java.io.IOException; 

import org.junit.*; 

import com.mongodb.BasicDBObject; 

import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.backend.standard.StandardServerRequestHandler;
import dk.au.cs.karibu.hobbydomain.*;
import dk.au.cs.karibu.serialization.*;
import dk.au.cs.karibu.testdoubles.InVMInterProcessConnector;

/** JUnit learning test, a Hello World example of 
 * using the Karibu framework for defining a producer  
 * of data; and example of using the backend  
 * framework for handling the data tier storage. 
 *  
 * This is stage 1 in the testing environment, that 
 * is, the environment is not distributed. 
 *  
 * NOTE: The JUnit code below contains redundant code 
 * and the example classes are contained within this 
 * file. This is on purpose to make the code as easy 
 * to read as possible! 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public class TestRoundtrip { 
 
  /** Our example of some arbitrary domain class that 
    suits your particular client side need.  
    All example classes are defined at the end of 
    this source file. 
   */ 
  GameFavorite ex; 
   
  /** Create object from our domain class for 
   * use in all the test cases */ 
  @Before 
  public void setup() { 
    // Our domain class encapsulate a person name and a favorite 
    // game. 
    ex = new GameFavorite("Henrik", "StarCraft II");     
  } 
   
  // ==== STEP 1: You must define a way to serialize and 
  //              later deserialize your objects 
   
  /** The client side must provide a serializer object 
   * that converts from the in-memory representation of 
   * object(s) into a byte array suitable for transmission 
   * to the backend.  
   *  
   * This test case demonstrates the process of doing this. 
   */ 
  @Test 
  public void shouldHaveIdemPotentSerialization() { 
    Serializer<GameFavorite> theSerializer =  
        new GameFavoriteSerializer(); 
     
    // Serialize the domain object into a byte array 
    // for on the wire transmission 
    byte[] onTheWirePayload = theSerializer.serialize(ex); 
     
    // Smoketest that the serialization is proper 
    assertNotNull( onTheWirePayload ); 
     
    assertEquals( 'H', onTheWirePayload[0]); 
    assertEquals( 'k', onTheWirePayload[5]); 
    assertEquals( '|', onTheWirePayload[6]); 
    assertEquals( 'S', onTheWirePayload[7]); 
    assertEquals( 'C', onTheWirePayload[11]); 
    assertEquals( 't', onTheWirePayload[15]); 
     
    // Next, deserialize it into a MongoDB BSON object 
    Deserializer theDeserializer = new GameFavoriteDeserializer(); 
        
    BasicDBObject dbo =  
      theDeserializer.buildDocumentFromByteArray(onTheWirePayload); 
     
    // And smoketest that indeed the mongo object corresponds to 
    // the original client side object 
    assertNotNull(dbo); 
     
    assertEquals("Henrik", dbo.get("name")); 
    assertEquals("StarCraft II", dbo.get("game")); 
  } 
 
  /** Validate the serializer/deserializer by more test cases */  
  @Test 
  public void shouldHaveIdemPotentSerializationTriangulated() { 
    GameFavorite  
      ex1 = new GameFavorite("Birte", "Age of Empires"), 
      ex2 = new GameFavorite("Kalle", "Modern Warfare"); 
    createSerializerDeserializerPair(); 
    BasicDBObject dbo1, dbo2; 
     
    dbo1 = theDeserializer. 
      buildDocumentFromByteArray( theSerializer.serialize(ex1)); 
    dbo2 = theDeserializer. 
      buildDocumentFromByteArray( theSerializer.serialize(ex2)); 
     
    // validate  
    assertEquals("Birte", dbo1.get("name")); 
    assertEquals("Modern Warfare", dbo2.get("game")); 
  } 
 
  // These classes are duplicated for the sake of 
  // making the first test case as easy to read as 
  // possible. 
  Serializer<GameFavorite> theSerializer; 
  Deserializer theDeserializer; 
  private void createSerializerDeserializerPair() { 
    theSerializer = new GameFavoriteSerializer(); 
    theDeserializer =  new GameFavoriteDeserializer(); 
  } 
  
  /** A sent object should end up in the Mongo storage. This test case 
   * defines a stubbed environment that can validate this. This is 
   * mainly a test of the core framework code (which hopefully should work) 
   * but demonstrates what needs to be defined to setup the 
   * communication with the backend as seen from the client. 
   */ 
  @Test 
  public void shouldStoreASentObject() { 
    // First, for the sake of testing, we have to create the 
    // server side stub and fake objects. THIS IS NOT PART 
    // OF PRODUCTION CLIENT SIDE DEVELOPMENT. See the 
    // Hello World documentation! 
     
    // Create a fake object storage 
    LocalFakeObjectStorage storage = new LocalFakeObjectStorage(); 
    // Create the factory which the server side uses to 
    // lookup the deserializer 
    DeserializerFactory factory = new DeserializerFactory() { 
      @Override 
      public Deserializer createDeserializer(String producerCode) { 
        if (! producerCode.equals(GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE) ) { 
          throw new RuntimeException("This factory only supports the example deserializer."); 
        } 
        return new GameFavoriteDeserializer(); 
      } 
    }; 
    // Create a server side request handler 
    ServerRequestHandler srh =  
      new StandardServerRequestHandler(storage, factory); 
    // === End of creating test versions of the server side objects 
     
    // === CLIENT SIDE 
    // Create the client side objects - these objects must always 
    // be defined for clients 
    ChannelConnector connector; 
    // Here we use an in-memory connector, a normal client shall 
    // instead use the RabbitMessageProducer connector 
    connector = new InVMInterProcessConnector(srh); 
    // Configure the actual 'sender' object. It requires a 
    // unique producer code to identify the 
    // sender of data (see HelloWorld tutorial for a description 
    // of the format); which connector to use to send 
    // the data; and of course which serializer to use 
    // to create the byte array payload to send over the wire. 
    ClientRequestHandler<GameFavorite> crh =  
        new StandardClientRequestHandler<GameFavorite>( 
            GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE,  
            connector, new GameFavoriteSerializer()); 
     
    // Client side 'normal operations' is just sending data to 
    // the server    
    try { 
      crh.send(ex, "exampleproject.reading.store"); 
    } catch (IOException e) { 
      e.printStackTrace(); 
    } 
 
    // Validate that the object has been stored in the data storage 
    BasicDBObject dboStored = storage.lastDocumentStored(); 
    assertEquals("Henrik", dboStored.get("name")); 
  } 
} 
 
/* A fake object storage just for testing purposes */ 
class LocalFakeObjectStorage implements ProcessingStrategy { 
 
  private BasicDBObject remember; 
 
  public BasicDBObject lastDocumentStored() { 
    return remember; 
  } 
  
  @Override 
  public void process(String producerCode, BasicDBObject dbo) { 
    remember = dbo; 
  } 
} 
 
