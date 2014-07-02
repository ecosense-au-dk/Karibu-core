package dk.au.cs.karibu.hobbydomain;

import dk.au.cs.karibu.producer.Serializer;

/* Example of a serializer that converts the domain class to the 
 * on-the-wire format 
 */ 
public class ExampleSerializer implements Serializer<ExampleDomainClass> { 
 
  @Override 
  public byte[] serialize(ExampleDomainClass myData) { 
    // just make something for the sake of the example 
    String onTheWireString = myData.getName() + "|" + myData.getGame(); 
    byte[] payload = onTheWireString.getBytes(); 
    return payload; 
  } 
} 
 
