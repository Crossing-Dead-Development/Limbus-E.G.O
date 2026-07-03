package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
public class StrangeGlyphTalisman extends BaseAccessory {
    public StrangeGlyphTalisman(GiftsModule plugin) {
        super(plugin, "strange_glyph_talisman", "異文符咒",
                "&#8E5608", "天究星─沒遮攔。",
                "擊殺：對 5 格內敵人擴散破裂 3·2");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double m = plugin.getUpgradeMultiplier(killer, getId());
        double r = 5 * m;
        for (var e : event.getEntity().getLocation().getNearbyLivingEntities(r)) {
            if (e.equals(killer) || e instanceof Player) continue;
            apply(e, StatusEffect.RUPTURE, 3, 2, killer);
        }
    }
}
