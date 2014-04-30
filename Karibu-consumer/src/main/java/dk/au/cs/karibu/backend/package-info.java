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
 
/** The Karibu backend core framework roles.
 * 
 * Karibu is a message oriented framework for collecting sensor data
 * and storing them in a backend storage. It provides A) a simple
 * (Java) client side API and default implementations for sending data
 * B) a backend API and default implementations based upon RabbitMQ
 * and MongoDB C) a running backend for the EcoSense project.
 * 
 * The backtier requires a running RabbitMQ server(s), one or
 * several Karibu daemons (this and other packages) and a
 * MongoDB instance (or a replica set).
 * 
 * The StandardDaemon (standard package) is the Java application
 * to run the daemon. It is configured with a MessageReceiverEndpoint
 * instance which in turn is configured through the builder in
 * MesageReceiverEndpointFactory. In module karibu-backend-production 
 * you can find the Ecosense production system configuration for
 * the standard daemon.
 *  
 * Emphasis has been put on staged testing approach: you can develop client 
 * side applications using light-weight (single-VM in-memory) implementations 
 * of all abstractions for speedy development and later gradually migrate 
 * to a local distributed setup (own rabbitmq and mongodb installations)  
 * and finally to our full EcoSense backtier system. 
 * 
 * Interfaces in this package define the core roles.
 * 
 * You will find bindings to RabbitMQ and MongoDB in the respective
 * sub packages.
 * 
 * The naming convention is that classes prefixed with 'StandardX'
 * are the framework implementation of the interfaces named 'X'
 * and it is located in the 'standard' sub package. 
 * 
 * @author Henrik Baerbak Christensen, Aarhus University 
 * 
 */ 
package dk.au.cs.karibu.backend;