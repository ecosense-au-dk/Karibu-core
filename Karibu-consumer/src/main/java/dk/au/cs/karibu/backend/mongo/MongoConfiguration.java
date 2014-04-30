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
package dk.au.cs.karibu.backend.mongo;

import java.util.List;

import com.mongodb.ServerAddress;

/**
 * Configuration object defining the characteristics of a MongoDB connection. 
 *   
 * @author Peter Urbak, Aarhus University & Henrik Baerbak Christensen
 * @version 2013-06-10
 */
public interface MongoConfiguration {

  /**
   * Returns the name of the database to connect to.
   * 
   * @return the name of the database to connect to.
   */
  public String getDatabaseName();
	
  /**
   * Returns the list of server addresses.
   * 
   * @return the list of server addresses.
   */
  public List<ServerAddress> getServerAddressList();

  /** 
   * Returns the user name or null if no
   * credentials are supplied
   * @return username or null
   */
  public String getUsername();

  /** 
   * Returns the password or null if no
   * credentials are supplied
   * @return password or null
   */
  public String getPassword();
	
}
