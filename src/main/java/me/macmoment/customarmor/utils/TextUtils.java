package me.macmoment.customarmor.utils;

import me.macmoment.customarmor.CustomArmor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

/**
 * Utility class for text formatting
 * All colors and prefix are read from config.yml
 */
public class TextUtils {

    /**
     * Gets the prefix from config
     */
    public static String getPrefix() {
        return colorize(CustomArmor.getInstance().getConfigManager().getPrefix());
    }

    /**
     * Gets the accent color from config
     */
    public static String getColor() {
        return CustomArmor.getInstance().getConfigManager().getAccentColor();
    }

    /**
     * Colorizes a string with & and hex color codes
     */
    public static String colorize(String text) {
        if (text == null) return "";
        
        // Handle hex colors like <##RRGGBB>
        text = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text);
        
        // Handle MiniMessage-style hex colors
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("<##([A-Fa-f0-9]{6})>");
        java.util.regex.Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }

    /**
     * Colorizes a string and returns it as an Adventure Component.
     * Properly handles both legacy color codes (&) and hex colors (<##RRGGBB>).
     * @param text The text to colorize
     * @return The colorized text as a Component
     */
    public static Component colorizeToComponent(String text) {
        if (text == null) return Component.empty();
        
        // First colorize using legacy method to get the properly formatted string
        String colorized = colorize(text);
        
        // Then deserialize the legacy-formatted string to Component
        return LegacyComponentSerializer.legacySection().deserialize(colorized);
    }

    /**
     * Converts MiniMessage format to Component
     */
    public static Component miniMessage(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    /**
     * Strips all color codes from text
     */
    public static String stripColor(String text) {
        return ChatColor.stripColor(colorize(text));
    }
    
    /**
     * Replaces common placeholders in text with actual values
     * @param text The text with placeholders
     * @return The text with {accent} replaced with the accent color from config
     */
    public static String replacePlaceholders(String text) {
        if (text == null) return "";
        return text.replace("{accent}", getColor());
    }
}
