package org.server.http.controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.server.models.ExchangeResponse;
import org.server.http.processor.RequestProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.nio.charset.StandardCharsets;

public class Controller {
    private final RequestProcessor processor;

    public Controller(RequestProcessor processor) {
        this.processor = processor;
    }

    public void startHttpServer(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;

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
                ExchangeResponse response;
                switch (requestMethod) {
                    case "GET" -> response = handleGetRequest(exchange, requestPath);
                    case "POST" -> response = handlePostRequest(exchange, requestPath);
                    case "DELETE" -> response = handleDeleteRequest(exchange, requestPath);
                    case "PATCH" -> response = handlePatchRequest(exchange, requestPath);
                    default -> response = new ExchangeResponse(405, "Method Not Allowed: " + requestMethod);
                }
                sendResponse(exchange, response);
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, new ExchangeResponse(500, "Internal Server Error"));
            } catch (IOException e) {
                e.printStackTrace();
                sendResponse(exchange, new ExchangeResponse(500, "Internal Server Error"));
            }

        }

        // GET-запросы
        private ExchangeResponse handleGetRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            return switch (requestPath) {
                case "/api/data/get" -> processor.getNoteByID(exchange);
                case "/api/data/get/all" -> processor.getUserNotes(exchange);
                case "/api/history/" -> processor.getUserHistory(exchange);
                case "/login/authentication" -> processor.authenticateUser(exchange);
                default -> new ExchangeResponse(405, "Method Not Allowed: " + exchange.getRequestMethod());
            };
        }

        // POST-запросы
        private ExchangeResponse handlePostRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            return switch (requestPath) {
                case "/login/registration" -> processor.registerUser(exchange);
                case "/api/data/new" -> processor.addNote(exchange);
                default -> new ExchangeResponse(405, "Method Not Allowed: " + exchange.getRequestMethod());
            };
        }

        // DELETE-запросы
        private ExchangeResponse handleDeleteRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            return switch (requestPath) {
                case "/api/data/delete" -> processor.deleteNoteById(exchange);
                case "/user/delete" -> processor.deleteUser(exchange);
                default -> new ExchangeResponse(405, "Method Not Allowed: " + exchange.getRequestMethod());
            };
        }

        private ExchangeResponse handlePatchRequest(HttpExchange exchange, String requestPath) throws IOException, SQLException {
            return switch (requestPath) {
                case "/api/data/patch" -> processor.patchNote(exchange);
                case "user/patch" -> processor.patchUser(exchange);
                default -> new ExchangeResponse(405, "Method Not Allowed: " + exchange.getRequestMethod());
            };
        }
    }

    // Отправка ответа
    private void sendResponse(HttpExchange exchange, ExchangeResponse response) throws IOException {
        byte[] bytes = response.getResponseMessage().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", response.getContentType() + "; charset=utf-8");
        exchange.sendResponseHeaders(response.getStatusCode(), bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

        System.out.println(String.format("[Res][%s]", response));
    }
}