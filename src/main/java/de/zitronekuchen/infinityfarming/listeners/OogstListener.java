package de.zitronekuchen.infinityfarming.listeners;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.managers.PlantManager;
import de.zitronekuchen.infinityfarming.objects.FarmPlayer;
import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.objects.drugs.Coke;
import de.zitronekuchen.infinityfarming.objects.drugs.Weed;
import de.zitronekuchen.infinityfarming.utils.ItemBuilder;
import de.zitronekuchen.infinityfarming.utils.WorldGuardUtils;
import de.zitronekuchen.infinityfarming.utils.WrappedTask;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class OogstListener implements Listener {
    @EventHandler
    public void onHarvest(final PlayerInteractEvent event) {
        Plant plant = Main.getInstance().getPlantManager().getPlantByBlock(event.getClickedBlock());
        if (plant == null) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        event.setCancelled(true);
        if (!plant.isReady()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Dit wiet plantje is nog niet volledig gegroeid.");
            return;
        }
        if (plant.isFarming()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Iemand is dit plantje al aan het oogsten!");
            return;
        }
        if (plant instanceof Weed && !WorldGuardUtils.isMemberPlot(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Je kan alleen wiet oogsten op je eigen plot.");
            return;
        }
        FarmPlayer farmPlayer = Main.getInstance().getPlayerManager().getFarmPlayer(event.getPlayer().getUniqueId());
        if (farmPlayer.isFarming()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Je bent al aan het planten.");
            return;
        }
        if (farmPlayer.isOogsten()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Je bent al aan het oogsten.");
            return;
        }
        final double[] time = new double[]{(double) Main.getInstance().getConfig().getInt("OogstenTime")};
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat decimalFormat = new DecimalFormat("0.0", symbols);
        farmPlayer.setOogsten(true);
        plant.setFarming(true);
        WrappedTask task = new WrappedTask(Main.getInstance(), 0, 2) {
            public void run() {
                double[] localOogstenTime = time;
                localOogstenTime[0] -= 0.1D;
                if (!event.getPlayer().isOnline()) {
                    this.cancelTask();
                    farmPlayer.setOogsten(false);
                } else {
                    if (Double.parseDouble(decimalFormat.format(time[0])) % 2.0D == 0.0D) {
                        event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_GRAVEL_HIT, 20.0F, 1.0F);
                        event.getPlayer().getWorld().spawnParticle(Particle.BLOCK_CRACK, event.getClickedBlock().getLocation().clone().add(0.5D, 1.1D, 0.5D), 10, 0.2D, 0.0D, 0.2D, new MaterialData(Material.DIRT));
                    }
                    event.getPlayer().sendTitle(ChatColor.GREEN + "Aan het oogsten..", ChatColor.WHITE + decimalFormat.format(time[0]) + "s", 0, 20, 0);
                    if (time[0] < 0.1D) {
                        if (plant instanceof Coke) {
                            event.getClickedBlock().setType(Material.LONG_GRASS);
                            if (event.getClickedBlock().getLocation().add(0,1,0).getBlock().getType().equals(Material.DOUBLE_PLANT)) {
                                event.getClickedBlock().getLocation().clone().add(0,1,0).getBlock().setType(Material.AIR);
                            }
                            event.getClickedBlock().setData((byte) 2);
                            plant.setState(0);
                            plant.growTimer();
                            
                            plant.setFarming(false);
                        } else {
                            event.getClickedBlock().setType(Material.AIR);
                            plant.remove();
                        }
                        ItemStack harvest = plant.getCropItem().clone();
                        if (plant instanceof Coke) {
                            harvest.getData().setData((byte) 3);
                            harvest.setDurability((short) 3);
                        }
                        event.getPlayer().getInventory().addItem(harvest);
                        if (plant instanceof Coke) {
                            event.getPlayer().sendMessage(ChatColor.GREEN + "Je hebt een coke plant geoogst.");
                        } else {
                            event.getPlayer().sendMessage(ChatColor.GREEN + "Je hebt een wiet plant geoogst.");
                        }

                        event.getPlayer().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 20.0F, 2.0F);
                        this.cancelTask();
                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()-> {
                            farmPlayer.setOogsten(false);
                        },20L);
                    }

                }
            }
        };

    }



    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        FarmPlayer farmPlayer = Main.getInstance().getPlayerManager().getFarmPlayer(event.getPlayer().getUniqueId());
        if (farmPlayer.isOogsten() && event.getFrom().distance(event.getTo()) > 0.0D) {
            event.setCancelled(true);
        }

    }
}
