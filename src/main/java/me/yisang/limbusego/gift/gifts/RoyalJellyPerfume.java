package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class RoyalJellyPerfume extends BaseAccessory {
    public RoyalJellyPerfume(GiftsModule plugin) {
        super(plugin, "royal_jelly_perfume", "蜂王漿香水",
                "&7被動：附近蜜蜂不攻擊｜受擊：對燒傷中的攻擊者 -15% 傷害｜受擊：對攻擊者施加燒傷 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        player.getNearbyEntities(8, 8, 8).forEach(e -> {
            if (e instanceof Bee bee && bee.getTarget() != null && bee.getTarget().equals(player))
                bee.setTarget(null);
        });
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        if (event.getDamager() instanceof LivingEntity atk) {
            if (has(atk, StatusEffect.BURN)) {
                event.setDamage(event.getDamage() * 0.85);
            }
            applyScaled(atk, StatusEffect.BURN, 2, 2, victim);
        }
    }
}
