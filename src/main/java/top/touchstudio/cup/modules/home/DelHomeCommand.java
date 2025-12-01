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
import top.touchstudio.cup.configs.PlayerConfig;
import top.touchstudio.cup.utils.ChatUtil;

import java.io.IOException;
import java.util.*;

/**
 * 删除家命令 - /delhome <名称> [confirm]
 */
public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "Home";
    // 存储待确认的删除请求 <玩家UUID, <家名称, 过期时间>>
    private static final Map<UUID, Map.Entry<String, Long>> pendingDeletes = new HashMap<>();
    private static final long CONFIRM_TIMEOUT = 30000; // 30秒确认超时

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            ChatUtil.pluginSay(commandSender, PREFIX, "&4此命令只能由玩家执行!");
            return false;
        }

        if (strings.length == 0) {
            ChatUtil.pluginSay(player, PREFIX, "用法: /delhome <名称>");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        YamlConfiguration config = PlayerConfig.playerConfig;
        ConfigurationSection playerSection = config.getConfigurationSection(player.getName());

        if (playerSection == null) {
            ChatUtil.pluginSay(player, PREFIX, "您还没有设置任何家");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
        if (homeSection == null || homeSection.getKeys(false).isEmpty()) {
            ChatUtil.pluginSay(player, PREFIX, "您还没有设置任何家");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        String homeName = strings[0];
        if (!homeSection.contains(homeName)) {
            ChatUtil.pluginSay(player, PREFIX, "家 &6" + homeName + " &r不存在");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        UUID playerId = player.getUniqueId();

        // 检查是否带有 confirm 参数
        if (strings.length >= 2 && strings[1].equalsIgnoreCase("confirm")) {
            Map.Entry<String, Long> pending = pendingDeletes.get(playerId);
            if (pending != null && pending.getKey().equals(homeName) && System.currentTimeMillis() < pending.getValue()) {
                // 确认删除
                homeSection.set(homeName, null);
                try {
                    config.save(PlayerConfig.playerConfigFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                pendingDeletes.remove(playerId);
                ChatUtil.pluginSay(player, PREFIX, "已删除家 &6" + homeName);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
                return true;
            } else {
                ChatUtil.pluginSay(player, PREFIX, "&4确认已过期，请重新执行删除命令");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                pendingDeletes.remove(playerId);
                return false;
            }
        }

        // 请求确认
        pendingDeletes.put(playerId, new AbstractMap.SimpleEntry<>(homeName, System.currentTimeMillis() + CONFIRM_TIMEOUT));
        ChatUtil.pluginSay(player, PREFIX, "&e确定要删除家 &6" + homeName + " &e吗?");
        ChatUtil.pluginSay(player, PREFIX, "&7输入 &6/delhome " + homeName + " confirm &7确认删除 (30秒内有效)");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> list = new ArrayList<>();

        if (!(commandSender instanceof Player player)) {
            return list;
        }

        if (strings.length == 1) {
            // 补全家名称
            YamlConfiguration config = PlayerConfig.playerConfig;
            ConfigurationSection playerSection = config.getConfigurationSection(player.getName());
            if (playerSection != null) {
                ConfigurationSection homeSection = playerSection.getConfigurationSection("homes");
                if (homeSection != null) {
                    list.addAll(homeSection.getKeys(false));
                }
            }
            list.removeIf(option -> !option.toLowerCase().startsWith(strings[0].toLowerCase()));
        } else if (strings.length == 2) {
            // 补全 confirm
            UUID playerId = player.getUniqueId();
            Map.Entry<String, Long> pending = pendingDeletes.get(playerId);
            if (pending != null && pending.getKey().equals(strings[0]) && System.currentTimeMillis() < pending.getValue()) {
                list.add("confirm");
            }
            list.removeIf(option -> !option.toLowerCase().startsWith(strings[1].toLowerCase()));
        }

        return list;
    }
}
