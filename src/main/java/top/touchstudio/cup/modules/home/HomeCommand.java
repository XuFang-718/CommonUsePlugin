package top.touchstudio.cup.modules.home;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import top.touchstudio.cup.utils.TeleportManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class HomeCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "Home";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int maxHome = Integer.parseInt(ModuleConfig.modulesSection.get("home.MaxHome").toString());
        Player player = (Player) commandSender;
        YamlConfiguration config = PlayerConfig.playerConfig;

        // 确保玩家数据存在
        if (!config.contains(player.getName())) {
            PlayerConfig.createPlayerData(player);
        }

        ConfigurationSection playerSection = config.getConfigurationSection(player.getName());
        if (playerSection == null) {
            // 如果仍然为空，手动创建
            config.createSection(player.getName());
            playerSection = config.getConfigurationSection(player.getName());
            playerSection.createSection("homes");
            playerSection.set("money", 50);
            try {
                config.save(PlayerConfig.playerConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
        if (homeSection == null) {
            // 创建 homes 节点
            homeSection = playerSection.createSection("homes");
            try {
                config.save(PlayerConfig.playerConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // /home list - 列出所有家
        if (strings.length == 1 && strings[0].equalsIgnoreCase("list")) {
            if (homeSection.getKeys(false).isEmpty()) {
                ChatUtil.pluginSay(player, PREFIX, "未找到任何Home");
                return true;
            }
            ChatUtil.pluginSay(player, PREFIX, "您的家列表:");
            homeSection.getKeys(false).forEach(key -> {
                ChatUtil.pluginSay(player, PREFIX, "- &6" + key);
            });
            return true;
        }

        // /home - 显示帮助
        if (strings.length < 1) {
            showHelp(player);
            return true;
        }

        final ConfigurationSection finalHomeSection = homeSection;

        // /home <名称> - 传送到家
        if (strings.length == 1) {
            if (!finalHomeSection.contains(strings[0])) {
                ChatUtil.pluginSay(player, PREFIX, "家 &6" + strings[0] + " &r不存在");
                return true;
            }

            Location homeLoc = (Location) finalHomeSection.get(strings[0]);
            String homeName = strings[0];
            TeleportManager.teleportWithDelay(player, homeLoc, 3,
                    p -> ChatUtil.pluginSay(p, PREFIX, "已传送到 " + homeName),
                    null);
            return true;
        }

        showHelp(player);
        return false;
    }

    private void showHelp(Player player) {
        ChatUtil.pluginSay(player, PREFIX, "&e===== Home 帮助 =====");
        ChatUtil.pluginSay(player, PREFIX, "&6/home <名称> &7- 传送到家");
        ChatUtil.pluginSay(player, PREFIX, "&6/home list &7- 列出所有家");
        ChatUtil.pluginSay(player, PREFIX, "&6/sethome <名称> &7- 设置家");
        ChatUtil.pluginSay(player, PREFIX, "&6/delhome <名称> &7- 删除家");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("list");
            // 添加玩家的家名称
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                YamlConfiguration config = PlayerConfig.playerConfig;
                ConfigurationSection playerSection = config.getConfigurationSection(player.getName());
                if (playerSection != null) {
                    ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
                    if (homeSection != null) {
                        list.addAll(homeSection.getKeys(false));
                    }
                }
            }
            // 过滤
            list.removeIf(option -> !option.toLowerCase().startsWith(strings[0].toLowerCase()));
        }
        return list;
    }
}
