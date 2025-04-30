package org.server.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Note {
    private static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private final long createDate;
    private long lastUpdateDate;
    private User owner;
    private int id;
    private String text;
    private String title;

    public Note() {
        this.createDate = new Date().getTime();
        this.lastUpdateDate = new Date().getTime();
        this.owner = null;
        this.text = null;
    }

    public Note(User user) {
        this.createDate = new Date().getTime();
        this.lastUpdateDate = new Date().getTime();
        this.owner = user;
        this.text = null;
    }

    public Note(User owner, String text) {
        this.createDate = new Date().getTime();
        this.lastUpdateDate = new Date().getTime();
        this.owner = owner;
        this.text = text;
    }

    public Note(int id, long creationDate, long lastModify, User owner, String text, String title) {
        this.id = id;
        this.createDate = creationDate;
        this.lastUpdateDate = lastModify;
        this.owner = owner;
        this.text = text;
        this.title = title;
    }

//    @Override
//    public String toString() {
//        return title;
//    }

    public void updateNote(String text) {
        this.text = text;
        this.lastUpdateDate = new Date().getTime();
    }

    @Override
    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCreateDate() {
        return createDate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getOwnerID() {
        return owner.getId();
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public void setLastUpdateDate() {
        this.lastUpdateDate = new Date().getTime();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String content) {
        this.text = content;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setId(int id) {
        this.id = id;
    }
}