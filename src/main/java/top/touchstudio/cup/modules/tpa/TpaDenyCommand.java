package top.touchstudio.cup.modules.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
 * TPA 拒绝命令 - /tpadeny <玩家名>
 */
public class TpaDenyCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(buildPrefix().append(Component.text("此命令只能由玩家执行!", NamedTextColor.RED)));
            return false;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            List<String> requesters = TpaCommand.getRequesters(player.getUniqueId());
            if (requesters.isEmpty()) {
                player.sendMessage(buildPrefix().append(Component.text("您没有待处理的传送请求", NamedTextColor.GRAY)));
            } else {
                player.sendMessage(buildPrefix().append(Component.text("用法: ", NamedTextColor.GRAY))
                        .append(Component.text("/tpadeny <玩家名>", NamedTextColor.YELLOW)));
                player.sendMessage(buildPrefix().append(Component.text("待处理: ", NamedTextColor.GRAY))
                        .append(Component.text(String.join(", ", requesters), NamedTextColor.GOLD)));
            }
            return false;
        }

        String requesterName = strings[0];

        if (!TpaCommand.hasRequest(player.getUniqueId(), requesterName)) {
            player.sendMessage(buildPrefix().append(Component.text("没有来自 ", NamedTextColor.RED))
                    .append(Component.text(requesterName, NamedTextColor.GOLD))
                    .append(Component.text(" 的传送请求", NamedTextColor.RED)));
            return false;
        }

        Player requester = TpaCommand.getRequester(player.getUniqueId(), requesterName);
        TpaCommand.removeRequest(player.getUniqueId(), requesterName);

        // 通知拒绝者
        player.sendMessage(buildPrefix().append(Component.text("✘ ", NamedTextColor.RED))
                .append(Component.text("已拒绝 ", NamedTextColor.RED))
                .append(Component.text(requesterName, NamedTextColor.GOLD))
                .append(Component.text(" 的传送请求", NamedTextColor.RED)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

        // 通知请求者
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(buildPrefix().append(Component.text("✘ ", NamedTextColor.RED))
                    .append(Component.text(player.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" 拒绝了您的传送请求", NamedTextColor.RED)));
            requester.playSound(requester.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

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
