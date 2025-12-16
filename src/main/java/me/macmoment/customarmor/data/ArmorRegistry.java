package me.macmoment.customarmor.data;

import me.macmoment.customarmor.config.ConfigManager;
import me.macmoment.customarmor.utils.TextUtils;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all registered armor tiers
 * Maps to the on load section and regArmor function from Skript
 * Now loads from config.yml for full customization
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
                String name = TextUtils.fancy(configManager.getArmorName(tier));
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
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(hexColor + TextUtils.fancy("statistics"));
        lore.add(hexColor + "&l┃ &f" + TextUtils.fancy("multi") + ": " + hexColor + multiplier + "x");
        lore.add(hexColor + "&l┃ &f" + TextUtils.fancy("tier") + ": " + hexColor + tier);
        lore.add("");
        lore.add("&8" + TextUtils.fancy("this multi is per armor piece"));

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
