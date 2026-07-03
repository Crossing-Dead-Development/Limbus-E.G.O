package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class Harestride extends BaseAccessory {
    public Harestride(GiftsModule plugin) {
        super(plugin, "harestride", "卯足",
                "&#AAD179", "雲解顯現。",
                "被動：速度 II，跳躍提升 I｜速度效果中攻擊：施加破裂 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (attacker.hasPotionEffect(PotionEffectType.SPEED)) {
            applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker);
        }
    }
}
