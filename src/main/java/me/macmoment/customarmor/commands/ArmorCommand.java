package me.macmoment.customarmor.commands;

import me.macmoment.customarmor.gui.ArmorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /armor command
 * Maps to the /armor command from Skript
 */
public class ArmorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        ArmorGUI.openGUI(player, 1);
        return true;
    }
}
