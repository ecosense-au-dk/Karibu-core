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
 
import org.apache.commons.lang.exception.ExceptionUtils; 
import org.slf4j.*; 

import dk.au.cs.karibu.backend.*;
 
/** The default factory to produce deserializers. It dynamically
 * class-load the class file with the name of the producer code which
 * is stored in folder DEFAULT_DESERIALIZER_FOLDER.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 
 
public final class StandardDeserializerFactory implements DeserializerFactory { 
  private Logger log; 
 
  public final static String DEFAULT_DESERIALIZER_FOLDER = 
    "cs.ecosense.karibu.deserializer."; 
   
  public StandardDeserializerFactory() { 
    log = LoggerFactory.getLogger(StandardDeserializerFactory.class); 
  } 
   
  @Override 
  public Deserializer createDeserializer(String producerCode) { 
    Object theInstance = null; 
    try { 
      theInstance =  
          Class.forName(DEFAULT_DESERIALIZER_FOLDER+producerCode).newInstance(); 
    } catch (InstantiationException e) { 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("Instantiation error for producer code: " 
          +producerCode+". "+theTrace); 
    } catch (IllegalAccessException e) { 
      // should not happen as the deserializers are known 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("IllegalAccess error: "+theTrace); 
    } catch (ClassNotFoundException e) { 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("Could not find deserializer class for producer: "+producerCode 
          +". "+theTrace); 
    } 
    Deserializer theSerializer = (Deserializer) theInstance; 
    return theSerializer; 
  } 
 
} 
