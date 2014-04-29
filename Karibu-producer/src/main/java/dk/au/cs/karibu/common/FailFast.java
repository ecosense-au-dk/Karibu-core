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
package cs.karibu.common;

import java.util.Properties;

/** Utility functions that breaks the
 * execution fast to avoid running with
 * erroneous setup. To be used during
 * initialization, not during steady-state
 * execution.
 *
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class FailFast {

  /** get a named property from a property set
   * and throw an exception in case the property
   * is not present in the set.
   * @param prop the property set to look up
   * @param keyName the name of the key
   * @return the value stored under the key OR
   * throws a runtime exception in case the property
   * is not present.
   */
  public static String readProperty(Properties prop, String keyName) {
    String value = prop.getProperty(keyName);
    if ( value == null ) {
      throw new RuntimeException("The property list does not contain key: "+keyName);
    }
    return value;
  }

}
