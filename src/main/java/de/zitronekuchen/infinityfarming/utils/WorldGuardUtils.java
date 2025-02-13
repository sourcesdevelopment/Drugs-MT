package de.zitronekuchen.infinityfarming.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import de.zitronekuchen.infinityfarming.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldGuardUtils {
   public static boolean isMemberPlot(Player p) {
      WorldGuardPlugin WG = WorldGuardPlugin.inst();
      Location location = p.getLocation();
      RegionManager RM = WG.getRegionManager(location.getWorld());
      ApplicableRegionSet set = RM.getApplicableRegions(location);
      return set.getRegions().stream().filter((protectedRegion) -> {
         return protectedRegion.isOwner(WorldGuardPlugin.inst().wrapPlayer(p)) || protectedRegion.isMember(WorldGuardPlugin.inst().wrapPlayer(p));
      }).toArray().length > 0;
   }

   public static boolean isCokeRegion(Player p) {
      WorldGuardPlugin WG = WorldGuardPlugin.inst();
      Location location = p.getLocation();
      RegionManager RM = WG.getRegionManager(location.getWorld());
      ApplicableRegionSet set = RM.getApplicableRegions(location);
      return set.getRegions().stream().filter((protectedRegion) -> {
          return protectedRegion.getFlag(Main.COKE_REGION_FLAG) == StateFlag.State.ALLOW;
      }).toArray().length > 0;
   }
}
