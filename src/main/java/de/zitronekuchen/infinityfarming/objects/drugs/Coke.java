package de.zitronekuchen.infinityfarming.objects.drugs;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.utils.ItemBuilder;
import de.zitronekuchen.infinityfarming.utils.WrappedTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class Coke extends Plant {
    public Coke(String world, int x, int y, int z, int state) {
        super(world, x, y, z, state);
        super.cropItem = (new ItemBuilder(Material.DOUBLE_PLANT)).amount(1).lore("&5Officieel InfinityMT Item!").displayname(Main.getInstance().getConfig().getString("CokeLeaveName")).build();
    }
    @Override
    public boolean isReady() {
        return this.getState() >= Main.getInstance().getConfig().getInt("CokeState");
    }

    @Override
    public void growTimer() {
        int growing = Main.getInstance().getConfig().getInt("CokeGrowing");
        super.growTask = new WrappedTask(Main.getInstance(), growing * 20, growing * 20) {
            public void run() {
                Coke.this.getLocation().getBlock().setType(Material.LONG_GRASS);
                Coke.this.getLocation().getBlock().setData((byte) 2);
                Coke.this.state++;
                if (Coke.this.isReady()) {
                    Coke.this.getLocation().getBlock().setType(Material.DOUBLE_PLANT);
                    Coke.this.getLocation().getBlock().setData((byte) 3);
                    Location top = Coke.this.getLocation().clone().add(0, 1, 0);
                    top.getBlock().setType(Material.DOUBLE_PLANT);
                    top.getBlock().setData((byte) 8);
                    Coke.this.growTask.cancelTask();
                }

            }
        };
    }
}
