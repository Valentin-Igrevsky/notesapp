package org.server;

import org.server.database.Database;
import org.server.http.httpServer;

public class Main {
    public static void main(String[] args) {
        // Путь к базе данных (создастся автоматически, если не существует)
        String dbPath = "notes.db";

        // Создание подключения к БД
        Database database = new Database();
        database.connect(dbPath);
        database.createTables(); // <-- Добавь этот метод, чтобы таблицы создавались

        // Запуск HTTP-сервера
        httpServer server = new httpServer(database);
        try {
            server.startHttpServer(args); // Можно указать порт через args
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
