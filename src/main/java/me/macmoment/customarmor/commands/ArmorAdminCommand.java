package me.macmoment.customarmor.commands;

import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.utils.ArmorUtils;
import me.macmoment.customarmor.utils.EssenceUtils;
import me.macmoment.customarmor.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles /armoradmin command
 * Maps to the /armoradmin command from Skript
 */
public class ArmorAdminCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cUsage: /armoradmin <givearmor|giveessence|reload>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "givearmor":
                handleGiveArmor(sender, args);
                break;

            case "giveessence":
                handleGiveEssence(sender, args);
                break;

            case "reload":
                handleReload(sender);
                break;

            default:
                sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cUnknown subcommand!"));
                break;
        }

        return true;
    }

    private void handleGiveArmor(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-player")));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cPlayer not found!"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-type")));
            return;
        }

        String type = args[2].toLowerCase();
        if (!type.equals("fullset") && !type.equals("head") && 
            !type.equals("chestplate") && !type.equals("leggings") && !type.equals("boots")) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-type")));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-tier")));
            return;
        }

        try {
            int tier = Integer.parseInt(args[3]);
            int maxTier = CustomArmor.getInstance().getConfigManager().getMaxTier();
            if (tier < 1 || tier > maxTier) {
                sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cTier must be between 1 and " + maxTier + "!"));
                return;
            }

            ArmorUtils.giveArmor(target, tier, type);
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &aGave armor to " + target.getName() + "!"));
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cInvalid tier number!"));
        }
    }

    private void handleGiveEssence(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-player")));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cPlayer not found!"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
                CustomArmor.getInstance().getConfigManager().getAdminMessage("choose-amount")));
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cAmount must be positive!"));
                return;
            }

            EssenceUtils.giveEssence(target, amount);
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &aGave " + amount + "x Armor Essence to " + target.getName() + "!"));
        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &cInvalid amount number!"));
        }
    }

    private void handleReload(CommandSender sender) {
        sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " &aReloading plugin..."));
        // Reload config and armor registry
        CustomArmor.getInstance().getConfigManager().reloadConfig();
        CustomArmor.getInstance().getArmorRegistry().registerAllArmors();
        sender.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + 
            CustomArmor.getInstance().getConfigManager().getAdminMessage("reload")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("givearmor", "giveessence", "reload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("givearmor") || args[0].equalsIgnoreCase("giveessence")) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("givearmor")) {
                completions.addAll(Arrays.asList("fullset", "head", "chestplate", "leggings", "boots"));
            } else if (args[0].equalsIgnoreCase("giveessence")) {
                completions.addAll(Arrays.asList("1", "10", "100", "1000"));
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("givearmor")) {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            }
        }

        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
