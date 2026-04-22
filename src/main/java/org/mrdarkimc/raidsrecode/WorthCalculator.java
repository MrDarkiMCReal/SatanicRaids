package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.Message;
import org.mrdarkimc.SatanicLib.messages.MessageInterface;
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
        Bukkit.getLogger().info("Player left raidworld. Equipment cost on  of " + player.getName() + " is " + sum);
    }

    public String formatPlayerTop(Player player, long sum) {
        String template = "&f             %pFormat_{player}_mini% %design_clr_primary%вынес &f%img_money%%design_clr_money%{money}";
        return template.replace("{player}", player.getName()).replace("{money}", formatBal(sum));
    }

    //todo в worthCalculator (Или в ItemWorth)
    public String formatBal(long sum) {
        String sumStr = String.valueOf(sum);
        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (int i = sumStr.length() - 1; i >= 0; i--) {
            builder.append(sumStr.charAt(i));
            count++;

            if (count % 3 == 0 && i != 0) {
                builder.append(",");
            }
        }
        return builder.reverse().toString();
    }

    public void calculateJoin(Player player) {
        long equipmentCost = calculate(player);
        Bukkit.getLogger().info("Player joined raidworld. Equipment cost on  of " + player.getName() + " is " + equipmentCost);
        onJoinEventBalance.put(player.getUniqueId(), equipmentCost);
    }

    public Map<Player, Long> getTop(int amount) {
        Map<Player, Long> top3 = topPlayersByBalance.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(amount)
                .collect(Collectors.toMap((p) -> Bukkit.getPlayer(p.getKey()), Map.Entry::getValue));
        Set<Player> players = top3.keySet();
        for (Player player : players) {
            Long totalSum = top3.get(player);
            Long initialBalance = onJoinEventBalance.get(player.getUniqueId());
            if (initialBalance != null) {
                Bukkit.getLogger().warning("Initial balance of : " + player.getName() + " is null. Fix your damn plugin buddy. add an handler for these types of bugs");
                Long calculated = totalSum - initialBalance;
                top3.put(player, calculated);
            }


        }
        return top3;
    }

    //todo в worthCalculator (Или в ItemWorth)
    public void broadcastStats() {
        Map<Player, Long> top = this.getTop(3);
        System.out.println("top players: ");
        System.out.println(top);
        int counter = 0;
        List<String> s = new ArrayList<>(List.of(" \n", " \n", " \n"));
        for (Map.Entry<Player, Long> entry : top.entrySet()) {
            if (counter == 2) {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()));
            } else {
                s.set(counter, formatPlayerTop(entry.getKey(), entry.getValue()) + "\n");
            }
            counter++;
        }
        MessageInterface message = new KeyedMessage(null, "messages.event-end", Map.of("{players}", String.join(" ", s)));
        if (top.isEmpty()) {
            message = new KeyedMessage(null, "messages.event-end-empty", null);
        }
        message.broadcast();
    }

    private long calculate(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(PriceChecker::getPriceForSellingItem)
                .sum();
    }
}
