package top.touchstudio.cup.modules.home;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.touchstudio.cup.configs.ModuleConfig;
import top.touchstudio.cup.configs.PlayerConfig;
import top.touchstudio.cup.utils.ChatUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置家命令 - /sethome <名称>
 */
public class SetHomeCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "Home";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            ChatUtil.pluginSay(commandSender, PREFIX, "&4此命令只能由玩家执行!");
            return false;
        }

        if (strings.length == 0) {
            ChatUtil.pluginSay(player, PREFIX, "用法: /sethome <名称>");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
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

        String homeName = strings[0];
        // 实时检查家是否存在（homeSection.contains 会检查当前内存中的配置）
        boolean isOverwrite = homeSection.contains(homeName) && homeSection.get(homeName) != null;

        if (!isOverwrite && homeSection.getKeys(false).size() >= maxHome) {
            ChatUtil.pluginSay(player, PREFIX, "&4已达到最大Home数 (" + maxHome + ")");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        homeSection.set(homeName, player.getLocation());
        try {
            config.save(PlayerConfig.playerConfigFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (isOverwrite) {
            ChatUtil.pluginSay(player, PREFIX, "已更新家 &6" + homeName);
        } else {
            ChatUtil.pluginSay(player, PREFIX, "已设置家 &6" + homeName);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> list = new ArrayList<>();

        if (!(commandSender instanceof Player player)) {
            return list;
        }

        if (strings.length == 1) {
            // 补全已有的家名称（方便覆盖）
            YamlConfiguration config = PlayerConfig.playerConfig;
            ConfigurationSection playerSection = config.getConfigurationSection(player.getName());
            if (playerSection != null) {
                ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
                if (homeSection != null) {
                    list.addAll(homeSection.getKeys(false));
                }
            }
            list.removeIf(option -> !option.toLowerCase().startsWith(strings[0].toLowerCase()));
        }

        return list;
    }
}
