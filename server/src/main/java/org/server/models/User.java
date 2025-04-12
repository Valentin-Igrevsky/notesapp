package org.server.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public User(int id, String name, String surname, String password, String username) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.username = username;
    }

    @Override
    public String toString() {
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }
}
