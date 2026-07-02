package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class BrokenCompass extends BaseAccessory {
    public BrokenCompass(GiftsModule plugin) {
        super(plugin, "broken_compass", "破碎羅盤",
                "&#0772AB", "失去方向的人們的叫喊聲仍不停息。",
                "攻擊時：10% 機率施加反胃 3 秒");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (Math.random() < 0.10 && event.getEntity() instanceof LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, true, true));
        }
    }
}
