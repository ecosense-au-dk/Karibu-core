package dk.au.cs.karibu.endurance;

import java.io.IOException;
import java.util.*;

import dk.au.cs.karibu.domain.*;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.producer.rabbitmq.*;
import dk.au.cs.karibu.utilities.PropertyReader;

/** This is an endurance test to simulate traffic
 * from smartphones whose apps generally upload once
 * and a while in a (connect,send,close) manner.
 * 
 * Specifically to hunt a bug where connections
 * tend to 'hang' in RabbitMQ.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class SimulateSmartphoneApp {
  public static void main(String[] args) throws IOException, InterruptedException { 
    if ( args.length != 2 ) { 
      System.out.println( "usage: SimulateSmartphoneApp <resource-root-folder> <delay-in-sec>" ); 
      System.out.println( "     resource-root-folder = root folder which contains the exchange.properties file"); 
      System.out.println( "     delay-in-sec = seconds delay in sending"); 
      
      System.exit(-1); 
    } 
 
    System.out.println("*** Karibu Smartphone App Simulation v2 ***");    
    
    String resourceFolderRoot = args[0];
    int delayInSec = Integer.parseInt(args[1]);
    
    // Read in the property files
    PropertyReader rr = new PropertyReader(resourceFolderRoot);
    Properties exchangeProperties = rr.readPropertiesFailFast("exchange");

    // Configure the connector to the MQ
    ChannelConnector connector = null; 
    RabbitExchangeConfiguration rabbitExchangeConfig =
        new StandardRabbitExchangeConfiguration(exchangeProperties);
      
    connector = new RabbitChannelConnector( rabbitExchangeConfig ); 
    
    // Configure the client request handler
    ClientRequestHandler<ExampleMeasurement> readingHandler;
    StandardJSONSerializer<ExampleMeasurement> serializer;
    serializer = new StandardJSONSerializer<ExampleMeasurement>();



    // Finally - generate load by repeatedly sending data...
    System.out.println("Hit CTRL-C to stop producing data - delay betweeen sending: "+ delayInSec); 
    long count = 1L;
    long hickupsInterval = 10L;
    Calendar now; 
    ExampleMeasurement data;
    boolean hickup = false;

    readingHandler = new StandardClientRequestHandler<ExampleMeasurement>(DomainConstants.PRODUCER_CODE_EXAMPLE_MEASUREMENT,  
        connector, serializer ); 

    while ( true ) {
      now = Calendar.getInstance(); 
      data = new ExampleMeasurement(now, count); 
      hickup = count % hickupsInterval == 0;

      readingHandler.send(data, DomainConstants.STORE_TOPIC_EXAMPLE_MEASUREMENT); 
      // Simulate error condition where closeConnection is not called
      if (! hickup) { connector.closeConnection(); }

      System.out.println("  Send Count = "+count + " at "+ new Date() +" hickup: "+hickup); 

      Thread.sleep(delayInSec*1000L);

      count++;

    }
  }
}
