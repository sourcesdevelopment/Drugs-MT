package de.zitronekuchen.infinityfarming.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.zitronekuchen.infinityfarming.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class BlockFadeListener implements Listener {

   private boolean loaded;

   @EventHandler
   public void onBlockFade(BlockFadeEvent event) {
      if (event.getBlock().getType().equals(Material.SOIL)) {
         event.setCancelled(true);
      }
   }


}
