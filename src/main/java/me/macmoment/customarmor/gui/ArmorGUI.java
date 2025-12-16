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

    // GUI Layout (54 slots - 6 rows):
    // Row 1 (0-8):   Border with accents
    // Row 2 (9-17):  [border] [prev] [border] [border] [tier info] [border] [border] [next] [border]
    // Row 3 (18-26): [border] [border] [border] [border] [border] [border] [border] [border] [border]
    // Row 4 (27-35): [border] [border] [helmet] [chestplate] [leggings] [boots] [border] [border] [border]
    // Row 5 (36-44): [border] [border] [border] [border] [border] [border] [border] [border] [border]
    // Row 6 (45-53): [border] [border] [border] [border] [stats] [border] [border] [border] [border]
    
    public static final int GUI_SIZE = 54;
    public static final int SLOT_PREV = 10;
    public static final int SLOT_NEXT = 16;
    public static final int SLOT_TIER_INFO = 13;
    public static final int SLOT_HELMET = 29;
    public static final int SLOT_CHESTPLATE = 30;
    public static final int SLOT_LEGGINGS = 32;
    public static final int SLOT_BOOTS = 33;
    public static final int SLOT_STATS = 49;
    public static final int ARMOR_PIECES_PER_SET = 4;

    /**
     * Opens the armor GUI for a player at a specific page (tier)
     * Maps to armorGUI function from Skript
     */
    public static void openGUI(Player player, int page) {
        int totalArmorSets = CustomArmor.getInstance().getArmorRegistry().getMaxTier();
        
        if (page < 1) page = 1;
        if (page > totalArmorSets) page = totalArmorSets;
        
        int tier = page;
        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(tier);
        
        if (armorTier == null) return;
        
        // Create inventory with larger size
        String title = TextUtils.colorize("&8" + TextUtils.fancy("Armor Browser") + " &7(Tier " + page + "/" + totalArmorSets + ")");
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, title);
        
        // Fill border with glass panes
        ItemStack orangeGlass = createGlassPane(Material.ORANGE_STAINED_GLASS_PANE);
        ItemStack grayGlass = createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
        
        // Fill all slots with gray glass first (border)
        for (int i = 0; i < GUI_SIZE; i++) {
            inv.setItem(i, grayGlass);
        }
        
        // Add orange accents in corners and key positions
        inv.setItem(0, orangeGlass);
        inv.setItem(8, orangeGlass);
        inv.setItem(45, orangeGlass);
        inv.setItem(53, orangeGlass);
        inv.setItem(4, orangeGlass);  // Top center accent
        inv.setItem(31, orangeGlass); // Center accent between armor pieces
        
        // Create armor pieces with lore including price
        List<String> priceLore = new ArrayList<>(armorTier.getLore());
        priceLore.add("");
        priceLore.add(TextUtils.colorize("&fPrice: " + TextUtils.getColor() + armorTier.getPrice() + "x &fArmor Essence"));
        priceLore.add("");
        priceLore.add(TextUtils.colorize("&e▶ Click to buy or upgrade!"));
        
        // Tier info item (shows current tier details)
        ItemStack tierInfo = new ItemStack(Material.BOOK);
        ItemMeta tierInfoMeta = tierInfo.getItemMeta();
        tierInfoMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + "⚔ " + armorTier.getName() + " &7(Tier " + tier + ")")
        ));
        List<String> tierLore = new ArrayList<>();
        tierLore.add("");
        tierLore.add(TextUtils.colorize("&7This tier provides:"));
        tierLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "▸ &f+" + armorTier.getMultiplier() + "x &7multiplier per piece"));
        tierLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "▸ &f" + armorTier.getPrice() + " &7essence per piece"));
        tierLore.add("");
        tierLore.add(TextUtils.colorize("&7Full set bonus: " + TextUtils.getColor() + "+" + String.format("%.2f", armorTier.getMultiplier() * ARMOR_PIECES_PER_SET) + "x"));
        tierInfoMeta.setLore(tierLore);
        tierInfo.setItemMeta(tierInfoMeta);
        inv.setItem(SLOT_TIER_INFO, tierInfo);
        
        // Helmet
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
        inv.setItem(SLOT_HELMET, helmet);
        
        // Chestplate
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestMeta.setColor(armorTier.getRgbColor());
        chestMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Chestplate"))
        ));
        chestMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        chestplate.setItemMeta(chestMeta);
        inv.setItem(SLOT_CHESTPLATE, chestplate);
        
        // Leggings
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(armorTier.getRgbColor());
        leggingsMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Leggings"))
        ));
        leggingsMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        leggings.setItemMeta(leggingsMeta);
        inv.setItem(SLOT_LEGGINGS, leggings);
        
        // Boots
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(armorTier.getRgbColor());
        bootsMeta.displayName(Component.text(
            TextUtils.colorize(armorTier.getHexColor() + armorTier.getName() + " " + TextUtils.fancy("Boots"))
        ));
        bootsMeta.setLore(priceLore.stream().map(TextUtils::colorize).collect(Collectors.toList()));
        boots.setItemMeta(bootsMeta);
        inv.setItem(SLOT_BOOTS, boots);
        
        // Player stats
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        
        int playerEssence = EssenceUtils.getPlayerEssence(player);
        double armorStats = ArmorUtils.getArmorStats(player);
        int armorAmount = ArmorUtils.getArmorAmount(player);
        
        skullMeta.displayName(Component.text(
            TextUtils.colorize(TextUtils.getColor() + "⚡ " + player.getName() + "'s Stats")
        ));
        
        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add(TextUtils.colorize("&7Your armor stats:"));
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fMultiplier: " + TextUtils.getColor() + String.format("%.2f", armorStats) + "x"));
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fArmor Pieces: " + TextUtils.getColor() + armorAmount + "/" + ARMOR_PIECES_PER_SET));
        statsLore.add("");
        statsLore.add(TextUtils.colorize("&7Your inventory:"));
        statsLore.add(TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fArmor Essence: " + TextUtils.getColor() + playerEssence));
        skullMeta.setLore(statsLore);
        
        skull.setItemMeta(skullMeta);
        inv.setItem(SLOT_STATS, skull);
        
        // Navigation arrows
        if (page > 1) {
            ItemStack prevArrow = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevArrow.getItemMeta();
            prevMeta.displayName(Component.text(
                TextUtils.colorize("&c◀ Previous Tier")
            ));
            List<String> prevLore = new ArrayList<>();
            prevLore.add("");
            ArmorTier prevTier = CustomArmor.getInstance().getArmorRegistry().getTier(page - 1);
            if (prevTier != null) {
                prevLore.add(TextUtils.colorize("&7Go to: " + prevTier.getHexColor() + prevTier.getName()));
            }
            prevMeta.setLore(prevLore);
            prevArrow.setItemMeta(prevMeta);
            inv.setItem(SLOT_PREV, prevArrow);
        }
        
        if (page < totalArmorSets) {
            ItemStack nextArrow = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextArrow.getItemMeta();
            nextMeta.displayName(Component.text(
                TextUtils.colorize("&a▶ Next Tier")
            ));
            List<String> nextLore = new ArrayList<>();
            nextLore.add("");
            ArmorTier nextTier = CustomArmor.getInstance().getArmorRegistry().getTier(page + 1);
            if (nextTier != null) {
                nextLore.add(TextUtils.colorize("&7Go to: " + nextTier.getHexColor() + nextTier.getName()));
            }
            nextMeta.setLore(nextLore);
            nextArrow.setItemMeta(nextMeta);
            inv.setItem(SLOT_NEXT, nextArrow);
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Creates a glass pane item with no name
     */
    private static ItemStack createGlassPane(Material material) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);
        return glass;
    }
}
