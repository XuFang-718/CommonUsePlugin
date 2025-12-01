package top.touchstudio.cup.modules.login;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.touchstudio.cup.CommonUsePlugin;
import top.touchstudio.cup.utils.CU;
import top.touchstudio.cup.utils.ChatUtil;

public class LoginCommand implements CommandExecutor {

    private final CommonUsePlugin plugin;
    private static final String PREFIX = "Login";

    public LoginCommand(CommonUsePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            ChatUtil.pluginSay(sender, PREFIX, "&4只有玩家可以使用此命令!");
            return true;
        }

        Player player = (Player) sender;

        if (!PlayerDataManager.isPlayerRegistering(player)) {
            ChatUtil.pluginSay(player, PREFIX, "&4您不能在此时使用该命令!");
            return true;
        }

        String commandName = command.getName().toLowerCase();

        if (commandName.equals("reg") || commandName.equals("register")) {
            if (PlayerDataManager.loadPlayerData(player) != null) {
                ChatUtil.pluginSay(player, PREFIX, "&4您已经注册!");
                return true;
            }

            if (args.length != 2) {
                ChatUtil.pluginSay(player, PREFIX, "用法: &6/reg &b<密码> &b<重复密码>");
                return true;
            }

            String password = args[0];
            String confirmPassword = args[1];

            if (!password.equals(confirmPassword)) {
                ChatUtil.pluginSay(player, PREFIX, "&4两次输入的密码不一致!");
                return true;
            }

            PlayerDataManager.savePlayerData(player, password);
            PlayerDataManager.setPlayerLocked(player, false);
            PlayerDataManager.setPlayerLoginStatus(player, true);
            PlayerDataManager.restorePlayerInventory(player);
            PlayerDataManager.clearPlayerRegistering(player);
            ChatUtil.pluginSay(player, PREFIX, "&l&6注册成功!");
            return true;
        }

        if (commandName.equals("l") || commandName.equals("login")) {
            PlayerData data = PlayerDataManager.loadPlayerData(player);

            if (data == null) {
                ChatUtil.pluginSay(player, PREFIX, "&4您还没有注册&r, 请使用 &6/reg &r或 &6/register &r注册");
                return true;
            }

            if (args.length != 1) {
                ChatUtil.pluginSay(player, PREFIX, "用法: &6/l &b<密码>");
                return true;
            }

            String password = args[0];

            if (!data.getPassword().equals(password)) {
                player.kickPlayer(ChatColor.RED + "密码错误!");
                return true;
            }

            PlayerDataManager.setPlayerLocked(player, false);
            PlayerDataManager.setPlayerLoginStatus(player, true);
            PlayerDataManager.restorePlayerInventory(player);
            PlayerDataManager.clearPlayerRegistering(player);
            ChatUtil.pluginSay(player, PREFIX, "&l登录成功!");
            return true;
        }

        return false;
    }
}
