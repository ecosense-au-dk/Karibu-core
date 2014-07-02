package dk.au.cs.karibu.hobbydomain;

import org.slf4j.*;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.hobbydomain.*;

/** A stub factory, that just knows the producer code for 
 * the Example domain classes. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public class HobbyDeserializerFactory implements DeserializerFactory {
  
  Logger log = LoggerFactory.getLogger(HobbyDeserializerFactory.class); 
  
  @Override 
  public Deserializer createDeserializer(String producerCode) { 
    Deserializer returnvalue = null; 
    if (producerCode.equals(ExampleSerializer.EXAMPLE_PRODUCER_CODE)) { 
      returnvalue = new ExampleDeserializer(); 
    } else { 
      log.error("Requested deserializer for unknown producer code: " 
          + producerCode); 
    } 
    return returnvalue; 
  } 

}
