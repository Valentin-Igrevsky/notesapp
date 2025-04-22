package org.client.models;

public class httpMessage {
    private boolean success;
    private int status;
    private String message;

    public httpMessage(boolean success, int status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
