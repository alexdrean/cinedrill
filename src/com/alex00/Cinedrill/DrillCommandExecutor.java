package com.alex00.Cinedrill;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public class DrillCommandExecutor implements CommandExecutor {

    public static Set<UUID> drill = new TreeSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        toggleDrill(p);
        return true;
    }

    public static void toggleDrill(Player p) {
        if (drill.remove(p.getUniqueId())) {
            p.sendMessage("§eCinéCube §7§l»§r §cVous avez désactivé la super pioche. §7(/drill ou Sneak + Clic-droit avec pioche)");
        } else {
            drill.add(p.getUniqueId());
            p.sendMessage("§eCinéCube §7§l»§r §aVous avez activé la super pioche. §7(/drill ou Sneak + Clic-droit avec pioche)");
        }
    }
}
