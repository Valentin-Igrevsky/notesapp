package org.server.http.processor;

import com.sun.net.httpserver.HttpExchange;
import org.server.database.Database;
import org.server.models.ExchangeResponse;
import org.server.models.Note;
import org.server.models.User;
import org.server.utils.RequestParser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DefaultRequestProcessor implements RequestProcessor {
    private final RequestParser parser = new RequestParser();
    public final Database database;

    public DefaultRequestProcessor(Database database) {
        this.database = database;
    }

    @Override
    public ExchangeResponse registerUser(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> userData = parser.parseBody(exchange);

        if (userData == null) {
            return new ExchangeResponse(400, "Bad Request: Missing required fields");
        }

        String name = (String) userData.get("name");
        String surname = (String) userData.get("surname");
        String username = (String) userData.get("username");
        String password = (String) userData.get("password");

        if (name == null || surname == null || username == null || password == null) {
            return new ExchangeResponse(400, "Bad Request: Missing required fields");
        }

        Integer userCreated = database.createUser(name, surname, username, password);

        if (userCreated != null) {
            return new ExchangeResponse(201, "User registered successfully");
        } else {
            return new ExchangeResponse(409, "Conflict: Username already exists");
        }
    }

    @Override
    public ExchangeResponse authenticateUser(HttpExchange exchange) throws IOException, SQLException {
        Map<String, List<String>> queryParams = parser.parseQuery(exchange);
        Map<Boolean, List<String>> result = parser.validateAndExtractQueryParams(queryParams, List.of("username", "password"));

        if (!result.containsKey(true)) {
            return new ExchangeResponse(400, String.join("; ", result.get(false)));
        }

        String username = result.get(true).get(0);
        String password = result.get(true).get(1);

        User user = database.authenticateUser(username, password);
        if (user == null) {
            return new ExchangeResponse(401, "Unauthorized: Invalid username or password");
        }

        return new ExchangeResponse(200, user.toString(), "application/json");
    }

    @Override
    public ExchangeResponse getNoteByID(HttpExchange exchange) throws IOException, SQLException {
        Map<String, List<String>> queryParams = parser.parseQuery(exchange);
        Map<Boolean, List<String>> result = parser.validateAndExtractQueryParams(queryParams, List.of("id", "uid"));

        if (!result.containsKey(true)) {
            return new ExchangeResponse(400, String.join("; ", result.get(false)));
        }

        int noteId = Integer.parseInt(result.get(true).get(0));
        int userId = Integer.parseInt(result.get(true).get(1));

        List<Note> note = database.getNoteById(userId, noteId);

        if (note != null) {
            Map<String, List<Note>> responseMap = Map.of("notes", note);
            String jsonString = parser.toString(responseMap);

            return new ExchangeResponse(200, jsonString, "application/json");
        } else {
            return new ExchangeResponse(404, "Note not found: " + noteId);
        }
    }

    @Override
    public ExchangeResponse getUserNotes(HttpExchange exchange) throws IOException, SQLException {
        Map<String, List<String>> queryParams = parser.parseQuery(exchange);
        Map<Boolean, List<String>> result = parser.validateAndExtractQueryParams(queryParams, List.of("uid"));

        if (!result.containsKey(true)) {
            return new ExchangeResponse(400, String.join("; ", result.get(false)));
        }

        int userId = Integer.parseInt(result.get(true).get(0));

        List<Note> notes = database.getUserNotes(userId);

        Map<String, List<Note>> responseMap = Map.of("notes", notes);
        String jsonString = parser.toString(responseMap);

        return new ExchangeResponse(200, jsonString, "application/json");
    }

    @Override
    public ExchangeResponse addNote(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> noteBody = parser.parseBody(exchange);

        if (noteBody == null) {
            return new ExchangeResponse(400, "Bad Request: Missing required fields");
        }

        Note note = parser.formatNote(noteBody);

        Integer newNoteId = database.addNote(note);

        if (newNoteId == null) {
            return new ExchangeResponse(500, "Internal Server Error");
        } else {
            return new ExchangeResponse(200, note.toString(), "application/json");
        }
    }

    @Override
    public ExchangeResponse deleteNoteById(HttpExchange exchange) throws IOException, SQLException {
        Map<String, List<String>> queryParams = parser.parseQuery(exchange);
        Map<Boolean, List<String>> result = parser.validateAndExtractQueryParams(queryParams, List.of("id", "uid"));

        if (!result.containsKey(true)) {
            return new ExchangeResponse(400, String.join("; ", result.get(false)));
        }

        int noteId = Integer.parseInt(result.get(true).get(0));
        int userId = Integer.parseInt(result.get(true).get(1));

        boolean isDeleted = database.deleteNoteById(noteId, userId);

        if (isDeleted) {
            return new ExchangeResponse(200, "Note deleted successfully");
        } else {
            return new ExchangeResponse(500, "Internal Server Error");
        }
    }

    @Override
    public ExchangeResponse patchNote(HttpExchange exchange) throws IOException, SQLException {
        Map<String, Object> noteBody = parser.parseBody(exchange);

        if (noteBody == null) {
            return new ExchangeResponse(400, "Bad Request: Missing required fields");
        }

        Note note = parser.formatNote(noteBody);
        note.setLastUpdateDate();

        Integer noteId = database.updateNote(note);

        if (noteId == null) {
            return new ExchangeResponse(404, "Internal Server Error");
        } else {
            return new ExchangeResponse(200, "Note updated successfully: " + noteId);
        }
    }

    @Override
    public ExchangeResponse deleteUser(HttpExchange exchange) throws IOException, SQLException {
        return new ExchangeResponse(200, "In development");
    }

    @Override
    public ExchangeResponse patchUser(HttpExchange exchange) throws IOException, SQLException {
        return new ExchangeResponse(200, "In development");
    }

    @Override
    public ExchangeResponse getUserHistory(HttpExchange exchange) throws IOException, SQLException {
        return new ExchangeResponse(200, "In development");
    }
}
