package top.touchstudio.cup.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import top.touchstudio.cup.CommonUsePlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 传送管理器 - 处理传送等待期间的限制
 * 传送时：不能移动位置，但可以转动视角；不能受到伤害
 */
public class TeleportManager implements Listener {

    private static final HashMap<UUID, TeleportTask> pendingTeleports = new HashMap<>();

    /**
     * 延迟传送玩家
     * @param player 要传送的玩家
     * @param destination 目标位置
     * @param delaySeconds 延迟秒数
     * @param onSuccess 传送成功后的回调（可为null）
     * @param onCancel 传送取消后的回调（可为null）
     */
    public static void teleportWithDelay(Player player, Location destination, int delaySeconds,
                                         Consumer<Player> onSuccess, Consumer<Player> onCancel) {
        UUID playerId = player.getUniqueId();

        // 取消之前的传送任务
        cancelTeleport(player);

        // 记录初始位置（只记录坐标，不记录视角）
        Location initialLocation = player.getLocation().clone();

        ChatUtil.pluginSay(player, "将在" + delaySeconds + "秒后传送，请勿移动");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(CommonUsePlugin.instance, () -> {
            pendingTeleports.remove(playerId);
            player.teleport(destination);
            if (onSuccess != null) {
                onSuccess.accept(player);
            }
        }, delaySeconds * 20L);

        pendingTeleports.put(playerId, new TeleportTask(task, initialLocation, onCancel));
    }

    /**
     * 取消玩家的传送
     */
    public static void cancelTeleport(Player player) {
        UUID playerId = player.getUniqueId();
        TeleportTask teleportTask = pendingTeleports.remove(playerId);
        if (teleportTask != null) {
            teleportTask.task.cancel();
            if (teleportTask.onCancel != null) {
                teleportTask.onCancel.accept(player);
            }
        }
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
            // 取消移动，但保留视角变化
            Location newLoc = from.clone();
            newLoc.setYaw(to.getYaw());
            newLoc.setPitch(to.getPitch());
            event.setTo(newLoc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            // 传送期间免疫伤害
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时取消传送
        cancelTeleport(event.getPlayer());
    }

    private static class TeleportTask {
        final BukkitTask task;
        final Location initialLocation;
        final Consumer<Player> onCancel;

        TeleportTask(BukkitTask task, Location initialLocation, Consumer<Player> onCancel) {
            this.task = task;
            this.initialLocation = initialLocation;
            this.onCancel = onCancel;
        }
    }
}
