package me.macmoment.customarmor;

import me.macmoment.customarmor.commands.ArmorAdminCommand;
import me.macmoment.customarmor.commands.ArmorCommand;
import me.macmoment.customarmor.commands.EssenceCommand;
import me.macmoment.customarmor.config.ConfigManager;
import me.macmoment.customarmor.data.ArmorRegistry;
import me.macmoment.customarmor.listeners.ArmorGUIListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for CustomArmor system
 * Converted from Skript - maintains exact behavior
 */
public class CustomArmor extends JavaPlugin {

    private static CustomArmor instance;
    private ConfigManager configManager;
    private ArmorRegistry armorRegistry;

    @Override
    public void onEnable() {
        instance = this;
        
        // Load configuration
        configManager = new ConfigManager(this);
        
        // Initialize armor registry with all 10 tiers from config
        armorRegistry = new ArmorRegistry(configManager);
        armorRegistry.registerAllArmors();
        
        // Register commands
        getCommand("armoradmin").setExecutor(new ArmorAdminCommand());
        getCommand("armor").setExecutor(new ArmorCommand());
        getCommand("essence").setExecutor(new EssenceCommand());
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ArmorGUIListener(), this);
        
        getLogger().info("CustomArmor has been enabled with " + armorRegistry.getMaxTier() + " armor tiers!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomArmor has been disabled!");
    }

    public static CustomArmor getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArmorRegistry getArmorRegistry() {
        return armorRegistry;
    }
    
    /**
     * Reloads the plugin configuration and armor registry
     */
    public void reloadPlugin() {
        // Reload config
        configManager.reloadConfig();
        
        // Clear and re-register armor tiers
        armorRegistry.clearTiers();
        armorRegistry.registerAllArmors();
        
        getLogger().info("Configuration and armor tiers reloaded!");
    }
}
