package org.server.database;

import org.jetbrains.annotations.NotNull;
import org.server.models.Note;
import org.server.models.User;

import java.sql.*;
import java.util.List;
import java.util.Vector;

public class Database {
    private Connection conn = null;

    public void connect(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(path);
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.", e);
        }
    }

    public boolean createUser(String name, String surname, String username, String password) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, username);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            return false;
        }

        String query = "INSERT INTO users(username, password, name, surname) VALUES(?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1,   username);
        pstmt.setString(2, password);
        pstmt.setString(3, name);
        pstmt.setString(4, surname);
        pstmt.executeUpdate();
        return true;
    }

    public void addNote(@NotNull Note note) throws SQLException {
        if (note.getId() > 0) {
            String checkQuery = "SELECT COUNT(*) FROM notes WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, note.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                updateNote(note);
                return;
            }
        }

        String query = "INSERT INTO notes(create_date, last_modified, text, title, owner_id) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setDate(1, new java.sql.Date(note.getCreateDate().getTime()));
        pstmt.setDate(2, new java.sql.Date(note.getLastUpdateDate().getTime()));
        pstmt.setString(3, note.getText());
        pstmt.setString(4, note.getTitle());
        pstmt.setInt(5, note.getOwnerID());
        pstmt.executeUpdate();
    }

    public Note getNoteById(int noteId) throws SQLException {
        String query = "SELECT n.*, u.* " + "FROM notes n " + "JOIN users u ON n.owner_id = u.id " + "WHERE n.id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, noteId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return new Note(rs.getInt("id"), rs.getDate("create_date"), rs.getDate("last_modified"), new User(rs.getInt("owner_id"), rs.getString("name"), rs.getString("surname"), rs.getString("password")), rs.getString("text"), rs.getString("title"));
        }
        return null;
    }

    public List<Note> getUserNotes(int owner_id) throws SQLException {
        List<Note> res = new Vector<Note>();
        String query = "SELECT n.*, u.* " + "FROM notes n " + "JOIN users u ON u.id = n.owner_id " + "WHERE u.id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, owner_id);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            res.add(new Note(rs.getInt("id"), rs.getDate("create_date"), rs.getDate("last_modified"), new User(rs.getInt("owner_id"), rs.getString("name"), rs.getString("surname"), rs.getString("password")), rs.getString("text"), rs.getString("title")));
        }
        return res;
    }

    public User getUserById(int user_id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            new User(rs.getInt("owner_id"), rs.getString("name"), rs.getString("surname"), rs.getString("password"));
        }
        return null;
    }

    public User authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        if (!rs.next()) {
            return null;
        }
        return new User(rs.getInt("id"), rs.getString("name"), rs.getString("surname"), rs.getString("password"), rs.getString("username"));
    }

    public void updateUser(User user) throws SQLException {
        String query = "UPDATE users SET name = ?, surname = ?, username = ?, password = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, user.getName());
        pstmt.setString(2, user.getSurname());
        pstmt.setString(3, user.getUsername());
        pstmt.setString(4, user.getPassword());
        pstmt.setInt(5, user.getId());
        pstmt.executeUpdate();
    }

    public void updateNote(Note note) throws SQLException {
        String query = "UPDATE notes SET create_date = ?, last_modified = ?, text = ?, title = ?, owner_id = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setDate(1, new java.sql.Date(note.getCreateDate().getTime()));
        pstmt.setDate(2, new java.sql.Date(note.getLastUpdateDate().getTime()));
        pstmt.setString(3, note.getText());
        pstmt.setString(4, note.getTitle());
        pstmt.setInt(5, note.getOwnerID());
        pstmt.setInt(6, note.getId());
        pstmt.executeUpdate();
    }

    public boolean deleteNoteById(int noteId) throws SQLException {
        String query = "DELETE FROM notes WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, noteId);
        return pstmt.executeUpdate() > 0;
    }

    public void close() throws SQLException {
        conn.close();
    }
}
