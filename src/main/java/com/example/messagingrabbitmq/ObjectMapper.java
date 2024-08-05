package com.example.messagingrabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;

public class ObjectMapper {
    public FlareMessage readValue(String message, Class<FlareMessage> class1) {
        return new FlareMessage();
    }
    public void configure(DeserializationFeature failOnUnknownProperties, boolean b) {
    }
}
