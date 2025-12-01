package top.touchstudio.cup.modules.home;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.touchstudio.cup.configs.ModuleConfig;
import top.touchstudio.cup.configs.PlayerConfig;
import top.touchstudio.cup.utils.ChatUtil;

import java.io.IOException;

/**
 * 设置家命令 - /sethome <名称>
 */
public class SetHomeCommand implements CommandExecutor {

    private static final String PREFIX = "Home";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            ChatUtil.pluginSay(commandSender, PREFIX, "&4此命令只能由玩家执行!");
            return false;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            ChatUtil.pluginSay(player, PREFIX, "用法: /sethome <名称>");
            return false;
        }

        int maxHome = Integer.parseInt(ModuleConfig.modulesSection.get("home.MaxHome").toString());
        YamlConfiguration config = PlayerConfig.playerConfig;

        // 确保玩家数据存在
        if (!config.contains(player.getName())) {
            PlayerConfig.createPlayerData(player);
        }

        ConfigurationSection playerSection = config.getConfigurationSection(player.getName());
        if (playerSection == null) {
            config.createSection(player.getName());
            playerSection = config.getConfigurationSection(player.getName());
            playerSection.createSection("homes");
            playerSection.set("money", 50);
        }

        ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
        if (homeSection == null) {
            homeSection = playerSection.createSection("homes");
        }

        if (homeSection.getKeys(false).size() >= maxHome) {
            ChatUtil.pluginSay(player, PREFIX, "已达到最大Home数 (" + maxHome + ")");
            return false;
        }

        String homeName = strings[0];
        if (homeSection.contains(homeName)) {
            ChatUtil.pluginSay(player, PREFIX, "已存在同名的Home，将覆盖");
        }

        homeSection.set(homeName, player.getLocation());
        try {
            config.save(PlayerConfig.playerConfigFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ChatUtil.pluginSay(player, PREFIX, "已设置家 &6" + homeName);
        return true;
    }
}
