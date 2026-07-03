package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class PieceOfRelationship extends BaseAccessory {
    public PieceOfRelationship(GiftsModule plugin) {
        super(plugin, "piece_of_relationship", "緣分殘片",
                "&#444444", "三生緣分，三千世界，三世因緣。",
                "被動：吸引經驗球並使隊友再生｜擊殺時：經驗 +50%");
    }
    @Override public void onPassiveTick(Player player) {
        double m = plugin.getUpgradeMultiplier(player, getId());
        int radius = (int) Math.round(16 * m);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof ExperienceOrb) {
                entity.teleport(player.getLocation());
            }
        }
        for (Player nearby : player.getLocation().getNearbyPlayers(5.0)) {
            if (!nearby.equals(player)) {
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 0, true, false));
            }
        }
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        event.setDroppedExp((int) (event.getDroppedExp() * 1.5));
    }
}
