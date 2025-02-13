package de.zitronekuchen.infinityfarming.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class WrappedTask implements Runnable {
   int taskId;

   public WrappedTask(JavaPlugin plugin, int delay, int period) {
      this.taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, (long)delay, (long)period).getTaskId();
   }

   public void cancelTask() {
      Bukkit.getScheduler().cancelTask(this.taskId);
   }
}
