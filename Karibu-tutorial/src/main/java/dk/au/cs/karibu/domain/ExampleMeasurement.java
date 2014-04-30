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

package dk.au.cs.karibu.domain;

import java.util.Calendar;

/** This is an example of a measurement class
 * that contains a collection of data that is
 * treated by Karibu as a "whole" to be
 * produced in a client and sent to the database
 * using Karibu client library and daemons.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class ExampleMeasurement {

  private Calendar now;
  private long count;

  public ExampleMeasurement(Calendar now, long count) {
    this.now = now;
    this.count = count;
  }

  public Calendar getNow() {
    return now;
  }

  public void setNow(Calendar now) {
    this.now = now;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }


}
