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
 
/** Interface encapsulating the responsibility to 
 * do the final processing of BSON documents from
 * the message system. In the core use case, this
 * is storing them in the MongoDB database tier.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public interface ProcessingStrategy { 
 
  /** given a producer code and a BSON document, 
   * do the final processing/algorithm on the document.
   * The core use case is storing it.
   * @param producerCode the code of the 
   * producer of the document. 
   * @param dbo the actual BSON document to process. 
   */ 
  public void process(String producerCode, BasicDBObject dbo); 
} 
