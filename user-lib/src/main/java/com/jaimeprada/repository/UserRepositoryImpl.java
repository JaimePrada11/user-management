package com.jaimeprada.repository;

import com.jaimeprada.config.DatabaseConnection;

import com.jaimeprada.exceptions.DatabaseException;
import com.jaimeprada.mapper.UserMapper;
import com.jaimeprada.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl  implements UserRepository {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public User save(User user) {

        String sql = """
                INSERT INTO users (username, email, password, full_name, active, role, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING *
                """;

        try(Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            preparedData(user, ps);
            ps.setTimestamp(7, Timestamp.valueOf(user.getDateCreated()));
            ps.setTimestamp(8, Timestamp.valueOf(user.getDateUpdated()));

            ResultSet rs = ps.executeQuery();

            return rs.next() ? UserMapper.from(rs).toUser() : null;


        } catch (SQLException e) {
            throw new DatabaseException("Error saving user: " + e.getMessage());
        }
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET username = ?,
                    email = ?,
                    password = ?,
                    full_name = ?,
                    active = ?,
                    role = ?,
                    updated_at = ?
                WHERE id = ?
                RETURNING *
                """;

        try(Connection conn = getConn();
        PreparedStatement ps = conn.prepareStatement(sql)){

            preparedData(user, ps);
            ps.setTimestamp(7, Timestamp.valueOf(user.getDateUpdated()));
            ps.setLong(8, user.getId());

            ResultSet rs = ps.executeQuery();

            return rs.next() ? UserMapper.from(rs).toUser() : null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        return getUser(id, sql);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        return getUser(username, sql);
    }

    @Override
    public Optional<User> findByEmail(String email) {

        String sql = "SELECT * FROM users WHERE email = ?";
        return getUser(email, sql);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        return exists(id, sql);
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        return exists(username, sql);
    }

    @Override
    public boolean existsByEmail(String email) {

        String sql = "SELECT 1 FROM users WHERE email = ?";
        return exists(email, sql);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return getUsers(sql);
    }

    @Override
    public List<User> findAllByActive() {

        String sql = "SELECT * FROM users WHERE active = true";
        return getUsers(sql);
    }

    @Override
    public List<User> findAllDeleted() {
       String sql = "SELECT * FROM users WHERE active = false";
        return getUsers(sql);
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try(Connection conn = getConn();
        PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean softDelete(Long id) {
        String sql = """
                UPDATE users
                SET active = false,
                    updated_at = ?
                WHERE id = ?
                """;

        try(Connection conn = getConn();
        PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Optional<User> getUser(Object param, String sql) {
        try(Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setObject(1, param);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(UserMapper.from(rs).toUser()) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean exists(Object param, String sql) {
        try(Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setObject(1, param);
            ResultSet rs = ps.executeQuery();

            return rs.next() ;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> getUsers(String sql) {
        try(Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            List<User> users = new ArrayList<>();

            while(rs.next()){
                users.add(UserMapper.from(rs).toUser());
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void preparedData(User user, PreparedStatement ps) throws SQLException {
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getFullName());
        ps.setBoolean(5, user.isActive());
        ps.setString(6, user.getRole().name());
    }
}
