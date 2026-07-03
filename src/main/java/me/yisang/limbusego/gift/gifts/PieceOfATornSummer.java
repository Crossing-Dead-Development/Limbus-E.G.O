package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
public class PieceOfATornSummer extends BaseAccessory {
    public PieceOfATornSummer(GiftsModule plugin) {
        super(plugin, "piece_of_a_torn_summer", "破碎之夏的殘片",
                "&#5FE2C5", "\"試圖了解變化莫測的天氣，在我看來是毫無意義的。\"",
                "受火焰或熔岩傷害時：獲得強壯 2·2");
    }
    @Override public void onAnyDamage(EntityDamageEvent event, Player victim) {
        DamageCause cause = event.getCause();
        if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK
                || cause == DamageCause.HOT_FLOOR || cause == DamageCause.LAVA) {
            applyScaled(victim, StatusEffect.POWER, 2, 2, victim);
        }
    }
}
