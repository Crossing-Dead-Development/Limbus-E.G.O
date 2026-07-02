package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class ClearMirrorCalmWater extends BaseAccessory {
    public ClearMirrorCalmWater(GiftsModule plugin) {
        super(plugin, "clear_mirror_calm_water", "明鏡止水",
                "&#56BBDB", "人莫鑑於流水，而鑑於止水。",
                "受傷時：20% 機率減少 50% 傷害");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        if (Math.random() < 0.20) {
            event.setDamage(event.getDamage() * 0.5);
        }
    }
}
