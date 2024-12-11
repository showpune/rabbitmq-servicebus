package com.example.messagingrabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import lombok.extern.slf4j.Slf4j;

import com.azure.spring.messaging.AzureMessagingException;
import com.azure.spring.messaging.annotation.ServiceBusListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MassRolloutRabbitQueueListener2 {

    /**
     * Kafka Listener to consume all the VIN list from MassRollout Kafka Topic
     *
     * @param message
     */
    private MassRolloutMessageEventHandler massRolloutVinEventHandler;
    private ObjectMapper objectMapper;

    public MassRolloutRabbitQueueListener2(ObjectMapper objectMapper,
                                         MassRolloutMessageEventHandler massRolloutKafkaVinEventHandler) {
        this.objectMapper = objectMapper;
        this.massRolloutVinEventHandler = massRolloutKafkaVinEventHandler;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    /**
     * Kafka Listener to consume all the VIN list from MassRollout Kafka Topic
     *
     * @param message
     */
    @ServiceBusListener(destination = "${rabbitmq.firemassrollout.message.queue.name}", containerFactory = "containerFactoryAckAuto", exclusive = true)
    public void listenMassRolloutEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenMassRolloutEvent() ... Message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());
            massRolloutVinEventHandler.handleAddEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (Azure Service Bus): listenMassRolloutEvent() ...Error processing message: {}",
                    LogUtil.getErrorStrFromException(exe));
            throw new AzureMessagingException(exe, ServiceBusErrorSource.ABANDON);
        }
    }

    @ServiceBusListener(destination = "${rabbitmq.firemassrollout.remove.message.queue.name}", containerFactory = "containerFactoryAckAuto", exclusive = true)
    public void listenRemoveEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenRemoveEvent() ... Remove message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());

            massRolloutVinEventHandler.handleRemoveEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (Azure Service Bus): listenRemoveEvent() ...Error processing message: {}",
                    LogUtil.getErrorStrFromException(exe));
            throw new AzureMessagingException(exe, ServiceBusErrorSource.ABANDON);
        }
    }
}