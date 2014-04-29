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

import cs.karibu.backend.standard.*;
 
/** A server request handler (Patterns of Software Architecture, 
 * Volume 4).  
 *  
 * Use the Standard implementation of this interface for 
 * production!
 * 
 *  @see StandardServerRequestHandler
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
public interface ServerRequestHandler { 
   
  public static final int PRODUCER_CODE_LENGTH = 8; 
 
  /** invoked by server side processes every time 
   * a payload of bytes are received. This 
   * method is responsible for  
   * 
   * a) inspecting the producer code as the 
   * header of the payload, and retrieve the 
   * proper deserializer 
   * 
   * b) deserializing the byte array payload into 
   * a domain object 
   * 
   * c) storing the domain object into an injected 
   * storage. 
   *  
   * PRECONDITION: The payload is not null, and a 
   * well formed payload including producer code 
   * of exactly PRODUCER_CODE_LENGTH length. 
   *  
   * @param bytes the raw payload
   * @return true iff the received payload was successfully
   * processed (which in the default case means
   * stored correctly in the database.)  
   */ 
  boolean receive(byte[] bytes); 
   
  /** Get a status object that describes 
   * statistics concerning the server. 
   *  
   * @return current statistics object
   */ 
  StatisticHandler getStatistic();
  
} 
