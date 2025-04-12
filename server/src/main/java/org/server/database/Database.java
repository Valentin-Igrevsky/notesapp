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
            conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.", e);
        }
    }

    public void createTables() {
        try (Statement stmt = conn.createStatement()) {
            String createUsers = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,
                        surname TEXT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    );
                    """;

            String createNotes = """
                    CREATE TABLE IF NOT EXISTS notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        create_date TEXT NOT NULL,
                        last_modified TEXT NOT NULL,
                        text TEXT,
                        title TEXT,
                        owner_id INTEGER,
                        FOREIGN KEY (owner_id) REFERENCES users(id)
                    );
                    """;

            stmt.execute(createUsers);
            stmt.execute(createNotes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Integer createUser(String name, String surname, String username, String password) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, username);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            return null;
        }

        String query = "INSERT INTO users(username, password, name, surname) VALUES(?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, name);
        pstmt.setString(4, surname);
        pstmt.executeUpdate();

        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new SQLException("Creating user failed, no ID obtained.");
        }
    }

    public Integer addNote(@NotNull Note note) throws SQLException {
        if (note.getId() > 0) {
            String checkQuery = "SELECT COUNT(*) FROM notes WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, note.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return updateNote(note);
            }
        }

        String checkUserQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
        PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery);
        checkUserStmt.setInt(1, note.getOwnerID());
        ResultSet rs = checkUserStmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            String query = "INSERT INTO notes(create_date, last_modified, text, title, owner_id) VALUES (?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDate(1, new java.sql.Date(note.getCreateDate()));
            pstmt.setDate(2, new java.sql.Date(note.getLastUpdateDate()));
            pstmt.setString(3, note.getText());
            pstmt.setString(4, note.getTitle());
            pstmt.setInt(5, note.getOwnerID());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Inserting note failed, no ID obtained.");
            }
        } else {
            throw new SQLException("Foreign key constraint failed: User with ID " + note.getOwnerID() + " does not exist.");
        }
    }

    public List<Note> getNoteById(int userId, int noteId) throws SQLException {
        List<Note> res = new Vector<Note>();
        String query = "SELECT n.*, u.* " +
                "FROM notes n " +
                "JOIN users u ON n.owner_id = u.id " +
                "WHERE n.id = ? AND n.owner_id = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, noteId);
        pstmt.setInt(2, userId);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            res.add(new Note(
                            rs.getInt("id"),
                            rs.getLong("create_date"),
                            rs.getLong("last_modified"),
                            new User(
                                    rs.getInt("owner_id"),
                                    rs.getString("name"),
                                    rs.getString("surname"),
                                    rs.getString("password"),
                                    rs.getString("username")
                            ),
                            rs.getString("text"),
                            rs.getString("title")
                    )
            );
            return res;
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
            res.add(
                    new Note(
                            rs.getInt("id"),
                            rs.getLong("create_date"),
                            rs.getLong("last_modified"),
                            new User(
                                    rs.getInt("owner_id"),
                                    rs.getString("name"),
                                    rs.getString("surname"),
                                    rs.getString("username"),
                                    rs.getString("password")
                            ),
                            rs.getString("text"),
                            rs.getString("title")
                    )
            );
        }
        return res;
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


    public Integer updateNote(Note note) throws SQLException {
        String query = "UPDATE notes SET create_date = ?, last_modified = ?, text = ?, title = ? WHERE id = ? AND owner_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setDate(1, new java.sql.Date(note.getCreateDate()));
        pstmt.setDate(2, new java.sql.Date(note.getLastUpdateDate()));
        pstmt.setString(3, note.getText());
        pstmt.setString(4, note.getTitle());
        pstmt.setInt(5, note.getId());
        pstmt.setInt(6, note.getOwnerID());

        return pstmt.executeUpdate() > 0 ? note.getId() : null;
    }


    public boolean deleteNoteById(int noteId, int userId) throws SQLException {
        String query = "DELETE FROM notes WHERE id = ? AND owner_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, noteId);
        pstmt.setInt(2, userId);
        return pstmt.executeUpdate() > 0;
    }

    public void close() throws SQLException {
        conn.close();
    }
}
