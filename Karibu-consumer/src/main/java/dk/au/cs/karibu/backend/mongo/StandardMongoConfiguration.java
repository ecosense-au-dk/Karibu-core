package dk.au.cs.karibu.backend.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mongodb.ServerAddress;

import dk.au.cs.karibu.common.FailFast;

/**
 * Standard implementation of the <code>MongoConfiguration</code> interface.
 * 
 * @author Peter Urbak, Aarhus University
 * @version 2013-06-10
 */
public class StandardMongoConfiguration implements MongoConfiguration {

  // --*-- Fields --*--

  private String databaseName;
  private List<ServerAddress> serverAddressList;
  private String username;
  private String password;
	
  private final String DATABASE_NAME = "databaseName";
  private final String SERVER_ADDRESS_LIST = "serverAddressList";
  private final String USERNAME  = "username";
  private final String PASSWORD = "password";
	

  // --*-- Constructors --*--

  /**
   * Constructs a <code>StandardMongoConfiguration</code>.
   * 
   * @param databaseName -
   * @param serverAddressList -
   */
  public StandardMongoConfiguration(String databaseName,
                                    List<ServerAddress> serverAddressList) {
    init(databaseName, serverAddressList, null, null);
  }
  
  /**
   * Constructs a <code>StandardMongoConfiguration</code>
   * with credentials
   * 
   * @param databaseName -
   * @param serverAddressList -
   */
  public StandardMongoConfiguration(String databaseName,
                                    List<ServerAddress> serverAddressList,
                                    String username,
                                    String password) {
    init(databaseName, serverAddressList, username, password);
  }

  /**
   * Constructs a <code>StandardMongoConfiguration</code>.
   * 
   * @param mongoProperties the properties of the mongo
   * @throws UnknownHostException 
   */
  public StandardMongoConfiguration(Properties mongoProperties) throws UnknownHostException {
    String databaseName = 
      FailFast.readProperty(mongoProperties, DATABASE_NAME);

    String addressString = 
      FailFast.readProperty(mongoProperties, SERVER_ADDRESS_LIST);
    String[] addressStrings = addressString.split(",");

    ArrayList<ServerAddress> serverAddressList = new ArrayList<ServerAddress>();
    String nodename = null;

    // Do not catch the potential UnknownHostException here. Why?
    // Because this code will be run under initialization and thus
    // will fail-fast during startup - it signals a wrong
    // setup of the mongo db machines to connect to.
    for (String entry : addressStrings) { 
      nodename = entry;
      serverAddressList.add(new ServerAddress(nodename)); 
    }
		
    // Note: FailFast not used there as the fields may be
    // missing for backward compatability
    String username = mongoProperties.getProperty(USERNAME);
    String password = mongoProperties.getProperty(PASSWORD);
    
    init(databaseName, serverAddressList, username, password);
  }


  // --*-- Methods --*--

  /**
   * Initializes the <code>StandardMongoConfiguration</code> object.
   * 
   * @param databaseName
   * @param serverAddressList
   */
  private void init(String databaseName,
                    List<ServerAddress> serverAddressList,
                    String username,
                    String password ) {
    this.databaseName = databaseName;
    this.serverAddressList = serverAddressList;
    this.username = username;
    this.password = password;
  }
	
  /**
   * {@inheritDoc}
   */
  @Override
    public String toString() {
    String adrlist = "";
    for ( ServerAddress adr : getServerAddressList() ) {
      if ( adrlist.length() >  0 ) { adrlist += ","; }
      adrlist += adr.getHost()+":"+adr.getPort();
    }
    return "StandardMongoConfiguration" +
      " (databaseName : " + getDatabaseName() +
      ", serverAddressList : [" + adrlist + 
      "] User: "+
      getUsername()+")";
  }

  // -*- Getters/Setters -*-

  /**
   * {@inheritDoc}
   */
  @Override
    public String getDatabaseName() {
    return databaseName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
    public List<ServerAddress> getServerAddressList() {
    return serverAddressList;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

}