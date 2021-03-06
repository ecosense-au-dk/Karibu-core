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

package dk.au.cs.karibu.testdoubles;

import java.util.*;

import com.mongodb.*;

import dk.au.cs.karibu.backend.ProcessingStrategy;

/** Fake Object storage that mimics MongoDB behaviour 
 * somewhat: It handles multiple collections 
 * and stores items in each collection as a list. 
 * 
 * Also it may mimic as replica set election exception
 * during storage.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 

public class FakeObjectStorage implements ProcessingStrategy {
  private String toThrow;
  private int countOfStoresBeforeThrow = -1;
  private int storeMethodInvocationCount = 0;
 
  private Map<String,List<BasicDBObject>> database; 
 
  public FakeObjectStorage() { 
    database = new HashMap<String, List<BasicDBObject>>(10); 
  } 
 
  public List<BasicDBObject> getCollectionNamed(String collectionName) { 
    return database.get(collectionName); 
  } 
 
  @Override 
  public void process(String collectionName, BasicDBObject dbo) {
    if ( storeMethodInvocationCount == countOfStoresBeforeThrow ) {
      storeMethodInvocationCount++;
      throw new MongoException(toThrow);
    }
    storeMethodInvocationCount++;

    // System.out.println(" FakeObjectStorage: storing "+dbo);
    List<BasicDBObject> collection = database.get(collectionName); 
    if ( collection == null ) { 
      collection = new ArrayList<BasicDBObject>(2); 
      database.put(collectionName, collection); 
    } 
    collection.add(dbo); 
  } 
  
  public void setExceptionTrigger( String exceptionMsg, int countBeforeThrow) {
    toThrow = exceptionMsg; countOfStoresBeforeThrow = countBeforeThrow;
  }
}
