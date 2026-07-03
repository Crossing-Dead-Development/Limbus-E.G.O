package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
public class CrystallizedBlood extends BaseAccessory {
    public CrystallizedBlood(GiftsModule plugin) {
        super(plugin, "crystallized_blood", "血液結晶",
                "&#FF0000", "無盡的遊行。",
                "被動：每 5 秒消耗全部流血層數回復生命（回復量為層數一半，上限 4，隨升級提升）");
    }
    @Override public void onPassiveTick(Player player) {
        if (!gate(player, 5000)) return;
        if (pot(player, StatusEffect.BLEED) <= 0) return;
        var s = status().get(player);
        int p = s.potency(StatusEffect.BLEED);
        int c = s.consume(StatusEffect.BLEED, Integer.MAX_VALUE);   // 消耗全部
        double m = plugin.getUpgradeMultiplier(player, getId());
        double heal = Math.min(4 * m, p * 0.5);
        player.setHealth(Math.min(player.getAttribute(Attribute.MAX_HEALTH).getValue(), player.getHealth() + heal));
    }
}
