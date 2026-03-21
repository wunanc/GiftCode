package top.wunanc.giftcode.database;

import top.wunanc.giftcode.model.CodeData;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String url;
    private Connection connection;

    public DatabaseManager(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "data.db");
        this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    public void init() throws SQLException {
        connection = DriverManager.getConnection(url);
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS gift_codes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid VARCHAR(36) UNIQUE NOT NULL," +
                    "type VARCHAR(16) NOT NULL," +
                    "content TEXT NOT NULL," +
                    "remaining INTEGER NOT NULL," +
                    "expire_time BIGINT NOT NULL" +
                    ");";
            stmt.execute(sql);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 插入新的兑换码
    public synchronized void insertCode(CodeData data) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "INSERT INTO gift_codes (uuid, type, content, remaining, expire_time) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getUuid());
            pstmt.setString(2, data.getType());
            pstmt.setString(3, data.getContent());
            pstmt.setInt(4, data.getRemaining());
            pstmt.setLong(5, data.getExpireTime());
            pstmt.executeUpdate();
        }
    }

    // 获取兑换码信息
    public synchronized CodeData getCode(String uuid) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "SELECT * FROM gift_codes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new CodeData(
                            rs.getString("uuid"),
                            rs.getString("type"),
                            rs.getString("content"),
                            rs.getInt("remaining"),
                            rs.getLong("expire_time")
                    );
                }
            }
        }
        return null;
    }

    // 原子操作：尝试扣除次数（防止并发超发）
    public synchronized boolean claimCode(String uuid, long currentTime) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "UPDATE gift_codes SET remaining = remaining - 1 WHERE uuid = ? AND remaining > 0 AND (expire_time = -1 OR expire_time > ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setLong(2, currentTime);
            // 如果更新行数大于0，说明成功抢到了1次
            return pstmt.executeUpdate() > 0;
        }
    }
}