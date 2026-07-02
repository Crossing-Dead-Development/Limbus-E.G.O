package me.yisang.limbusego;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface EGOWeapon {
    String getId();
    ItemStack createItem();
    default void give(Player player) { player.getInventory().addItem(createItem()); }
    default void handleMelee(EntityDamageByEntityEvent event, Player attacker) {}
}