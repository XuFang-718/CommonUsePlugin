package top.touchstudio.cup.modules.back;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import top.touchstudio.cup.CommonUsePlugin;
import top.touchstudio.cup.utils.ChatUtil;
import top.touchstudio.cup.utils.TeleportManager;

import java.util.HashMap;

/**
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class BackCommand implements CommandExecutor, Listener {
    public static HashMap<Player, Location> BackMap = new HashMap<>();
    private static final String PREFIX = "Back";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        if (!BackMap.containsKey(player)){
            ChatUtil.pluginSay(player, PREFIX, "未找到死亡地点");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        Location backLoc = BackMap.get(player);
        TeleportManager.teleportWithDelay(player, backLoc, 3,
                p -> {
                    BackMap.remove(p);
                    ChatUtil.pluginSay(p, PREFIX, "已传送到死亡地点");
                },
                null);

        return true;
    }


    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();
        BackMap.put(player,player.getLocation());
        ChatUtil.pluginSay(player, PREFIX, "使用/back命令返回死亡地点");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
    }
}
