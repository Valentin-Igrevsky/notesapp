package org.server;

import org.server.database.Database;
import org.server.database.SQLiteDatabase;
import org.server.http.controller.Controller;
import org.server.http.httpServer;
import org.server.http.processor.DefaultRequestProcessor;
import org.server.http.processor.RequestProcessor;

public class Main {
//        public static void main(String[] args) {
//        String dbPath = "notes.db";
//
//        SQLiteDatabase SQLiteDatabase = new SQLiteDatabase();
//        SQLiteDatabase.connect(dbPath);
//        SQLiteDatabase.createTables();
//
//        httpServer server = new httpServer(SQLiteDatabase);
//        try {
//            server.startHttpServer(args);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static void main(String[] args) {
        String dbPath = "notes.db";

        Database database = new SQLiteDatabase();
        database.connect(dbPath);
        database.createTables();

        RequestProcessor processor = new DefaultRequestProcessor(database);
        Controller controller = new Controller(processor);

        try {
            controller.startHttpServer(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
