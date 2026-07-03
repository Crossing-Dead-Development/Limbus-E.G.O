package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
public class EndlessHunger extends BaseAccessory {
    public EndlessHunger(GiftsModule plugin) {
        super(plugin, "endless_hunger", "無盡的飢餓",
                "&7飢餓不虛弱｜飽食度高時攻擊：獲得強壯 2·1");
    }
    @Override public void onPassiveTick(Player player) {
        player.removePotionEffect(PotionEffectType.WEAKNESS);
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (attacker.getFoodLevel() >= 16) {
            applyScaled(attacker, StatusEffect.POWER, 2, 1, attacker);
        }
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION)
            event.setCancelled(true);
    }
}
