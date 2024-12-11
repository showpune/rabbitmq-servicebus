package com.example.messagingrabbitmq;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MassRolloutServiceBusListener {

    private MassRolloutMessageEventHandler massRolloutVinEventHandler;
    private ObjectMapper objectMapper;
    private ServiceBusProcessorClient processorClient;

    @Value("${serviceBus.connectionString}")
    private String connectionString;

    @Value("${serviceBus.massRolloutQueueName}")
    private String massRolloutQueueName;

    @Value("${serviceBus.massRolloutRemoveQueueName}")
    private String massRolloutRemoveQueueName;

    public MassRolloutServiceBusListener(ObjectMapper objectMapper,
                                         MassRolloutMessageEventHandler massRolloutVinEventHandler) {
        this.objectMapper = objectMapper;
        this.massRolloutVinEventHandler = massRolloutVinEventHandler;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        this.processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .queueName(massRolloutQueueName)
                .processMessage(context -> {
                    String message = context.getMessage().getBody().toString();
                    listenMassRolloutEvent(message);
                })
                .processError(context -> {
                    log.error("Error occurred within queue processing: {}", context.getException().toString());
                })
                .buildProcessorClient();
        this.processorClient.start();
    }

    public void listenMassRolloutEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenMassRolloutEvent() ... Message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());
            massRolloutVinEventHandler.handleAddEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (AZURE SERVICE BUS): listenMassRolloutEvent() ...Error processing service bus message: {}",
                    LogUtil.getErrorStrFromException(exe));
        }
    }

    public void listenRemoveEvent(String message) {
        try {
            FlareMessage flareMessage = objectMapper.readValue(message, FlareMessage.class);

            log.info("listenRemoveEvent() ... Remove message received from FLARE mass rollout topic for discoveryId : {}",
                    flareMessage.getDiscoveryId());

            massRolloutVinEventHandler.handleRemoveEvent(flareMessage);
        } catch (Exception exe) {
            log.error("CRITICAL FAILURE (AZURE SERVICE BUS): listenRemoveEvent() ...Error processing service bus message: {}",
                    LogUtil.getErrorStrFromException(exe));
        }
    }
}