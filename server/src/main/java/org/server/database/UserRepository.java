package org.server.database;

import org.server.models.User;
import java.sql.*;

public class UserRepository {
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPassword());
            return stmt.executeUpdate() > 0;
        }
    }

    public User findUserByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setPassword(rs.getString("password"));
                return user;
            }
            return null;
        }
    }
}