package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
public class PieceOfCrumbledEgg extends BaseAccessory {
    public PieceOfCrumbledEgg(GiftsModule plugin) {
        super(plugin, "piece_of_crumbled_egg", "破碎之卵的殘片",
                "&#33CF4F", "綻放的九人會。",
                "死亡時：對殺手落雷並施加震顫 5·3");
    }
    @Override public void onDeath(PlayerDeathEvent event, Player deceased) {
        Player killer = deceased.getKiller();
        if (killer != null) {
            deceased.getWorld().strikeLightning(killer.getLocation());
            applyScaled(killer, StatusEffect.TREMOR, 5, 3, deceased);
        }
    }
}
