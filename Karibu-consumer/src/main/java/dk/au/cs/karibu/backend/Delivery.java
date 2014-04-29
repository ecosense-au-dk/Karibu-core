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

package cs.karibu.backend;

/** A message delivery from the MQ containing the
 * payload itself as well as the ID of the message. 
 * Mirrors partially the RabbitMQ equivalent.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class Delivery {

  private byte[] payload;
  private long deliveryTag;

  public Delivery(long deliveryTag, byte[] payload) {
    this.deliveryTag = deliveryTag;
    this.payload = payload;
  }

  public byte[] getPayload() {
    return payload;
  }

  public long getDeliveryTag() {
    return deliveryTag;
  }
}
