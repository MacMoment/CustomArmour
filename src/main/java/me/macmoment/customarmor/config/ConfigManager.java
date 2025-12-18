package me.macmoment.customarmor.config;

import me.macmoment.customarmor.CustomArmor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Manages plugin configuration
 * Provides easy access to all config values
 */
public class ConfigManager {
    private final CustomArmor plugin;
    private FileConfiguration config;

    public ConfigManager(CustomArmor plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Prefix and colors
    public String getPrefix() {
        return config.getString("prefix", "&8[<##FF9100>&lA<##FFB047>&lR<##FFCE8D>&lM<##FFB047>&lO<##FF9100>&lR&8] &8»&7");
    }

    public String getAccentColor() {
        return config.getString("accent-color", "<##FFCE8D>");
    }

    // Upgrade settings
    public int getDiscountPerTier() {
        return config.getInt("upgrade-settings.discount-per-tier", 15);
    }

    public int getMinimumPrice() {
        return config.getInt("upgrade-settings.minimum-price", 10);
    }

    // Armor tier data
    public String getArmorName(int tier) {
        return config.getString("armor-tiers." + tier + ".name", "Unknown");
    }

    public Color getArmorRGBColor(int tier) {
        String rgb = config.getString("armor-tiers." + tier + ".rgb-color", "150,150,150");
        String[] parts = rgb.split(",");
        try {
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return Color.fromRGB(150, 150, 150);
        }
    }

    public String getArmorHexColor(int tier) {
        return config.getString("armor-tiers." + tier + ".hex-color", "<##969696>");
    }

    public double getArmorMultiplier(int tier) {
        return config.getDouble("armor-tiers." + tier + ".multiplier", 0.05);
    }

    public int getArmorPrice(int tier) {
        return config.getInt("armor-tiers." + tier + ".price", 25);
    }

    public String getArmorHeadTexture(int tier) {
        return config.getString("armor-tiers." + tier + ".head-texture", "");
    }

    // Messages
    public String getMessage(String path) {
        return config.getString("messages." + path, "&cMessage not found: " + path);
    }

    public String getAdminMessage(String key) {
        return getMessage("admin." + key);
    }

    public String getEssenceMessage(String key) {
        return getMessage("essence." + key);
    }

    public String getShopMessage(String key) {
        return getMessage("shop." + key);
    }

    // ==================== GUI Settings ====================

    public int getGUISize() {
        return config.getInt("gui.size", 54);
    }

    public String getGUITitle() {
        return config.getString("gui.title", "&8Armor Browser &7(Tier {tier}/{max_tier})");
    }

    // GUI Slot positions
    public int getSlotPrevious() {
        return config.getInt("gui.slots.previous-button", 10);
    }

    public int getSlotNext() {
        return config.getInt("gui.slots.next-button", 16);
    }

    public int getSlotTierInfo() {
        return config.getInt("gui.slots.tier-info", 13);
    }

    public int getSlotHelmet() {
        return config.getInt("gui.slots.helmet", 29);
    }

    public int getSlotChestplate() {
        return config.getInt("gui.slots.chestplate", 30);
    }

    public int getSlotLeggings() {
        return config.getInt("gui.slots.leggings", 32);
    }

    public int getSlotBoots() {
        return config.getInt("gui.slots.boots", 33);
    }

    public int getSlotPlayerStats() {
        return config.getInt("gui.slots.player-stats", 49);
    }

    public List<Integer> getAccentSlots() {
        return config.getIntegerList("gui.accent-slots");
    }

    // GUI Materials
    public Material getBorderPaneMaterial() {
        return parseMaterial(config.getString("gui.materials.border-pane", "GRAY_STAINED_GLASS_PANE"), 
                            Material.GRAY_STAINED_GLASS_PANE);
    }

    public Material getAccentPaneMaterial() {
        return parseMaterial(config.getString("gui.materials.accent-pane", "ORANGE_STAINED_GLASS_PANE"), 
                            Material.ORANGE_STAINED_GLASS_PANE);
    }

    public Material getTierInfoMaterial() {
        return parseMaterial(config.getString("gui.materials.tier-info-item", "BOOK"), 
                            Material.BOOK);
    }

    public Material getNavigationArrowMaterial() {
        return parseMaterial(config.getString("gui.materials.navigation-arrow", "ARROW"), 
                            Material.ARROW);
    }

    /**
     * Parses a material name from config, returning a default if invalid.
     */
    private Material parseMaterial(String materialName, Material defaultMaterial) {
        if (materialName == null || materialName.isEmpty()) {
            return defaultMaterial;
        }
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultMaterial;
        }
    }

    // Tier info settings
    public String getTierInfoName() {
        return config.getString("gui.tier-info.name", "{hex_color}⚔ {tier_name} &7(Tier {tier})");
    }

    public List<String> getTierInfoLore() {
        return config.getStringList("gui.tier-info.lore");
    }

    // Navigation settings
    public String getNavigationPreviousName() {
        return config.getString("gui.navigation.previous.name", "&c◀ Previous Tier");
    }

    public List<String> getNavigationPreviousLore() {
        return config.getStringList("gui.navigation.previous.lore");
    }

    public String getNavigationNextName() {
        return config.getString("gui.navigation.next.name", "&a▶ Next Tier");
    }

    public List<String> getNavigationNextLore() {
        return config.getStringList("gui.navigation.next.lore");
    }

    // Player stats settings
    public String getPlayerStatsName() {
        return config.getString("gui.player-stats.name", "{accent}⚡ {player}'s Stats");
    }

    public List<String> getPlayerStatsLore() {
        return config.getStringList("gui.player-stats.lore");
    }

    // Armor piece settings
    public String getArmorPieceNameFormat() {
        return config.getString("gui.armor-piece.name-format", "{hex_color}{tier_name} {piece_name}");
    }

    public List<String> getArmorPieceLore() {
        return config.getStringList("gui.armor-piece.lore");
    }

    // Essence settings
    public String getEssenceName() {
        return config.getString("essence.name", "<gradient:#8B00FF:#FF1493>Armor Essence</gradient>");
    }

    public List<String> getEssenceLore() {
        return config.getStringList("essence.lore");
    }

    // Check if tier exists in config
    public boolean hasTier(int tier) {
        return config.contains("armor-tiers." + tier);
    }

    public int getMaxTier() {
        ConfigurationSection section = config.getConfigurationSection("armor-tiers");
        if (section != null) {
            return section.getKeys(false).stream()
                .filter(key -> key.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(10);
        }
        return 10;
    }
}
