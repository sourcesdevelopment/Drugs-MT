package de.zitronekuchen.infinityfarming.objects;

import java.util.UUID;

public class FarmPlayer {
   private final UUID uuid;
   private boolean farming;
   private boolean oogsten;

   public boolean isFarming() {
      return this.farming;
   }

   public boolean isOogsten() {
      return this.oogsten;
   }

   public void setOogsten(boolean oogsten) {
      this.oogsten = oogsten;
   }

   public void setFarming(boolean farming) {
      this.farming = farming;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public FarmPlayer(UUID uuid) {
      this.uuid = uuid;
   }
}
