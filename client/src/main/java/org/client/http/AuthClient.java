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

    public Integer register(User user) throws Exception {
        String json = user.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/registration"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );
        return response.statusCode();
    }

    public Map.Entry<Integer, User> login(String username, String password) throws Exception {
        String url = String.format("%s/authentication?username=%s&password=%s",
                BASE_URL,
                URLEncoder.encode(username, StandardCharsets.UTF_8),
                URLEncoder.encode(password, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        int statusCode = response.statusCode();
        User user = null;

        if (statusCode == 200) {
            user = NoteJsonParser.fromJson(response.body(), User.class);
        }

        return Map.entry(statusCode, user != null ? user : new User());
    }
}
