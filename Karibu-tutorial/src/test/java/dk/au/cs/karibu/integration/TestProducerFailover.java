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

package dk.au.cs.karibu.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ConnectException;

import org.junit.*;

import com.mongodb.BasicDBObject;
import com.rabbitmq.client.AlreadyClosedException;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.backend.standard.StandardServerRequestHandler;
import dk.au.cs.karibu.hobbydomain.*;
import dk.au.cs.karibu.producer.*;
import dk.au.cs.karibu.testdoubles.*;

/** Test failover behaviour on the client side.  
 * Uses test double classes that can simulate exceptions 
 * from the RabbitMQ clients side API. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 

public class TestProducerFailover {
  
  private ClientRequestHandler<GameFavorite> crh; 
  private ChannelConnector connector; 
  private ServerRequestHandler srh; 
  private FakeObjectStorage storage; 
  private SpyLogger spyLogger; 
   
  private static final String EXAMPLE_TOPIC = "example.reading.store"; 
  private static final String PRODUCER_CODE = "EXMSS001"; 
   
  // to get access to the special testing API 
  private InVMInterProcessConnector asStubConnector; 
 
  private GameFavorite p1, p2; 

  @Before 
  public void setup() throws IOException { 
    // create a spy into the logging so we can validate what goes on 
    // in the client request handler 
    spyLogger = new SpyLogger(); 
    
    // create the depended-on objects in fake object implementations 
    // for the server side to allow inspection. 
    storage = new FakeObjectStorage(); 
    DeserializerFactory factory = new DeserializerFactory() {    
      @Override 
      public Deserializer createDeserializer(String producerCode) { 
        return new GameFavoriteDeserializer(); 
      } 
    }; 
    srh = new StandardServerRequestHandler(storage, factory); 
    asStubConnector = new InVMInterProcessConnector(srh);   
    connector = asStubConnector; 
     
    crh = new StandardClientRequestHandler<GameFavorite>(PRODUCER_CODE,  
        connector, new GameFavoriteSerializer(), 0, 5, spyLogger ); 
     
    p1 = new GameFavorite("Henrik", "SCII"); 
    p2 = new GameFavorite("Bimse", "Bimses julerejse"); 
   } 

  @Test 
  public void shouldSendOverNonFailingConnection() throws IOException { 
     
    // First, send package 1 and see it at the db 
    crh.send(p1, EXAMPLE_TOPIC); 
    BasicDBObject dbo = storage.getCollectionNamed(PRODUCER_CODE).get(0); 
    assertNotNull( dbo ); 
    assertEquals( "Henrik", dbo.getString("name")); 
    // assert no logging made 
    assertNull( spyLogger.getLastLog() ); 
     
    // assert that just the right number of open calls are made to the 
    // channel connector 
    assertEquals( 1, asStubConnector.getCountOfCallsToOpen() ); 
  } 

  
  @Test 
  public void shouldResendWhenAlreadyClosedExceptionOccurs() throws IOException { 
    // Next ensure that a AlreadyClosedException occurs 
    // and validate that a) a retry is attempted and b) 
    // the data are sent nevertheless. 
    asStubConnector.pushExceptionToBeThrownAtNextSend( new AlreadyClosedException("Ex01", null)); 
    crh.send(p2, EXAMPLE_TOPIC); 
    // verify the logged message, thus spying on the request handler 
    assertEquals( "INFO:AlreadyClosedException/retry=1/Msg=clean connection shutdown; reason: Ex01",  
        spyLogger.getLastLog()); 
    // and verify that the message indeed got through 
    assertEquals( "Bimse", storage.getCollectionNamed(PRODUCER_CODE).get(0).getString("name")); 
 
    // assert that only two open calls were made 
    assertEquals( 2, asStubConnector.getCountOfCallsToOpen() ); 
  } 
   
  @Test 
  public void shouldResendWhenMultipleAlreadyClosedExcpetionsOccurs() throws IOException { 
   // Assert multiple exceptions are handled, by pushing the last exception first (stack behaviour) 
    asStubConnector.pushExceptionToBeThrownAtNextSend( new AlreadyClosedException("Ex03", null)); 
    asStubConnector.pushExceptionToBeThrownAtNextSend( new AlreadyClosedException("Ex02", null)); 
    crh.send(p1, EXAMPLE_TOPIC); 
    assertEquals( "INFO:AlreadyClosedException/retry=2/Msg=clean connection shutdown; reason: Ex03",  
        spyLogger.getLastLog()); 
    // and verify that the message indeed got through 
    assertEquals( "Henrik", storage.getCollectionNamed(PRODUCER_CODE).get(0).getString("name")); 
 
  } 
   
  @Test 
  public void shouldResendWhenBothConnectAndAlreadyClosedExcpetionsOccurs() throws IOException { 
    // Validate handling of ConnectException as well 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx03")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx02")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx01")); 
    crh.send(p2, EXAMPLE_TOPIC); 
    assertEquals( "INFO:ConnectException/retry=3/Msg=CEx03",  
        spyLogger.getLastLog()); 
    // and verify that the message indeed got through 
    assertEquals( "Bimse", storage.getCollectionNamed(PRODUCER_CODE).get(0).getString("name")); 
     
    // Finally validate interleaved failures 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx04")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new AlreadyClosedException("Ex05", null)); 
    crh.send(p1, EXAMPLE_TOPIC); 
    assertEquals( "INFO:ConnectException/retry=2/Msg=CEx04",  
        spyLogger.getLastLog()); 
    // and verify that the message indeed got through (has index 1 in collection as Bimse is already stored) 
    assertEquals( "Henrik", storage.getCollectionNamed(PRODUCER_CODE).get(1).getString("name"));  
  } 
   
  @Test 
  public void shouldFailAfter6ReconnectAttempts() throws IOException { 
     
    // Ensure that reconnect attempts are only made five times. 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx06")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx05")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new AlreadyClosedException("Ex04", null)); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new AlreadyClosedException("Ex03", null)); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx02")); 
    asStubConnector.pushExceptionToBeThrownAtNextSend(new ConnectException("CEx01")); 
   
    try  { 
      crh.send(p1, EXAMPLE_TOPIC); 
      fail( "Reconnection attempt was not given up after the 5th try/"+spyLogger.getLastLog()); 
    } catch ( ConnectException ex ) { 
      assertEquals("INFO:ConnectException/No more retry attempts permitted/Msg=CEx06",  
          spyLogger.getLastLog()); 
    } 
    // assert that only two open calls were made 
    assertEquals( 6, asStubConnector.getCountOfCallsToOpen() ); 
 
  } 


}
