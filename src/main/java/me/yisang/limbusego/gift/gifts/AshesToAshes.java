package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import java.util.Objects;
public class AshesToAshes extends BaseAccessory {
    public AshesToAshes(GiftsModule plugin) {
        super(plugin, "ashes_to_ashes", "塵歸塵",
                "&#9A9A9A", "步入迷霧。",
                "擊殺時：恢復 4 HP");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        double max = Objects.requireNonNull(killer.getAttribute(Attribute.MAX_HEALTH)).getValue();
        killer.setHealth(Math.min(killer.getHealth() + 4.0, max));
    }
}
