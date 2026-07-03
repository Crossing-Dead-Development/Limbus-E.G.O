package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class EbonyBrooch extends BaseAccessory {
    public EbonyBrooch(GiftsModule plugin) {
        super(plugin, "ebony_brooch", "黑檀胸針",
                "&#5B1365", "女王的蘋果。",
                "被動：夜視｜攻擊：施加破裂 2·2；15% 機率追加束縛 1·2");
    }
    @Override public void onPassiveTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 30, 0, true, false));
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.RUPTURE, 2, 2, attacker);
        if (Math.random() < 0.15) {
            apply(target, StatusEffect.BIND, 1, 2, attacker);
        }
    }
}
