package de.zitronekuchen.infinityfarming.objects;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.utils.SQLiteUtils;
import de.zitronekuchen.infinityfarming.utils.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class Plant {
   protected boolean invalid;
   protected boolean isFarming;
   protected int state;
   protected WrappedTask growTask;
   protected Location location;
   protected ItemStack seedItem;
   protected ItemStack cropItem;
   protected Material cropType;

   public Plant(String world, int x, int y, int z, int state) {
      World tempWorld = Bukkit.getWorld(world);
      if (tempWorld == null) {
         this.invalid = true;
      } else {
         this.location = new Location(tempWorld, (double) x, (double) y, (double) z);
         this.state = state;
         if (!this.isReady()) {
            this.growTimer();
         }
      }
   }

   public int getState() {
      return this.state;
   }

   public boolean isFarming() {
      return this.isFarming;
   }

   public boolean isInvalid() {
      return this.invalid;
   }

   public Location getLocation() {
      return this.location;
   }

   public boolean isReady() {
      return false;
   }

   public void setFarming(boolean farming) {
      this.isFarming = farming;
   }

   public ItemStack getCropItem() {
      return cropItem;
   }

   public ItemStack getSeedItem() {
      return seedItem;
   }

   public void setState(int state) {
      this.state = state;
   }

   public void growTimer() {
      return;
   }

   /**
    * Removes the plant from the database and the PlantManager.
    */
   public void remove() {
      boolean success = SQLiteUtils.updateSync(
              "DELETE FROM plants WHERE world=? AND posX=? AND posY=? AND posZ=?",
              this.getLocation().getWorld().getName(),
              this.getLocation().getBlockX(),
              this.getLocation().getBlockY(),
              this.getLocation().getBlockZ()
      );
      if (success) {
         Main.getInstance().getPlantManager().getPlants().remove(this);
         System.out.println("Plant removed successfully at " + this.getLocation());
      } else {
         System.err.println("Failed to remove plant at " + this.getLocation());
      }
   }

   /**
    * Saves the plant to the database.
    */
   public void save() {
      if (!this.isInvalid()) {
         try {
            if (this.growTask != null) {
               this.growTask.cancelTask();
               this.growTask = null;
            }

            // Insert the plant if it doesn't exist
            boolean insertSuccess = SQLiteUtils.updateSync(
                    "INSERT OR IGNORE INTO plants (world, posX, posY, posZ, state) VALUES (?, ?, ?, ?, ?)",
                    this.getLocation().getWorld().getName(),
                    this.getLocation().getBlockX(),
                    this.getLocation().getBlockY(),
                    this.getLocation().getBlockZ(),
                    this.state
            );

            if (insertSuccess) {
               // Update the plant's state
               boolean updateSuccess = SQLiteUtils.updateSync(
                       "UPDATE plants SET state = ? WHERE world = ? AND posX = ? AND posY = ? AND posZ = ?",
                       this.state,
                       this.getLocation().getWorld().getName(),
                       this.getLocation().getBlockX(),
                       this.getLocation().getBlockY(),
                       this.getLocation().getBlockZ()
               );

               if (!updateSuccess) {
                  System.err.println("Failed to update plant state at " + this.getLocation());
               }
            } else {
               System.err.println("Failed to insert plant at " + this.getLocation());
            }

         } catch (Exception e) {
            System.err.println("Error saving plant data to database:");
            e.printStackTrace();
         }
      }
   }

   /**
    * Overrides equals to compare plants based on their location.
    *
    * @param o The object to compare.
    * @return True if locations are the same, false otherwise.
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Plant plant = (Plant) o;

      // Use the location object to compare world and position
      return Objects.equals(location, plant.location) && state == plant.state;
   }

   @Override
   public int hashCode() {
      // Hashcode based on the location and state
      return Objects.hash(location, state);
   }



}
