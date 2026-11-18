package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class ActivityLogDao {

    private static final String SELECT_COLUMNS =
            "SELECT log_id, user_id, action_type, description, timestamp FROM activity_logs ";

    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId > 0) {
                pstmt.setInt(1, userId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Audit log write failed: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        String sql = SELECT_COLUMNS + "ORDER BY timestamp DESC, log_id DESC";
        return query(sql, null);
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = SELECT_COLUMNS + "WHERE user_id = ? ORDER BY timestamp DESC, log_id DESC";
        return query(sql, userId);
    }

    public int clearLogs() {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing logs: " + e.getMessage());
            return -1;
        }
    }

    private List<ActivityLog> query(String sql, Integer userId) {
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                pstmt.setInt(1, userId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    logs.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.wasNull() ? null : uid,
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving logs: " + e.getMessage());
        }

        return logs;
    }
}