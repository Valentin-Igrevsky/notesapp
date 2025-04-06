package org.server.database;

import org.server.models.Note;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    public boolean createNote(Note note) throws SQLException {
        String sql = "INSERT INTO notes (user_id, title, content) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, note.getUserId());
            stmt.setString(2, note.getTitle());
            stmt.setString(3, note.getContent());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Note> getUserNotes(int userId) throws SQLException {
        String sql = "SELECT * FROM notes WHERE user_id = ?";
        List<Note> notes = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Note note = new Note();
                note.setId(rs.getInt("id"));
                note.setUserId(rs.getInt("user_id"));
                note.setTitle(rs.getString("title"));
                note.setContent(rs.getString("content"));
                notes.add(note);
            }
        }
        return notes;
    }
}