package me.macmoment.customarmor.utils;

import me.macmoment.customarmor.CustomArmor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Pattern;

/**
 * Utility class for text formatting
 * All colors and prefix are read from config.yml
 */
public class TextUtils {

    /**
     * Pattern to detect MiniMessage formatting tags that should trigger MiniMessage parsing.
     * Matches: <gradient:, <rainbow, <color:
     */
    private static final Pattern MINIMESSAGE_PATTERN = Pattern.compile("<(gradient:|rainbow|color:)");

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
     * Checks if the text contains MiniMessage formatting tags that require MiniMessage parsing.
     * Detects: {@code <gradient:}, {@code <rainbow}, {@code <color:}
     * 
     * @param text The text to check
     * @return true if text contains MiniMessage formatting tags
     */
    public static boolean containsMiniMessageTags(String text) {
        if (text == null) return false;
        return MINIMESSAGE_PATTERN.matcher(text).find();
    }

    /**
     * Smart text parsing method that automatically detects and uses the appropriate parser.
     * If text contains MiniMessage tags ({@code <gradient:}, {@code <rainbow}, {@code <color:}), uses MiniMessage parser.
     * Otherwise, uses legacy color code parsing.
     * Always disables default italic formatting on the returned Component.
     * 
     * @param text The text to parse
     * @return The parsed Component with italic decoration disabled by default
     */
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        
        Component component;
        if (containsMiniMessageTags(text)) {
            component = MiniMessage.miniMessage().deserialize(text);
        } else {
            String colorized = colorize(text);
            component = LegacyComponentSerializer.legacySection().deserialize(colorized);
        }
        
        return removeDefaultItalic(component);
    }

    /**
     * Colorizes a string and returns it as an Adventure Component.
     * Properly handles both legacy color codes (&) and hex colors (<##RRGGBB>).
     * Also auto-detects MiniMessage tags and uses appropriate parser.
     * Always disables default italic formatting on the returned Component.
     * 
     * @param text The text to colorize
     * @return The colorized text as a Component with italic decoration disabled
     */
    public static Component colorizeToComponent(String text) {
        return parse(text);
    }

    /**
     * Converts MiniMessage format to Component.
     * Always disables default italic formatting on the returned Component.
     * 
     * @param text The MiniMessage formatted text
     * @return The parsed Component with italic decoration disabled
     */
    public static Component miniMessage(String text) {
        if (text == null) return Component.empty();
        return removeDefaultItalic(MiniMessage.miniMessage().deserialize(text));
    }

    /**
     * Removes the default italic decoration from a Component.
     * This is useful for item display names and lore which have italic as default in Minecraft.
     * 
     * @param component The component to modify
     * @return The component with italic decoration set to false (disabled)
     */
    public static Component removeDefaultItalic(Component component) {
        if (component == null) return Component.empty();
        return component.decoration(TextDecoration.ITALIC, false);
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
