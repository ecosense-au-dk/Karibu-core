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
package dk.au.cs.karibu.hobbydomain;

import org.slf4j.*;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.serialization.Deserializer;

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
