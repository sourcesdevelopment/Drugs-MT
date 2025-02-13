package de.zitronekuchen.infinityfarming.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.objects.drugs.Coke;
import de.zitronekuchen.infinityfarming.objects.drugs.Weed;
import de.zitronekuchen.infinityfarming.utils.SQLiteUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class PlantManager {
   private final ArrayList<Plant> plants = new ArrayList<>();

   public ArrayList<Plant> getPlants() {
      return this.plants;
   }

   /**
    * Retrieves a Plant by its Block.
    *
    * @param block The block to search for.
    * @return The corresponding Plant, or null if not found.
    */
   public Plant getPlantByBlock(Block block) {
      for (Plant plant : this.getPlants()) {
         if (plant.getLocation().getBlock().equals(block)) {
            return plant;
         }
      }
      return null;
   }

   /**
    * Loads plants from the database into the plugin.
    * Ensures that no duplicate plants are loaded.
    */
   public void load() {
      String sql = "SELECT * FROM plants;";
      ResultSet results = SQLiteUtils.querySync(sql);
      if (results == null) {
         System.err.println("Failed to load plants from the database.");
         return;
      }

      try {
         while (results.next()) {
            String world = results.getString("world");
            int x = results.getInt("posX");
            int y = results.getInt("posY");
            int z = results.getInt("posZ");
            int state = results.getInt("state");

            if (Bukkit.getWorld(world) != null) {
               // Ensure the chunk is loaded
               Bukkit.getWorld(world).loadChunk(x >> 4, z >> 4);
            } else {
               System.err.println("World '" + world + "' does not exist. Skipping plant at (" + x + ", " + y + ", " + z + ").");
               continue;
            }

            // Create the plant object based on block type
            Material blockType = Bukkit.getWorld(world).getBlockAt(x, y, z).getType();
            Plant plant;
            if (blockType == Material.CROPS || blockType == Material.WHEAT) {
               plant = new Weed(world, x, y, z, state);  // Make sure Weed constructor matches
            } else {
               plant = new Coke(world, x, y, z, state);  // Make sure Coke constructor matches
            }

            // Check for duplicate based on position and state
            if (this.plants.contains(plant)) {
               System.out.println("Duplicate plant found at (" + x + ", " + y + ", " + z + ") in world '" + world + "' with state " + state + ". Skipping.");
               continue;
            }

            this.plants.add(plant);
            System.out.println("Loaded plant: " + plant.getClass().getSimpleName() + " at (" + x + ", " + y + ", " + z + ") in world '" + world + "'.");
         }
         results.close();
      } catch (SQLException e) {
         System.err.println("Error processing plant data from the database:");
         e.printStackTrace();
      }
   }


}
