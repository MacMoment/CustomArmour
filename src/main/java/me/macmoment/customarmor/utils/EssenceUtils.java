package me.macmoment.customarmor.utils;

import me.macmoment.customarmor.CustomArmor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Armor Essence operations
 * Maps to giveEssence, getPlayerEssence, removeEssence functions from Skript
 */
public class EssenceUtils {

    private static final NamespacedKey ESSENCE_KEY = new NamespacedKey(CustomArmor.getInstance(), "armoressence");

    /**
     * Gives armor essence to a player
     * Maps to giveEssence function from Skript
     */
    public static void giveEssence(Player player, int amount) {
        ItemStack essence = new ItemStack(Material.NETHER_STAR, amount);
        ItemMeta meta = essence.getItemMeta();
        
        // Set name with gradient (using MiniMessage)
        meta.displayName(TextUtils.miniMessage("<gradient:#8B00FF:#FF1493>Armor Essence</gradient>"));
        
        // Set lore
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(TextUtils.colorize(TextUtils.getColor() + TextUtils.fancy("information")));
        lore.add(TextUtils.colorize(TextUtils.getColor() + "&l┃ &f" + TextUtils.fancy("A crystallized essence of power")));
        lore.add(TextUtils.colorize(TextUtils.getColor() + "&l┃ &f" + TextUtils.fancy("Used to buy & upgrade armor")));
        lore.add("");
        lore.add(TextUtils.colorize("&7" + TextUtils.fancy("Use in /armor menu")));
        meta.setLore(lore);
        
        // Set custom data
        meta.getPersistentDataContainer().set(ESSENCE_KEY, PersistentDataType.BYTE, (byte) 1);
        
        essence.setItemMeta(meta);
        player.getInventory().addItem(essence);
    }

    /**
     * Gets the total amount of essence a player has
     * Maps to getPlayerEssence function from Skript
     */
    public static int getPlayerEssence(Player player) {
        int total = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (isEssence(item)) {
                total += item.getAmount();
            }
        }
        
        return total;
    }

    /**
     * Removes essence from a player's inventory
     * Maps to removeEssence function from Skript
     */
    public static void removeEssence(Player player, int amount) {
        int toRemove = amount;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.NETHER_STAR && isEssence(item)) {
                int itemAmount = item.getAmount();
                
                if (itemAmount <= toRemove) {
                    player.getInventory().remove(item);
                    toRemove -= itemAmount;
                } else {
                    item.setAmount(itemAmount - toRemove);
                    toRemove = 0;
                }
                
                if (toRemove <= 0) {
                    break;
                }
            }
        }
    }

    /**
     * Checks if an item is armor essence
     */
    public static boolean isEssence(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ESSENCE_KEY, PersistentDataType.BYTE);
    }
}
