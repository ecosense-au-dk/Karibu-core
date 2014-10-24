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
 
    System.out.println("*** Karibu Smartphone App Simulation ***");    
    
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
    long count = 0L;
    Calendar now; 
    ExampleMeasurement data;

    while ( true ) {
        now = Calendar.getInstance(); 
        data = new ExampleMeasurement(now, count); 

        // A smartphone app goes through the full cycle of creating a
        // client request handler, sending data, and closing connection
        readingHandler = new StandardClientRequestHandler<ExampleMeasurement>(DomainConstants.PRODUCER_CODE_EXAMPLE_MEASUREMENT,  
            connector, serializer ); 
        readingHandler.send(data, DomainConstants.STORE_TOPIC_EXAMPLE_MEASUREMENT); 
        connector.closeConnection();
        
        count++;
        
        System.out.println("  Send Count = "+count + " at "+ new Date() ); 
        
        Thread.sleep(delayInSec*1000L);
    }
  }
}
