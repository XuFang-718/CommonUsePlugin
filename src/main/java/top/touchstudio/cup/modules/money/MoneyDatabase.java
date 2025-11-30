package top.touchstudio.cup.modules.money;

import top.touchstudio.cup.CommonUsePlugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

/**
 * Money 模块的 SQLite 数据库管理类
 */
public class MoneyDatabase {
    private static MoneyDatabase instance;
    private Connection connection;
    private final CommonUsePlugin plugin;
    private static final int DEFAULT_MONEY = 50;

    private MoneyDatabase(CommonUsePlugin plugin) {
        this.plugin = plugin;
    }

    public static MoneyDatabase getInstance() {
        if (instance == null) {
            instance = new MoneyDatabase(CommonUsePlugin.instance);
        }
        return instance;
    }

    /**
     * 初始化数据库连接和表结构
     */
    public void init() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = new File(dataFolder, "money.db").getAbsolutePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            createTable();
            plugin.getLogger().info("Money 数据库初始化成功");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Money 数据库初始化失败", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_money (
                player_name VARCHAR(64) PRIMARY KEY,
                money INTEGER NOT NULL DEFAULT 50,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 获取玩家金钱
     */
    public int getMoney(String playerName) {
        String sql = "SELECT money FROM player_money WHERE player_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("money");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家金钱失败: " + playerName, e);
        }
        return 0;
    }

    /**
     * 设置玩家金钱
     */
    public boolean setMoney(String playerName, int amount) {
        String sql = """
            INSERT INTO player_money (player_name, money, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(player_name) DO UPDATE SET money = ?, updated_at = CURRENT_TIMESTAMP
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, amount);
            pstmt.setInt(3, amount);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "设置玩家金钱失败: " + playerName, e);
            return false;
        }
    }

    /**
     * 增加玩家金钱
     */
    public boolean addMoney(String playerName, int amount) {
        int current = getMoney(playerName);
        return setMoney(playerName, current + amount);
    }

    /**
     * 扣除玩家金钱
     */
    public boolean subtractMoney(String playerName, int amount) {
        int current = getMoney(playerName);
        if (current < amount) {
            return false;
        }
        return setMoney(playerName, current - amount);
    }

    /**
     * 转账
     */
    public boolean transfer(String from, String to, int amount) {
        try {
            connection.setAutoCommit(false);

            int fromMoney = getMoney(from);
            if (fromMoney < amount) {
                connection.rollback();
                return false;
            }

            int toMoney = getMoney(to);
            setMoney(from, fromMoney - amount);
            setMoney(to, toMoney + amount);

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "回滚事务失败", ex);
            }
            plugin.getLogger().log(Level.WARNING, "转账失败", e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "恢复自动提交失败", e);
            }
        }
    }

    /**
     * 检查玩家是否存在，不存在则创建
     */
    public void createPlayerIfNotExists(String playerName) {
        String checkSql = "SELECT 1 FROM player_money WHERE player_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                setMoney(playerName, DEFAULT_MONEY);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "检查玩家数据失败: " + playerName, e);
        }
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Money 数据库连接已关闭");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "关闭数据库连接失败", e);
        }
    }
}
