package org.server.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.server.models.Note;
import org.server.models.User;
import org.server.database.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;

public class httpServer {
    private Database database;
    ObjectMapper objectMapper = new ObjectMapper();

    public httpServer(Database database) {
        this.database = database;
    }

    public void startHttpServer(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new FileHandler());

        server.setExecutor(Executors.newCachedThreadPool());

        server.start();
        System.out.println(String.format("[Start Server][%d]", port));
    }

    // Обработчик
    class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            System.out.println(String.format("[Req][%s][%s]", requestMethod, requestPath));
            try {
                switch (requestMethod) {
                    case "GET":
                        handleGetRequest(exchange, requestPath);
                        break;
                    case "POST":
                        handlePostRequest(exchange, requestPath);
                        break;
                    case "DELETE":
                        handleDeleteRequest(exchange, requestPath);
                        break;
                    case "PATCH":
                        handlePatchRequest(exchange, requestPath);
                        break;
                    default:
                        sendResponse(exchange, 405, "Method Not Allowed: " + requestMethod);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error");
            }
        }

        private Map<String, List<String>> parseQuery(HttpExchange exchange) {
            Map<String, List<String>> res = new HashMap<>();

            String queryParams = exchange.getRequestURI().getQuery();
            if (queryParams != null) {
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    res.put(keyValue[0], Collections.singletonList(keyValue.length > 1 ? keyValue[1] : ""));
                }
            }
            return res;
        }

        private Map<String, Object> parseBody(HttpExchange exchange) throws IOException {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes());

            try {
                return objectMapper.readValue(body, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid JSON format");
                return null;
            }
        }

        private Note formatNote(Map<String, Object> noteBody) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            LocalDateTime localDateTime = LocalDateTime.parse((String) noteBody.get("creationDate"), formatter);
            Date creationDate = java.sql.Timestamp.valueOf(localDateTime);

            localDateTime = LocalDateTime.parse((String) noteBody.get("lastModifyDate"), formatter);
            Date lastModifyDate = java.sql.Timestamp.valueOf(localDateTime);

            int note_id = (int) noteBody.get("id");
            String title = (String) noteBody.get("title");
            String text = (String) noteBody.get("text");

            Map<String, Object> owner = (Map<String, Object>) noteBody.get("owner");
            int owner_id = (int) owner.get("id");
            String name = (String) owner.get("name");
            String surname = (String) owner.get("surname");
            String username = (String) owner.get("username");
            String password = (String) owner.get("password");

            return new Note(note_id, creationDate, lastModifyDate, new User(owner_id, name, surname, password, username), text, title);
        }

        // GET-запросы
        private void handleGetRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            switch (requestPath) {
                case "/data/get":
                    getNoteByID(exchange);
                    break;
                case "/data/get/all":
                    getUserNotes(exchange);
                    break;
                case "/authentication":
                    authenticateUser(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed: " + exchange.getRequestMethod());
            }
        }

        private void getNoteByID(HttpExchange exchange) throws IOException, SQLException {
            Map<String, List<String>> queryParams = parseQuery(exchange);

            int noteID;
            if (queryParams.containsKey("id")) {
                try {
                    noteID = Integer.parseInt(queryParams.get("id").get(0));
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "Bad Request: Invalid note ID format");
                    return;
                }

            } else {
                sendResponse(exchange, 400, "Bad Request: The record ID is specified incorrectly");
                return;
            }

            Note note = database.getNoteById(noteID);

            if (note != null) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, note.toJSON());
            } else {
                sendResponse(exchange, 404, "Note not found: " + noteID);
            }
        }

        private void getUserNotes(HttpExchange exchange) throws IOException, SQLException {
            Map<String, List<String>> queryParams = parseQuery(exchange);

            int userID;
            if (queryParams.containsKey("uid")) {
                try {
                    userID = Integer.parseInt(queryParams.get("uid").get(0));
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "Bad Request: Invalid user ID format");
                    return;
                }
            } else {
                sendResponse(exchange, 400, "Bad Request: The user ID is missing");
                return;
            }

            List<Note> notes = database.getUserNotes(userID);

            Map<String, List<Note>> responseMap = Map.of("notes", notes);
            String jsonString = objectMapper.writeValueAsString(responseMap);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonString);
        }

        private void authenticateUser(HttpExchange exchange) throws IOException, SQLException {
            Map<String, Object> credentials = parseBody(exchange);

            if (credentials == null) {
                return;
            }

            String username = (String) credentials.get("username");
            String password = (String) credentials.get("password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "Bad Request: Missing username or password");
                return;
            }

            User user = database.authenticateUser(username, password);
            if (user == null) {
                sendResponse(exchange, 401, "Unauthorized: Invalid username or password");
                return;
            }

            String jsonResponse = user.toJSON();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
        }

        // POST-запросы
        private void handlePostRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            switch (requestPath) {
                case "/registration":
                    registerUser(exchange);
                    break;
                case "/data/new":
                    addNote(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed: " + exchange.getRequestMethod());
            }
        }

        private void registerUser(HttpExchange exchange) throws IOException, SQLException {
            Map<String, Object> userData = parseBody(exchange);

            if (userData == null) {
                sendResponse(exchange, 400, "Bad Request: Missing required fields");
                return;
            }

            String name = (String) userData.get("name");
            String surname = (String) userData.get("surname");
            String username = (String) userData.get("username");
            String password = (String) userData.get("password");

            if (name == null || surname == null || username == null || password == null) {
                sendResponse(exchange, 400, "Bad Request: Missing required fields");
                return;
            }

            Integer userCreated = database.createUser(name, surname, username, password);

            if (userCreated != null) {
                sendResponse(exchange, 201, "User created successfully");
            } else {
                sendResponse(exchange, 409, "Conflict: Username already exists");
            }
        }

        private void addNote(HttpExchange exchange) throws SQLException, IOException {
            Map<String, Object> noteBody = parseBody(exchange);

            if (noteBody == null) {
                sendResponse(exchange, 400, "Bad Request: Missing required fields");
                return;
            }

            Note note = formatNote(noteBody);

            Integer newNoteId = database.addNote(note);

            if (newNoteId == null) {
                sendResponse(exchange, 500, "Internal Server Error");
            } else {
                note.setId(newNoteId);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, note.toJSON());
            }
        }


        // DELETE-запросы
        private void handleDeleteRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            if (!requestPath.equals("/data/delete")) {
                sendResponse(exchange, 405, "Method Not Allowed: " + exchange.getRequestMethod());
                return;
            }

            Map<String, List<String>> queryParams = parseQuery(exchange);

            if (!queryParams.containsKey("id")) {
                sendResponse(exchange, 400, "Bad Request: Missing note ID");
                return;
            }

            if (!queryParams.containsKey("uid")) {
                sendResponse(exchange, 400, "Bad Request: Missing user ID");
                return;
            }

            int noteId;
            try {
                noteId = Integer.parseInt(queryParams.get("id").get(0));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid note ID format");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(queryParams.get("uid").get(0));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid user ID format");
                return;
            }

            boolean isDeleted = database.deleteNoteById(noteId, userId);

            if (isDeleted) {
                sendResponse(exchange, 200, "Note deleted successfully");
            } else {
                sendResponse(exchange, 404, "Note not found: " + noteId);
            }
        }

        private void handlePatchRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            if (!requestPath.equals("/data/patch")) {
                sendResponse(exchange, 405, "Method Not Allowed: " + exchange.getRequestMethod());
                return;
            }

            Map<String, Object> noteBody = parseBody(exchange);

            if (noteBody == null) {
                sendResponse(exchange, 400, "Bad Request: Missing required fields");
                return;
            }

            Note note = formatNote(noteBody);

            Integer noteId = database.updateNote(note);

            if (noteId == null) {
                sendResponse(exchange, 404, "Note not found: " + note.getId());
            } else {
                sendResponse(exchange, 200, "Note updated successfully: " + noteId);
            }


        }
    }


    // Отправка ответа
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

        System.out.println(String.format("[Res][%s]", response));
    }
}