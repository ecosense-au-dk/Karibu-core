package dk.au.cs.karibu.hobbydomain;

import java.util.StringTokenizer;

import com.mongodb.BasicDBObject;

import dk.au.cs.karibu.serialization.Deserializer;

/* Example of a deserializer that converts the on the wire format 
 * into a MongoDB object. 
 */ 
public class GameFavoriteDeserializer implements Deserializer { 
 
  @Override 
  public BasicDBObject buildDocumentFromByteArray(byte[] payload) { 
     
    String name, game; 
    String payloadAsString = new String(payload); // convert to String 
    StringTokenizer st = new StringTokenizer(payloadAsString, "|"); 
    name = st.nextToken(); 
    game = st.nextToken(); 
     
    BasicDBObject root = new BasicDBObject(); 
    root.put("name", name); 
    root.put("game", game); 
    return root; 
  } 
  
  public String toString() {
    return "GameFavoriteDeserializer";
  }
} 
