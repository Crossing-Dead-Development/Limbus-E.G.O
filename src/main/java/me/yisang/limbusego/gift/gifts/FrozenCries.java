package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class FrozenCries extends BaseAccessory {
    public FrozenCries(GiftsModule plugin) {
        super(plugin, "frozen_cries", "冰封的哀號",
                "&#4498DB", "不甘平凡。",
                "攻擊時：施加緩慢 III 2 秒");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (event.getEntity() instanceof LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2, true, true));
        }
    }
}
