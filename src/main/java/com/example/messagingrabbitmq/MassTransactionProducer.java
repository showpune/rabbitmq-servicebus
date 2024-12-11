package com.example.messagingrabbitmq;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MassTransactionProducer implements MessageProducer {
    private static final String MASS_DEPLOYMENT_RESPONSE_QUEUE = "FIRE-MASS-DEPLOYMENT-";
    private static final String FIRE_MASS_DEPLOYMENT_QUEUE = "FIRE-MASS-DEPLOYMENT-QUEUE-";
    private static final String MANIFEST_ORCHESTRATOR_RESPONSE_QUEUE = "FIRE-MANIFEST-ORCHESTRATOR-";
    private static final String FIRE_DCS_SUPER_PACKAGE = "FIRE-DCS-SUPER-PACKAGE-";
    private static final String FIRE_FILTERED_MASS_DEPLOYMENT_QUEUE = "FIRE-FILTERED-MASS-DEPLOYMENT-QUEUE-";
    private static final String CREATE_UC_MANIFEST_REQUEST_QUEUE = "CREATE_UC_MANIFEST_REQUEST_QUEUE-";
    private static final String MASS_DEPLOY_RESULT_RESPONSE_QUEUE = "FIRE-MASS_DEPLOY_RESULT_RESPONSE_QUEUE-";
    private final ServiceBusSenderClient senderClient;
    private final UserInfoService userInfoService;
    @Value("${serviceBus.connectionString}")
    private String connectionString;
    @Value("${currentEnvironment}")
    private String environment;

    LogUtil log;

    public MassTransactionProducer(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .buildClient();
        log = new LogUtil();
    }

    @Override
    public void sendMassRolloutRequest(RolloutDataAccessorRequest request) {
        String responseString = "test";
        log.info("Sending mass rollout request: " + responseString);
        senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                .setContentType("application/json")
                .setSubject(MASS_DEPLOYMENT_RESPONSE_QUEUE + environment));
    }

    @Override
    public void queueMassDeploymentRequest(DeployRequest request) {
        String user = userInfoService.getIdentifier();
        request.setCreator(user);

        log.info("Queueing mass deployment request: " + request + " for user " + user);
        String responseString = toJsonThrowingException(request);

        senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                .setContentType("application/json")
                .setSubject(FIRE_MASS_DEPLOYMENT_QUEUE + environment));
    }

    private String toJsonThrowingException(Object request) {
        return "";
    }

    @Override
    public void sendManifestOrchestratorRequest(PackageManifestRequest request) {
        log.info("Sending manifest orchestrator request: " + request);
        String responseString = toJsonThrowingException(request);

        senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                .setContentType("application/json")
                .setSubject(MANIFEST_ORCHESTRATOR_RESPONSE_QUEUE + environment));
    }

    @Override
    public void superPackageRequestDCs(SuperPackageDCRequest request) {
        log.info("Sending request to get DCs for super package: " + request);
        String responseString = toJsonThrowingException(request);

        senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                .setContentType("application/json")
                .setSubject(FIRE_DCS_SUPER_PACKAGE + environment));
    }

    @Override
    public void queueFilteredMassDeploymentRequest(SuperPackageDeployRequest request) {
        log.info("Sending request filtered mass deployment request: " + request);
        String responseString = toJsonThrowingException(request);

        senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                .setContentType("application/json")
                .setSubject(FIRE_FILTERED_MASS_DEPLOYMENT_QUEUE + environment));
    }

    @Override
    public void sendDcsRequestToQueue(String dCSRequest) {
        log.info("DCS Request for queue : {}", dCSRequest);
        try {
            senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(dCSRequest)
                    .setContentType("application/json")
                    .setSubject(DCSQueue));
            log.info("Message Published Successfully.");
        } catch (Exception exe) {
            CriticalAlert.rabbitMq("DCS Request :sendDcsRequestToQueue: Failure to send message to DCS Request to queue: " + exe.getMessage(), exe);
            throw exe;
        }
    }

    @Override
    public void sendUcManifestOrchestratorRequest(CreateUcManifestRequest request) {
        log.info("Sending update construct manifest request: " + request);
        String responseString = toJsonThrowingException(request);
        try {
            senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                    .setContentType("application/json")
                    .setSubject(CREATE_UC_MANIFEST_REQUEST_QUEUE + environment));
            log.info("Sent Update Construct manifest creation request to MDO for " + ": " + request);
        } catch (Exception ex) {
            CriticalAlert.rabbitMq("Error posting Update Construct manifest orchestrator request on queue: "
                    + CREATE_UC_MANIFEST_REQUEST_QUEUE + " with message: " + responseString, ex);
        }
    }

    @Override
    public void sendUcSuccessDeploymentStatusResponse(UcSuccessDeploymentStatusResponse response) {
        log.warn("Successful UC deploy notification to FLARE not applicable to China");
    }

    @Override
    public void sendMassDeploymentResultResponse(DeployResponse deployResponse) {
        log.info("Sending mass deployment result response: " + deployResponse.summarize());
        String responseString = toJsonThrowingException(deployResponse);
        try {
            senderClient.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(responseString)
                    .setContentType("application/json")
                    .setSubject(MASS_DEPLOY_RESULT_RESPONSE_QUEUE + environment));
            log.info("Sent Update Construct manifest creation request to MDO for " + ": " + deployResponse.summarize());
        } catch (Exception ex) {
            CriticalAlert.rabbitMq("Error posting Update Construct manifest orchestrator request on queue: "
                    + MASS_DEPLOY_RESULT_RESPONSE_QUEUE + " with message: " + responseString, ex);
        }
    }
}
