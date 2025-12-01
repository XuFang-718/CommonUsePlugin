package top.touchstudio.cup.modules.home;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.touchstudio.cup.configs.PlayerConfig;
import top.touchstudio.cup.utils.ChatUtil;

import java.io.IOException;

/**
 * 删除家命令 - /delhome <名称>
 */
public class DelHomeCommand implements CommandExecutor {

    private static final String PREFIX = "Home";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            ChatUtil.pluginSay(commandSender, PREFIX, "&4此命令只能由玩家执行!");
            return false;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            ChatUtil.pluginSay(player, PREFIX, "用法: /delhome <名称>");
            return false;
        }

        YamlConfiguration config = PlayerConfig.playerConfig;
        ConfigurationSection playerSection = config.getConfigurationSection(player.getName());

        if (playerSection == null) {
            ChatUtil.pluginSay(player, PREFIX, "您还没有设置任何家");
            return false;
        }

        ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
        if (homeSection == null || homeSection.getKeys(false).isEmpty()) {
            ChatUtil.pluginSay(player, PREFIX, "您还没有设置任何家");
            return false;
        }

        String homeName = strings[0];
        if (!homeSection.contains(homeName)) {
            ChatUtil.pluginSay(player, PREFIX, "家 &6" + homeName + " &r不存在");
            return false;
        }

        homeSection.set(homeName, null);
        try {
            config.save(PlayerConfig.playerConfigFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ChatUtil.pluginSay(player, PREFIX, "已删除家 &6" + homeName);
        return true;
    }
}
