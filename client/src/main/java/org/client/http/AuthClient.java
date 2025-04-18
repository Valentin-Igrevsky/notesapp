package org.client.http;

import org.client.models.User;
import org.client.utils.NoteJsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthClient {
    private static final String BASE_URL = "http://localhost:8080/login";
    private final HttpClient client = HttpClient.newHttpClient();

    public HttpResponse<String> register(User user) throws Exception {
        String json = user.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/registration"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> login(String username, String password) throws Exception {
        String url = String.format("%s/authentication?username=%s&password=%s",
                BASE_URL,
                URLEncoder.encode(username, StandardCharsets.UTF_8),
                URLEncoder.encode(password, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
