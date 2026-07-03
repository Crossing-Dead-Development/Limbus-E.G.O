package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class CQCManual extends BaseAccessory {
    public CQCManual(GiftsModule plugin) {
        super(plugin, "cqc_manual", "近身格鬥手冊", "&7近戰攻擊：獲得呼吸法 2·2");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        applyScaled(attacker, StatusEffect.POISE, 2, 2, attacker);
    }
}
