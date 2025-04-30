package org.server.models;

public class ExchangeResponse {
    private final int statusCode;
    private final String responseMessage;
    private final String contentType;

    public ExchangeResponse(int statusCode, String responseMessage, String contentType) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.contentType = contentType;
    }

    public ExchangeResponse(int statusCode, String responseMessage) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.contentType = "text/plain";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getContentType() {
        return contentType;
    }
}
