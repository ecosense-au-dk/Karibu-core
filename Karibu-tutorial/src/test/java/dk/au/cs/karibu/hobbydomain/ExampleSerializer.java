package dk.au.cs.karibu.hobbydomain;

import dk.au.cs.karibu.producer.Serializer;

/* Example of a serializer that converts the domain class to the 
 * on-the-wire format 
 */ 
public class ExampleSerializer implements Serializer<ExampleDomainClass> { 
  
  // Define the producer code that identify the sender, format and version 
  // of the sent data. Consult the documentation for more information. 
  public final static String EXAMPLE_PRODUCER_CODE = "EXMXX001"; 
 
  @Override 
  public byte[] serialize(ExampleDomainClass myData) { 
    // just make something for the sake of the example 
    String onTheWireString = myData.getName() + "|" + myData.getGame(); 
    byte[] payload = onTheWireString.getBytes(); 
    return payload; 
  } 
} 
 