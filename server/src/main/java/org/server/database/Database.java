package org.server.database;

import org.jetbrains.annotations.NotNull;
import org.server.models.Note;
import org.server.models.User;

import java.sql.SQLException;
import java.util.List;

public interface Database {
    void connect(String path);
    void createTables();
    Integer createUser(String name, String surname, String username, String password) throws SQLException;
    Integer addNote(@NotNull Note note) throws SQLException;
    List<Note> getNoteById(int userId, int noteId) throws SQLException;
    List<Note> getUserNotes(int owner_id) throws SQLException;
    User getUserById(int user_id) throws SQLException;
    User authenticateUser(String username, String password) throws SQLException;
    User updateUser(User user) throws SQLException;
    Integer updateNote(Note note) throws SQLException;
    boolean deleteNoteById(int noteId, int userId) throws SQLException;
    void close() throws SQLException;
}
