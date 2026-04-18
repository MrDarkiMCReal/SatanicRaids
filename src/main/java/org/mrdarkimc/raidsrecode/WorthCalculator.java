package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mrdarkimc.itemworth.WorthProvider.PriceChecker;

import java.util.*;
import java.util.stream.Collectors;

public class WorthCalculator {
    private Map<UUID, Long> topPlayersByBalance = new LinkedHashMap<>();
    private Map<UUID, Long> onJoinEventBalance = new HashMap<>();

    public void calculateExit(Player player) {
        long sum = calculate(player);
        if (topPlayersByBalance.containsKey(player.getUniqueId())) {
            Long l = topPlayersByBalance.get(player.getUniqueId());
            l = l + sum;
            topPlayersByBalance.put(player.getUniqueId(), l);
        } else {
            topPlayersByBalance.put(player.getUniqueId(), sum);
        }
    }

    public void calculateJoin(Player player) {
        long equipmentCost = calculate(player);
        onJoinEventBalance.put(player.getUniqueId(),equipmentCost);
    }
    public Map<Player, Long> getTop(int amount){
        Map<Player, Long> top3 = topPlayersByBalance.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(amount)
                .collect(Collectors.toMap((p) -> Bukkit.getPlayer(p.getKey()), Map.Entry::getValue));
        Set<Player> players = top3.keySet();
        for (Player player : players) {
            Long totalSum = top3.get(player);
            long calculated = totalSum-onJoinEventBalance.get(player.getUniqueId());
            top3.put(player,calculated);
        }
        return top3;
    }

    private long calculate(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(PriceChecker::getPriceForSellingItem)
                .sum();
    }
}
