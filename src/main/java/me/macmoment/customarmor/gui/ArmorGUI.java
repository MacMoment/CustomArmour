package me.macmoment.customarmor.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.data.ArmorTier;
import me.macmoment.customarmor.utils.ArmorUtils;
import me.macmoment.customarmor.utils.EssenceUtils;
import me.macmoment.customarmor.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the armor shop GUI
 * Maps to the armorGUI function and GUI creation from Skript
 */
public class ArmorGUI {

    /**
     * Opens the armor GUI for a player at a specific page (tier)
     * Maps to armorGUI function from Skript
     */
    public static void openGUI(Player player, int page) {
        int totalArmorSets = 10;
        
        if (page < 1) page = 1;
        if (page > totalArmorSets) page = totalArmorSets;
        
        int tier = page;
        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(tier);
        
        if (armorTier == null) return;
        
        // Create inventory
        String title = TextUtils.colorize("&8" + TextUtils.fancy("armor browser") + " &7(Page " + page + ")");
        Inventory inv = Bukkit.createInventory(null, 9, title);
        
        // Orange stained glass panes at slots 0 and 7
        ItemStack glass = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text("§7"));
        glass.setItemMeta(glassMeta);
        inv.setItem(0, glass);
        inv.setItem(7, glass);
        
        // Create armor pieces with lore including price
        List<String> priceLore = new ArrayList<>(armorTier.getLore());
        priceLore.add("");
        priceLore.add(TextUtils.colorize("&f" + TextUtils.fancy("price") + ": " + TextUtils.getColor() + armorTier.getPrice() + "x Armor Essence"));
        priceLore.add("");
        priceLore.add(TextUtils.colorize("&7" + TextUtils.fancy("Click to buy or upgrade!")));
        
        // Helmet (slot 2)
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta helmetMeta = (SkullMeta) helmet.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", armorTier.getHeadTexture()));
        helmetMeta.setPlayerProfile(profile);
        helmetMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Helmet"))
        ));
        helmetMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        helmet.setItemMeta(helmetMeta);
        inv.setItem(2, helmet);
        
        // Chestplate (slot 3)
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestMeta.setColor(armorTier.getRgbColor());
        chestMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Chestplate"))
        ));
        chestMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        chestplate.setItemMeta(chestMeta);
        inv.setItem(3, chestplate);
        
        // Leggings (slot 4)
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(armorTier.getRgbColor());
        leggingsMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Leggings"))
        ));
        leggingsMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        leggings.setItemMeta(leggingsMeta);
        inv.setItem(4, leggings);
        
        // Boots (slot 5)
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(armorTier.getRgbColor());
        bootsMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Boots"))
        ));
        bootsMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        boots.setItemMeta(bootsMeta);
        inv.setItem(5, boots);
        
        // Player stats (slot 8)
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        
        String tempName = TextUtils.fancy(player.getName() + "'s stats");
        int playerEssence = EssenceUtils.getPlayerEssence(player);
        double armorStats = ArmorUtils.getArmorStats(player);
        int armorAmount = ArmorUtils.getArmorAmount(player);
        
        skullMeta.displayName(Component.text(
            TextUtils.colorize(TextUtils.getColor() + tempName)
        ));
        
        List<String> statsLore = new ArrayList<>();
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "&l| &f" + TextUtils.fancy("Multiplier:") + " " + TextUtils.getColor() + armorStats + "x"));
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "&l| &f" + TextUtils.fancy("armor pieces:") + " " + TextUtils.getColor() + armorAmount));
        statsLore.add("");
        statsLore.add(TextUtils.colorize(TextUtils.getColor() + TextUtils.fancy("inventory:")));
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "&l| &f" + TextUtils.fancy("Armor Essence:") + " " + TextUtils.getColor() + playerEssence));
        skullMeta.setLore(statsLore);
        
        skull.setItemMeta(skullMeta);
        inv.setItem(8, skull);
        
        // Navigation arrows
        if (page > 1) {
            ItemStack prevArrow = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevArrow.getItemMeta();
            prevMeta.displayName(Component.text(
                TextUtils.colorize("&c" + TextUtils.fancy("Previous Page"))
            ));
            prevArrow.setItemMeta(prevMeta);
            inv.setItem(1, prevArrow);
        }
        
        if (page < totalArmorSets) {
            ItemStack nextArrow = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextArrow.getItemMeta();
            nextMeta.displayName(Component.text(
                TextUtils.colorize("&a" + TextUtils.fancy("Next Page"))
            ));
            nextArrow.setItemMeta(nextMeta);
            inv.setItem(6, nextArrow);
        }
        
        player.openInventory(inv);
    }
}
