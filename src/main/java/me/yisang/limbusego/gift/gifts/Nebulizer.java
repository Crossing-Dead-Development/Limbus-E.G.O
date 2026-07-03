package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
public class Nebulizer extends BaseAccessory {
    public Nebulizer(GiftsModule plugin) {
        super(plugin, "nebulizer", "霧化吸入器",
                "&7被動：每 5 秒自身與 5 格內玩家獲得呼吸法 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POISE, 2, 2, player);
        for (Player p2 : player.getLocation().getNearbyPlayers(5.0)) {
            if (p2.equals(player)) continue;
            applyScaled(p2, StatusEffect.POISE, 2, 2, player);
        }
    }
}
