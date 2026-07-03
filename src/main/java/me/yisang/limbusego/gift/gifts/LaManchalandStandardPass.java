package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class LaManchalandStandardPass extends BaseAccessory {
    public LaManchalandStandardPass(GiftsModule plugin) {
        super(plugin, "la_manchaland_standard_pass", "拉．曼查樂園常規通行券",
                "&#FCD05C", "偉大的收尾人追求夢想的故事。",
                "被動：速度 I｜攻擊流血中目標：獲得呼吸法 1·1");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        if (has(target, StatusEffect.BLEED)) {
            applyScaled(attacker, StatusEffect.POISE, 1, 1, attacker);
        }
    }
}
