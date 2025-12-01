package top.touchstudio.cup.modules.tpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.touchstudio.cup.CommonUsePlugin;
import top.touchstudio.cup.utils.ChatUtil;
import top.touchstudio.cup.utils.TeleportManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class Tpa implements CommandExecutor, TabExecutor, Listener {

    public static HashMap<Player, Player> TpaMap = new HashMap<>();
    public static HashMap<Player, BukkitTask> TpaTasks = new HashMap<>();

    private static final String PREFIX = "TPA";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            Player player = (Player) commandSender;
            ChatUtil.pluginSay(player, PREFIX, "/tpa 玩家名称");
            return false;
        }

        if (strings.length == 1 && strings[0].equalsIgnoreCase("accept")) {
            Player player = (Player) commandSender;
            if (!TpaMap.containsKey(player)) {
                ChatUtil.pluginSay(player, PREFIX, "您没有传送请求");
                return false;
            }
            Player tpaTo = TpaMap.get(player);
            ChatUtil.pluginSay(player, PREFIX, "对方已接受传送请求");
            Location targetLoc = player.getLocation();
            TeleportManager.teleportWithDelay(tpaTo, targetLoc, 3,
                    p -> {
                        TpaMap.remove(player);
                        if (TpaTasks.containsKey(player)) {
                            TpaTasks.get(player).cancel();
                            TpaTasks.remove(player);
                        }
                        ChatUtil.pluginSay(p, PREFIX, "传送完成");
                        ChatUtil.pluginSay(player, PREFIX, "对方已传送到您身边");
                    },
                    null);
            return true;
        }

        if (strings.length == 1 && strings[0].equalsIgnoreCase("deny")) {
            Player player = (Player) commandSender;
            if (!TpaMap.containsKey(player)) {
                ChatUtil.pluginSay(player, PREFIX, "您没有传送请求");
                return false;
            }
            Player tpaTo = TpaMap.get(player);
            TpaMap.remove(player);
            if (TpaTasks.containsKey(player)) {
                TpaTasks.get(player).cancel();
                TpaTasks.remove(player);
            }
            ChatUtil.pluginSay(player, PREFIX, "已拒绝传送");
            ChatUtil.pluginSay(tpaTo, PREFIX, "对方拒绝了传送");
            return true;
        }

        if (strings.length <= 1) {
            Player player = (Player) commandSender;
            Player tpaTo = Bukkit.getPlayer(strings[0]);
            if (tpaTo == null) {
                ChatUtil.pluginSay(commandSender, PREFIX, "玩家不存在或不在线");
                return false;
            }
            if (tpaTo.equals(player)) {
                ChatUtil.pluginSay(player, PREFIX, "不能传送自己");
                return false;
            }

            TpaMap.put(tpaTo, player);
            ChatUtil.pluginSay(player, PREFIX, "传送请求已发送");
            ChatUtil.pluginSay(tpaTo, PREFIX, "玩家 " + player.getName() + " 想要传送到您身边，请在三分钟内接受\n" + ChatColor.WHITE + "[" + ChatColor.AQUA + PREFIX + ChatColor.WHITE + "] 输入 /tpa accept 同意 /tpa deny 拒绝");

            if (TpaTasks.containsKey(tpaTo)) {
                TpaTasks.get(tpaTo).cancel();
                TpaTasks.remove(tpaTo);
            }

            BukkitTask task = Bukkit.getScheduler().runTaskLater(CommonUsePlugin.instance, () -> {
                TpaMap.remove(tpaTo);
                ChatUtil.pluginSay(player, PREFIX, "请求过期");
                ChatUtil.pluginSay(tpaTo, PREFIX, "请求过期");
                TpaTasks.remove(tpaTo);
            }, 3 * 60 * 20); // 3分钟，单位为tick，20 tick = 1秒

            TpaTasks.put(tpaTo, task);
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> tab = new ArrayList<>();
        if (strings.length == 1) {
            tab.add("accept");
            tab.add("deny");
            for (Player player : Bukkit.getOnlinePlayers()) {
                tab.add(player.getName());
            }
        }
        tab.removeIf(option -> !option.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase()));
        return tab;
    }
}
