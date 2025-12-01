package top.touchstudio.cup.modules.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.touchstudio.cup.CommonUsePlugin;

import java.util.ArrayList;
import java.util.List;

import static top.touchstudio.cup.modules.tpa.TpaCommand.buildPrefix;

/**
 * TPA 接受命令 - /tpaccept <玩家名>
 */
public class TpaAcceptCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(buildPrefix().append(Component.text("此命令只能由玩家执行!", NamedTextColor.RED)));
            return false;
        }

        Player accepter = (Player) commandSender;

        if (strings.length == 0) {
            List<String> requesters = TpaCommand.getRequesters(accepter.getUniqueId());
            if (requesters.isEmpty()) {
                accepter.sendMessage(buildPrefix().append(Component.text("您没有待处理的传送请求", NamedTextColor.GRAY)));
            } else {
                accepter.sendMessage(buildPrefix().append(Component.text("用法: ", NamedTextColor.GRAY))
                        .append(Component.text("/tpaccept <玩家名>", NamedTextColor.YELLOW)));
                accepter.sendMessage(buildPrefix().append(Component.text("待处理: ", NamedTextColor.GRAY))
                        .append(Component.text(String.join(", ", requesters), NamedTextColor.GOLD)));
            }
            return false;
        }

        String requesterName = strings[0];

        if (!TpaCommand.hasRequest(accepter.getUniqueId(), requesterName)) {
            accepter.sendMessage(buildPrefix().append(Component.text("没有来自 ", NamedTextColor.RED))
                    .append(Component.text(requesterName, NamedTextColor.GOLD))
                    .append(Component.text(" 的传送请求", NamedTextColor.RED)));
            return false;
        }

        Player requester = TpaCommand.getRequester(accepter.getUniqueId(), requesterName);
        TpaCommand.removeRequest(accepter.getUniqueId(), requesterName);

        if (requester == null || !requester.isOnline()) {
            accepter.sendMessage(buildPrefix().append(Component.text("玩家 ", NamedTextColor.RED))
                    .append(Component.text(requesterName, NamedTextColor.GOLD))
                    .append(Component.text(" 已离线", NamedTextColor.RED)));
            return false;
        }

        // 通知接受者
        accepter.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                .append(Component.text("已接受 ", NamedTextColor.GREEN))
                .append(Component.text(requesterName, NamedTextColor.GOLD))
                .append(Component.text(" 的传送请求", NamedTextColor.GREEN)));
        accepter.playSound(accepter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // 通知请求者
        requester.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        requester.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                .append(Component.text(accepter.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" 已接受您的传送请求!", NamedTextColor.GREEN)));
        requester.playSound(requester.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        // 使用 TeleportManager 处理传送（支持移动取消）
        final Player finalAccepter = accepter;
        final Player finalRequester = requester;

        top.touchstudio.cup.utils.TeleportManager.teleportWithDelay(
                requester,
                () -> finalAccepter.isOnline() ? finalAccepter.getLocation() : null, // 动态获取目标位置
                3,
                p -> {
                    // 传送成功
                    p.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                            .append(Component.text("传送完成!", NamedTextColor.GREEN)));

                    if (finalAccepter.isOnline()) {
                        finalAccepter.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                                .append(Component.text(p.getName(), NamedTextColor.GOLD))
                                .append(Component.text(" 已传送到您身边", NamedTextColor.GREEN)));
                    }
                },
                p -> {
                    // 传送取消
                    if (finalAccepter.isOnline()) {
                        finalAccepter.sendMessage(buildPrefix().append(Component.text("✘ ", NamedTextColor.RED))
                                .append(Component.text(p.getName(), NamedTextColor.GOLD))
                                .append(Component.text(" 取消了传送", NamedTextColor.RED)));
                    }
                }
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> tab = new ArrayList<>();
        if (strings.length == 1 && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> requesters = TpaCommand.getRequesters(player.getUniqueId());
            for (String name : requesters) {
                if (name.toLowerCase().startsWith(strings[0].toLowerCase())) {
                    tab.add(name);
                }
            }
        }
        return tab;
    }
}
