package com.example.messagingrabbitmq;

public interface MessageProducer {
    void sendMassRolloutRequest(RolloutDataAccessorRequest request);

    void queueMassDeploymentRequest(DeployRequest request);

    void sendManifestOrchestratorRequest(PackageManifestRequest request);

    void superPackageRequestDCs(SuperPackageDCRequest request);

    void queueFilteredMassDeploymentRequest(SuperPackageDeployRequest request);

    void sendDcsRequestToQueue(String dCSRequest);

    void sendUcManifestOrchestratorRequest(CreateUcManifestRequest request);

    void sendUcSuccessDeploymentStatusResponse(UcSuccessDeploymentStatusResponse response);

    void sendMassDeploymentResultResponse(DeployResponse deployResponse);
}
