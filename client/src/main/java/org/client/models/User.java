package org.client.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {
    private static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private int id;
    private String name;
    private String surname;
    private String username;
    private String password;


    public User() {
        this.name = null;
        this.surname = null;
    }

    public User(int id, String name, String surname, String password) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.password = password;
    }

    public User(int id, String name, String surname, String password, String username) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toJSON() throws com.fasterxml.jackson.core.JsonProcessingException {
        return ow.writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
