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
 
package cs.karibu.backend; 
 
import com.mongodb.BasicDBObject; 
 
/** The Deserializer is responsible for deserializing
 * a binary payload and constructs a MongoDB document for storage.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public interface Deserializer { 
 
  /** Given a byte array payload convert it into a MongoDB 
   * document. 
   * @param payload the actual binary representation of 
   * a domain object 
   * @return the mongoDB document that can be stored. 
   */ 
  public BasicDBObject buildDocumentFromByteArray(byte[] payload); 
   
} 
