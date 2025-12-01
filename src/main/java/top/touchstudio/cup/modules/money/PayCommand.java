package top.touchstudio.cup.modules.money;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.touchstudio.cup.utils.CU;
import top.touchstudio.cup.utils.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips 快捷转账命令
 */
public class PayCommand implements CommandExecutor, TabExecutor {
    private final MoneyDatabase db = MoneyDatabase.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            ChatUtil.pluginSay(commandSender, "Money", "&4此命令只能由玩家执行!");
            return false;
        }
        Player player = (Player) commandSender;

        if (strings.length < 2) {
            ChatUtil.pluginSay(player, "Money", "用法: /pay <玩家名> <数量>");
            return false;
        }

        Player payTo = Bukkit.getPlayer(strings[0]);
        if (payTo == null) {
            ChatUtil.pluginSay(player, "Money", "未找到此玩家");
            return false;
        }

        if (payTo.equals(player)) {
            ChatUtil.pluginSay(player, "Money", "&4不能给自己转账!");
            return false;
        }

        int amount = parsePositiveInt(strings[1]);
        if (amount <= 0) {
            ChatUtil.pluginSay(player, "Money", "&4请输入有效的正整数金额");
            return false;
        }

        if (db.getMoney(player.getName()) < amount) {
            ChatUtil.pluginSay(player, "Money", "您的余额不足");
            return false;
        }

        if (db.transfer(player.getName(), payTo.getName(), amount)) {
            ChatUtil.pluginSay(player, "Money", CU.t("&r您已向玩家&6 " + payTo.getName() + " &r转了&6 " + amount + " &r枚硬币"));
            ChatUtil.pluginSay(payTo, "Money", CU.t("&r玩家&6 " + player.getName() + " &r向您转了&6 " + amount + " &r枚硬币"));
            return true;
        } else {
            ChatUtil.pluginSay(player, "Money", "&4转账失败，请稍后重试");
            return false;
        }
    }

    private int parsePositiveInt(String str) {
        try {
            int value = Integer.parseInt(str);
            return value > 0 ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> tab = new ArrayList<>();

        if (strings.length == 1) {
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                tab.add(onlinePlayer.getName());
            }
            return filterTab(tab, strings[0]);
        }

        if (strings.length == 2) {
            tab.add("<数量>");
        }

        return tab;
    }

    private List<String> filterTab(List<String> options, String input) {
        if (input == null || input.isEmpty()) {
            return options;
        }
        String lowerInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}
