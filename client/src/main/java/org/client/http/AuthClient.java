package org.client.http;

import org.client.models.User;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthClient {
    private static final String BASE_URL = "http://localhost:8080/auth";
    private final HttpClient client;

    public AuthClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean register(User user) throws Exception {
        String json = String.format(
                "{\"login\":\"%s\",\"password\":\"%s\"}",
                user.getLogin(), user.getPassword()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return response.statusCode() == 201;
    }

    public boolean login(User user) throws Exception {
        String json = String.format(
                "{\"login\":\"%s\",\"password\":\"%s\"}",
                user.getLogin(), user.getPassword()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        return response.statusCode() == 200;
    }
}