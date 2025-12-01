package top.touchstudio.cup.modules.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static top.touchstudio.cup.modules.tpa.TpaCommand.buildPrefix;

/**
 * TPA Here 接受命令 - /tphereaccept <玩家名>
 * 接受者传送到请求者身边
 */
public class TpaHereAcceptCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(buildPrefix().append(Component.text("此命令只能由玩家执行!", NamedTextColor.RED)));
            return false;
        }

        Player accepter = (Player) commandSender;

        if (strings.length == 0) {
            List<String> requesters = TpaHereCommand.getRequesters(accepter.getUniqueId());
            if (requesters.isEmpty()) {
                accepter.sendMessage(buildPrefix().append(Component.text("您没有待处理的传送请求", NamedTextColor.GRAY)));
            } else {
                accepter.sendMessage(buildPrefix().append(Component.text("用法: ", NamedTextColor.GRAY))
                        .append(Component.text("/tphereaccept <玩家名>", NamedTextColor.YELLOW)));
                accepter.sendMessage(buildPrefix().append(Component.text("待处理: ", NamedTextColor.GRAY))
                        .append(Component.text(String.join(", ", requesters), NamedTextColor.GOLD)));
            }
            return false;
        }

        String requesterName = strings[0];

        if (!TpaHereCommand.hasRequest(accepter.getUniqueId(), requesterName)) {
            accepter.sendMessage(buildPrefix().append(Component.text("没有来自 ", NamedTextColor.RED))
                    .append(Component.text(requesterName, NamedTextColor.GOLD))
                    .append(Component.text(" 的传送请求", NamedTextColor.RED)));
            return false;
        }

        Player requester = TpaHereCommand.getRequester(accepter.getUniqueId(), requesterName);
        TpaHereCommand.removeRequest(accepter.getUniqueId(), requesterName);

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

        // 通知请求者
        requester.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        requester.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                .append(Component.text(accepter.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" 已接受您的传送请求!", NamedTextColor.GREEN)));
        requester.sendMessage(buildPrefix().append(Component.text("对方将在 ", NamedTextColor.GRAY))
                .append(Component.text("3", NamedTextColor.GOLD))
                .append(Component.text(" 秒后传送到您身边", NamedTextColor.GRAY)));
        requester.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));

        // 接受者传送到请求者身边（使用 TeleportManager）
        final Player finalAccepter = accepter;
        final Player finalRequester = requester;

        top.touchstudio.cup.utils.TeleportManager.teleportWithDelay(
                accepter,
                () -> finalRequester.isOnline() ? finalRequester.getLocation() : null,
                3,
                p -> {
                    // 传送成功
                    p.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                            .append(Component.text("传送完成!", NamedTextColor.GREEN)));

                    if (finalRequester.isOnline()) {
                        finalRequester.sendMessage(buildPrefix().append(Component.text("✔ ", NamedTextColor.GREEN))
                                .append(Component.text(p.getName(), NamedTextColor.GOLD))
                                .append(Component.text(" 已传送到您身边", NamedTextColor.GREEN)));
                    }
                },
                p -> {
                    // 传送取消
                    if (finalRequester.isOnline()) {
                        finalRequester.sendMessage(buildPrefix().append(Component.text("✘ ", NamedTextColor.RED))
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
            List<String> requesters = TpaHereCommand.getRequesters(player.getUniqueId());
            for (String name : requesters) {
                if (name.toLowerCase().startsWith(strings[0].toLowerCase())) {
                    tab.add(name);
                }
            }
        }
        return tab;
    }
}
