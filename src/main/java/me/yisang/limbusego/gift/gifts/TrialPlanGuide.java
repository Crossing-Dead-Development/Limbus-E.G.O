package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
public class TrialPlanGuide extends BaseAccessory {
    public TrialPlanGuide(GiftsModule plugin) {
        super(plugin, "trial_plan_guide", "試用規劃指南", "&7擊殺時：額外獲得 50% 經驗值");
    }
    @Override public void onKill(EntityDeathEvent event, Player killer) {
        killer.giveExp((int)(event.getDroppedExp() * 0.5));
    }
}
