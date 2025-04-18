package org.client.models;

import java.util.List;

public class httpPacket {
    private boolean correct;
    private int statusCode = 0;
    private String body = null;
    private User user = null;
    private List<Note> notes = null;
    private Note note = null;

    public httpPacket(boolean correct, int statusCode, String body, User user, List<Note> notes, Note note) {
        this.correct = correct;
        this.statusCode = statusCode;
        this.body = body;
        this.user = user;
        this.notes = notes;
        this.note = note;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public User getUser() {
        return user;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Note getNote() {
        return note;
    }
}
