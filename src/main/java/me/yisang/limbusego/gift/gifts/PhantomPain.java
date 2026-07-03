package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class PhantomPain extends BaseAccessory {
    public PhantomPain(GiftsModule plugin) {
        super(plugin, "phantom_pain", "幻痛",
                "&#656565", "他試圖抓住伸去的手，\n但沒能抓住。",
                "攻擊：+15% 傷害");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        double m = plugin.getUpgradeMultiplier(attacker, getId());
        event.setDamage(event.getDamage() * (1.0 + Math.min(0.30, 0.15 * m)));
    }
}
