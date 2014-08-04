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
package dk.au.cs.karibu.testdoubles;

import java.util.ArrayList;

import org.slf4j.*; 

/** A spy on the logging process. ONLY THOSE  
 * KNOWN TO BE USED ARE IMPLEMENTED - THE REST 
 * JUST THROWS AN EXCEPTION.
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 */ 

public class SpyLogger implements Logger {
  private ArrayList<String> fullLog = new ArrayList<String>(256);
  private String lastLog; 
  
  public String getLastLog() {
    return getLastLog(1);
  }
  /** get the n'th latest log entry */
  public String getLastLog(int n) {
    assert n > 0;
    if (fullLog.size() == 0 ) 
      return null;
    return fullLog.get( fullLog.size()-n );
  }

  public ArrayList<String> getFullLog() {
    return fullLog;
  }
 
  @Override 
  public void debug(String arg0) { e(); } 
  @Override 
  public void debug(String arg0, Object arg1) { e(); } 
 
  @Override 
  public void debug(String arg0, Object... arg1) { e(); } 
 
  @Override 
  public void debug(String arg0, Throwable arg1) { e(); } 
 
  @Override 
  public void debug(Marker arg0, String arg1) { e(); } 
 
  @Override 
  public void debug(String arg0, Object arg1, Object arg2) { e(); } 
 
  @Override 
  public void debug(Marker arg0, String arg1, Object arg2) { e(); } 
 
  @Override 
  public void debug(Marker arg0, String arg1, Object... arg2) { e(); } 
 
  @Override 
  public void debug(Marker arg0, String arg1, Throwable arg2) { e(); } 
 
  @Override 
  public void debug(Marker arg0, String arg1, Object arg2, Object arg3) { e(); } 
 
  @Override 
  public void error(String arg0) {
    lastLog = "ERROR:"+arg0; 
    fullLog.add(lastLog);
  } 
 
  @Override 
  public void error(String arg0, Object arg1) { e(); } 
 
  @Override 
  public void error(String arg0, Object... arg1) { e(); } 
 
  @Override 
  public void error(String arg0, Throwable arg1) { e(); } 
 
  @Override 
  public void error(Marker arg0, String arg1) { e(); } 
 
  @Override 
  public void error(String arg0, Object arg1, Object arg2) { e(); } 
 
  @Override 
  public void error(Marker arg0, String arg1, Object arg2) { e(); } 
 
  @Override 
  public void error(Marker arg0, String arg1, Object... arg2) { e(); } 
 
  @Override 
  public void error(Marker arg0, String arg1, Throwable arg2) { e(); } 
 
  @Override 
  public void error(Marker arg0, String arg1, Object arg2, Object arg3) { e(); } 
 
  @Override 
  public String getName() { 
    return "SpyLogger"; 
  } 
 
  @Override 
  public void info(String arg0) { 
    lastLog = "INFO:"+arg0; 
    fullLog.add(lastLog);
    //System.out.println(" --> "+ lastLog); 
  } 
 
  @Override 
  public void info(String arg0, Object arg1) { e(); } 
 
  @Override 
  public void info(String arg0, Object... arg1) { e(); } 
 
  @Override 
  public void info(String arg0, Throwable arg1) { e(); } 
 
  @Override 
  public void info(Marker arg0, String arg1) { e(); } 
 
  @Override 
  public void info(String arg0, Object arg1, Object arg2) { e(); } 
 
  @Override 
  public void info(Marker arg0, String arg1, Object arg2) { e(); } 
 
  @Override 
  public void info(Marker arg0, String arg1, Object... arg2) { e(); } 
 
  @Override 
  public void info(Marker arg0, String arg1, Throwable arg2) { e(); } 
 
  @Override 
  public void info(Marker arg0, String arg1, Object arg2, Object arg3) { e(); } 
 
  @Override 
  public boolean isDebugEnabled() { 
    return false; 
  } 
 
  @Override 
  public boolean isDebugEnabled(Marker arg0) { 
    return false; 
  } 
 
  @Override 
  public boolean isErrorEnabled() { 
    return false; 
  } 
 
  @Override 
  public boolean isErrorEnabled(Marker arg0) { 
    return false; 
  } 
 
  @Override 
  public boolean isInfoEnabled() { 
    return false; 
  } 
 
  @Override 
  public boolean isInfoEnabled(Marker arg0) { 
    return false; 
  } 
 
  @Override 
  public boolean isTraceEnabled() { 
    return false; 
  } 
 
  @Override 
  public boolean isTraceEnabled(Marker arg0) { 
    return false; 
  } 
 
  @Override 
  public boolean isWarnEnabled() { 
    return false; 
  } 
 
  @Override 
  public boolean isWarnEnabled(Marker arg0) { 
    return false; 
  } 
 
  @Override 
  public void trace(String arg0) {  } 
 
  @Override 
  public void trace(String arg0, Object arg1) { e(); } 
 
  @Override 
  public void trace(String arg0, Object... arg1) { e(); } 
 
  @Override 
  public void trace(String arg0, Throwable arg1) { e(); } 
 
  @Override 
  public void trace(Marker arg0, String arg1) { e(); } 
 
  @Override 
  public void trace(String arg0, Object arg1, Object arg2) { e(); } 
 
  @Override 
  public void trace(Marker arg0, String arg1, Object arg2) { e(); } 
 
  @Override 
  public void trace(Marker arg0, String arg1, Object... arg2) { e(); } 
 
  @Override 
  public void trace(Marker arg0, String arg1, Throwable arg2) { e(); } 
 
  @Override 
  public void trace(Marker arg0, String arg1, Object arg2, Object arg3) { e(); } 
 
  @Override 
  public void warn(String arg0) { e(); } 
 
  @Override 
  public void warn(String arg0, Object arg1) { e(); } 
 
  @Override 
  public void warn(String arg0, Object... arg1) { e(); } 
 
  @Override 
  public void warn(String arg0, Throwable arg1) { e(); } 
 
  @Override 
  public void warn(Marker arg0, String arg1) { e(); } 
 
  @Override 
  public void warn(String arg0, Object arg1, Object arg2) { e(); } 
 
  @Override 
  public void warn(Marker arg0, String arg1, Object arg2) { e(); } 
 
  @Override 
  public void warn(Marker arg0, String arg1, Object... arg2) { e(); } 
 
  @Override 
  public void warn(Marker arg0, String arg1, Throwable arg2) { e(); } 
 
  @Override 
  public void warn(Marker arg0, String arg1, Object arg2, Object arg3) { e(); }

  private void e() {
    throw new RuntimeException("*** YOU ARE USING A UNIMPLEMENTED METHOD IN THE SPYLOGGER - CORRECT! ***");
  }



}
