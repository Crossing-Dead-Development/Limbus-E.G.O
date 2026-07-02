package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class IllusoryHunt extends BaseAccessory {
    public IllusoryHunt(GiftsModule plugin) {
        super(plugin, "illusory_hunt", "異想狩獵",
                "&#C6CDEF", "你是特别的。",
                "攻擊時：20% 機率施加失明 3 秒");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (Math.random() < 0.20 && event.getEntity() instanceof LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, true));
        }
    }
}
