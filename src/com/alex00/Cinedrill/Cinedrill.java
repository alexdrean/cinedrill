package com.alex00.Cinedrill;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static org.bukkit.Material.*;

@SuppressWarnings("SpellCheckingInspection")
public class Cinedrill extends JavaPlugin {

    private static Set<UUID> drilling = new HashSet<>();
    public static Cinedrill plugin;

    public void onEnable() {
        plugin = this;
        Objects.requireNonNull(getCommand("drill")).setExecutor(new DrillCommandExecutor());
        getServer().getPluginManager().registerEvents(new DrillEventsManager(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(
                this,
                () -> getServer().getOnlinePlayers().forEach(Cinedrill::updateDrilling),
                10L,
                10L
        );
    }

    public static boolean couldDrill(Player p) {
        if (p == null) return false;
        if (!DrillCommandExecutor.drill.contains(p.getUniqueId())) return false;
        ItemStack inHand = p.getInventory().getItemInMainHand();
        return p.hasPermission("cinedrill.use") && isTool(inHand.getType());
    }

    public static boolean canDrill(Player p) {
        if (!couldDrill(p)) return false;
        return p.getInventory().contains(Material.IRON_NUGGET);
    }

    private static List<Material> tools = Lists.newArrayList(
            DIAMOND_PICKAXE,
            IRON_PICKAXE,
            GOLDEN_PICKAXE,
            STONE_PICKAXE,
            WOODEN_PICKAXE,
            DIAMOND_SHOVEL,
            IRON_SHOVEL,
            GOLDEN_SHOVEL,
            STONE_SHOVEL,
            WOODEN_SHOVEL
    );

    public static boolean isTool(Material type) {
        return tools.contains(type);
    }

    public static void updateDrilling(Player p) {
        if (couldDrill(p)) {
            notifyActionBar(p);
            drilling.add(p.getUniqueId());
        } else {
            clearActionBar(p);
        }
    }

    public static void clearActionBar(Player p) {
        if (drilling.remove(p.getUniqueId())) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
        }
    }

    public static void notifyActionBar(Player p) {
        int count = countItem(p, IRON_NUGGET);
        String num = count > 0 ? String.valueOf(count) : "Aucune";
        String plural = count > 1 ? "s" : "";
        String title = "§7» §aSuper pioche activée §7▪ §f" + num + " pépite" + plural + " de fer restante" + plural + " §7«";
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(title));
    }

    public static boolean consumeItem(Player player, int count, Material mat) {
        Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);

        int found = 0;
        for (ItemStack stack : ammo.values())
            found += stack.getAmount();
        if (count > found)
            return false;

        for (Integer index : ammo.keySet()) {
            ItemStack stack = ammo.get(index);

            int removed = Math.min(count, stack.getAmount());
            count -= removed;

            if (stack.getAmount() == removed)
                player.getInventory().setItem(index, null);
            else
                stack.setAmount(stack.getAmount() - removed);

            if (count <= 0)
                break;
        }

        player.updateInventory();
        return true;
    }

    @SuppressWarnings("unused")
    public static void giveItem(Player player, ItemStack itemStack) {
        HashMap<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
        for (ItemStack stack : map.values())
            player.getWorld().dropItem(player.getLocation(), stack);
    }

    public static int countItem(Player player, Material mat) {
        Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);

        int found = 0;
        for (ItemStack stack : ammo.values())
            found += stack.getAmount();
        return found;
    }

}
