package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
public class SmokingGunpowder extends BaseAccessory {
    public SmokingGunpowder(GiftsModule plugin) {
        super(plugin, "smoking_gunpowder", "有煙火藥",
                "&7觸發（死亡）：對周圍 4 格敵人造成 8 傷害");
    }
    @Override public void onDeath(PlayerDeathEvent event, Player deceased) {
        for (Entity e : deceased.getNearbyEntities(4, 4, 4)) {
            if (e instanceof LivingEntity living && !(e instanceof Player)) {
                living.damage(8.0);
            }
        }
    }
}
