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
 
package cs.karibu.utilities; 
 
import org.bson.types.Binary; 
 

import com.mongodb.BasicDBObject; 
 

import cs.karibu.backend.Deserializer;
 
/** The deserializer used when Karibu encounters a project code
 * that is not known. It creates a document with a single entry
 * with key 'payload' and stores the binary payload as its value.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class DeadLetterDeserializer implements Deserializer { 
   
  @Override 
  public BasicDBObject buildDocumentFromByteArray(byte[] payload) { 
    BasicDBObject root = new BasicDBObject(); 
    Binary xys = new Binary(payload); 
    root.put("payload", xys); 
    return root; 
  } 
 
} 
