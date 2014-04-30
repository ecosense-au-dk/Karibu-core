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
 
/** 
 * This package defines roles and implementations for the 
 * core abstraction of the Karibu producers of data. 
 *  
 * The main abstractions are the ClientRequestHandler with 
 * the central 'send(data)' method; the Serializer interface 
 * that the user of Karibu must implement to support 
 * the client request handler's serialization of data, and 
 * the ChannelConnector which binds the request handler 
 * to the actual inter process communication medium. 
 *  
 * The rabbitmq package contains an implementation of 
 * the ChannelConnector for RabbitMQ. 
 *  
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
package dk.au.cs.karibu.producer;