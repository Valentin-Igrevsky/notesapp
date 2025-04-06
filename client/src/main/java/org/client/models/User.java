package org.client.models;

public class User {
    private String login;
    private String password;

    public User() {}
    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public void setLogin(String login) { this.login = login; }
    public void setPassword(String password) { this.password = password; }
}