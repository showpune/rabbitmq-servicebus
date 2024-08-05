package com.example.messagingrabbitmq;

public class MessageProperties {
    public static final String CONTENT_TYPE_JSON = "application/json";
    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
