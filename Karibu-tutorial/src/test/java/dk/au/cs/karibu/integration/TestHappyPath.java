package dk.au.cs.karibu.integration;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.*;

import com.mongodb.BasicDBObject;

import dk.au.cs.karibu.backend.DeserializerFactory;
import dk.au.cs.karibu.backend.standard.StandardServerRequestHandler;
import dk.au.cs.karibu.hobbydomain.*;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.testdoubles.*;

/** Test the normal, happy path, situation in which 
 * no errors occurs. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 

public class TestHappyPath {
  
  private GameFavorite ex1, ex2; 
  
  // Backend roles 
  private FakeObjectStorage                        storage; 
  private StandardServerRequestHandler             srh; 
  private InVMInterProcessConnector                connector; 
 
  // Producer roles 
  private ClientRequestHandler<GameFavorite> crh; 
  private Serializer<GameFavorite> theSerializer; 

  @Before 
  public void setup() { 
    ex1 = new GameFavorite("Henrik", "StarCraft II"); 
    ex2 = new GameFavorite("Mikkel", "SkyRim"); 
 
    // Configure a testing backend... 
    storage = new FakeObjectStorage(); 
    DeserializerFactory factory;
    factory = new GameFavoriteDeserializerFactory(); 
    srh = new StandardServerRequestHandler(storage, factory); 
    connector = new InVMInterProcessConnector(srh); 
 
    // Configure a testing producer 
    theSerializer = new GameFavoriteSerializer(); 
    crh = new StandardClientRequestHandler<GameFavorite>( 
        GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE, connector, theSerializer); 
  } 

  @Test 
  public void shouldStoreCorrectDataInBackTierForCorrectProducer() { 
    // Client side 'normal operations' is just sending data to 
    // the server 
    try { 
      crh.send(ex1, "exampleproject.reading.store"); 
      crh.send(ex2, "exampleproject.reading.store"); 
    } catch (IOException e) { 
      e.printStackTrace(); 
    } 
    // Validate that the object has been stored in the data storage 
    BasicDBObject dboStored; 
     
    dboStored = storage. 
        getCollectionNamed(GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE).get(0); 
    assertEquals("Henrik", dboStored.get("name")); 
    assertEquals("StarCraft II", dboStored.get("game")); 
 
    dboStored = storage. 
        getCollectionNamed(GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE).get(1); 
    assertEquals("Mikkel", dboStored.get("name")); 
    assertEquals("SkyRim", dboStored.get("game")); 
     
  } 


}
