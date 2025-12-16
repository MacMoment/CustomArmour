package me.macmoment.customarmor.commands;

import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.utils.EssenceUtils;
import me.macmoment.customarmor.utils.TextUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /essence command
 * Maps to the /essence command from Skript
 */
public class EssenceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        int count = EssenceUtils.getPlayerEssence(player);

        String message;
        if (count > 0) {
            message = CustomArmor.getInstance().getConfigManager().getEssenceMessage("count")
                .replace("{accent}", TextUtils.getColor())
                .replace("{amount}", String.valueOf(count));
        } else {
            message = CustomArmor.getInstance().getConfigManager().getEssenceMessage("none");
        }

        player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));

        return true;
    }
}
