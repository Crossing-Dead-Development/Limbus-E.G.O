package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class ColdIllusion extends BaseAccessory {
    public ColdIllusion(GiftsModule plugin) {
        super(plugin, "cold_illusion", "冰冷的幻想",
                "&#33CF4F", "綻放的九人會。",
                "攻擊時：施加緩慢 II 3 秒");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (event.getEntity() instanceof LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, true));
        }
    }
}
