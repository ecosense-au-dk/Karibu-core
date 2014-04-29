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
package cs.karibu.producer.rabbitmq;

import java.util.*;

import org.slf4j.*;

import com.rabbitmq.client.Address;

import cs.karibu.common.FailFast;

/**
 * Standard implementation of the <code>RabbitExchangeConfiguration</code>
 * interface.
 * 
 * @author Peter Urbak, Aarhus University
 * @version 2013-06-10
 *
 */
public class StandardRabbitExchangeConfiguration
	implements RabbitExchangeConfiguration {

	// --*-- Fields --*--
  
	private boolean sslConnection, exchangeDurable;
	private String username, password, exchangeName, exchangeType;
	private Address[] serverAddressList;
	
	private final String USERNAME = "username";
	private final String PASSWORD = "password";
	private final String SERVER_ADDRESS_LIST = "serverAddressList";
	private final String SSL_CONNECTION = "sslConnection";
	private final String EXCHANGE_NAME = "exchangeName";
	private final String EXCHANGE_DURABLE = "exchangeDurable";
	private final String EXCHANGE_TYPE = "exchangeType";
	
	// --*-- Constructors --*--
	
	/**
	 * Constructs a <code>StandardRabbitExchangeConfiguration</code>.
	 * 
	 * @param username -
	 * @param password -
	 * @param serverAddressList -
	 * @param sslConnection -
	 * @param exchangeName -
	 * @param exchangeDurable -
	 * @param exchangeType -
	 */
	public StandardRabbitExchangeConfiguration(String username, String password,
		Address[] serverAddressList, boolean sslConnection, String exchangeName,
		boolean exchangeDurable, String exchangeType) {
		init(username, password, serverAddressList, 
		    sslConnection, exchangeName,
				exchangeDurable, exchangeType);	
	}
	
	/**
	 * Constructs a <code>StandardRabbitExchangeConfiguration</code>.
	 * 
	 * @param exchangeProperties
	 */
	public StandardRabbitExchangeConfiguration(Properties exchangeProperties) {
	
	  String username = 
		    FailFast.readProperty(exchangeProperties, USERNAME);
		String password = 
		    FailFast.readProperty(exchangeProperties, PASSWORD);
		
		boolean sslConnection =
		    FailFast.readProperty(exchangeProperties, SSL_CONNECTION).equalsIgnoreCase("true");
		String exchangeName = 
		    FailFast.readProperty(exchangeProperties, EXCHANGE_NAME);
		
		boolean exchangeDurable = 
		    FailFast.readProperty(exchangeProperties, EXCHANGE_DURABLE).equalsIgnoreCase("true");
		
		String exchangeType = 
		    FailFast.readProperty(exchangeProperties, EXCHANGE_TYPE);
		
		String addressString =
		    FailFast.readProperty(exchangeProperties, SERVER_ADDRESS_LIST);
		String[] addressStrings = addressString.split(",");
		List<Address> addresses = new ArrayList<Address>(5);
		
		for (String address : addressStrings) {
			String[] splitAddress = address.split(":");
			String host = splitAddress[0];
			
			int port = 5672;
			if (sslConnection) {
				port = 5671;
			}
			
			if (splitAddress.length > 1) {
				try {
					port = Integer.parseInt(splitAddress[1]);
				} catch (Exception e) {
				  Logger log = LoggerFactory.getLogger(StandardRabbitExchangeConfiguration.class);
				  log.error("Integer parsing error on port number from address property", e);
				  System.out.println("Port number error in property file, review the log...");
				  // Fail fast, no need to carry on before the property file has been fixed.
				  System.exit(-1);
				}
			}
			addresses.add(new Address(host, port));
		}
		Address[] serverAddressList = addresses.toArray(new Address[0]);
		init(username, password, serverAddressList, sslConnection,
				exchangeName, exchangeDurable, exchangeType);
	}
	
	// --*-- Methods --*--

	/**
	 * Initializes the <code>StandardRabbitExchangeConfiguration</code> object.
	 * 
	 * @param username -
	 * @param password -
	 * @param serverAddressList -
	 * @param sslConection -
	 * @param exchangeName -
	 * @param exchangeDurable -
	 * @param exchangeType -
	 */
	private void init(String username, String password,
			Address[] serverAddressList, boolean sslConnection,
			String exchangeName, boolean exchangeDurable, String exchangeType) {
		this.username = username;
		this.password = password;
		this.serverAddressList = serverAddressList;
		this.sslConnection = sslConnection;
		this.exchangeName = exchangeName;
		this.exchangeDurable = exchangeDurable;
		this.exchangeType = exchangeType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	  String adrList = ""; Address[] list = getServerAddressList();
	  for ( int i = 0; i < list.length; i++ ) {
	    if ( i > 0 ) { adrList += ","; }
	    adrList += list[i].getHost()+":"+list[i].getPort();
	  }
		return "StandardRabbitExchangeConfiguration" +
			   " (username : " + getUsername() +
			   // REMOVED as it will appear in logs. ", password : " + getPassword() +
			   ", serverAddressList : [" + adrList +
			   "], sslConnection : " + isSSLConnection() +
			   ", exchangeName : " + getExchangeName() +
			   ", exchangeDurable : " + isExchangeDurable() +
			   ", exchangeType : " + getExchangeType() +
			   ")";
	}
	
	// -*- Getters -*-
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Address[] getServerAddressList() {
		return serverAddressList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSSLConnection() {
		return sslConnection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExchangeName() {
		return exchangeName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExchangeDurable() {
		return exchangeDurable;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExchangeType() {
		return exchangeType;
	}

}
