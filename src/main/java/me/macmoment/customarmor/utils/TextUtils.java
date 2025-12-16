package me.macmoment.customarmor.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

/**
 * Utility class for text formatting
 * Maps to the fancy() function and color formatting from Skript
 */
public class TextUtils {
    
    private static final String PREFIX = "&8[<##FF9100>&lA<##FFB047>&lR<##FFCE8D>&lM<##FFB047>&lO<##FF9100>&lR&8] &8»&7";
    private static final String COLOR = "<##FFCE8D>";

    /**
     * Converts text to fancy format (just returns the text for 1:1 parity)
     * In the original Skript, fancy() appears to just return the input
     */
    public static String fancy(String text) {
        return text;
    }

    /**
     * Gets the prefix from options
     */
    public static String getPrefix() {
        return colorize(PREFIX);
    }

    /**
     * Gets the color from options
     */
    public static String getColor() {
        return COLOR;
    }

    /**
     * Colorizes a string with & and hex color codes
     */
    public static String colorize(String text) {
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
}
