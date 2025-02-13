package de.zitronekuchen.infinityfarming.listeners;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.objects.FarmPlayer;
import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.objects.drugs.Coke;
import de.zitronekuchen.infinityfarming.objects.drugs.Weed;
import de.zitronekuchen.infinityfarming.utils.WorldGuardUtils;
import de.zitronekuchen.infinityfarming.utils.WrappedTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class PlantListener implements Listener {
    @EventHandler
    public void onPlace(final BlockPlaceEvent event) {
        if (!event.getItemInHand().hasItemMeta()
                || !event.getItemInHand().getItemMeta().hasDisplayName())
            return;
         Player player = event.getPlayer();
       final FarmPlayer farmPlayer = Main.getInstance().getPlayerManager().getFarmPlayer(event.getPlayer().getUniqueId());

        /* Weed-Planting-Handler */
       if (event.getItemInHand().getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("WietSeedName"))) {
           event.setCancelled(true);
           if (!WorldGuardUtils.isMemberPlot(event.getPlayer())) {
             player.sendMessage(ChatColor.RED + "Je kan alleen wiet plaatsen op je eigen plot.");
              return;
           }
          if (farmPlayer.isFarming()) {
             player.sendMessage(ChatColor.RED + "Je bent al aan het planten.");
             return;
          }
          if (farmPlayer.isOogsten()) {
             player.sendMessage(ChatColor.RED + "Je bent al aan het oogsten.");
             return;
          }
           handlePlayer(event.getPlayer());
           final double[] time = new double[]{(double) Main.getInstance().getConfig().getInt("PlantingTime")};
           DecimalFormatSymbols symbols = new DecimalFormatSymbols();
           symbols.setDecimalSeparator('.');
           final DecimalFormat decimalFormat = new DecimalFormat("0.0", symbols);
           WrappedTask task = new WrappedTask(Main.getInstance(), 0, 2) {
              public void run() {
                 if (!event.getPlayer().isOnline()) {
                    this.cancelTask();
                    farmPlayer.setFarming(false);
                    return;
                 }
                 time[0] -= 0.1D;
                 if (Double.parseDouble(decimalFormat.format(time[0])) % 2.0D == 0.0D) {
                   player.playSound(event.getBlockAgainst().getLocation(), Sound.BLOCK_GRAVEL_HIT, 20.0F, 1.0F);
                   player.getWorld().spawnParticle(Particle.BLOCK_CRACK, event.getBlockAgainst().getLocation().clone().add(0.0D, 1.1D, 0.0D), 10, 0.2D, 0.0D, 0.2D, new MaterialData(event.getBlockAgainst().getType()));
                 }

                player.sendTitle(ChatColor.GREEN + "Aan het planten..", ChatColor.WHITE + decimalFormat.format(time[0]) + "s", 0, 20, 0);
                 if (time[0] < 0.1D) {
                    this.cancelTask();
                    farmPlayer.setFarming(false);
                    event.getBlockPlaced().setType(Material.CROPS);
                    event.getBlockPlaced().setData((byte) 0);
                   player.playSound(event.getBlockPlaced().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 20.0F, 2.0F);
                    Block block = event.getBlockPlaced();
                    Main.getInstance().getPlantManager().getPlants().add(new Weed(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 0));
                   player.sendMessage(ChatColor.GREEN + "Je hebt een wiet geplant.");
                 }
              }
           };
        }

       /* Coke-Planting-Handler */
       if (event.getItemInHand().getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("CokeSeedName"))) {
          event.setCancelled(true);
          if (!event.getBlockPlaced().getLocation().add(0, -1,0).getBlock().getType().equals(Material.DIRT) || event.getBlockPlaced().getLocation().add(0, -1,0).getBlock().getData() != 2) {
             player.sendMessage(ChatColor.RED + "Je kunt alleen coke kweken op Podzol.");
             return;
          }
          if (!WorldGuardUtils.isCokeRegion(event.getPlayer())) {
            player.sendMessage(ChatColor.RED + "Je kunt het alleen in de juiste Coke-regio plaatsen.");
             return;
          }
          if (farmPlayer.isFarming()) {
             player.sendMessage(ChatColor.RED + "Je bent al aan het planten.");
             return;
          }
          if (farmPlayer.isOogsten()) {
             player.sendMessage(ChatColor.RED + "Je bent al aan het oogsten.");
             return;
          }
          handlePlayer(event.getPlayer());
          final double[] time = new double[]{(double) Main.getInstance().getConfig().getInt("PlantingTime")};
          DecimalFormatSymbols symbols = new DecimalFormatSymbols();
          symbols.setDecimalSeparator('.');
          final DecimalFormat decimalFormat = new DecimalFormat("0.0", symbols);
          WrappedTask task = new WrappedTask(Main.getInstance(), 0, 2) {
             public void run() {
                if (!event.getPlayer().isOnline()) {
                   this.cancelTask();
                   farmPlayer.setFarming(false);
                   return;
                }
                time[0] -= 0.1D;
                if (Double.parseDouble(decimalFormat.format(time[0])) % 2.0D == 0.0D) {
                   player.playSound(event.getBlockAgainst().getLocation(), Sound.BLOCK_GRAVEL_HIT, 20.0F, 1.0F);
                   player.getWorld().spawnParticle(Particle.BLOCK_CRACK, event.getBlockAgainst().getLocation().clone().add(0.5D, 1.1D, 0.5D), 10, 0.2D, 0.0D, 0.2D, new MaterialData(event.getBlockAgainst().getType()));
                }

                player.sendTitle(ChatColor.GREEN + "Aan het planten..", ChatColor.WHITE + decimalFormat.format(time[0]) + "s", 0, 20, 0);
                if (time[0] < 0.1D) {
                   this.cancelTask();
                   farmPlayer.setFarming(false);
                   event.getBlockPlaced().setType(Material.LONG_GRASS);
                   event.getBlockPlaced().setData((byte) 2);
                   player.playSound(event.getBlockPlaced().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 20.0F, 2.0F);
                   Block block = event.getBlockPlaced();
                   Main.getInstance().getPlantManager().getPlants().add(new Coke(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 0));
                   player.sendMessage(ChatColor.GREEN + "Je hebt een coke geplant.");
                }
             }
          };
       }
    }

    private void handlePlayer(Player player) {
       final FarmPlayer farmPlayer = Main.getInstance().getPlayerManager().getFarmPlayer(player.getUniqueId());
       farmPlayer.setFarming(true);
       ItemStack weed = player.getInventory().getItemInMainHand();
       weed.setAmount(weed.getAmount() - 1);
       player.getInventory().setItemInMainHand(weed);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        FarmPlayer farmPlayer = Main.getInstance().getPlayerManager().getFarmPlayer(event.getPlayer().getUniqueId());
        if (farmPlayer.isFarming() && event.getFrom().distance(event.getTo()) > 0.0D) {
            event.setCancelled(true);
        }

    }
}
