package top.wunanc.giftcode.database;

import top.wunanc.giftcode.model.CodeData;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
            pstmt.setString(1, data.uuid());
            pstmt.setString(2, data.type());
            pstmt.setString(3, data.content());
            pstmt.setInt(4, data.remaining());
            pstmt.setLong(5, data.expireTime());
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

    // 获取数据库中所有的兑换码总数
    public synchronized int getTotalCodesCount() throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "SELECT COUNT(*) FROM gift_codes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // 分页获取兑换码列表
    public synchronized List<CodeData> getCodes(int limit, int offset) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "SELECT * FROM gift_codes LIMIT ? OFFSET ?";
        List<CodeData> list = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new CodeData(
                            rs.getString("uuid"),
                            rs.getString("type"),
                            rs.getString("content"),
                            rs.getInt("remaining"),
                            rs.getLong("expire_time")
                    ));
                }
            }
        }
        return list;
    }

    // 清理已过期或剩余次数为0的兑换码
    public synchronized int clearInvalidCodes(long currentTime) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "DELETE FROM gift_codes WHERE remaining <= 0 OR (expire_time != -1 AND expire_time < ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, currentTime);
            return pstmt.executeUpdate(); // 返回被删除的行数
        }
    }

    public synchronized boolean deleteCode(String uuid) throws SQLException {
        //noinspection SqlResolve,SqlNoDataSourceInspection
        String sql = "DELETE FROM gift_codes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            // 如果受影响的行数大于 0，说明删除成功；等于 0 说明找不到这个码
            return pstmt.executeUpdate() > 0;
        }
    }
}