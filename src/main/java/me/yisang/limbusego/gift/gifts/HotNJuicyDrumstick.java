package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class HotNJuicyDrumstick extends BaseAccessory {
    public HotNJuicyDrumstick(GiftsModule plugin) {
        super(plugin, "hot_n_juicy_drumstick", "火熱多汁枇杷腿",
                "&#D77F00", "雞肉就是雞肉囉。",
                "被動：飽食度不流失｜攻擊燒傷中目標：延長燒傷持續 2 層");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.BURN)) {
            status().refresh(target, StatusEffect.BURN, 2);
        }
    }
}
