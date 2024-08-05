package com.example.messagingrabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MassRolloutRabbitQueueListener {

    /**
     * Kafka Listener to consume all the VIN list from MassRollout Kafka Topic
     *
     * @param message
     */
    @RabbitListener(queues = MessagingRabbitmqApplication.queueName, group = "${rabbitmq.firemassrollout.group}")
    public void listenMassRolloutEvent(String message) {
        System.out.println("Received from Listener <" + message + ">");
    }

}