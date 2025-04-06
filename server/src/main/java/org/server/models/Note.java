package org.server.models;

public class Note {
    private int id;
    private int userId;
    private String title;
    private String content;

    // Конструкторы
    public Note() {}
    public Note(int id, int userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}