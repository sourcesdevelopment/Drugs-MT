package de.zitronekuchen.infinityfarming.objects.drugs;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.objects.Plant;
import de.zitronekuchen.infinityfarming.utils.ItemBuilder;
import de.zitronekuchen.infinityfarming.utils.WrappedTask;
import org.bukkit.Material;
import org.bukkit.material.Crops;

public class Weed extends Plant {

    public Weed(String world, int x, int y, int z, int state) {
        super(world, x, y, z, state);
        super.cropItem = (new ItemBuilder(Material.POISONOUS_POTATO)).amount(1).lore("&5Officieel InfinityMT Item!").displayname(Main.getInstance().getConfig().getString("WietName")).build();
    }

    public boolean isReady() {
        return this.getState() >= Main.getInstance().getConfig().getInt("WeedState");
    }

    @Override
    public void growTimer() {
        int growing = Main.getInstance().getConfig().getInt("WeedGrowing");
        super.growTask = new WrappedTask(Main.getInstance(), growing * 20, growing * 20) {
            public void run() {
                Weed.this.getLocation().getBlock().setType(Material.CROPS);
                Weed.this.getLocation().getBlock().setData((byte)Weed.this.state);
                Weed.this.state++;
                if (Weed.this.isReady()) {
                    Weed.this.getLocation().getBlock().setType(Material.CROPS);
                    Weed.this.getLocation().getBlock().setData((byte)7);
                    Weed.this.growTask.cancelTask();
                }

            }
        };
    }
}
