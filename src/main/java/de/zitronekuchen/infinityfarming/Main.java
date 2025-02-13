package de.zitronekuchen.infinityfarming;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.zitronekuchen.infinityfarming.listeners.BlockFadeListener;
import de.zitronekuchen.infinityfarming.listeners.NPCListener;
import de.zitronekuchen.infinityfarming.listeners.OogstListener;
import de.zitronekuchen.infinityfarming.listeners.PlantListener;
import de.zitronekuchen.infinityfarming.managers.PlantManager;
import de.zitronekuchen.infinityfarming.managers.PlayerManager;
import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.traits.CokeConverterTrait;
import de.zitronekuchen.infinityfarming.traits.CokeSellerTrait;
import de.zitronekuchen.infinityfarming.traits.WeedSellerTrait;
import de.zitronekuchen.infinityfarming.utils.SQLiteUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public final class Main extends JavaPlugin {
    private static Main instance;
    private PlantManager plantManager;
    private PlayerManager playerManager;
    private Economy econ = null;
    public static final StateFlag COKE_REGION_FLAG = new StateFlag("coke-region", false);

    public static Main getInstance() {
        return instance;
    }

    public PlantManager getPlantManager() {
        return this.plantManager;
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public Economy getEconomy() {
        return this.econ;
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                this.econ = rsp.getProvider();
                return this.econ != null;
            }
        }
    }

    @Override
    public void onLoad() {
        // Register custom WorldGuard flags here
        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        if (wg != null) {
            try {
                wg.getFlagRegistry().register(COKE_REGION_FLAG);
                getLogger().info("COKE_REGION_FLAG registered successfully.");
            } catch (Exception e) {
                getLogger().severe("Could not register COKE_REGION_FLAG: " + e.getMessage());
                // It's essential to disable the plugin if critical flags can't be registered
                this.getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            getLogger().severe("WorldGuard not found. Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabling...");
        instance = this;

        // Setup Economy
        if (!this.setupEconomy()) {
            getLogger().severe("Vault or Economy plugin not found. Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save default config
        this.saveDefaultConfig();

        // Initialize managers
        this.plantManager = new PlantManager();
        this.playerManager = new PlayerManager();

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new BlockFadeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlantListener(), this);
        Bukkit.getPluginManager().registerEvents(new NPCListener(), this);
        Bukkit.getPluginManager().registerEvents(new OogstListener(), this);

        // Register Citizens traits
        if (CitizensAPI.getTraitFactory().getTrait("Dealer") == null) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(WeedSellerTrait.class).withName("Dealer"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CokeSellerTrait.class).withName("Professor"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CokeConverterTrait.class).withName("CokeConverter"));
        }

        // Create data folder if it doesn't exist
        this.getDataFolder().mkdirs();

        // Initialize SQLite connection
        try {
            SQLiteUtils.initialize();
            getLogger().info("SQLite connection initialized.");
        } catch (SQLException | ClassNotFoundException e) {
            getLogger().severe("Failed to initialize SQLite connection: " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Synchronously create the plants table with UNIQUE constraint
        try (PreparedStatement statement = SQLiteUtils.getConnection().prepareStatement(
                "CREATE TABLE IF NOT EXISTS plants(" +
                        "world VARCHAR NOT NULL, " +
                        "posX INT NOT NULL, " +
                        "posY INT NOT NULL, " +
                        "posZ INT NOT NULL, " +
                        "state INT NOT NULL, " +
                        "UNIQUE(world, posX, posY, posZ)" +
                        ");")) {
            statement.execute();
            getLogger().info("Plants table created or already exists with UNIQUE constraint.");
        } catch (SQLException e) {
            getLogger().severe("Failed to create plants table: " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load plants from the database
        this.getPlantManager().load();
        getLogger().info("Plugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabling...");
        getLogger().info("Saving plants...");

        for (Plant plant : this.getPlantManager().getPlants()) {
            try {
                plant.save();
                getLogger().info("Successfully saved plant at " + plant.getLocation());
            } catch (Exception e) {
                getLogger().warning("Failed to save plant at " + plant.getLocation() + ": " + e.getMessage());
            }
        }

        getLogger().info("Closing SQLite connection...");
        SQLiteUtils.closeConnection();
        getLogger().info("All plants saved and database connections closed.");
        getLogger().info("Plugin disabled successfully.");
    }
}
