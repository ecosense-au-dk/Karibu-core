package dk.au.cs.karibu.integration;

import dk.au.cs.karibu.backend.standard.StandardServerRequestHandler;
import dk.au.cs.karibu.hobbydomain.ExampleDomainClass;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.testdoubles.*;

/** Test the normal, happy path, situation in which 
 * no errors occurs. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 

public class TestHappyPath {
  
  private ExampleDomainClass ex1, ex2; 
  
  // Backend roles 
  private FakeObjectStorage                        storage; 
  private StandardServerRequestHandler             srh; 
  private InVMInterProcessConnector                connector; 
 
  // Producer roles 
  private ClientRequestHandler<ExampleDomainClass> crh; 
  private Serializer<ExampleDomainClass> theSerializer; 


}
