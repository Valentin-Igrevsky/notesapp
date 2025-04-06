package org.server;

import com.sun.net.httpserver.HttpServer;
import org.server.http.AuthApi;
import org.server.http.NotesApi;
import org.server.database.Database;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        // Инициализация БД
        Database.init();

        // Создание HTTP-сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Регистрация обработчиков
        server.createContext("/auth", new AuthApi());
        server.createContext("/notes", new NotesApi());

        server.start();
        System.out.println("Server started on port 8080");
    }
}