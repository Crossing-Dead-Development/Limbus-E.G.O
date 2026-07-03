package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class MoonInTheWater extends BaseAccessory {
    public MoonInTheWater(GiftsModule plugin) {
        super(plugin, "moon_in_the_water", "水中月",
                "&#8EC5F0", "枉凝眉。",
                "被動：夜視｜攻擊破裂≥3目標：獲得呼吸法 2·1");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (pot(target, StatusEffect.RUPTURE) >= 3) {
            applyScaled(attacker, StatusEffect.POISE, 2, 1, attacker);
        }
    }
}
