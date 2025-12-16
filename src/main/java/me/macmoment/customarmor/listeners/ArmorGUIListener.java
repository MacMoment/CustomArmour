package me.macmoment.customarmor.listeners;

import me.macmoment.customarmor.CustomArmor;
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
 * Maps to the inventory click event from Skript
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
        
        // Handle navigation using constants from ArmorGUI
        if (slot == ArmorGUI.SLOT_PREV) {
            // Previous tier
            ArmorGUI.openGUI(player, page - 1);
        } else if (slot == ArmorGUI.SLOT_NEXT) {
            // Next tier
            ArmorGUI.openGUI(player, page + 1);
        } else if (slot == ArmorGUI.SLOT_HELMET) {
            // Helmet
            handleArmorPurchase(player, page, "head");
        } else if (slot == ArmorGUI.SLOT_CHESTPLATE) {
            // Chestplate
            handleArmorPurchase(player, page, "chestplate");
        } else if (slot == ArmorGUI.SLOT_LEGGINGS) {
            // Leggings
            handleArmorPurchase(player, page, "leggings");
        } else if (slot == ArmorGUI.SLOT_BOOTS) {
            // Boots
            handleArmorPurchase(player, page, "boots");
        }
    }

    /**
     * Handles armor purchase/upgrade logic
     * Maps to handleArmorPurchase function from Skript
     */
    private void handleArmorPurchase(Player player, int tier, String part) {
        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(tier);
        if (armorTier == null) return;
        
        int price = armorTier.getPrice();
        int playerEssence = EssenceUtils.getPlayerEssence(player);
        
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
                String message = CustomArmor.getInstance().getConfigManager().getShopMessage("already-owned");
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
                return;
            }
            
            // Upgrade mode - calculate discount
            int discountPerTier = CustomArmor.getInstance().getConfigManager().getDiscountPerTier();
            int minPrice = CustomArmor.getInstance().getConfigManager().getMinimumPrice();
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
                String message = CustomArmor.getInstance().getConfigManager().getShopMessage("upgraded")
                    .replace("{part}", part)
                    .replace("{tier}", String.valueOf(tier))
                    .replace("{accent}", TextUtils.getColor())
                    .replace("{price}", String.valueOf(finalPrice))
                    .replace("{discount}", String.valueOf(discount));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            } else {
                String message = CustomArmor.getInstance().getConfigManager().getShopMessage("need-essence-upgrade")
                    .replace("{accent}", TextUtils.getColor())
                    .replace("{price}", String.valueOf(finalPrice))
                    .replace("{discount}", String.valueOf(discount));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            }
        } else {
            // Buy new armor
            if (playerEssence >= price) {
                EssenceUtils.removeEssence(player, price);
                ArmorUtils.giveArmor(player, tier, part);
                String message = CustomArmor.getInstance().getConfigManager().getShopMessage("purchased")
                    .replace("{part}", part)
                    .replace("{tier}", String.valueOf(tier))
                    .replace("{accent}", TextUtils.getColor())
                    .replace("{price}", String.valueOf(price));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            } else {
                String message = CustomArmor.getInstance().getConfigManager().getShopMessage("need-essence")
                    .replace("{accent}", TextUtils.getColor())
                    .replace("{price}", String.valueOf(price));
                player.sendMessage(TextUtils.colorize(TextUtils.getPrefix() + " " + message));
            }
        }
        
        // Refresh GUI
        ArmorGUI.openGUI(player, tier);
    }
}
