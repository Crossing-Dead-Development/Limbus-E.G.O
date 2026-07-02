package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class StrangeGlyphTalisman extends BaseAccessory {
    public StrangeGlyphTalisman(GiftsModule plugin) {
        super(plugin, "strange_glyph_talisman", "異文符咒",
                "&#8E5608", "天究星─沒遮攔。",
                "被動：所有傷害減少 15%");
    }
    @Override public void onDamaged(EntityDamageByEntityEvent event, Player victim) {
        event.setDamage(event.getDamage() * 0.85);
    }
}
