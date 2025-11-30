package top.touchstudio.cup.modules.money;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class MoneyEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 使用 SQLite 存储，玩家加入时检查并创建数据
        MoneyDatabase.getInstance().createPlayerIfNotExists(event.getPlayer().getName());
    }
}
