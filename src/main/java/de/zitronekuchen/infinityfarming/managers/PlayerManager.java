package de.zitronekuchen.infinityfarming.managers;

import de.zitronekuchen.infinityfarming.objects.FarmPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;


public class PlayerManager {
   private ArrayList<FarmPlayer> players = new ArrayList();

   public ArrayList<FarmPlayer> getPlayers() {
      return this.players;
   }

   public FarmPlayer getFarmPlayer(UUID uuid) {
      Iterator var2 = this.getPlayers().iterator();

      FarmPlayer player;
      do {
         if (!var2.hasNext()) {
            FarmPlayer farmPlayer = new FarmPlayer(uuid);
            this.getPlayers().add(farmPlayer);
            return farmPlayer;
         }

         player = (FarmPlayer)var2.next();
      } while(!player.getUUID().equals(uuid));

      return player;
   }
}
