package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class PieceOfATornSummer extends BaseAccessory {
    public PieceOfATornSummer(GiftsModule plugin) {
        super(plugin, "piece_of_a_torn_summer", "破碎之夏的殘片",
                "&#5FE2C5", "\"試圖了解變化莫測的天氣，在我看來是毫無意義的。\"",
                "觸發（著火/熔岩傷害）：獲得力量 I 5 秒");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        DamageCause cause = event.getCause();
        if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK
                || cause == DamageCause.HOT_FLOOR || cause == DamageCause.LAVA) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, true, true));
        }
    }
}
