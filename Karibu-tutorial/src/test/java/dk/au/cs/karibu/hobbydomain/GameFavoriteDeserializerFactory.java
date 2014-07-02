package dk.au.cs.karibu.hobbydomain;

import org.slf4j.*;

import dk.au.cs.karibu.backend.*;

/** A stub factory, that just knows the producer code for 
 * the Example domain classes. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public class GameFavoriteDeserializerFactory implements DeserializerFactory {
  
  Logger log = LoggerFactory.getLogger(GameFavoriteDeserializerFactory.class); 
  
  @Override 
  public Deserializer createDeserializer(String producerCode) { 
    Deserializer returnvalue = null; 
    if (producerCode.equals(GameFavoriteSerializer.EXAMPLE_PRODUCER_CODE)) { 
      returnvalue = new GameFavoriteDeserializer(); 
    } else { 
      log.error("Requested deserializer for unknown producer code: " 
          + producerCode); 
    } 
    return returnvalue; 
  } 

}
