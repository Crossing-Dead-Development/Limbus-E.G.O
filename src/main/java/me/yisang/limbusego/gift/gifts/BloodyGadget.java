package me.yisang.limbusego.gift.gifts;
import me.yisang.limbusego.gift.BaseAccessory;
import me.yisang.limbusego.gift.GiftsModule;
import me.yisang.limbusego.status.StatusEffect;
import org.bukkit.entity.Player;
public class BloodyGadget extends BaseAccessory {
    public BloodyGadget(GiftsModule plugin) {
        super(plugin, "bloody_gadget", "鮮血裝飾", "&7被動：每 5 秒獲得強壯 2·2");
    }
    @Override public void onPassiveTick(Player player) {
        if (!gate(player, 5000)) return;
        applyScaled(player, StatusEffect.POWER, 2, 2, player);
    }
}
