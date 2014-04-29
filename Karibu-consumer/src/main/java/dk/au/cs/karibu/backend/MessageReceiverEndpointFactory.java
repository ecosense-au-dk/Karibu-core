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

import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.*;

import cs.karibu.backend.mongo.*;
import cs.karibu.backend.rabbitmq.*;
import cs.karibu.backend.standard.*;
import cs.karibu.producer.rabbitmq.*;
import cs.karibu.testdoubles.*;

/** This the factory for correctly configuring a MessageReceiverEndpoint
 * that handles backtier processing in Karibu.
 * 
 * It uses the Builder pattern (not from GOF but from Effective Java / Joshua Bloch)
 * for easing the pain of creating the highly configurable MessageReceiverEndpoint
 * and ensuring the resulting configuration is valid and sound.
 * 
 * Usage:
 *   You create an instance of MessageReceiverEndpointFactory.Builder(),
 *   invoke the required methods for configuration, and finally invoke
 *   the build() method which returns a MessageReceiverEndpoint instance.
 *   If the configuration is invalid a runtime exception will be thrown
 *   AND more detail is reported in the log files.
 *   
 * Ex:
 *   Study the code in 'StorageDaemon.java', 'GFViewApp.java' and
 *   'TestMessageReceiverEndpoint.java'
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class MessageReceiverEndpointFactory {

  public static class Builder {
    private PollingConsumer pollingConsumer;
    private Properties exhangeProperties;
    private Properties queueProperties;

    private ProcessingStrategy processingStrategy;
    private Properties mongoDBProperties;

    private StatisticHandler statisticHandler;

    private DeserializerFactory deserializerFactory;
    private Logger logger;
    
    private int baseReconnectionDelayInMs;
    private int mongoElectionDelayMs;
    private int countOfMessagesBetweenOutput;

    public Builder() { 
      // mark all delegates as null to allow
      // later 'build()' method to detect which
      // delegates are unconfigured and thus
      // must be given standard implementations.
      pollingConsumer = null;
      processingStrategy = null;
      statisticHandler = null;
      deserializerFactory = null;
      logger = null;
      exhangeProperties = null;
      queueProperties = null;
      mongoDBProperties = null;
      
      countOfMessagesBetweenOutput = 0;

      // set default values for delays
      baseReconnectionDelayInMs = MessageReceiverEndpoint.defaultBaseDelayInMS;
      mongoElectionDelayMs = MessageReceiverEndpoint.defaultNotProcessedDelayInMs;
    }
    /** configure using a pollingConsumer instance directly. This exclude using
     * the exchangeAndQueueProperies builder method.
     */
    public Builder pollingConsumer( PollingConsumer pc ) {
      if ( exhangeProperties != null ) {
        throw new RuntimeException("MessageReceiverEndpoint build error: Calling pollingConsumer() and "+
            "exchangeAndQueueProperties() are mutually exclusive!");
      }
      this.pollingConsumer = pc;
      return this;
    }
    /** configure a RabbitMQ based polling consumer, with the 
     * configurations outlined in the given property files.
     * This excludes using the pollingConsumer method
     */
    public Builder exhangeAndQueueProperties(Properties exchangeProperties,
        Properties queueProperties) {
      if ( pollingConsumer != null ) {
        throw new RuntimeException("MessageReceiverEndpoint build error: Calling pollingConsumer() and "+
            "exchangeAndQueueProperties() are mutually exclusive!");
      }
      this.exhangeProperties = exchangeProperties;
      this.queueProperties = queueProperties;
      return this;
    }

    /** configure to use a specific processing strategy. Excludes
     * using the mongoDBProperties method.
     */
    public Builder processingStrategy( ProcessingStrategy ps ) {
      if ( mongoDBProperties != null ) {
        throw new RuntimeException("MessageReceiverEndpoint build error: Calling processingStrategy() and "+
            "mongoDBProperties() are mutually exclusive!");
      }
      this.processingStrategy = ps;
      return this;
    }
    
    /** configure to use a mongo db storage processing strategy. Excludes using
     * the processingStrategy method
     */
    public Builder mongoDBProperties(Properties mongoProperties) {
      if ( processingStrategy != null ) {
        throw new RuntimeException("MessageReceiverEndpoint build error: Calling processingStrategy() and "+
            "mongoDBProperties() are mutually exclusive!");
      }
      this.mongoDBProperties = mongoProperties;
      return this;
    }
    
    /** configure for monitoring output when using mongo db storage */
    public Builder monitorAndOutputStatisticsInInterval(int countMsgBetweenOutput) {
      assert countMsgBetweenOutput > 0;
      this.countOfMessagesBetweenOutput = countMsgBetweenOutput;
      return this;
    }

    /** configure for a specific statistics handler
     * 
     */
    public Builder statisticsHandler( StatisticHandler handler ) {
      this.statisticHandler = handler;
      return this;
    }
    /** configure with a specific deserializer factory. if not called
     * then the standard deserializer factory is used.
     */
    public Builder deserializerFactory( DeserializerFactory df ) {
      this.deserializerFactory = df;
      return this;
    }

    /** configure with a specific logger. if not called then
     * a standard logger is used (configured by ivy and log4j properties)
     */
    public Builder logger(Logger logger) {
      this.logger = logger;
      return this;
    }

    /** configure special delay in reconnnection to MQ. if not
     * called a default values is used.
     */
    public Builder baseReconnectionDelayMs(int delayInMs) {
      this.baseReconnectionDelayInMs = delayInMs;
      return this;
    }

    /** configure special delay in delay during mongo primary
     * election. if not called, a default value is used.
     * @param delayInMs
     * @return
     */
    public Builder mongoElectDelayMs(int delayInMs) {
      this.mongoElectionDelayMs = delayInMs;
      return this;
    }

    /** the final method that builds the configuration. */
    public MessageReceiverEndpoint build() {

      ensureConfigurationIsSoundOrFail();
      
      ServerRequestHandler requestHandler =  
          new StandardServerRequestHandler(processingStrategy, 
              deserializerFactory, 
              statisticHandler,
              logger); 
   
      MessageReceiverEndpoint mre =
          new MessageReceiverEndpoint(pollingConsumer, 
              requestHandler, 
              baseReconnectionDelayInMs, 
              logger, 
              mongoElectionDelayMs); 

      return mre;
    }

    private void ensureConfigurationIsSoundOrFail() {
      // If there is not logger, we will just define one
      if ( logger == null ) {
        logger = LoggerFactory.getLogger(MessageReceiverEndpoint.class);
      }
      // the pollingConsumer is either defined directly (for testing)
      // or defined as RabbitMQ using property files. Fail if none
      // exists
      if ( pollingConsumer == null && exhangeProperties == null ) {
        String errMsg = "Invalid configuration in MessageEndPointFactory: "+
            "You have to define either exhange and queue properties OR "+
            "a specific pollingConsumer.";  
        logger.error( errMsg );
        throw new RuntimeException( errMsg );
      }
      // The case that both are set is handled already in the set methods
      
      // if exchange and queue properties are defined, then create a RabbitMQ
      // polling consumer
      if ( exhangeProperties != null ) {
        pollingConsumer = createRabbitMQPollingConsumer(exhangeProperties, queueProperties);
      }
      
      // If no deserializer factory is defined we use the standard one
      if ( deserializerFactory == null ) {
        deserializerFactory = new StandardDeserializerFactory();
      }
            
      // Either a processing strategy OR mongoDB properties must be defined
      if ( processingStrategy == null && mongoDBProperties == null) {
        String errMsg = "Invalid configuration in MessageEndPointFactory: No processing strategy defined.";
        logger.error(errMsg);
        throw new RuntimeException(errMsg);
      }
      // The case where both are defined has been handled by the setter methods
      
      // Remember the 'delegate' in case a decorator is put around the
      // processing strategy; need for the statistics handler
      ProcessingStrategy coreProcessingStrategy = null;
      
      // If mongo properties are defined then we configure for MongoDB storage
      if ( mongoDBProperties != null ) {
        MongoConfiguration mongoConfig;
        try {
          mongoConfig = new StandardMongoConfiguration(mongoDBProperties);
        } catch (UnknownHostException e) {
          logger.error("MessageReceiverEndpoint factory: Unknown Mongo host", e);
          throw new RuntimeException("MessageReceiverEndpoint factory: Unknown Mongo host, Review the log");
        }
        processingStrategy = new MongoDBStorage(mongoConfig);
        coreProcessingStrategy = processingStrategy;
        // if a value has been set for the output count then we
        // decorate the processing strategy with a monitoring decorator
        if ( countOfMessagesBetweenOutput > 0 ) {
          ProcessingStrategy decorator = 
              new MonitoringStorageDecorator(processingStrategy, 
                  countOfMessagesBetweenOutput);
          processingStrategy = decorator;
        }
      }
      
      if ( statisticHandler == null ) {
        if ( coreProcessingStrategy != null ) {
          StatisticStorageStrategy storageForStats = (StatisticStorageStrategy) coreProcessingStrategy;
          statisticHandler = new StandardStatisticHandler( storageForStats, new RealTimestampStrategy() );
        } else {
          String errMsg = "No statistics handler defined. Either you must inject a MongoDB configuration or supply your own handler.";
          logger.error(errMsg);
          throw new RuntimeException(errMsg);
        }
      }
    }

    private PollingConsumer createRabbitMQPollingConsumer(Properties exchangeProperties, 
        Properties queueProperties) {
      // Define the rabbit MQ exchange and queue configuration 
      RabbitExchangeConfiguration rabbitExchangeConfig =
          new StandardRabbitExchangeConfiguration(exchangeProperties);
      RabbitQueueConfiguration rabbitQueueConfig =  
          new StandardRabbitQueueConfiguration(queueProperties); 

      // Configure and create the MessageReceiverEndpoint, 
      // using a RabbitMQ as polling consumer 
      PollingConsumer pollingConsumer; 
      pollingConsumer = 
          new RabbitMQPollingConsumer(rabbitExchangeConfig,
              rabbitQueueConfig); 

      return pollingConsumer;
    }
  }
}
