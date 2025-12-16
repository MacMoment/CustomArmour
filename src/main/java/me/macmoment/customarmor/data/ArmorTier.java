package me.macmoment.customarmor.data;

import org.bukkit.Color;

import java.util.List;

/**
 * Represents a tier of armor with all its properties
 * Maps to the regArmor function from Skript
 */
public class ArmorTier {
    private final int tier;
    private final String name;
    private final Color rgbColor;
    private final String hexColor;
    private final double multiplier;
    private final int price;
    private final String headTexture;
    private final List<String> lore;

    public ArmorTier(int tier, String name, Color rgbColor, String hexColor, 
                     double multiplier, int price, String headTexture, List<String> lore) {
        this.tier = tier;
        this.name = name;
        this.rgbColor = rgbColor;
        this.hexColor = hexColor;
        this.multiplier = multiplier;
        this.price = price;
        this.headTexture = headTexture;
        this.lore = lore;
    }

    public int getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }

    public Color getRgbColor() {
        return rgbColor;
    }

    public String getHexColor() {
        return hexColor;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getPrice() {
        return price;
    }

    public String getHeadTexture() {
        return headTexture;
    }

    public List<String> getLore() {
        return lore;
    }
}
