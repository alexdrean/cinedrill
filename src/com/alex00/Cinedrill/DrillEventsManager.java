package com.alex00.Cinedrill;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

import static com.alex00.Cinedrill.Cinedrill.canDrill;
import static com.alex00.Cinedrill.Cinedrill.plugin;
import static com.alex00.Cinedrill.DrillCommandExecutor.toggleDrill;

public class DrillEventsManager implements Listener {

    public static HashMap<UUID, BlockFace> lastInteract = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMine(BlockBreakEvent e) {
        if (isRecursive(Thread.currentThread().getStackTrace()))
            return;
        if (canDrill(e.getPlayer())) {
            BlockFace lastFace = lastInteract.get(e.getPlayer().getUniqueId());
            if (lastFace == null) {
                plugin.getLogger().warning("Could not determine block face for " + e.getPlayer().getName());
                return;
            }
            int drilled = 0;
            for (Block block : getBlocksAdjacentToFace(lastFace, e.getBlock().getLocation())) {
                ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR)
                    break;
                if (isMineable(block.getType())) {
                    BlockBreakEvent event = new BlockBreakEvent(block, e.getPlayer());
                    plugin.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        block.breakNaturally(hand);
                        drilled++;
                        if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
                            damageTool(hand);
                    }
                }
            }
            if (drilled != 0 && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                boolean success = Cinedrill.consumeItem(e.getPlayer(), 1, Material.IRON_NUGGET);
                if (!success)
                    plugin.getLogger().warning("Could not consume super pickaxe consumable for " + e.getPlayer().getName());
            }
        }
    }

    private boolean isRecursive(StackTraceElement[] stackTrace) {
        for (int i = 2; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(DrillEventsManager.class.getName()))
                return true;
        }
        return false;
    }

    private Block[] getBlocksAdjacentToFace(BlockFace face, Location location) {
        Block[] blocks = new Block[8];
        int i = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (face.getModX() != 0 && x != 0) continue;
                    if (face.getModY() != 0 && y != 0) continue;
                    if (face.getModZ() != 0 && z != 0) continue;
                    if (x == 0 && y == 0 && z == 0) continue;
                    blocks[i++] = location.clone().add(x, y, z).getBlock();
                }
            }
        }
        return blocks;
    }

    private void damageTool(ItemStack hand) {
        ItemMeta meta = hand.getItemMeta();
        if (!hand.containsEnchantment(Enchantment.DURABILITY) && meta instanceof Damageable) {
            int damage = ((Damageable) meta).getDamage();
            if (hand.getType().getMaxDurability() == damage) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                ((Damageable) meta).setDamage(damage + 1);
                hand.setItemMeta(meta);
            }
        }
    }

    private boolean isMineable(Material type) {
        return type.isSolid() && type.getBlastResistance() < 10;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null)
            lastInteract.put(e.getPlayer().getUniqueId(), e.getBlockFace());
        else if (e.getPlayer().isSneaking() && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (e.getItem() != null && Cinedrill.isTool(e.getItem().getType())) {
                toggleDrill(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        lastInteract.remove(e.getPlayer().getUniqueId());
    }

}
