package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class DryToTheBoneBreast extends BaseAccessory {
    public DryToTheBoneBreast(GiftsModule plugin) {
        super(plugin, "dry_to_the_bone_breast", "乾巴柴澀雞胸肉",
                "&#D77F00", "雞肉就是雞肉囉。",
                "被動：飽食度不流失；飽足時力量 I｜攻擊破裂中目標：延長破裂 2 層");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.RUPTURE)) {
            status().refresh(target, StatusEffect.RUPTURE, 2);
        }
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 0, true, false));
        if (player.getFoodLevel() >= 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
        } else {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
    }
}
