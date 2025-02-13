package de.zitronekuchen.infinityfarming.listeners;

import de.zitronekuchen.infinityfarming.Main;
import de.zitronekuchen.infinityfarming.traits.CokeConverterTrait;
import de.zitronekuchen.infinityfarming.traits.CokeSellerTrait;
import de.zitronekuchen.infinityfarming.traits.WeedSellerTrait;
import de.zitronekuchen.infinityfarming.utils.ItemBuilder;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class NPCListener implements Listener {

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();

        // Weed Seller: Sell weed (POISONOUS_POTATO with matching display name)
        if (event.getNPC().hasTrait(WeedSellerTrait.class)) {
            int money = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null
                        && item.getType() == Material.POISONOUS_POTATO
                        && item.hasItemMeta()
                        && item.getItemMeta().hasDisplayName()
                        && item.getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("WietName"))) {
                    money += Main.getInstance().getConfig().getInt("WeedPrice") * item.getAmount();
                }
            }
            if (money < 1) {
                player.sendMessage(ChatColor.RED + "Je hebt geen wiet bij je.");
            } else {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 50.0F, 1.0F);
                // Remove all matching weed items.
                removeItems(player, Material.POISONOUS_POTATO, Main.getInstance().getConfig().getString("WietName"));
                player.sendMessage(ChatColor.GREEN + "Je hebt al je wiet verkocht voor €" + money + ".");
                Main.getInstance().getEconomy().depositPlayer(player, money);
            }
        }

        // Coke Seller: Sell Coke (SUGAR with matching display name)
        if (event.getNPC().hasTrait(CokeSellerTrait.class)) {
            int money = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null
                        && item.getType() == Material.SUGAR
                        && item.hasItemMeta()
                        && item.getItemMeta().hasDisplayName()
                        && item.getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("CokeName"))) {
                    money += Main.getInstance().getConfig().getInt("CokePrice") * item.getAmount();
                }
            }
            if (money < 1) {
                player.sendMessage(ChatColor.RED + "Je hebt geen coke bij je.");
            } else {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 50.0F, 1.0F);
                removeItems(player, Material.SUGAR, Main.getInstance().getConfig().getString("CokeName"));
                player.sendMessage(ChatColor.GREEN + "Je hebt al je coke verkocht voor €" + money + ".");
                Main.getInstance().getEconomy().depositPlayer(player, money);
            }
        }

        // Coke Converter: Convert leaves (DOUBLE_PLANT with matching display name and data == 3) into Coke
        if (event.getNPC().hasTrait(CokeConverterTrait.class)) {
            int totalLeaves = 0;
            // Count all matching leaves
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null
                        && item.getType() == Material.DOUBLE_PLANT
                        && item.hasItemMeta()
                        && item.getItemMeta().hasDisplayName()
                        && item.getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("CokeLeaveName"))
                        && item.getData().getData() == 3) {
                    totalLeaves += item.getAmount();
                }
            }
            if (totalLeaves < 1) {
                player.sendMessage(ChatColor.RED + "Je hebt geen coke blaadjes bij je.");
                return;
            }
            int conversionPrice = Main.getInstance().getConfig().getInt("CokeConversionPrice");
            int totalCost = conversionPrice * totalLeaves;

            // If a conversion price is set, check if the player has enough money
            if (conversionPrice != 0 && Main.getInstance().getEconomy().getBalance(player) < totalCost) {
                player.sendMessage(ChatColor.RED + "Je hebt niet genoeg geld om te converteren.");
                return;
            }

            // Remove exactly the counted leaves from the player's inventory
            removeMatchingLeaves(player, totalLeaves);
            // Give Coke items (each leaf converts into one Coke)
            giveCoke(player, totalLeaves);

            if (conversionPrice != 0) {
                Main.getInstance().getEconomy().withdrawPlayer(player, totalCost);
                player.sendMessage(ChatColor.GREEN + "Ik heb je bladeren voor " + totalCost + "€ omgezet naar Coke.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Ik heb je bladeren omgezet naar Coke.");
            }
        }
    }

    /**
     * Removes the exact amount of matching leaf items from the player's inventory.
     *
     * @param player         The player.
     * @param amountToRemove The total number of leaves to remove.
     */
    private void removeMatchingLeaves(Player player, int amountToRemove) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (amountToRemove <= 0)
                break;
            if (item == null)
                continue;
            if (item.getType() == Material.DOUBLE_PLANT
                    && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().contains(Main.getInstance().getConfig().getString("CokeLeaveName"))
                    && item.getData().getData() == 3) {
                int stackAmount = item.getAmount();
                if (stackAmount <= amountToRemove) {
                    amountToRemove -= stackAmount;
                    player.getInventory().remove(item);
                } else {
                    item.setAmount(stackAmount - amountToRemove);
                    amountToRemove = 0;
                }
            }
        }
    }

    /**
     * Gives Coke items to the player, splitting them into full stacks (64 per stack) if necessary.
     *
     * @param player The player.
     * @param amount The total number of Coke items to give.
     */
    private void giveCoke(Player player, int amount) {
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 50.0F, 1.0F);
        if (amount > 64) {
            int fullStacks = amount / 64;
            int remainingItems = amount % 64;
            for (int i = 0; i < fullStacks; i++) {
                player.getInventory().addItem(
                        new ItemBuilder(Material.SUGAR)
                                .amount(64)
                                .lore("&5Officieel InfinityMT Item!")
                                .displayname(Main.getInstance().getConfig().getString("CokeName"))
                                .build()
                );
            }
            if (remainingItems > 0) {
                player.getInventory().addItem(
                        new ItemBuilder(Material.SUGAR)
                                .amount(remainingItems)
                                .lore("&5Officieel InfinityMT Item!")
                                .displayname(Main.getInstance().getConfig().getString("CokeName"))
                                .build()
                );
            }
        } else {
            player.getInventory().addItem(
                    new ItemBuilder(Material.SUGAR)
                            .amount(amount)
                            .lore("&5Officieel InfinityMT Item!")
                            .displayname(Main.getInstance().getConfig().getString("CokeName"))
                            .build()
            );
        }
    }

    /**
     * Removes all items of the given material that have a display name containing the specified substring.
     *
     * @param player               The player.
     * @param material             The type of item to remove.
     * @param displayNameSubstring The substring to look for in the display name.
     */
    private void removeItems(Player player, Material material, String displayNameSubstring) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null
                    && item.getType() == material
                    && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().contains(displayNameSubstring)) {
                player.getInventory().remove(item);
            }
        }
    }
}
