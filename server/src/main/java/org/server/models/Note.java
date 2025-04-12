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

    public Note(int id, long creationDate, long lastModify, User owner, String text, String title) {
        this.id = id;
        this.createDate = creationDate;
        this.lastUpdateDate = lastModify;
        this.owner = owner;
        this.text = text;
        this.title = title;
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

    public void setId(int id) {
        this.id = id;
    }
}