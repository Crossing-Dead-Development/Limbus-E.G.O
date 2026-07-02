package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class BlueZippoLighter extends BaseAccessory {
    public BlueZippoLighter(GiftsModule plugin) {
        super(plugin, "blue_zippo_lighter", "藍色Zippo牌打火機",
                "&#44D8DB", "那是翼發放給員工的供給品。",
                "攻擊時：25% 機率點燃目標 3 秒");
    }
    @Override public void onAttack(EntityDamageByEntityEvent event, Player attacker) {
        if (Math.random() < 0.25 && event.getEntity() instanceof LivingEntity target) {
            target.setFireTicks(60);
        }
    }
}
