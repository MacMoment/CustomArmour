package me.macmoment.customarmor.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.data.ArmorTier;
import me.macmoment.customarmor.utils.ArmorUtils;
import me.macmoment.customarmor.utils.EssenceUtils;
import me.macmoment.customarmor.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Manages the armor shop GUI for browsing and purchasing armor tiers.
 * <p>
 * This class handles the creation and population of a 54-slot (6-row) inventory GUI
 * that displays armor pieces, tier information, player statistics, and navigation controls.
 * </p>
 *
 * <pre>
 * GUI Layout (54 slots - 6 rows):
 * ┌───┬───┬───┬───┬───┬───┬───┬───┬───┐
 * │ ○ │   │   │   │ ○ │   │   │   │ ○ │  Row 1: Border with corner/center accents
 * ├───┼───┼───┼───┼───┼───┼───┼───┼───┤
 * │   │ ◄ │   │   │ 📖│   │   │ ► │   │  Row 2: Navigation and tier info
 * ├───┼───┼───┼───┼───┼───┼───┼───┼───┤
 * │   │   │   │   │   │   │   │   │   │  Row 3: Spacer
 * ├───┼───┼───┼───┼───┼───┼───┼───┼───┤
 * │   │   │ 🪖│ 👕│ ○ │ 👖│ 👟│   │   │  Row 4: Armor pieces with center accent
 * ├───┼───┼───┼───┼───┼───┼───┼───┼───┤
 * │   │   │   │   │   │   │   │   │   │  Row 5: Spacer
 * ├───┼───┼───┼───┼───┼───┼───┼───┼───┤
 * │ ○ │   │   │   │ 👤│   │   │   │ ○ │  Row 6: Player stats with corner accents
 * └───┴───┴───┴───┴───┴───┴───┴───┴───┘
 * </pre>
 */
public class ArmorGUI {

    // ==================== GUI Configuration ====================

    /** Total number of slots in the GUI (6 rows × 9 columns). */
    public static final int GUI_SIZE = 54;

    /** Number of armor pieces in a complete set. */
    public static final int ARMOR_PIECES_PER_SET = 4;

    // ==================== Slot Positions ====================

    /** Slot position for the previous tier navigation arrow. */
    public static final int SLOT_PREV = 10;

    /** Slot position for the next tier navigation arrow. */
    public static final int SLOT_NEXT = 16;

    /** Slot position for the tier information book. */
    public static final int SLOT_TIER_INFO = 13;

    /** Slot position for the helmet armor piece. */
    public static final int SLOT_HELMET = 29;

    /** Slot position for the chestplate armor piece. */
    public static final int SLOT_CHESTPLATE = 30;

    /** Slot position for the leggings armor piece. */
    public static final int SLOT_LEGGINGS = 32;

    /** Slot position for the boots armor piece. */
    public static final int SLOT_BOOTS = 33;

    /** Slot position for the player statistics display. */
    public static final int SLOT_STATS = 49;

    /** Slot positions for orange accent glass panes (corners and key positions). */
    private static final int[] ACCENT_SLOTS = {0, 4, 8, 31, 45, 53};

    // ==================== Public Methods ====================

    /**
     * Opens the armor browser GUI for a player at the specified tier page.
     *
     * @param player the player to show the GUI to
     * @param page   the tier page number to display (1-indexed)
     */
    public static void openGUI(Player player, int page) {
        int maxTier = CustomArmor.getInstance().getArmorRegistry().getMaxTier();
        int normalizedPage = clamp(page, 1, maxTier);

        ArmorTier armorTier = CustomArmor.getInstance().getArmorRegistry().getTier(normalizedPage);
        if (armorTier == null) {
            return;
        }

        Inventory inventory = createBaseInventory(normalizedPage, maxTier);
        populateInventory(inventory, player, armorTier, normalizedPage, maxTier);
        player.openInventory(inventory);
    }

    // ==================== Inventory Creation ====================

    /**
     * Creates the base inventory with border glass panes and accent decorations.
     */
    private static Inventory createBaseInventory(int currentPage, int maxPages) {
        String title = TextUtils.colorize(
            "&8" + TextUtils.fancy("Armor Browser") + " &7(Tier " + currentPage + "/" + maxPages + ")"
        );
        Inventory inventory = Bukkit.createInventory(null, GUI_SIZE, title);

        fillWithBorder(inventory);
        addAccentDecorations(inventory);

        return inventory;
    }

    /**
     * Fills all inventory slots with gray glass pane borders.
     */
    private static void fillWithBorder(Inventory inventory) {
        ItemStack borderPane = createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot = 0; slot < GUI_SIZE; slot++) {
            inventory.setItem(slot, borderPane);
        }
    }

    /**
     * Adds orange accent glass panes at designated positions.
     */
    private static void addAccentDecorations(Inventory inventory) {
        ItemStack accentPane = createGlassPane(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot : ACCENT_SLOTS) {
            inventory.setItem(slot, accentPane);
        }
    }

    /**
     * Populates the inventory with all interactive and informational items.
     */
    private static void populateInventory(Inventory inventory, Player player, ArmorTier tier,
                                          int currentPage, int maxPages) {
        List<String> armorLore = buildArmorPriceLore(tier);

        inventory.setItem(SLOT_TIER_INFO, createTierInfoItem(tier));
        inventory.setItem(SLOT_HELMET, createHelmetItem(tier, armorLore));
        inventory.setItem(SLOT_CHESTPLATE, createLeatherArmorItem(Material.LEATHER_CHESTPLATE, tier, "Chestplate", armorLore));
        inventory.setItem(SLOT_LEGGINGS, createLeatherArmorItem(Material.LEATHER_LEGGINGS, tier, "Leggings", armorLore));
        inventory.setItem(SLOT_BOOTS, createLeatherArmorItem(Material.LEATHER_BOOTS, tier, "Boots", armorLore));
        inventory.setItem(SLOT_STATS, createPlayerStatsItem(player));

        addNavigationItems(inventory, currentPage, maxPages);
    }

    // ==================== Item Creation Methods ====================

    /**
     * Creates a glass pane item with an empty display name (for borders/decorations).
     */
    private static ItemStack createGlassPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            pane.setItemMeta(meta);
        }
        return pane;
    }

    /**
     * Creates the tier information book item displaying tier statistics.
     */
    private static ItemStack createTierInfoItem(ArmorTier tier) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) {
            return book;
        }

        String displayName = tier.getHexColor() + "⚔ " + tier.getName() + " &7(Tier " + tier.getTier() + ")";
        meta.displayName(Component.text(TextUtils.colorize(displayName)));

        double fullSetBonus = tier.getMultiplier() * ARMOR_PIECES_PER_SET;
        List<String> lore = Arrays.asList(
            "",
            TextUtils.colorize("&7This tier provides:"),
            TextUtils.colorize(" " + TextUtils.getColor() + "▸ &f+" + tier.getMultiplier() + "x &7multiplier per piece"),
            TextUtils.colorize(" " + TextUtils.getColor() + "▸ &f" + tier.getPrice() + " &7essence per piece"),
            "",
            TextUtils.colorize("&7Full set bonus: " + TextUtils.getColor() + "+" + String.format("%.2f", fullSetBonus) + "x")
        );
        meta.setLore(lore);

        book.setItemMeta(meta);
        return book;
    }

    /**
     * Creates the custom helmet item using a player head with custom texture.
     */
    private static ItemStack createHelmetItem(ArmorTier tier, List<String> lore) {
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) helmet.getItemMeta();
        if (meta == null) {
            return helmet;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", tier.getHeadTexture()));
        meta.setPlayerProfile(profile);

        String displayName = tier.getHexColor() + tier.getName() + " " + TextUtils.fancy("Helmet");
        meta.displayName(Component.text(TextUtils.colorize(displayName)));
        meta.setLore(lore);

        helmet.setItemMeta(meta);
        return helmet;
    }

    /**
     * Creates a leather armor item (chestplate, leggings, or boots) with tier-specific color.
     */
    private static ItemStack createLeatherArmorItem(Material material, ArmorTier tier,
                                                    String pieceName, List<String> lore) {
        ItemStack armorPiece = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
        if (meta == null) {
            return armorPiece;
        }

        meta.setColor(tier.getRgbColor());

        String displayName = tier.getHexColor() + tier.getName() + " " + TextUtils.fancy(pieceName);
        meta.displayName(Component.text(TextUtils.colorize(displayName)));
        meta.setLore(lore);

        armorPiece.setItemMeta(meta);
        return armorPiece;
    }

    /**
     * Creates the player statistics item showing current armor stats and essence balance.
     */
    private static ItemStack createPlayerStatsItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) {
            return skull;
        }

        meta.setOwningPlayer(player);

        int essence = EssenceUtils.getPlayerEssence(player);
        double multiplier = ArmorUtils.getArmorStats(player);
        int armorCount = ArmorUtils.getArmorAmount(player);

        String displayName = TextUtils.getColor() + "⚡ " + player.getName() + "'s Stats";
        meta.displayName(Component.text(TextUtils.colorize(displayName)));

        List<String> lore = Arrays.asList(
            "",
            TextUtils.colorize("&7Your armor stats:"),
            TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fMultiplier: " + TextUtils.getColor() + String.format("%.2f", multiplier) + "x"),
            TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fArmor Pieces: " + TextUtils.getColor() + armorCount + "/" + ARMOR_PIECES_PER_SET),
            "",
            TextUtils.colorize("&7Your inventory:"),
            TextUtils.colorize(" " + TextUtils.getColor() + "▸ &fArmor Essence: " + TextUtils.getColor() + essence)
        );
        meta.setLore(lore);

        skull.setItemMeta(meta);
        return skull;
    }

    // ==================== Navigation Methods ====================

    /**
     * Adds navigation arrow items for browsing between tier pages.
     */
    private static void addNavigationItems(Inventory inventory, int currentPage, int maxPages) {
        if (currentPage > 1) {
            inventory.setItem(SLOT_PREV, createNavigationArrow(currentPage - 1, false));
        }
        if (currentPage < maxPages) {
            inventory.setItem(SLOT_NEXT, createNavigationArrow(currentPage + 1, true));
        }
    }

    /**
     * Creates a navigation arrow item for moving between tier pages.
     *
     * @param targetPage the page number this arrow navigates to
     * @param isNext     true for next arrow, false for previous arrow
     */
    private static ItemStack createNavigationArrow(int targetPage, boolean isNext) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        if (meta == null) {
            return arrow;
        }

        String displayName = isNext ? "&a▶ Next Tier" : "&c◀ Previous Tier";
        meta.displayName(Component.text(TextUtils.colorize(displayName)));

        ArmorTier targetTier = CustomArmor.getInstance().getArmorRegistry().getTier(targetPage);
        if (targetTier != null) {
            List<String> lore = Arrays.asList(
                "",
                TextUtils.colorize("&7Go to: " + targetTier.getHexColor() + targetTier.getName())
            );
            meta.setLore(lore);
        }

        arrow.setItemMeta(meta);
        return arrow;
    }

    // ==================== Helper Methods ====================

    /**
     * Builds the lore list for armor pieces including tier stats, price, and interaction hint.
     * Uses the tier's base lore and appends purchase information.
     */
    private static List<String> buildArmorPriceLore(ArmorTier tier) {
        List<String> lore = new ArrayList<>();
        
        // Add the tier's base statistics lore
        for (String line : tier.getLore()) {
            lore.add(TextUtils.colorize(line));
        }
        
        // Add price and interaction information
        lore.add("");
        lore.add(TextUtils.colorize("&fPrice: " + TextUtils.getColor() + tier.getPrice() + "x &fArmor Essence"));
        lore.add("");
        lore.add(TextUtils.colorize("&e▶ Click to buy or upgrade!"));
        
        return lore;
    }

    /**
     * Clamps a value between a minimum and maximum bound.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
