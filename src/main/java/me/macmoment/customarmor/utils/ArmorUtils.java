package me.macmoment.customarmor.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.config.ConfigManager;
import me.macmoment.customarmor.data.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for armor-related operations
 * All display names and lore are read from config.yml
 */
public class ArmorUtils {

    private static final NamespacedKey ARMOR_PIECE_KEY = new NamespacedKey(CustomArmor.getInstance(), "armorpiece");
    private static final NamespacedKey TIER_KEY = new NamespacedKey(CustomArmor.getInstance(), "tier");
    private static final NamespacedKey MULTI_KEY = new NamespacedKey(CustomArmor.getInstance(), "multi");

    /**
     * Gives armor to a player
     */
    public static void giveArmor(Player player, int tier, String part) {
        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(tier);
        if (armorTier == null) return;

        String[] parts = part.equalsIgnoreCase("fullset") 
            ? new String[]{"head", "chestplate", "leggings", "boots"}
            : new String[]{part};

        for (String armorPart : parts) {
            ItemStack armor = createArmorPiece(armorTier, armorPart);
            if (armor != null) {
                player.getInventory().addItem(armor);
            }
        }
    }

    /**
     * Creates an armor piece item with name and lore from config
     */
    private static ItemStack createArmorPiece(ArmorTier tier, String part) {
        ItemStack item;
        String partName = part.substring(0, 1).toUpperCase() + part.substring(1);
        ConfigManager config = CustomArmor.getInstance().getConfigManager();

        switch (part.toLowerCase()) {
            case "head":
                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                
                // Set the texture
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", tier.getHeadTexture()));
                skullMeta.setPlayerProfile(profile);
                
                // Set display name from config format
                String skullDisplayName = config.getArmorPieceNameFormat()
                    .replace("{hex_color}", tier.getHexColor())
                    .replace("{tier_name}", tier.getName())
                    .replace("{piece_name}", partName);
                skullMeta.displayName(Component.text(TextUtils.colorize(skullDisplayName)));
                
                // Set custom data
                skullMeta.getPersistentDataContainer().set(ARMOR_PIECE_KEY, PersistentDataType.BYTE, (byte) 1);
                skullMeta.getPersistentDataContainer().set(TIER_KEY, PersistentDataType.INTEGER, tier.getTier());
                skullMeta.getPersistentDataContainer().set(MULTI_KEY, PersistentDataType.DOUBLE, tier.getMultiplier());
                
                // Set lore - use simplified lore for actual armor items (not GUI display)
                List<String> skullLore = buildArmorItemLore(tier, config);
                skullMeta.setLore(skullLore);
                
                item.setItemMeta(skullMeta);
                break;

            case "chestplate":
                item = new ItemStack(Material.LEATHER_CHESTPLATE);
                setLeatherArmorMeta(item, tier, partName, config);
                break;

            case "leggings":
                item = new ItemStack(Material.LEATHER_LEGGINGS);
                setLeatherArmorMeta(item, tier, partName, config);
                break;

            case "boots":
                item = new ItemStack(Material.LEATHER_BOOTS);
                setLeatherArmorMeta(item, tier, partName, config);
                break;

            default:
                return null;
        }

        return item;
    }

    /**
     * Builds simplified lore for actual armor items (not GUI display items)
     */
    private static List<String> buildArmorItemLore(ArmorTier tier, ConfigManager config) {
        List<String> lore = new ArrayList<>();
        String accentColor = config.getAccentColor();
        
        // Add basic stats lore without price/click information
        lore.add("");
        lore.add(TextUtils.colorize(tier.getHexColor() + "Statistics"));
        lore.add(TextUtils.colorize(tier.getHexColor() + "&l┃ &fMultiplier: " + tier.getHexColor() + tier.getMultiplier() + "x"));
        lore.add(TextUtils.colorize(tier.getHexColor() + "&l┃ &fTier: " + tier.getHexColor() + tier.getTier()));
        lore.add("");
        lore.add(TextUtils.colorize("&8This multiplier is per armor piece"));
        
        return lore;
    }

    /**
     * Sets leather armor meta with color and custom data
     */
    private static void setLeatherArmorMeta(ItemStack item, ArmorTier tier, String partName, ConfigManager config) {
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        
        meta.setColor(tier.getRgbColor());
        
        // Set display name from config format
        String displayName = config.getArmorPieceNameFormat()
            .replace("{hex_color}", tier.getHexColor())
            .replace("{tier_name}", tier.getName())
            .replace("{piece_name}", partName);
        meta.displayName(Component.text(TextUtils.colorize(displayName)));
        
        // Set custom data
        meta.getPersistentDataContainer().set(ARMOR_PIECE_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(TIER_KEY, PersistentDataType.INTEGER, tier.getTier());
        meta.getPersistentDataContainer().set(MULTI_KEY, PersistentDataType.DOUBLE, tier.getMultiplier());
        
        // Set lore
        List<String> lore = buildArmorItemLore(tier, config);
        meta.setLore(lore);
        
        item.setItemMeta(meta);
    }

    /**
     * Gets total armor stats multiplier from equipped armor
     */
    public static double getArmorStats(Player player) {
        double total = 0.0;
        
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.hasItemMeta()) {
                ItemMeta meta = armor.getItemMeta();
                if (meta.getPersistentDataContainer().has(MULTI_KEY, PersistentDataType.DOUBLE)) {
                    total += meta.getPersistentDataContainer().get(MULTI_KEY, PersistentDataType.DOUBLE);
                }
            }
        }
        
        return total;
    }

    /**
     * Gets the number of custom armor pieces equipped
     */
    public static int getArmorAmount(Player player) {
        int count = 0;
        
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.hasItemMeta()) {
                ItemMeta meta = armor.getItemMeta();
                if (meta.getPersistentDataContainer().has(ARMOR_PIECE_KEY, PersistentDataType.BYTE)) {
                    count++;
                }
            }
        }
        
        return count;
    }

    /**
     * Checks if an item is a custom armor piece
     */
    public static boolean isArmorPiece(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ARMOR_PIECE_KEY, PersistentDataType.BYTE);
    }

    /**
     * Gets the tier of a custom armor piece
     */
    public static int getArmorTier(ItemStack item) {
        if (!isArmorPiece(item)) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(TIER_KEY, PersistentDataType.INTEGER, 0);
    }
}
