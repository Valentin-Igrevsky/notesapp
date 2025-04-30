package org.server.http.processor;

import com.sun.net.httpserver.HttpExchange;
import org.server.models.ExchangeResponse;

import java.io.IOException;
import java.sql.SQLException;

public interface RequestProcessor {
    ExchangeResponse registerUser(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse authenticateUser(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse getNoteByID(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse getUserNotes(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse addNote(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse deleteNoteById(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse patchNote(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse deleteUser(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse patchUser(HttpExchange exchange) throws IOException, SQLException;
    ExchangeResponse getUserHistory(HttpExchange exchange) throws IOException, SQLException;
}
