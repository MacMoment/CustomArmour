package me.macmoment.customarmor.data;

import me.macmoment.customarmor.config.ConfigManager;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all registered armor tiers
 * All tier data is loaded from config.yml for full customization
 */
public class ArmorRegistry {
    private final Map<Integer, ArmorTier> tiers = new HashMap<>();
    private final ConfigManager configManager;

    public ArmorRegistry(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void registerAllArmors() {
        // Load all tiers from config (1-10 by default)
        int maxTier = configManager.getMaxTier();
        
        for (int tier = 1; tier <= maxTier; tier++) {
            if (configManager.hasTier(tier)) {
                String name = configManager.getArmorName(tier);
                Color rgbColor = configManager.getArmorRGBColor(tier);
                String hexColor = configManager.getArmorHexColor(tier);
                double multiplier = configManager.getArmorMultiplier(tier);
                int price = configManager.getArmorPrice(tier);
                String headTexture = configManager.getArmorHeadTexture(tier);
                
                registerArmor(tier, name, rgbColor, hexColor, multiplier, price, headTexture);
            }
        }
    }

    private void registerArmor(int tier, String name, Color rgbColor, String hexColor, 
                               double multiplier, int price, String headTexture) {
        // Build lore from armor piece lore config
        List<String> armorPieceLore = configManager.getArmorPieceLore();
        List<String> lore = new ArrayList<>();
        
        for (String line : armorPieceLore) {
            // Replace placeholders but don't add price/click text for stored lore
            // (that's handled separately in the GUI for display items vs actual armor)
            String processedLine = line
                .replace("{accent}", configManager.getAccentColor())
                .replace("{hex_color}", hexColor)
                .replace("{tier_name}", name)
                .replace("{tier}", String.valueOf(tier))
                .replace("{multiplier}", String.valueOf(multiplier))
                .replace("{price}", String.valueOf(price));
            lore.add(processedLine);
        }

        ArmorTier armorTier = new ArmorTier(tier, name, rgbColor, hexColor, multiplier, price, headTexture, lore);
        tiers.put(tier, armorTier);
    }

    public ArmorTier getTier(int tier) {
        return tiers.get(tier);
    }

    public int getMaxTier() {
        return tiers.keySet().stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(10);
    }
    
    public void clearTiers() {
        tiers.clear();
    }
}
