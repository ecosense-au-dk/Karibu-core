package dk.au.cs.karibu.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.*;

import com.mongodb.*;

import dk.au.cs.karibu.backend.DeserializerFactory;
import dk.au.cs.karibu.backend.standard.StandardServerRequestHandler;
import dk.au.cs.karibu.hobbydomain.*;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.testdoubles.*;

/** Reproduce mongo failures from the spring 2013
 * and FD production time and validate recovery.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestMongoFailures {
  private SpyLogger spyLogger; 

  private GameFavorite ex1, ex2, ex3; 
  
  // Backend roles 
  private FakeObjectStorage                        storage; 
  private StandardServerRequestHandler             srh; 
  private InVMInterProcessConnector                connector; 
 
  // Producer roles 
  private ClientRequestHandler<GameFavorite> crh; 
  private Serializer<GameFavorite> theSerializer; 
 
 
  @Before 
  public void setup() {     
    // create a spy into the logging so we can validate what goes on 
    // in the client request handler 
    spyLogger = new SpyLogger(); 

    ex1 = new GameFavorite("Henrik", "StarCraft II"); 
    ex2 = new GameFavorite("Mikkel", "SkyRim"); 
    ex3 = new GameFavorite("Mathilde", "MovieStarPlanet"); 
 
    // Configure a testing backend... 
    storage = new FakeObjectStorage(); 
    
    DeserializerFactory factory = new GameFavoriteDeserializerFactory(); 
    srh = new StandardServerRequestHandler(storage, factory, new NullStatisticHandler(), spyLogger); 
    connector = new InVMInterProcessConnector(srh); 
 
    // Configure a testing producer 
    theSerializer = new GameFavoriteSerializer(); 
    crh = new StandardClientRequestHandler<GameFavorite>( 
        GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE, connector, theSerializer); 
  } 
  
  @Test 
  public void shouldStoreCorrectDataInBackTierForCorrectProducer() {    
    // make the storage throw a Mongo exceptino after 1 successful send
    storage.setExceptionTrigger("com.mongodb.MongoException$Network:"+
        "Write operation to server ecosensedb01.cs.au.dk failed", 1);
    
    // Client side 'normal operations' is just sending data to 
    // the server 
    try { 
      crh.send(ex1, "exampleproject.reading.store");
      // validate that the connector was informed by the server request
      // handler that the processing succeeded.
      assertEquals( 1, connector.getCountOfSuccessfulProcessing() );
      
      crh.send(ex2, "exampleproject.reading.store"); 
      // validate that this msg was NOT acknowledged
      assertEquals( 1, connector.getCountOfSuccessfulProcessing() );
      
      crh.send(ex3, "exampleproject.reading.store");
      assertEquals( 2, connector.getCountOfSuccessfulProcessing() );
    } catch (IOException e) { 
      e.printStackTrace(); 
    } 
    // Validate that the object has been stored in the data storage 
    BasicDBObject dboStored; 
     
    List<BasicDBObject> exampleCollection = 
        storage.getCollectionNamed(GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE);
    dboStored = exampleCollection.get(0); 
    assertEquals("Henrik", dboStored.get("name")); 
    assertEquals("StarCraft II", dboStored.get("game")); 
    
    // validate that we have logged the the exception
    String lastLog = spyLogger.getLastLog();
    assertTrue("The failure was logged", lastLog.contains("MongoException$Network:"));
    
    // System.out.println( "*LAST LOG*: "+spyLogger.getLastLog() );
    
    // The ex2 object was not stored however :(
    assertEquals( 2, exampleCollection.size() );

    dboStored = exampleCollection.get(1); 
    assertEquals("Mathilde", dboStored.get("name")); 
  }


}
