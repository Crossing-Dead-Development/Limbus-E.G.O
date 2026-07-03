package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class HandheldMirror extends BaseAccessory {
    public HandheldMirror(GiftsModule plugin) {
        super(plugin, "handheld_mirror", "手鏡", "&7受擊：對攻擊者施加束縛 2·2 與脆弱 1·2");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        if (event.getDamager() instanceof LivingEntity atk) {
            applyScaled(atk, StatusEffect.BIND, 2, 2, victim);
            apply(atk, StatusEffect.FRAGILE, 1, 2, victim);
        }
    }
}
