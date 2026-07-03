package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class TenacityBolus extends BaseAccessory {
    public TenacityBolus(GiftsModule plugin) {
        super(plugin, "tenacity_bolus", "強韌丸",
                "&#0C440C", "孔家滅門之日。",
                "受擊：獲得守護 2·2");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        applyScaled(victim, StatusEffect.PROTECTION, 2, 2, victim);
    }
}
