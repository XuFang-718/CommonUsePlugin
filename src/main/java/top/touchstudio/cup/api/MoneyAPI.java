package top.touchstudio.cup.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.touchstudio.cup.CommonUsePlugin;
import top.touchstudio.cup.modules.ModuleManager;
import top.touchstudio.cup.modules.money.MoneyDatabase;

/**
 * CommonUsePlugin Money 模块的公开 API
 * 供其他插件调用查询和操作玩家金钱
 *
 * @author TouchStudio
 * @version 1.0
 *
 * 使用示例:
 * <pre>
 * // 获取 API 实例
 * MoneyAPI api = MoneyAPI.getInstance();
 *
 * // 检查 Money 模块是否启用
 * if (api.isEnabled()) {
 *     // 查询余额
 *     int balance = api.getBalance("PlayerName");
 *
 *     // 加钱
 *     api.addMoney("PlayerName", 100);
 *
 *     // 扣钱
 *     api.takeMoney("PlayerName", 50);
 * }
 * </pre>
 */
public class MoneyAPI {
    private static MoneyAPI instance;

    private MoneyAPI() {}

    /**
     * 获取 MoneyAPI 实例
     * @return MoneyAPI 单例实例
     */
    public static MoneyAPI getInstance() {
        if (instance == null) {
            instance = new MoneyAPI();
        }
        return instance;
    }

    /**
     * 检查 Money 模块是否启用
     * @return true 如果模块已启用
     */
    public boolean isEnabled() {
        Boolean enabled = ModuleManager.ModuleMap.get("money");
        return enabled != null && enabled;
    }

    /**
     * 获取玩家余额
     * @param playerName 玩家名称
     * @return 玩家余额，如果模块未启用返回 -1
     */
    public int getBalance(String playerName) {
        if (!isEnabled()) return -1;
        return MoneyDatabase.getInstance().getMoney(playerName);
    }

    /**
     * 获取在线玩家余额
     * @param player 玩家对象
     * @return 玩家余额，如果模块未启用返回 -1
     */
    public int getBalance(Player player) {
        return getBalance(player.getName());
    }

    /**
     * 设置玩家余额
     * @param playerName 玩家名称
     * @param amount 金额（必须 >= 0）
     * @return true 如果设置成功
     */
    public boolean setBalance(String playerName, int amount) {
        if (!isEnabled() || amount < 0) return false;
        return MoneyDatabase.getInstance().setMoney(playerName, amount);
    }

    /**
     * 设置在线玩家余额
     * @param player 玩家对象
     * @param amount 金额（必须 >= 0）
     * @return true 如果设置成功
     */
    public boolean setBalance(Player player, int amount) {
        return setBalance(player.getName(), amount);
    }

    /**
     * 给玩家加钱
     * @param playerName 玩家名称
     * @param amount 金额（必须 > 0）
     * @return true 如果操作成功
     */
    public boolean addMoney(String playerName, int amount) {
        if (!isEnabled() || amount <= 0) return false;
        return MoneyDatabase.getInstance().addMoney(playerName, amount);
    }

    /**
     * 给在线玩家加钱
     * @param player 玩家对象
     * @param amount 金额（必须 > 0）
     * @return true 如果操作成功
     */
    public boolean addMoney(Player player, int amount) {
        return addMoney(player.getName(), amount);
    }

    /**
     * 扣除玩家金钱
     * @param playerName 玩家名称
     * @param amount 金额（必须 > 0）
     * @return true 如果扣除成功（余额足够），false 如果余额不足或操作失败
     */
    public boolean takeMoney(String playerName, int amount) {
        if (!isEnabled() || amount <= 0) return false;
        return MoneyDatabase.getInstance().subtractMoney(playerName, amount);
    }

    /**
     * 扣除在线玩家金钱
     * @param player 玩家对象
     * @param amount 金额（必须 > 0）
     * @return true 如果扣除成功（余额足够），false 如果余额不足或操作失败
     */
    public boolean takeMoney(Player player, int amount) {
        return takeMoney(player.getName(), amount);
    }

    /**
     * 检查玩家是否有足够的金钱
     * @param playerName 玩家名称
     * @param amount 需要检查的金额
     * @return true 如果玩家余额 >= amount
     */
    public boolean hasEnough(String playerName, int amount) {
        if (!isEnabled()) return false;
        return getBalance(playerName) >= amount;
    }

    /**
     * 检查在线玩家是否有足够的金钱
     * @param player 玩家对象
     * @param amount 需要检查的金额
     * @return true 如果玩家余额 >= amount
     */
    public boolean hasEnough(Player player, int amount) {
        return hasEnough(player.getName(), amount);
    }

    /**
     * 玩家之间转账
     * @param from 付款玩家名称
     * @param to 收款玩家名称
     * @param amount 转账金额（必须 > 0）
     * @return true 如果转账成功
     */
    public boolean transfer(String from, String to, int amount) {
        if (!isEnabled() || amount <= 0) return false;
        return MoneyDatabase.getInstance().transfer(from, to, amount);
    }

    /**
     * 在线玩家之间转账
     * @param from 付款玩家
     * @param to 收款玩家
     * @param amount 转账金额（必须 > 0）
     * @return true 如果转账成功
     */
    public boolean transfer(Player from, Player to, int amount) {
        return transfer(from.getName(), to.getName(), amount);
    }

    /**
     * 获取货币名称
     * @return 货币名称
     */
    public String getCurrencyName() {
        return "枚硬币";
    }

    /**
     * 获取货币符号
     * @return 货币符号
     */
    public String getCurrencySymbol() {
        return "¥";
    }
}
