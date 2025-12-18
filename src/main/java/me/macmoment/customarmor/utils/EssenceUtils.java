package me.macmoment.customarmor.utils;

import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.config.ConfigManager;
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
 * Essence name and lore are read from config.yml
 */
public class EssenceUtils {

    private static final NamespacedKey ESSENCE_KEY = new NamespacedKey(CustomArmor.getInstance(), "armoressence");

    /**
     * Gives armor essence to a player
     * Uses name and lore from config
     */
    public static void giveEssence(Player player, int amount) {
        ConfigManager config = CustomArmor.getInstance().getConfigManager();
        
        ItemStack essence = new ItemStack(Material.NETHER_STAR, amount);
        ItemMeta meta = essence.getItemMeta();
        
        // Set name from config (supports MiniMessage gradients)
        String essenceName = config.getEssenceName();
        meta.displayName(TextUtils.miniMessage(essenceName));
        
        // Set lore from config
        List<String> configLore = config.getEssenceLore();
        List<String> lore = new ArrayList<>();
        String accentColor = config.getAccentColor();
        
        for (String line : configLore) {
            String processedLine = line.replace("{accent}", accentColor);
            lore.add(TextUtils.colorize(processedLine));
        }
        meta.setLore(lore);
        
        // Set custom data
        meta.getPersistentDataContainer().set(ESSENCE_KEY, PersistentDataType.BYTE, (byte) 1);
        
        essence.setItemMeta(meta);
        player.getInventory().addItem(essence);
    }

    /**
     * Gets the total amount of essence a player has
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
