package me.yisang.limbusego;

import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;
import me.yisang.limbusego.status.StatusState;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * W公司匕首 — 電流承載體。
 * 每次命中對自己累積 CHARGE，攻擊力隨層數溫和成長。
 * 停手 5 秒（count 用完）後充能歸零，符合「短促連擊 → 電流放出」的節奏。
 */
public class WCorpKnife implements EGOWeapon, Listener {
    private static final int CHARGE_POTENCY_CAP = 10; // +30% 攻擊為上限
    private static final int CHARGE_PER_HIT = 1;
    private static final int CHARGE_COUNT_PER_HIT = 5;

    private final LimbusEGO plugin;

    public WCorpKnife(LimbusEGO plugin) { this.plugin = plugin; }

    @Override
    public String getId() { return "w_corp_knife"; }

    @Override
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.msg("weapon.w_corp_knife.name"));
            meta.setLore(List.of(plugin.msg("weapon.w_corp_knife.lore")));
            meta.setCustomModelData(1011);
            meta.setUnbreakable(true);
            meta.setItemModel(NamespacedKey.fromString("w_corp_knife:w_corp_knife"));
            // 匕首手感：低基礎傷、快攻速
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(new NamespacedKey(plugin, "wcorp_dmg"),
                            4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(new NamespacedKey(plugin, "wcorp_spd"),
                            -1.6, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(
                    plugin.getItemIdKey(), PersistentDataType.STRING, "w_corp_knife");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void handleMelee(EntityDamageByEntityEvent event, Player attacker) {
        StatusManager sm = plugin.getStatusManager();
        if (sm == null) return;
        StatusState s = sm.get(attacker);
        int cur = s == null ? 0 : s.potency(StatusEffect.CHARGE);
        // 基礎：potency 達上限就只 refresh 續 count（維持滿充能）
        if (cur < CHARGE_POTENCY_CAP) {
            sm.apply(attacker, StatusEffect.CHARGE, CHARGE_PER_HIT, CHARGE_COUNT_PER_HIT, attacker);
        } else {
            sm.refresh(attacker, StatusEffect.CHARGE, CHARGE_COUNT_PER_HIT);
        }
        // 過載：20% 機率額外 +1 potency +1 count（層數與級數同步再上一階）
        if (Math.random() < 0.20) {
            sm.apply(attacker, StatusEffect.CHARGE, 1, 1, attacker);
            attacker.sendActionBar(plugin.msg("msg.wcorp.overload"));
            attacker.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK,
                    attacker.getLocation().add(0, 1.2, 0), 8, 0.2, 0.3, 0.2, 0.05);
        }
    }
}
