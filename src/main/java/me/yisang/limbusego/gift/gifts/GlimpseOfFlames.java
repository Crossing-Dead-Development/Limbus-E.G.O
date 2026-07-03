package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class GlimpseOfFlames extends BaseAccessory {
    public GlimpseOfFlames(GiftsModule plugin) {
        super(plugin, "glimpse_of_flames", "炎鱗",
                "&7攻擊燒傷中目標：引爆燒傷造成真傷並施加易損 1·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        var s = status().get(target);
        if (s == null || s.potency(StatusEffect.BURN) <= 0) return;
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        if (!gate(attacker, (long)(8000 / m))) return;
        int p = s.potency(StatusEffect.BURN);
        int consumed = s.consume(StatusEffect.BURN, 5);           // 引爆上限 c5
        if (consumed <= 0) return;
        status().hurtTrue(target, attacker, p * consumed * 0.5, StatusEffect.BURN);
        apply(target, StatusEffect.FRAGILE, 1, 2, attacker);
    }
}
