package org.client.http;

import org.client.models.User;
import org.client.utils.JsonUtils_client;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthClient {
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final HttpClient client = HttpClient.newHttpClient();

    public boolean register(User user) throws Exception {
        String json = JsonUtils_client.toJson(user);
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

    public Integer login(User user) throws Exception {
        String json = JsonUtils_client.toJson(user);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            return JsonUtils_client.fromJson(response.body(), User.class).getId();
        }
        return null;
    }
}