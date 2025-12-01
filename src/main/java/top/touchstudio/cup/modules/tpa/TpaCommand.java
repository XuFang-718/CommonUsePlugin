package top.touchstudio.cup.modules.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.touchstudio.cup.CommonUsePlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * TPA 命令 - /tpa <玩家名>
 */
public class TpaCommand implements CommandExecutor, TabExecutor {

    public static HashMap<UUID, HashMap<String, UUID>> TpaRequests = new HashMap<>();
    public static HashMap<UUID, HashMap<UUID, BukkitTask>> TpaTasks = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(buildPrefix().append(Component.text("此命令只能由玩家执行!", NamedTextColor.RED)));
            return false;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            player.sendMessage(buildPrefix().append(Component.text("用法: ", NamedTextColor.GRAY))
                    .append(Component.text("/tpa <玩家名>", NamedTextColor.YELLOW)));
            return false;
        }

        Player target = Bukkit.getPlayer(strings[0]);
        if (target == null) {
            player.sendMessage(buildPrefix().append(Component.text("玩家 ", NamedTextColor.RED))
                    .append(Component.text(strings[0], NamedTextColor.GOLD))
                    .append(Component.text(" 不存在或不在线", NamedTextColor.RED)));
            return false;
        }

        if (target.equals(player)) {
            player.sendMessage(buildPrefix().append(Component.text("不能传送到自己!", NamedTextColor.RED)));
            return false;
        }

        UUID targetUUID = target.getUniqueId();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();

        TpaRequests.computeIfAbsent(targetUUID, k -> new HashMap<>());
        TpaTasks.computeIfAbsent(targetUUID, k -> new HashMap<>());

        if (TpaRequests.get(targetUUID).containsValue(playerUUID)) {
            player.sendMessage(buildPrefix().append(Component.text("您已经向 ", NamedTextColor.RED))
                    .append(Component.text(target.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" 发送过请求了", NamedTextColor.RED)));
            return false;
        }

        TpaRequests.get(targetUUID).put(playerName, playerUUID);

        HashMap<UUID, BukkitTask> targetTasks = TpaTasks.get(targetUUID);
        if (targetTasks.containsKey(playerUUID)) {
            targetTasks.get(playerUUID).cancel();
            targetTasks.remove(playerUUID);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(CommonUsePlugin.instance, () -> {
            HashMap<String, UUID> requests = TpaRequests.get(targetUUID);
            if (requests != null && requests.containsValue(playerUUID)) {
                requests.entrySet().removeIf(e -> e.getValue().equals(playerUUID));
                if (requests.isEmpty()) {
                    TpaRequests.remove(targetUUID);
                }
            }
            HashMap<UUID, BukkitTask> tasks = TpaTasks.get(targetUUID);
            if (tasks != null) {
                tasks.remove(playerUUID);
                if (tasks.isEmpty()) {
                    TpaTasks.remove(targetUUID);
                }
            }

            Player p = Bukkit.getPlayer(playerUUID);
            Player t = Bukkit.getPlayer(targetUUID);
            if (p != null && p.isOnline()) {
                p.sendMessage(buildPrefix().append(Component.text("发送给 ", NamedTextColor.GRAY))
                        .append(Component.text(target.getName(), NamedTextColor.GOLD))
                        .append(Component.text(" 的传送请求已过期", NamedTextColor.GRAY)));
            }
            if (t != null && t.isOnline()) {
                t.sendMessage(buildPrefix().append(Component.text(playerName, NamedTextColor.GOLD))
                        .append(Component.text(" 的传送请求已过期", NamedTextColor.GRAY)));
            }
        }, 3 * 60 * 20L);

        targetTasks.put(playerUUID, task);

        // 发送给请求者
        player.sendMessage(buildPrefix().append(Component.text("已向 ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.GOLD))
                .append(Component.text(" 发送传送请求", NamedTextColor.GREEN)));

        // 发送给目标玩家
        sendTpaRequest(target, playerName);

        return true;
    }

    /**
     * 发送美化的 TPA 请求消息
     */
    private void sendTpaRequest(Player target, String requesterName) {
        // 分隔线
        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.AQUA));

        // 请求信息
        target.sendMessage(Component.text("  ✉ ", NamedTextColor.YELLOW)
                .append(Component.text("传送请求", NamedTextColor.WHITE, TextDecoration.BOLD)));

        target.sendMessage(Component.text("  玩家 ", NamedTextColor.GRAY)
                .append(Component.text(requesterName, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" 想要传送到您身边", NamedTextColor.GRAY)));

        target.sendMessage(Component.empty());

        // 可点击按钮
        Component acceptButton = Component.text("  [✔ 同意]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpaccept " + requesterName))
                .hoverEvent(HoverEvent.showText(Component.text("点击同意传送请求", NamedTextColor.GREEN)));

        Component denyButton = Component.text("  [✘ 拒绝]", NamedTextColor.RED, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/tpadeny " + requesterName))
                .hoverEvent(HoverEvent.showText(Component.text("点击拒绝传送请求", NamedTextColor.RED)));

        target.sendMessage(acceptButton.append(denyButton));

        target.sendMessage(Component.text("  ⏱ ", NamedTextColor.YELLOW)
                .append(Component.text("请求将在 ", NamedTextColor.GRAY))
                .append(Component.text("3分钟", NamedTextColor.YELLOW))
                .append(Component.text(" 后过期", NamedTextColor.GRAY)));

        // 分隔线
        target.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.AQUA));
    }

    /**
     * 构建前缀
     */
    public static Component buildPrefix() {
        return Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("TPA", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text("] ", NamedTextColor.GRAY));
    }

    public static Player getRequester(UUID targetUUID, String requesterName) {
        HashMap<String, UUID> requests = TpaRequests.get(targetUUID);
        if (requests == null || !requests.containsKey(requesterName)) {
            return null;
        }
        return Bukkit.getPlayer(requests.get(requesterName));
    }

    public static void removeRequest(UUID targetUUID, String requesterName) {
        HashMap<String, UUID> requests = TpaRequests.get(targetUUID);
        if (requests != null) {
            UUID requesterUUID = requests.remove(requesterName);
            if (requests.isEmpty()) {
                TpaRequests.remove(targetUUID);
            }
            if (requesterUUID != null) {
                HashMap<UUID, BukkitTask> tasks = TpaTasks.get(targetUUID);
                if (tasks != null) {
                    BukkitTask task = tasks.remove(requesterUUID);
                    if (task != null) {
                        task.cancel();
                    }
                    if (tasks.isEmpty()) {
                        TpaTasks.remove(targetUUID);
                    }
                }
            }
        }
    }

    public static boolean hasRequest(UUID targetUUID, String requesterName) {
        HashMap<String, UUID> requests = TpaRequests.get(targetUUID);
        return requests != null && requests.containsKey(requesterName);
    }

    public static List<String> getRequesters(UUID targetUUID) {
        HashMap<String, UUID> requests = TpaRequests.get(targetUUID);
        if (requests == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(requests.keySet());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> tab = new ArrayList<>();
        if (strings.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(strings[0].toLowerCase())) {
                    tab.add(player.getName());
                }
            }
        }
        return tab;
    }
}
