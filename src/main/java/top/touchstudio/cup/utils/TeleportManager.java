package top.touchstudio.cup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import top.touchstudio.cup.CommonUsePlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 传送管理器 - 处理传送等待期间的限制
 * 玩家移动位置会取消传送，但可以自由转动视角
 */
public class TeleportManager implements Listener {

    private static final HashMap<UUID, TeleportTask> pendingTeleports = new HashMap<>();

    /**
     * 延迟传送玩家到固定位置
     */
    public static void teleportWithDelay(Player player, Location destination, int delaySeconds,
                                         Consumer<Player> onSuccess, Consumer<Player> onCancel) {
        teleportWithDelay(player, () -> destination, delaySeconds, onSuccess, onCancel);
    }

    /**
     * 延迟传送玩家到动态位置（传送时才获取目标位置）
     */
    public static void teleportWithDelay(Player player, Supplier<Location> destinationSupplier, int delaySeconds,
                                         Consumer<Player> onSuccess, Consumer<Player> onCancel) {
        UUID playerId = player.getUniqueId();

        // 取消之前的传送任务
        cancelTeleport(player, false);

        // 记录初始位置
        Location initialLocation = player.getLocation().clone();

        // 存储所有任务
        List<BukkitTask> allTasks = new ArrayList<>();

        // 先创建 TeleportTask 并存储，确保倒计时检查时能找到
        TeleportTask teleportTask = new TeleportTask(allTasks, initialLocation, onCancel);
        pendingTeleports.put(playerId, teleportTask);

        // 立即显示第一个倒计时
        showCountdown(player, delaySeconds);

        // 后续倒计时显示（从第2秒开始）
        for (int i = delaySeconds - 1; i >= 1; i--) {
            final int seconds = i;
            BukkitTask countdownTask = Bukkit.getScheduler().runTaskLater(CommonUsePlugin.instance, () -> {
                if (pendingTeleports.containsKey(playerId)) {
                    showCountdown(player, seconds);
                }
            }, (delaySeconds - i) * 20L);
            allTasks.add(countdownTask);
        }

        // 传送任务
        BukkitTask finalTask = Bukkit.getScheduler().runTaskLater(CommonUsePlugin.instance, () -> {
            TeleportTask task = pendingTeleports.remove(playerId);
            if (task != null) {
                Location dest = destinationSupplier.get();
                if (dest != null) {
                    player.teleport(dest);
                    showTeleportSuccess(player);
                    if (onSuccess != null) {
                        onSuccess.accept(player);
                    }
                }
            }
        }, delaySeconds * 20L);

        allTasks.add(finalTask);
    }

    /**
     * 显示倒计时
     */
    private static void showCountdown(Player player, int seconds) {
        // 根据剩余时间选择颜色
        NamedTextColor color;
        if (seconds >= 3) {
            color = NamedTextColor.GREEN;
        } else if (seconds == 2) {
            color = NamedTextColor.YELLOW;
        } else {
            color = NamedTextColor.RED;
        }

        // 显示 Title
        Component title = Component.text("传送中", NamedTextColor.AQUA, TextDecoration.BOLD);
        Component subtitle = Component.text(seconds, color, TextDecoration.BOLD)
                .append(Component.text(" 秒", NamedTextColor.GRAY));

        Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO);
        player.showTitle(Title.title(title, subtitle, times));

        // 播放倒计时音效
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, seconds == 1 ? 1.5f : 1.0f);
    }

    /**
     * 显示传送成功
     */
    private static void showTeleportSuccess(Player player) {
        Component title = Component.text("✔", NamedTextColor.GREEN, TextDecoration.BOLD);
        Component subtitle = Component.text("传送完成!", NamedTextColor.GREEN);

        Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(500));
        player.showTitle(Title.title(title, subtitle, times));

        // 播放传送成功音效
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * 显示传送取消
     */
    private static void showTeleportCancelled(Player player) {
        Component title = Component.text("✘", NamedTextColor.RED, TextDecoration.BOLD);
        Component subtitle = Component.text("传送已取消", NamedTextColor.RED);

        Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(500));
        player.showTitle(Title.title(title, subtitle, times));

        // 播放取消音效
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    /**
     * 取消玩家的传送
     */
    public static void cancelTeleport(Player player, boolean notify) {
        UUID playerId = player.getUniqueId();
        TeleportTask teleportTask = pendingTeleports.remove(playerId);
        if (teleportTask != null) {
            // 取消所有任务
            for (BukkitTask task : teleportTask.tasks) {
                task.cancel();
            }
            if (notify) {
                showTeleportCancelled(player);
            }
            if (teleportTask.onCancel != null) {
                teleportTask.onCancel.accept(player);
            }
        }
    }

    /**
     * 取消玩家的传送（默认不通知）
     */
    public static void cancelTeleport(Player player) {
        cancelTeleport(player, false);
    }

    /**
     * 检查玩家是否正在传送中
     */
    public static boolean isTeleporting(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!pendingTeleports.containsKey(playerId)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // 只检查位置变化，忽略视角变化（允许转动视角）
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            // 玩家移动了，取消传送
            cancelTeleport(player, true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时取消传送
        cancelTeleport(event.getPlayer(), false);
    }

    private static class TeleportTask {
        final List<BukkitTask> tasks;
        final Location initialLocation;
        final Consumer<Player> onCancel;

        TeleportTask(List<BukkitTask> tasks, Location initialLocation, Consumer<Player> onCancel) {
            this.tasks = tasks;
            this.initialLocation = initialLocation;
            this.onCancel = onCancel;
        }
    }
}
