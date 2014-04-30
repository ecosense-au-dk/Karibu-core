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

package dk.au.cs.karibu.backend.standard;

import java.lang.management.ManagementFactory;

import javax.management.*;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.*;

import dk.au.cs.karibu.backend.*;
import dk.au.cs.karibu.monitor.*;

/** The default implementation for a backtier daemon that
 * pulls messages from the MQ and processes them, typically
 * stores them in the database tier.
 * 
 * The actual behavior of the daemon is defined by the
 * delegates which must be configured in advance.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class StandardDaemon {
  private Logger log;
  private String daemonName;
  private Thread receiverThread;

  /** Create a daemon with the given name to handle the
   * provided message receiver endpoint. The daemon is
   * NOT run, use 'startAndJoin' for that, or start it manually
   * using the accessor method for the underlying thread.
   * 
   * @param nameOfDaemon name of daemon used in the log files.
   * @param the message receiver endpoint
   */

  public StandardDaemon(String nameOfDaemon, MessageReceiverEndpoint mre) {
    log = LoggerFactory.getLogger(StandardDaemon.class); 
    daemonName = nameOfDaemon;

    receiverThread = 
        new Thread( mre, daemonName+"-ReceiverThread" );   

    MBeanServer mbs = null; 
    MonitoringMBean monitor; 

    mbs = ManagementFactory.getPlatformMBeanServer(); 
    monitor = new Monitoring(mre); 
    try { 
      String theName2 = "EcoSense:name="+daemonName+"Monitor"; 
      ObjectName monitorBeanName =  
          new ObjectName(theName2); 
      mbs.registerMBean(monitor, monitorBeanName); 
      log.info("Registered JMX bean: " + nameOfDaemon); 
    } catch( Exception e ) { 
      String theTrace = ExceptionUtils.getStackTrace(e); 
      log.error("JMX bean exception: " + theTrace); 
    } 

  }
  
  /** Start the daemon and join the calling thread.
   * 
   * @throws InterruptedException
   */
  public void startAndJoin() throws InterruptedException {
    // === and turn on the daemon...
    log.info(daemonName+" starting."); 
    
    receiverThread.start(); 
    receiverThread.join();  
    
    log.info(daemonName+" stopped."); 
  }

  /** Return the underlying thread. 
   * @return the thread created.
   */
  public Thread getReceiverThread() {
    return receiverThread;
  }
}
