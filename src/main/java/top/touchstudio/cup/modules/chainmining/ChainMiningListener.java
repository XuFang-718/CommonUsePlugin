package top.touchstudio.cup.modules.chainmining;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import top.touchstudio.cup.CommonUsePlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChainMiningListener implements Listener {

    private final CommonUsePlugin plugin;

    public ChainMiningListener(CommonUsePlugin plugin) {
        this.plugin = plugin;
    }
    public static HashMap<Player,Boolean> CMMap = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // 默认开启连锁挖矿，只有明确关闭时才不触发
        if (CMMap.containsKey(event.getPlayer()) && !CMMap.get(event.getPlayer())) {
            return;
        }
        Block block = event.getBlock();
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

        if (isValidTool(tool) && isOre(block.getType())) {
            Set<Block> blocksToBreak = new HashSet<>();
            findBlocksToBreak(block, block.getType(), blocksToBreak);

            for (Block b : blocksToBreak) {
                if (isValidTool(tool) && isOre(b.getType())) {
                    b.breakNaturally(tool);
                }
            }

            int blocksBroken = blocksToBreak.size();

            // 连锁挖矿成功音效（挖掉超过1个方块时播放）
            if (blocksBroken > 1) {
                Player player = event.getPlayer();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);

                // 首次使用连锁挖矿提示
                ChainMiningDatabase db = ChainMiningDatabase.getInstance();
                if (!db.hasUsedChainMining(player.getName())) {
                    db.markAsUsed(player.getName());
                    sendFirstUseMessage(player);
                }
            }
            int newDurability = tool.getDurability() + blocksBroken;

            if (newDurability >= tool.getType().getMaxDurability()) {
                event.getPlayer().getInventory().remove(tool);
            } else {
                tool.setDurability((short) newDurability);
            }
        }
    }

    private boolean isValidTool(ItemStack tool) {
        return tool != null && tool.getType().name().endsWith("_PICKAXE");
    }

    private boolean isOre(Material material) {
        return material == Material.COAL_ORE || material == Material.IRON_ORE || material == Material.GOLD_ORE
                || material == Material.DIAMOND_ORE || material == Material.EMERALD_ORE || material == Material.REDSTONE_ORE
                || material == Material.LAPIS_ORE || material == Material.NETHER_QUARTZ_ORE || material == Material.NETHER_GOLD_ORE
                || material == Material.DEEPSLATE_COAL_ORE || material == Material.DEEPSLATE_IRON_ORE || material == Material.DEEPSLATE_GOLD_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE || material == Material.DEEPSLATE_EMERALD_ORE || material == Material.DEEPSLATE_REDSTONE_ORE
                || material == Material.DEEPSLATE_LAPIS_ORE || material == Material.ANCIENT_DEBRIS || material == Material.COPPER_ORE;
    }

    private void findBlocksToBreak(Block start, Material type, Set<Block> blocksToBreak) {
        if (blocksToBreak.size() > 100) return; // 不挖超过100
        blocksToBreak.add(start);

        for (Block b : getAdjacentBlocks(start)) {
            if (!blocksToBreak.contains(b) && b.getType() == type) {
                findBlocksToBreak(b, type, blocksToBreak);
            }
        }
    }

    private Set<Block> getAdjacentBlocks(Block block) {
        Set<Block> adjacentBlocks = new HashSet<>();

        adjacentBlocks.add(block.getRelative(BlockFace.NORTH));
        adjacentBlocks.add(block.getRelative(BlockFace.SOUTH));
        adjacentBlocks.add(block.getRelative(BlockFace.EAST));
        adjacentBlocks.add(block.getRelative(BlockFace.WEST));
        adjacentBlocks.add(block.getRelative(BlockFace.UP));
        adjacentBlocks.add(block.getRelative(BlockFace.DOWN));

        adjacentBlocks.add(block.getRelative(BlockFace.NORTH_EAST));
        adjacentBlocks.add(block.getRelative(BlockFace.NORTH_WEST));
        adjacentBlocks.add(block.getRelative(BlockFace.SOUTH_EAST));
        adjacentBlocks.add(block.getRelative(BlockFace.SOUTH_WEST));
        return adjacentBlocks;
    }

    private void sendFirstUseMessage(Player player) {
        // 播放特殊音效
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        // 构建美化消息
        Component prefix = Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("BlockLife", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text("] ", NamedTextColor.GRAY));

        Component message = prefix
                .append(Component.text("✦ ", NamedTextColor.GOLD))
                .append(Component.text("你又掌握了一个技巧: ", NamedTextColor.WHITE))
                .append(Component.text("连锁挖矿", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text("!", NamedTextColor.WHITE));

        Component tip = Component.text("   ")
                .append(Component.text("💡 ", NamedTextColor.YELLOW))
                .append(Component.text("不想要连锁挖矿? 输入 ", NamedTextColor.GRAY))
                .append(Component.text("/cm off", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.suggestCommand("/cm off"))
                        .decorate(TextDecoration.UNDERLINED))
                .append(Component.text(" 关闭", NamedTextColor.GRAY));

        player.sendMessage(Component.empty());
        player.sendMessage(message);
        player.sendMessage(tip);
        player.sendMessage(Component.empty());
    }
}
