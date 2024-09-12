package com.example.messagingrabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
    @RabbitListener(queues = "${rabbitmq.firemassrollout.message.queue.name}", group = "${rabbitmq.firemassrollout.group}", containerFactory = "containerFactoryAckAuto",exclusive = true)
    public void listenMassRolloutEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenMassRolloutEvent() ... Message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());
            massRolloutVinEventHandler.handleAddEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (RABBITMQ): listenMassRolloutEvent() ...Error processing rabbit message: {}",
                    LogUtil.getErrorStrFromException(exe));
            throw new AmqpRejectAndDontRequeueException(exe);
        }
    }

    @RabbitListener(queues = "${rabbitmq.firemassrollout.remove.message.queue.name}", group = "${rabbitmq.firemassrollout.group}", containerFactory = "containerFactoryAckAuto",exclusive = true)
    public void listenRemoveEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenRemoveEvent() ... Remove message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());

            massRolloutVinEventHandler.handleRemoveEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (RABBITMQ): listenRemoveEvent() ...Error processing rabbit message: {}",
                    LogUtil.getErrorStrFromException(exe));
            throw new AmqpRejectAndDontRequeueException(exe);
        }
    }


}