package com.snowgears.shop.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

/**
 * Records and allows players to reproduce their last offline-transations message
 */
public class OfflineTransactionsCache {

    private HashMap<Player, OfflineTransactions> cache = new HashMap<>();

    public OfflineTransactionsCache(){
    }

    public void add(Player player, OfflineTransactions tx){
        cache.put(player, tx);
    }

    public void remove(Player player){
        cache.remove(player);
    }

    public void resendMessage(Player player){
        if(cache.containsKey(player)){
            // from ShopListener:onLogin:344
            List<String> messageList = ShopMessage.getUnformattedMessageList("offline", "summary");
            for (String message : messageList) {
                ShopMessage.sendMessage(message, player, cache.get(player));
            }
        }
        else{
            ShopMessage.sendMessage("showOffline", "error", player, null);
        }
    }
    
}
