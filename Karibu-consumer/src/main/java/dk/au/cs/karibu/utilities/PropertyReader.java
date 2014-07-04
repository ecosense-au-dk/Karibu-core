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
package dk.au.cs.karibu.utilities;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.*;

/** Encapsulate a simple property reader used for reading
 * mongo and rabbit properties from a specified root folder.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class PropertyReader {

  private String resourceFolder;
  private Logger log;

  /** Create the reader and fix the root folder which
   * the individual property files are located in.
   * @param resourceFolderRoot
   */
  public PropertyReader(String resourceFolderRoot) {
    log = LoggerFactory.getLogger(PropertyReader.class); 
    resourceFolder = resourceFolderRoot;
    if ( !resourceFolder.endsWith("/") ) {
      resourceFolder += "/";
    }
  }

  /** Read a given property file and fail fast (terminate execution)
   * if the given file load failed.
   * @param filenamePrefix the prefix of the property file name,
   * '.properties' are automatically appended.
   * @return the property set in the file.
   */
  public Properties readPropertiesFailFast(String filenamePrefix) {
    Properties properties = new Properties();
    String filename = resourceFolder + filenamePrefix +".properties";
    try {
      properties.load(new FileInputStream(filename));
    } catch (Exception e) {
      log.error("Property file ("+filename+") load failed.", e);
      System.out.println("Property file ("+filename+") load failed, review log for details...");
      System.exit(1);
    }
    return properties;
  }

}
