package dk.au.cs.karibu.unit;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.*;

import org.junit.*;

import com.mongodb.ServerAddress;
import com.rabbitmq.client.Address;

import dk.au.cs.karibu.backend.mongo.*;
import dk.au.cs.karibu.backend.rabbitmq.*;
import dk.au.cs.karibu.producer.rabbitmq.*;
import dk.au.cs.karibu.utilities.PropertyReader;

/** Test the configuration loaders 
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
 public class TestConfigurations {

   private PropertyReader propReader;
   
   @Before
   public void setup() {
     propReader = new PropertyReader("testinput");
   }
   
   @Test
   public void shouldParseExchangeConfigurationCorrectly() {
     Properties exchangeProperties = propReader.readPropertiesFailFast("exchange");
     // validate that the property is correctly read
     assertEquals("false", exchangeProperties.getProperty("exchangeDurable"));
     
     // Validate that the configuration matches the values defined in the property file
     StandardRabbitExchangeConfiguration config = new StandardRabbitExchangeConfiguration(exchangeProperties);
     
     assertEquals( "guest", config.getUsername());
     assertEquals( "pwd", config.getPassword());
     
     Address[] adrList = config.getServerAddressList();
     assertEquals(3, adrList.length);
     assertEquals( "10.11.111.199", adrList[0].getHost());
     assertEquals( 5672, adrList[0].getPort());
     
     assertEquals( "ecosensemq01.cs.au.dk", adrList[1].getHost());
     assertEquals( 2332, adrList[1].getPort());

     assertEquals( "ecosensemq02.cs.au.dk", adrList[2].getHost());
     assertEquals( 5671, adrList[2].getPort());
     
     assertTrue("SSL should be true", config.isSSLConnection());
     assertEquals( "ecosense-exchange", config.getExchangeName());

     assertFalse("Exchange should not be durable", config.isExchangeDurable());
     assertEquals( RabbitConstants.TOPIC, config.getExchangeType());
     
     // validate nice output in toString
     assertTrue("toString contains server list", 
         config.toString().contains("10.11.111.199:5672,ecosensemq01.cs.au.dk:2332,ecosensemq02.cs.au.dk:5671"));
   }

   @Test
   public void shouldParseQueueConfigurationCorrectly() {
     Properties properties = propReader.readPropertiesFailFast("queue");
     
     assertEquals("trUe", properties.getProperty("exclusive"));
     
     RabbitQueueConfiguration config = new StandardRabbitQueueConfiguration(properties);

     assertEquals( "storage-queue", config.getQueueName());
     assertEquals( "*.*.store", config.getRoutingKey());
     assertTrue( "Queue must be durable", config.isQueueDurable());
     assertTrue( "Queue is not exclusive", config.isQueueExclusive());
     assertTrue( "Queue is not auto delete", config.isQueueAutoDelete());
   }
   
   @Test
   public void shouldParseMongoConfigurationCorrectly() {
     Properties properties = propReader.readPropertiesFailFast("mongo");

     assertEquals("ecosense", properties.getProperty("databaseName"));
     
     MongoConfiguration config = null;
     try {
       config = new StandardMongoConfiguration(properties);
     } catch (UnknownHostException e ){
       // a DNS lookup failed but this is not what the test is about.
       fail("This test only runs when your machine is connected to network.");
     }
     assertEquals( "ecosense", config.getDatabaseName());

     List<ServerAddress> adrlist = config.getServerAddressList();
     assertEquals( 2, adrlist.size() );
     
     assertEquals("www.cs.au.dk", adrlist.get(0).getHost() );
     assertEquals(27001, adrlist.get(0).getPort() );
     
     assertEquals("www.imhotep.dk", adrlist.get(1).getHost() );
     assertEquals(27017, adrlist.get(1).getPort() );
     
     // assert the toString
     assertTrue( "Address list incorrect ("+config.toString()+")", 
         config.toString().contains("www.cs.au.dk:27001,www.imhotep.dk:27017"));
     
     // Addition in release 1.4
     
     assertNull(config.getUsername() );
     assertNull(config.getPassword() );
   }
   
   @Test(expected=RuntimeException.class)
   public void shouldThrowExceptionInCaseInvalidKey() {
     Properties properties = propReader.readPropertiesFailFast("wrongmongo");
     
     assertEquals("ecosense", properties.getProperty("AdatabaseName"));
     // The constructor will throw an runtime exception
     try {
       MongoConfiguration config = new StandardMongoConfiguration(properties);
     } catch ( UnknownHostException e ) {
       // ignore - means a dns lookup failed but this is not what I test here...
     }
   }
   
   @Test
   public void shouldParseMongoCredentialConfigurationCorrectly() {
     Properties properties = propReader.readPropertiesFailFast("mongowithpwd");
     
     MongoConfiguration config = null;
     try {
       config = new StandardMongoConfiguration(properties);
     } catch (UnknownHostException e ){
       // a DNS lookup failed but this is not what the test is about.
       fail("This test only runs when your machine is connected to network.");
     }
  
     assertEquals( "StoreDaemon", config.getUsername() );
     assertEquals( "pindsvin", config.getPassword() );
   }

}
