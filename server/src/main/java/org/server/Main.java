package org.server;

import org.server.database.Database;
import org.server.http.httpServer;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String dbPath = "notes.db";

        Database database = new Database();
        database.connect(dbPath);
        database.createTables();

        httpServer server = new httpServer(database);
        try {
            server.startHttpServer(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
