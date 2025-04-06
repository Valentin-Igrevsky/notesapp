package org.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.server.database.UserRepository;
import org.server.models.User;
import java.sql.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthApi implements HttpHandler {
    private final UserRepository userRepo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Здесь должна быть десериализация JSON
            User user = new User(); // Заглушка
            user.setLogin(body.split("\"")[3]); // Простейший парсинг
            user.setPassword(body.split("\"")[7]);

            try {
                if (userRepo.createUser(user)) {
                    sendResponse(exchange, 201, "User created");
                } else {
                    sendResponse(exchange, 400, "Error creating user");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "Database error");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String data) throws IOException {
        exchange.sendResponseHeaders(code, data.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data.getBytes());
        }
    }
}