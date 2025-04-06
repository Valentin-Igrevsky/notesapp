package org.client.models;

public class Note {
    private int id;
    private String title;
    private String content;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}