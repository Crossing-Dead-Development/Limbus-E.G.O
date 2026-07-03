package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class FlowerMound extends BaseAccessory {
    public FlowerMound(GiftsModule plugin) {
        super(plugin, "flower_mound", "花塚",
                "&#F1B1B1", "爾今死去儂收葬，未卜儂身何日喪？\n儂今葬花人笑癡，他日葬儂知是誰？",
                "被動：生命再生 I｜擊殺時：對附近敵人施加沉淪 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        for (org.bukkit.entity.Entity entity : event.getEntity().getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                applyScaled(living, StatusEffect.SINKING, 2, 2, killer);
            }
        }
    }
}
