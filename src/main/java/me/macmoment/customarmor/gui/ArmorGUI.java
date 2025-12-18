package me.macmoment.customarmor.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.macmoment.customarmor.CustomArmor;
import me.macmoment.customarmor.config.ConfigManager;
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
import java.util.List;
import java.util.UUID;

/**
 * Manages the armor shop GUI for browsing and purchasing armor tiers.
 * <p>
 * This class handles the creation and population of a configurable inventory GUI
 * that displays armor pieces, tier information, player statistics, and navigation controls.
 * All settings are read from config.yml for full customization.
 * </p>
 *
 * <pre>
 * Default GUI Layout (54 slots - 6 rows):
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

    /** Number of armor pieces in a complete set. */
    public static final int ARMOR_PIECES_PER_SET = 4;

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

        ConfigManager config = CustomArmor.getInstance().getConfigManager();
        Inventory inventory = createBaseInventory(normalizedPage, maxTier, config);
        populateInventory(inventory, player, armorTier, normalizedPage, maxTier, config);
        player.openInventory(inventory);
    }

    // ==================== Inventory Creation ====================

    /**
     * Creates the base inventory with border glass panes and accent decorations.
     */
    private static Inventory createBaseInventory(int currentPage, int maxPages, ConfigManager config) {
        String title = config.getGUITitle()
            .replace("{tier}", String.valueOf(currentPage))
            .replace("{max_tier}", String.valueOf(maxPages));
        
        int guiSize = config.getGUISize();
        // Use Adventure Component for proper color support in modern Paper
        Component titleComponent = TextUtils.colorizeToComponent(title);
        Inventory inventory = Bukkit.createInventory(null, guiSize, titleComponent);

        fillWithBorder(inventory, guiSize, config);
        addAccentDecorations(inventory, config);

        return inventory;
    }

    /**
     * Fills all inventory slots with border glass panes.
     */
    private static void fillWithBorder(Inventory inventory, int guiSize, ConfigManager config) {
        ItemStack borderPane = createGlassPane(config.getBorderPaneMaterial());
        for (int slot = 0; slot < guiSize; slot++) {
            inventory.setItem(slot, borderPane);
        }
    }

    /**
     * Adds accent glass panes at designated positions from config.
     */
    private static void addAccentDecorations(Inventory inventory, ConfigManager config) {
        ItemStack accentPane = createGlassPane(config.getAccentPaneMaterial());
        for (int slot : config.getAccentSlots()) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, accentPane);
            }
        }
    }

    /**
     * Populates the inventory with all interactive and informational items.
     */
    private static void populateInventory(Inventory inventory, Player player, ArmorTier tier,
                                          int currentPage, int maxPages, ConfigManager config) {
        List<Component> armorLore = buildArmorPieceLore(tier, config);

        inventory.setItem(config.getSlotTierInfo(), createTierInfoItem(tier, config));
        inventory.setItem(config.getSlotHelmet(), createHelmetItem(tier, armorLore, config));
        inventory.setItem(config.getSlotChestplate(), createLeatherArmorItem(Material.LEATHER_CHESTPLATE, tier, "Chestplate", armorLore, config));
        inventory.setItem(config.getSlotLeggings(), createLeatherArmorItem(Material.LEATHER_LEGGINGS, tier, "Leggings", armorLore, config));
        inventory.setItem(config.getSlotBoots(), createLeatherArmorItem(Material.LEATHER_BOOTS, tier, "Boots", armorLore, config));
        inventory.setItem(config.getSlotPlayerStats(), createPlayerStatsItem(player, config));

        addNavigationItems(inventory, currentPage, maxPages, config);
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
     * Creates the tier information item displaying tier statistics.
     */
    private static ItemStack createTierInfoItem(ArmorTier tier, ConfigManager config) {
        ItemStack item = new ItemStack(config.getTierInfoMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String accentColor = config.getAccentColor();
        double fullSetBonus = tier.getMultiplier() * ARMOR_PIECES_PER_SET;
        
        String displayName = config.getTierInfoName()
            .replace("{hex_color}", tier.getHexColor())
            .replace("{tier_name}", tier.getName())
            .replace("{tier}", String.valueOf(tier.getTier()));
        meta.displayName(TextUtils.colorizeToComponent(displayName));

        List<Component> lore = new ArrayList<>();
        for (String line : config.getTierInfoLore()) {
            String processedLine = line
                .replace("{accent}", accentColor)
                .replace("{hex_color}", tier.getHexColor())
                .replace("{tier_name}", tier.getName())
                .replace("{tier}", String.valueOf(tier.getTier()))
                .replace("{multiplier}", String.valueOf(tier.getMultiplier()))
                .replace("{price}", String.valueOf(tier.getPrice()))
                .replace("{full_set_bonus}", String.format("%.2f", fullSetBonus));
            lore.add(TextUtils.colorizeToComponent(processedLine));
        }
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates the custom helmet item using a player head with custom texture.
     */
    private static ItemStack createHelmetItem(ArmorTier tier, List<Component> lore, ConfigManager config) {
        ItemStack helmet = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) helmet.getItemMeta();
        if (meta == null) {
            return helmet;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", tier.getHeadTexture()));
        meta.setPlayerProfile(profile);

        String displayName = config.getArmorPieceNameFormat()
            .replace("{hex_color}", tier.getHexColor())
            .replace("{tier_name}", tier.getName())
            .replace("{piece_name}", "Helmet");
        meta.displayName(TextUtils.colorizeToComponent(displayName));
        meta.lore(lore);

        helmet.setItemMeta(meta);
        return helmet;
    }

    /**
     * Creates a leather armor item (chestplate, leggings, or boots) with tier-specific color.
     */
    private static ItemStack createLeatherArmorItem(Material material, ArmorTier tier,
                                                    String pieceName, List<Component> lore, ConfigManager config) {
        ItemStack armorPiece = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
        if (meta == null) {
            return armorPiece;
        }

        meta.setColor(tier.getRgbColor());

        String displayName = config.getArmorPieceNameFormat()
            .replace("{hex_color}", tier.getHexColor())
            .replace("{tier_name}", tier.getName())
            .replace("{piece_name}", pieceName);
        meta.displayName(TextUtils.colorizeToComponent(displayName));
        meta.lore(lore);

        armorPiece.setItemMeta(meta);
        return armorPiece;
    }

    /**
     * Creates the player statistics item showing current armor stats and essence balance.
     */
    private static ItemStack createPlayerStatsItem(Player player, ConfigManager config) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) {
            return skull;
        }

        meta.setOwningPlayer(player);

        int essence = EssenceUtils.getPlayerEssence(player);
        double multiplier = ArmorUtils.getArmorStats(player);
        int armorCount = ArmorUtils.getArmorAmount(player);
        String accentColor = config.getAccentColor();

        String displayName = config.getPlayerStatsName()
            .replace("{accent}", accentColor)
            .replace("{player}", player.getName());
        meta.displayName(TextUtils.colorizeToComponent(displayName));

        List<Component> lore = new ArrayList<>();
        for (String line : config.getPlayerStatsLore()) {
            String processedLine = line
                .replace("{accent}", accentColor)
                .replace("{player}", player.getName())
                .replace("{multiplier}", String.format("%.2f", multiplier))
                .replace("{armor_count}", String.valueOf(armorCount))
                .replace("{essence}", String.valueOf(essence));
            lore.add(TextUtils.colorizeToComponent(processedLine));
        }
        meta.lore(lore);

        skull.setItemMeta(meta);
        return skull;
    }

    // ==================== Navigation Methods ====================

    /**
     * Adds navigation arrow items for browsing between tier pages.
     */
    private static void addNavigationItems(Inventory inventory, int currentPage, int maxPages, ConfigManager config) {
        if (currentPage > 1) {
            inventory.setItem(config.getSlotPrevious(), createNavigationArrow(currentPage - 1, false, config));
        }
        if (currentPage < maxPages) {
            inventory.setItem(config.getSlotNext(), createNavigationArrow(currentPage + 1, true, config));
        }
    }

    /**
     * Creates a navigation arrow item for moving between tier pages.
     *
     * @param targetPage the page number this arrow navigates to
     * @param isNext     true for next arrow, false for previous arrow
     */
    private static ItemStack createNavigationArrow(int targetPage, boolean isNext, ConfigManager config) {
        ItemStack arrow = new ItemStack(config.getNavigationArrowMaterial());
        ItemMeta meta = arrow.getItemMeta();
        if (meta == null) {
            return arrow;
        }

        String displayName = isNext ? config.getNavigationNextName() : config.getNavigationPreviousName();
        meta.displayName(TextUtils.colorizeToComponent(displayName));

        ArmorTier targetTier = CustomArmor.getInstance().getArmorRegistry().getTier(targetPage);
        if (targetTier != null) {
            List<String> loreTemplate = isNext ? config.getNavigationNextLore() : config.getNavigationPreviousLore();
            List<Component> lore = new ArrayList<>();
            for (String line : loreTemplate) {
                String processedLine = line
                    .replace("{target_hex_color}", targetTier.getHexColor())
                    .replace("{target_tier_name}", targetTier.getName())
                    .replace("{target_tier}", String.valueOf(targetPage));
                lore.add(TextUtils.colorizeToComponent(processedLine));
            }
            meta.lore(lore);
        }

        arrow.setItemMeta(meta);
        return arrow;
    }

    // ==================== Helper Methods ====================

    /**
     * Builds the lore list for armor pieces using config settings.
     * Returns Components with italic disabled by default.
     */
    private static List<Component> buildArmorPieceLore(ArmorTier tier, ConfigManager config) {
        List<Component> lore = new ArrayList<>();
        String accentColor = config.getAccentColor();
        
        for (String line : config.getArmorPieceLore()) {
            String processedLine = line
                .replace("{accent}", accentColor)
                .replace("{hex_color}", tier.getHexColor())
                .replace("{tier_name}", tier.getName())
                .replace("{tier}", String.valueOf(tier.getTier()))
                .replace("{multiplier}", String.valueOf(tier.getMultiplier()))
                .replace("{price}", String.valueOf(tier.getPrice()));
            lore.add(TextUtils.colorizeToComponent(processedLine));
        }
        
        return lore;
    }

    /**
     * Clamps a value between a minimum and maximum bound.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
