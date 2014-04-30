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

package dk.au.cs.karibu.producer.rabbitmq; 

import com.rabbitmq.client.Address; 

/**
 * Configuration object for a RabbitMQ server and a single exchange on the 
 * server. 
 *  
 * For explanation of the method names, please refer to RabbitMQ documentation 
 * or the book 'RabbitMQ in Action' by Wiley. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University
 * @author Peter Urbak, Aarhus University
 * @version 2013-06-10
 * 
 */
public interface RabbitExchangeConfiguration {
	
	// --*-- Methods --*--

	/**
	 * Returns the username of the exchange.
	 * 
	 * @return the username of the exchange.
	 */
	public String getUsername(); 

	/**
	 * Returns the password of the exchange.
	 * 
	 * @return the password of the exchange.
	 */
	public String getPassword();

	/**
	 * Returns the array of addresses of servers.
	 * 
	 * @return the array of addresses of servers.
	 */
	public Address[] getServerAddressList(); 

	/**
	 * Returns true if it should use a secure connection, false otherwise.
	 * 
	 * @return true if it should use a secure connection, false otherwise.
	 */
	public boolean isSSLConnection(); 

	/**
	 * Returns the name of the exchange.
	 * 
	 * @return the name of the exchange.
	 */
	public String getExchangeName(); 

	/**
	 * Returns true if the exchange is durable.
	 * 
	 * @return true if the exchange is durable.
	 */
	public boolean isExchangeDurable(); 

	/**
	 * Returns the exchange type.
	 * 
	 * @return the exchange type.
	 */
	public String getExchangeType(); 
}