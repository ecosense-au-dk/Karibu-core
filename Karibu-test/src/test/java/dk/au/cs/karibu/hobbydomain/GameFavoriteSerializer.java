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

import dk.au.cs.karibu.serialization.Serializer;

/* Example of a serializer that converts the domain class to the 
 * on-the-wire format 
 */ 
public class GameFavoriteSerializer implements Serializer<GameFavorite> { 
  
  // Define the producer code that identify the sender, format and version 
  // of the sent data. Consult the documentation for more information. 
  public final static String EXAMPLE_PRODUCER_CODE = "EXMXX001"; 
 
  @Override 
  public byte[] serialize(GameFavorite myData) { 
    // just make something for the sake of the example 
    String onTheWireString = myData.getName() + "|" + myData.getGame(); 
    byte[] payload = onTheWireString.getBytes(); 
    return payload; 
  } 
} 
 
