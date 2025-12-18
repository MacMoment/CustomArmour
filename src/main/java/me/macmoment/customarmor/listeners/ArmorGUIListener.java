package me.macmoment.customarmor.listeners;

import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.config.ConfigManager;
import me.macmoment.customarmor.data.ArmorTier;
import me.macmoment.customarmor.gui.ArmorGUI;
import me.macmoment.customarmor.utils.ArmorUtils;
import me.macmoment.customarmor.utils.EssenceUtils;
import me.macmoment.customarmor.utils.TextUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles clicks in the armor GUI
 * All slot positions are read from config for full customization
 */
public class ArmorGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        
        // Check if this is our armor browser GUI
        if (!title.contains("Armor Browser")) return;
        
        event.setCancelled(true);
        
        // Extract tier number from title (format: "Armor Browser (Tier X/Y)")
        Pattern pattern = Pattern.compile("Tier (\\d+)");
        Matcher matcher = pattern.matcher(title);
        
        if (!matcher.find()) return;
        
        int page = Integer.parseInt(matcher.group(1));
        int slot = event.getRawSlot();
        
        // Get slot positions from config
        ConfigManager config = CustomArmor.getInstance().getConfigManager();
        
        // Handle navigation and armor slots using config values
        if (slot == config.getSlotPrevious()) {
            // Previous tier
            ArmorGUI.openGUI(player, page - 1);
        } else if (slot == config.getSlotNext()) {
            // Next tier
            ArmorGUI.openGUI(player, page + 1);
        } else if (slot == config.getSlotHelmet()) {
            // Helmet
            handleArmorPurchase(player, page, "head");
        } else if (slot == config.getSlotChestplate()) {
            // Chestplate
            handleArmorPurchase(player, page, "chestplate");
        } else if (slot == config.getSlotLeggings()) {
            // Leggings
            handleArmorPurchase(player, page, "leggings");
        } else if (slot == config.getSlotBoots()) {
            // Boots
            handleArmorPurchase(player, page, "boots");
        }
    }

    /**
     * Handles armor purchase/upgrade logic
     * Uses config for all messages and colors
     */
    private void handleArmorPurchase(Player player, int tier, String part) {
        ConfigManager config = CustomArmor.getInstance().getConfigManager();
        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(tier);
        if (armorTier == null) return;
        
        int price = armorTier.getPrice();
        int playerEssence = EssenceUtils.getPlayerEssence(player);
        String accentColor = config.getAccentColor();
        
        // Check if player has armor piece equipped
        boolean hasArmor = false;
        int currentTier = 0;
        
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && ArmorUtils.isArmorPiece(armor)) {
                String armorType = TextUtils.stripColor(
                    PlainTextComponentSerializer.plainText().serialize(armor.displayName())
                );
                
                boolean matches = false;
                if (part.equals("head") && armorType.contains("Helmet")) matches = true;
                if (part.equals("chestplate") && armorType.contains("Chestplate")) matches = true;
                if (part.equals("leggings") && armorType.contains("Leggings")) matches = true;
                if (part.equals("boots") && armorType.contains("Boots")) matches = true;
                
                if (matches) {
                    hasArmor = true;
                    currentTier = ArmorUtils.getArmorTier(armor);
                    break;
                }
            }
        }
        
        // If player has armor, check if upgrade
        if (hasArmor) {
            if (tier <= currentTier) {
                String message = config.getShopMessage("already-owned");
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
                return;
            }
            
            // Upgrade mode - calculate discount
            int discountPerTier = config.getDiscountPerTier();
            int minPrice = config.getMinimumPrice();
            int discount = currentTier * discountPerTier;
            int finalPrice = price - discount;
            if (finalPrice < minPrice) finalPrice = minPrice;
            
            if (playerEssence >= finalPrice) {
                EssenceUtils.removeEssence(player, finalPrice);
                
                // Remove old armor piece
                ItemStack[] armorContents = player.getInventory().getArmorContents();
                for (int i = 0; i < armorContents.length; i++) {
                    ItemStack armor = armorContents[i];
                    if (armor != null && ArmorUtils.isArmorPiece(armor)) {
                        String armorType = TextUtils.stripColor(
                            PlainTextComponentSerializer.plainText().serialize(armor.displayName())
                        );
                        
                        boolean shouldRemove = false;
                        if (part.equals("head") && armorType.contains("Helmet")) shouldRemove = true;
                        if (part.equals("chestplate") && armorType.contains("Chestplate")) shouldRemove = true;
                        if (part.equals("leggings") && armorType.contains("Leggings")) shouldRemove = true;
                        if (part.equals("boots") && armorType.contains("Boots")) shouldRemove = true;
                        
                        if (shouldRemove) {
                            armorContents[i] = null;
                            player.getInventory().setArmorContents(armorContents);
                            break;
                        }
                    }
                }
                
                ArmorUtils.giveArmor(player, tier, part);
                String message = config.getShopMessage("upgraded")
                    .replace("{part}", part)
                    .replace("{tier}", String.valueOf(tier))
                    .replace("{accent}", accentColor)
                    .replace("{price}", String.valueOf(finalPrice))
                    .replace("{discount}", String.valueOf(discount));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            } else {
                String message = config.getShopMessage("need-essence-upgrade")
                    .replace("{accent}", accentColor)
                    .replace("{price}", String.valueOf(finalPrice))
                    .replace("{discount}", String.valueOf(discount));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            }
        } else {
            // Buy new armor
            if (playerEssence >= price) {
                EssenceUtils.removeEssence(player, price);
                ArmorUtils.giveArmor(player, tier, part);
                String message = config.getShopMessage("purchased")
                    .replace("{part}", part)
                    .replace("{tier}", String.valueOf(tier))
                    .replace("{accent}", accentColor)
                    .replace("{price}", String.valueOf(price));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            } else {
                String message = config.getShopMessage("need-essence")
                    .replace("{accent}", accentColor)
                    .replace("{price}", String.valueOf(price));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            }
        }
        
        // Refresh GUI
        ArmorGUI.openGUI(player, tier);
    }
}
