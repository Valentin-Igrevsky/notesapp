package org.client.http;

import org.client.models.httpPacket;
import org.client.models.Note;
import org.client.models.User;
import org.client.utils.NoteJsonParser;

import java.net.http.HttpResponse;
import java.util.List;

public class httpClient {
    private final AuthClient authClient = new AuthClient();
    private final NotesClient notesClient = new NotesClient();

    public httpPacket register(User user) throws Exception {
        HttpResponse<String> response = authClient.register(user);
        return new httpPacket(response.statusCode() == 201, response.statusCode(), response.body(), null, null, null);
    }

    public httpPacket login(String username, String password) throws Exception {
        HttpResponse<String> response = authClient.login(username, password);

        if (response.statusCode() == 200) {
            User user = NoteJsonParser.fromJson(response.body(), User.class);
            return new httpPacket(true, response.statusCode(), response.body(), user, null, null);
        } else {
            return new httpPacket(false, response.statusCode(), response.body(), null, null, null);
        }
    }

    public httpPacket getNoteById(int userId, int noteId) throws Exception {
        HttpResponse<String> response = notesClient.getNoteById(userId, noteId);

        if (response.statusCode() == 200) {
            List<Note> ServerNotes = NoteJsonParser.fromJsonList(response.body(), Note.class);
            return new httpPacket(true, response.statusCode(), response.body(), null, null, ServerNotes.getFirst());
        } else {
            return new httpPacket(false, response.statusCode(), response.body(), null, null, null);
        }
    }

    public httpPacket getAllNotes(int userId) throws Exception {
        HttpResponse<String> response = notesClient.getAllNotes(userId);

        if (response.statusCode() == 200) {
            List<Note> ServerNotes = NoteJsonParser.fromJsonList(response.body(), Note.class);
            return new httpPacket(true, response.statusCode(), response.body(), null, ServerNotes, null);
        } else {
            return new httpPacket(false, response.statusCode(), response.body(), null, null, null);
        }
    }

    public httpPacket addNote(Note note) throws Exception {
        HttpResponse<String> response = notesClient.addNote(note);

        if (response.statusCode() == 200) {
            Note ServerNote = NoteJsonParser.fromJson(response.body(), Note.class);
            return new httpPacket(true, response.statusCode(), response.body(), null, null, ServerNote);
        } else {
            return new httpPacket(true, response.statusCode(), response.body(), null, null, null);
        }
    }

    public httpPacket deleteNoteById(int userId, int noteId) throws Exception {
        HttpResponse<String> response = notesClient.deleteNoteById(userId, noteId);
        return new httpPacket(response.statusCode() == 200, response.statusCode(), response.body(), null, null, null);
    }

    public httpPacket patchNote(Note note) throws Exception {
        HttpResponse<String> response = notesClient.patchNote(note);
        return new httpPacket(response.statusCode() == 200, response.statusCode(), response.body(), null, null, null);
    }
}
