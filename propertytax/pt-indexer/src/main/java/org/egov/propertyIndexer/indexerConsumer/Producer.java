package org.egov.propertyIndexer.indexerConsumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.egov.propertyIndexer.config.PropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/*
 * This class will use for sending property object to kafka server
 * 
 */

@Configuration
@Service
@Slf4j
public class Producer {

    @Autowired
	PropertiesManager propertiesManager;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    /*
     * This method will return map object for producer configuration
     */

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> producerProperties = new HashMap<String, Object>();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesManager.getBootstrapServer());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return producerProperties;
    }

    /*
     * This method will return producer factory bean based on producer configuration
     */

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    /*
     * This method will return kafka template bean based on producer factory bean
     */

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /*
     * This method will send object to kafka server based on topic name topic: name of topic propertyRequest:PropertyRequest
     * object
     */

    public void send(String topic, Object propertyRequest) {
        log.info("topic name is : " + topic + "  producerecord is: " + propertyRequest);
        kafkaTemplate.send(topic, propertyRequest);
    }
}
