package top.touchstudio.cup.modules.chainmining;

import top.touchstudio.cup.CommonUsePlugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class ChainMiningDatabase {
    private static ChainMiningDatabase instance;
    private Connection connection;
    private final CommonUsePlugin plugin;

    private ChainMiningDatabase(CommonUsePlugin plugin) {
        this.plugin = plugin;
    }

    public static ChainMiningDatabase getInstance() {
        if (instance == null) {
            instance = new ChainMiningDatabase(CommonUsePlugin.instance);
        }
        return instance;
    }

    public void init() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            String dbPath = new File(dataFolder, "chainmining.db").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "ChainMining 数据库初始化失败", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_chainmining (
                player_name VARCHAR(64) PRIMARY KEY,
                first_use BOOLEAN NOT NULL DEFAULT 0
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 检查玩家是否已经使用过连锁挖矿
     */
    public boolean hasUsedChainMining(String playerName) {
        String sql = "SELECT first_use FROM player_chainmining WHERE player_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("first_use");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "查询玩家连锁挖矿记录失败: " + playerName, e);
        }
        return false;
    }

    /**
     * 标记玩家已使用过连锁挖矿
     */
    public void markAsUsed(String playerName) {
        String sql = """
            INSERT INTO player_chainmining (player_name, first_use) VALUES (?, 1)
            ON CONFLICT(player_name) DO UPDATE SET first_use = 1
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "标记玩家连锁挖矿记录失败: " + playerName, e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "关闭数据库连接失败", e);
        }
    }
}
