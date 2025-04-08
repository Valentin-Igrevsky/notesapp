package org.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.server.database.UserRepository;
import org.server.models.User;
import org.server.Utils.JsonUtils_server;
import java.sql.SQLException;
import java.io.*;

public class AuthApi implements HttpHandler {
    private final UserRepository userRepo = new UserRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes());

            if (path.endsWith("/register")) {
                handleRegister(exchange, body);
            } else if (path.endsWith("/login")) {
                handleLogin(exchange, body);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleRegister(HttpExchange exchange, String body) throws IOException {
        User user = JsonUtils_server.fromJson(body, User.class);
        try {
            if (userRepo.createUser(user)) {
                sendResponse(exchange, 201, "User created");
            } else {
                sendResponse(exchange, 400, "Error creating user");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Database error");
        }
    }

    private void handleLogin(HttpExchange exchange, String body) throws IOException {
        User requestUser = JsonUtils_server.fromJson(body, User.class);
        try {
            User dbUser = userRepo.findUserByLogin(requestUser.getLogin());
            if (dbUser != null && dbUser.getPassword().equals(requestUser.getPassword())) {
                String response = "{\"id\":" + dbUser.getId() + "}";
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 401, "Invalid credentials");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Database error");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, data.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data.getBytes());
        }
    }
}