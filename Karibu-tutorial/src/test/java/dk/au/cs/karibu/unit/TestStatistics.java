package dk.au.cs.karibu.unit;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.mongodb.BasicDBObject;

import dk.au.cs.karibu.backend.StatisticHandler;
import dk.au.cs.karibu.backend.standard.StandardStatisticHandler;
import dk.au.cs.karibu.testdoubles.*;

/** TDD tests to drive the statistics handling.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestStatistics {

  private SpyStatisticStorage spyStorage;
  private StatisticHandler stat;
  private BasicDBObject dbo;
  private StubTimestampStrategy timestampStrategy;
  
  @Before
  public void setUp() throws Exception {
    spyStorage = new SpyStatisticStorage();
    final Calendar cal = Calendar.getInstance(); 
    cal.set(2012,5,1,7,30,00);  
    timestampStrategy = new StubTimestampStrategy();
    timestampStrategy.setTime( cal );
    stat = new StandardStatisticHandler(spyStorage, timestampStrategy);
  }

  @Test
  public void shouldCoverSingleDataCollection() {   
    stat.notifyReceive("GFKRE003", 47);
    stat.flushToStorage();
    
    dbo = spyStorage.getLast();
    
    assertEquals( "GFKRE003", dbo.getString("MaxChunkProducerCode"));
    assertEquals( 47L, dbo.getLong("MaxChunkSize"));
    assertEquals( 47L, dbo.getLong("TotalBytesSent"));
    
    assertNotNull( dbo.getString("DaemonIP") );
    // System.out.println( dbo.getString("DaemonIP") );
  }

  @Test
  public void shouldCoverTwoDataCollections() {
    stat.notifyReceive("GFKRE003", 47);
    stat.notifyReceive("GFKSC002", 2645);
    stat.flushToStorage();

    dbo = spyStorage.getLast();
    
    assertEquals( "GFKSC002", dbo.getString("MaxChunkProducerCode"));
    assertEquals( 2645L, dbo.getLong("MaxChunkSize"));
    assertEquals( 47L+2645L, dbo.getLong("TotalBytesSent"));
    assertEquals( 2L, dbo.getLong("TotalCountMsg"));
  }
  
  @Test
  public void shouldCoverStatsOnEachProducerCode() {
    stat.notifyReceive("GFKRE003", 47);
    stat.notifyReceive("GFKRE003", 147);
    stat.notifyReceive("GFKRE003", 647);
    stat.notifyReceive("GFKRE003", 1647);
    stat.flushToStorage();

    dbo = spyStorage.getLast();

    // Get the array of code statistics
    List<BasicDBObject> list;
    list = (List<BasicDBObject>) dbo.get("CodeStatList");

    // get first entry in the array
    BasicDBObject item = list.get(0);
    
    assertEquals( "GFKRE003", item.getString("ProducerCode") );
    assertEquals( 4L, item.getLong("CountMsg") );
    assertEquals( 47+147+647+1647, item.getLong("TotalSizeBytes") );
  }

  @Test
  public void shouldCoverStatsOnEachSeveralProducerCode() {
    stat.notifyReceive("GFKRE003", 47);
    stat.notifyReceive("GFKRE003", 147);

    stat.notifyReceive("GFKSC002", 11);
    stat.notifyReceive("GFKSC002", 15);
    stat.flushToStorage();

    dbo = spyStorage.getLast();

    //System.out.println(dbo);
    // Get the array of code statistics
    List<BasicDBObject> list;
    list = (List<BasicDBObject>) dbo.get("CodeStatList");
    
    assertEquals( 2, list.size() );

    BasicDBObject item = list.get(0);
    
    assertEquals( "GFKSC002", item.getString("ProducerCode") );
    assertEquals( 2L, item.getLong("CountMsg") );
    assertEquals( 11+15, item.getLong("TotalSizeBytes") );
  }
  
  @Test
  public void shouldResetAllStatsAfterFlush() {
    stat.notifyReceive("GFKRE003", 47);
    stat.notifyReceive("GFKRE003", 147);

    stat.notifyReceive("GFKSC002", 11);
    stat.notifyReceive("GFKSC002", 15);
    stat.flushToStorage();

    // flush again after no intermediate notifies
    stat.flushToStorage();
    
    dbo = spyStorage.getLast();
    
    assertEquals( 0L, dbo.getLong("MaxChunkSize"));
    assertEquals( "", dbo.getString("MaxChunkProducerCode"));

    assertEquals( 0L, dbo.getLong("TotalBytesSent"));
    assertEquals( 0L, dbo.getLong("TotalCountMsg"));

    List<BasicDBObject> list;
    list = (List<BasicDBObject>) dbo.get("CodeStatList");
    
    assertEquals( 0, list.size() );

  }
  
  @Test
  public void shouldContainStartAndStopTimestamps() {
    // set the end time; the start time was recorded in setup()
    final Calendar cal = Calendar.getInstance(); 
    cal.set(2012,5,1,8,30,01);  
    timestampStrategy.setTime( cal );

    stat.flushToStorage();
  
    dbo = spyStorage.getLast();
    assertEquals( "Fri Jun 01 07:30:00 CEST 2012", dbo.getDate("StartTimestamp").toString() );
    assertEquals( "Fri Jun 01 08:30:01 CEST 2012", dbo.getDate("EndTimestamp").toString() );
    assertEquals( (60L*60+1)*1000, dbo.getLong("ChunkTimeInMs"));
    
    // validate that flush resets the timestamps
    cal.set(2012,5,1,9,29,59);  
    timestampStrategy.setTime( cal );
    
    stat.flushToStorage();
    
    dbo = spyStorage.getLast();
    assertEquals( "Fri Jun 01 08:30:01 CEST 2012", dbo.getDate("StartTimestamp").toString() );
    assertEquals( "Fri Jun 01 09:29:59 CEST 2012", dbo.getDate("EndTimestamp").toString() );
    assertEquals( (60L*60-2)*1000, dbo.getLong("ChunkTimeInMs"));
    
  }

}
