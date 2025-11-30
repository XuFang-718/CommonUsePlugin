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
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class MoneyCommand implements CommandExecutor, TabExecutor {
    private final MoneyDatabase db = MoneyDatabase.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            ChatUtil.pluginSay(commandSender, "&4此命令只能由玩家执行!");
            return false;
        }
        Player player = (Player) commandSender;

        if (strings.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = strings[0].toLowerCase();

        return switch (subCommand) {
            case "info" -> handleInfo(player);
            case "pay" -> handlePay(player, strings);
            case "set" -> handleSet(player, strings);
            case "add" -> handleAdd(player, strings);
            default -> {
                showHelp(player);
                yield false;
            }
        };
    }

    private void showHelp(Player player) {
        ChatUtil.pluginSay(player, "&e===== 金钱系统帮助 =====");
        ChatUtil.pluginSay(player, "&6/money info &7- 查看余额");
        ChatUtil.pluginSay(player, "&6/money pay <玩家> <数量> &7- 转账给其他玩家");
        if (player.isOp()) {
            ChatUtil.pluginSay(player, "&6/money set <玩家> <数量> &7- 设置玩家金钱");
            ChatUtil.pluginSay(player, "&6/money add <玩家> <数量> &7- 增加玩家金钱");
        }
    }

    private boolean handleInfo(Player player) {
        int playerMoney = db.getMoney(player.getName());
        ChatUtil.pluginSay(player, "您目前有 " + playerMoney + " 米");
        return true;
    }

    private boolean handlePay(Player player, String[] strings) {
        if (strings.length < 3) {
            ChatUtil.pluginSay(player, "用法 /money pay <玩家名> <数量>");
            return false;
        }

        Player payTo = Bukkit.getPlayer(strings[1]);
        if (payTo == null) {
            ChatUtil.pluginSay(player, "未找到此玩家");
            return false;
        }

        if (payTo.equals(player)) {
            ChatUtil.pluginSay(player, "&4不能给自己转账!");
            return false;
        }

        int amount = parsePositiveInt(strings[2]);
        if (amount <= 0) {
            ChatUtil.pluginSay(player, "&4请输入有效的正整数金额");
            return false;
        }

        if (db.getMoney(player.getName()) < amount) {
            ChatUtil.pluginSay(player, "您的余额不足");
            return false;
        }

        if (db.transfer(player.getName(), payTo.getName(), amount)) {
            ChatUtil.pluginSay(player, CU.t("&r您已向玩家&6 " + payTo.getName() + " &r转了&6 " + amount + " &r米"));
            ChatUtil.pluginSay(payTo, CU.t("&r玩家&6 " + player.getName() + " &r向您转了&6 " + amount + " &r米"));
            return true;
        } else {
            ChatUtil.pluginSay(player, "&4转账失败，请稍后重试");
            return false;
        }
    }

    private boolean handleSet(Player player, String[] strings) {
        if (!player.isOp()) {
            ChatUtil.pluginSay(player, "&4你不是OP!");
            return false;
        }

        if (strings.length < 3) {
            ChatUtil.pluginSay(player, "用法 /money set <玩家名> <数量>");
            return false;
        }

        Player setTo = Bukkit.getPlayer(strings[1]);
        if (setTo == null) {
            ChatUtil.pluginSay(player, "未找到此玩家");
            return false;
        }

        int amount = parseNonNegativeInt(strings[2]);
        if (amount < 0) {
            ChatUtil.pluginSay(player, "&4请输入有效的非负整数金额");
            return false;
        }

        if (db.setMoney(setTo.getName(), amount)) {
            ChatUtil.pluginSay(player, CU.t("&r您已将玩家&6 " + setTo.getName() + " &r的米设置为&6 " + amount + " &r米"));
            ChatUtil.pluginSay(setTo, CU.t("&r管理员已将你的米设置为&6 " + amount + " &r米"));
            return true;
        } else {
            ChatUtil.pluginSay(player, "&4设置失败，请稍后重试");
            return false;
        }
    }

    private boolean handleAdd(Player player, String[] strings) {
        if (!player.isOp()) {
            ChatUtil.pluginSay(player, "&4你不是OP!");
            return false;
        }

        if (strings.length < 3) {
            ChatUtil.pluginSay(player, "用法 /money add <玩家名> <数量>");
            return false;
        }

        Player addTo = Bukkit.getPlayer(strings[1]);
        if (addTo == null) {
            ChatUtil.pluginSay(player, "未找到此玩家");
            return false;
        }

        int amount = parsePositiveInt(strings[2]);
        if (amount <= 0) {
            ChatUtil.pluginSay(player, "&4请输入有效的正整数金额");
            return false;
        }

        if (db.addMoney(addTo.getName(), amount)) {
            int newBalance = db.getMoney(addTo.getName());
            ChatUtil.pluginSay(player, CU.t("&r您已为玩家&6 " + addTo.getName() + " &r添加了&6 " + amount + " &r米"));
            ChatUtil.pluginSay(addTo, CU.t("&r管理员给你添加了&6 " + amount + " &r米 您目前有&6 " + newBalance + " &r米"));
            return true;
        } else {
            ChatUtil.pluginSay(player, "&4添加失败，请稍后重试");
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

    private int parseNonNegativeInt(String str) {
        try {
            int value = Integer.parseInt(str);
            return value >= 0 ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> tab = new ArrayList<>();

        if (strings.length == 1) {
            tab.add("pay");
            tab.add("info");
            if (commandSender.isOp()) {
                tab.add("set");
                tab.add("add");
            }
            return filterTab(tab, strings[0]);
        }

        if (strings.length == 2) {
            String subCmd = strings[0].toLowerCase();
            if (subCmd.equals("pay") || subCmd.equals("set") || subCmd.equals("add")) {
                for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                    tab.add(onlinePlayer.getName());
                }
                return filterTab(tab, strings[1]);
            }
        }

        if (strings.length == 3) {
            String subCmd = strings[0].toLowerCase();
            if (subCmd.equals("pay") || subCmd.equals("set") || subCmd.equals("add")) {
                tab.add("<数量>");
            }
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
