package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class NixieDivergence extends BaseAccessory {
    public NixieDivergence(GiftsModule plugin) {
        super(plugin, "nixie_divergence", "輝光變動儀", "&7攻擊：施加震顫 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        LivingEntity target = victimOf(event);
        if (target == null) return;
        applyScaled(target, StatusEffect.TREMOR, 2, 2, attacker);
    }
}
