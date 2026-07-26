package com.snowgears.shop.hook;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import com.snowgears.shop.Shop;
import com.snowgears.shop.event.PlayerCreateShopEvent;
import com.snowgears.shop.event.PlayerDestroyShopEvent;
import com.snowgears.shop.event.PlayerResizeShopEvent;
import com.snowgears.shop.shop.AbstractShop;
import com.snowgears.shop.event.PlayerInitializeShopEvent;
import com.snowgears.shop.event.PlayerOpenShopEvent;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimChangeEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimTransferEvent;


public class GriefPreventionHookListener implements Listener {

    private GriefPrevention gpPlugin = null;
    private Shop plugin = null;

    public GriefPreventionHookListener() {
        plugin = Shop.getPlugin();
        if (checkPluginEnabled()) {
            Plugin p = Bukkit.getPluginManager().getPlugin("GriefPrevention");
            if (p instanceof GriefPrevention) {
                gpPlugin = (GriefPrevention) p;
            }
        }
    }

    // check for build permissions
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopCreate(PlayerCreateShopEvent e) {
        if (!checkPluginEnabledAndVarSet())
            return;
        AbstractShop created = e.getShop();
        Claim claim = gpPlugin.dataStore.getClaimAt(created.getChestLocation(), false, null);
        if (claim == null) {
            claim = gpPlugin.dataStore.getClaimAt(created.getSignLocation(), false, null);
            if (claim == null) {
                // neither sign nor container is in a claim
                return;
            } else {
                // only the sign is in a claim
                e.setCancelled(true);
                return;
            }
        }
        if (!claim.contains(created.getSignLocation(), false, false)) {
            // only the container is in a claim
            e.setCancelled(true);
            return;
        }
        String denialMsg = claim.checkPermission(e.getPlayer(), ClaimPermission.Build, null).get();
        if (denialMsg == null) {
            // no perms restrictions
            return;
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(denialMsg); // need to double check this isn't duplicate
        }
    }

    // check for build permissions
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopResize(PlayerResizeShopEvent e) {
        // when a block would increase the inventory of a shop (e.g. adding a chest-> double)
        if (!checkPluginEnabledAndVarSet())
            return;
        AbstractShop existing = e.getShop();
        Claim claimAtExpansion = gpPlugin.dataStore.getClaimAt(e.getLocation(), false, null);
        Claim claimAtSign = gpPlugin.dataStore.getClaimAt(existing.getSignLocation(), false, null);
        Claim claimAtContainer = gpPlugin.dataStore.getClaimAt(existing.getChestLocation(), false, null);
        if (claimAtExpansion == null && claimAtSign == null && claimAtContainer == null) {
            // no claims present
            return;
        }
        if ((claimAtExpansion != claimAtSign &&
                claimAtExpansion != claimAtContainer) ||
                claimAtExpansion == null) {
            // differing claims
            e.setCancelled(true);
            return;
        }

        String denialMsg = claimAtExpansion.checkPermission(e.getPlayer(), ClaimPermission.Build, null).get();
        if (denialMsg == null) {
            // no perms restrictions
            return;
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(denialMsg);
        }
    }

    // check for build permissions
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopDestroy(PlayerDestroyShopEvent e) {
        if (!checkPluginEnabledAndVarSet())
            return;
        AbstractShop destroyed = e.getShop();
        Claim claim = gpPlugin.dataStore.getClaimAt(destroyed.getChestLocation(), false, null);
        if (claim == null) {
            claim = gpPlugin.dataStore.getClaimAt(destroyed.getSignLocation(), false, null);
            if (claim == null) {
                // neither sign nor container is in a claim
                if (e.getPlayer().hasPermission("shop.destroy") &&
                    (destroyed.getOwnerUUID().equals(e.getPlayer().getUniqueId()) || 
                        e.getPlayer().hasPermission("shop.destroy.other"))){
                    // changing own shop, or bypassing
                    return;
                }
                e.setCancelled(true);
                return;
            } else {
                // only the sign is in a claim
                e.setCancelled(true);
                return;
            }
        }
        if (!claim.contains(destroyed.getSignLocation(), false, false)) {
            // only the container is in a claim
            e.setCancelled(true);
            return;
        }
        String denialMsg = claim.checkPermission(e.getPlayer(), ClaimPermission.Build, null).get();
        if (denialMsg == null) {
            // no perms restrictions
            return;
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(denialMsg); // need to double check this isn't duplicate
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopInit(PlayerInitializeShopEvent e) {
        // unclear how this is scheduled/what it does in addition to createShop
        if (!checkPluginEnabledAndVarSet())
            return;

        //TODO
    }

    // check for container permissions
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopOpen(PlayerOpenShopEvent e) {
        // fired on container open
        if (!checkPluginEnabledAndVarSet())
            return;
        AbstractShop opened = e.getShop();
        Claim claim = gpPlugin.dataStore.getClaimAt(opened.getChestLocation(), false, null);
        if (claim == null) {
            claim = gpPlugin.dataStore.getClaimAt(opened.getSignLocation(), false, null);
            if (claim == null) {
                // neither sign nor container is in a claim
                if (opened.getOwner().equals(e.getPlayer()) || e.getPlayer().hasPermission("shop.operator")){
                    // opening own shop, or OP
                    return;
                }
                e.setCancelled(true);
                return;
            } else {
                // only the sign is in a claim
                e.setCancelled(true);
                return;
            }
        }
        if (!claim.contains(opened.getSignLocation(), false, false)) {
            // only the container is in a claim
            e.setCancelled(true);
            return;
        }
        String denialMsg = claim.checkPermission(e.getPlayer(), ClaimPermission.Inventory, null).get(); // for some reason ClaimPermission.Container wasn't recognized, Inventory is the deprecated backwards-compatible term
        if (denialMsg == null) {
            // no perms restrictions
            return;
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(denialMsg); // need to double check this isn't duplicate
        }
    }

    private boolean checkPluginEnabledAndVarSet() {
        if (checkPluginEnabled() && this.gpPlugin != null) {
            return true;
        }
        return false;
    }

    private boolean checkPluginEnabled() {
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null &&
                Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            return true;
        }
        return false;
    }

    public void onClaimCreation(ClaimCreatedEvent e){
        Claim newClaim = e.getClaim();
        CommandSender creator = e.getCreator();
        Player p = null;
        if (creator instanceof Player){
            p = (Player) creator;
        }else{
            plugin.getLogger().warning("Non-player attempting to create claim, ignoring potentially contained shops");
            return;
        }

        String result = verifyShopsWithinClaim(newClaim);
        if (result != null){
            e.setCancelled(true);
            p.sendMessage(result);
        }
    }

    /**
     * Ensure shops within a claim are owned by players of appropriate permission
     * 
     * @param Claim the claim to check
     * @return an error message, or null if all shops pass checks
     */

    private @Nullable String verifyShopsWithinClaim(Claim c){
        Player p = null;
        if (!c.isAdminClaim()){
            p = Bukkit.getPlayer(c.getOwnerID());
        }
        Location center = c.getLesserBoundaryCorner().add(
            c.getGreaterBoundaryCorner().subtract(
                c.getLesserBoundaryCorner()).multiply(0.5));
        int chunkRadius = (int) (Math.max(c.getHeight(), c.getWidth())/2.0/16 + 1);

        List<AbstractShop> shopsToCheck = plugin.getShopHandler().getShopsNearLocation(
            center, chunkRadius);

        for (AbstractShop s : shopsToCheck){
            if (c.contains(s.getChestLocation(), true, true) || 
                c.contains(s.getSignLocation(), true, true)){
                if (!c.contains(s.getSignLocation(), true, true) || 
                    !c.contains(s.getChestLocation(), true, true)){
                    // c must contain both the sign and chest of each shop
                    return "New claims must not split shops.";
                }

                // admin shops may only be within admin claims
                if (s.getOwnerUUID().equals(plugin.getShopHandler().getAdminUUID())){
                    if (!c.isAdminClaim()){
                        return "Admin shops must exist in admin claims only.";
                    }
                }

                String canBuild = c.checkPermission(s.getOwnerUUID(), ClaimPermission.Build, null).get();
                if (p != s.getOwner().getPlayer() || 
                    !p.hasPermission("shop.operator") ||
                    canBuild != null){
                    // new claim owner must be the owner of each contained shop, or OP, or have build perms in the claim
                    return "You may not claim other players' shops.";
                }
            }
            // ignore shops not within claim
        }
        // fall through if all contained shops match new claim owner
        return null;
    }

    public void onClaimExpansion(ClaimChangeEvent e){
        Claim newClaim = e.getTo();

        String result = verifyShopsWithinClaim(newClaim);
        if (result !=null){
            e.setCancelled(true);
            if (newClaim.getOwnerID() != null){
                Bukkit.getPlayer(newClaim.getOwnerID()).sendMessage(result);
            } else{
                plugin.getLogger().info("Claim expansion: " + result);
            }
        }

    }

    public void onClaimTransfer(ClaimTransferEvent e){
        Claim c = new Claim(e.getClaim());
        c.ownerID = e.getNewOwner();

        String result = verifyShopsWithinClaim(c);
        if (result !=null){
            e.setCancelled(true);
            UUID oldOwner = e.getClaim().getOwnerID();
            if (oldOwner != null){
                Bukkit.getPlayer(e.getClaim().getOwnerID()).sendMessage(result);
            } else{
                plugin.getLogger().info("Claim transfer: " + result);
            }
        }

    }

    //TODO make sure config is respected - I think the listener setup handles this
    //TODO handle admin claims gracefully (ownerID is null, "an administrator") - done
    //TODO handle admin shops gracefully ("admin" or handler.getAdminUUID()) - done
    //TODO address what happens if improperly-permissioned shops are present in claims when the plugin is updated - need input from boss
}
