package org.client.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Note {
    private static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private final Date createDate;
    private User owner;
    private int id;
    private Date lastUpdateDate;
    private String text;
    private String title;

    public Note() {
        this.createDate = new Date();
        this.lastUpdateDate = new Date();
        this.owner = null;
        this.text = null;
    }

    public Note(User user) {
        this.createDate = new Date();
        this.lastUpdateDate = new Date();
        this.owner = user;
        this.text = null;
    }

    public Note(User owner, String text) {
        this.createDate = new Date();
        this.lastUpdateDate = new Date();
        this.owner = owner;
        this.text = text;
    }

    public Note(int id, Date creationDate, Date lastModify, User owner, String text, String title) {
        this.id = id;
        this.createDate = creationDate;
        this.lastUpdateDate = lastModify;
        this.owner = owner;
        this.text = text;
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    public void updateNote(String text) {
        this.text = text;
        this.lastUpdateDate = new Date();
    }

    public String toJSON() throws com.fasterxml.jackson.core.JsonProcessingException {
        return ow.writeValueAsString(this);
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getLastUpdateDate() {
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

    public String getPreview() {
        return text.length() > 10 ? text.substring(0, 10) : text;
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
}
