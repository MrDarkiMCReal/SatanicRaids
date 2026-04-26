package org.mrdarkimc.raidsrecode;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mrdarkimc.SatanicLib.Utils;
import org.mrdarkimc.SatanicLib.currency.interfaces.Currency;
import org.mrdarkimc.SatanicLib.messages.KeyedMessage;
import org.mrdarkimc.SatanicLib.messages.MessageInterface;
import org.mrdarkimc.itemworth.WorthProvider.PriceChecker;

import java.util.*;
import java.util.stream.Collectors;

public class WorthCalculator {
    private Map<UUID, Long> onExitEventBalance = new LinkedHashMap<>();
    private Map<UUID, Long> onJoinEventBalance = new HashMap<>();

    public void calculateExit(Player player) {
        long sum = calculate(player);
        if (onExitEventBalance.containsKey(player.getUniqueId())) {
            Long l = onExitEventBalance.get(player.getUniqueId());
            l = l + sum;
            onExitEventBalance.put(player.getUniqueId(), l);
        } else {
            onExitEventBalance.put(player.getUniqueId(), sum);
        }
        Bukkit.getLogger().info("Player left raidworld. Equipment cost on  of " + player.getName() + " is " + sum);
    }

    public void calculateJoin(Player player) {
        long equipmentCost = calculate(player);
        Bukkit.getLogger().info("Player joined raidworld. Equipment cost on  of " + player.getName() + " is " + equipmentCost);
        onJoinEventBalance.put(player.getUniqueId(), equipmentCost);
    }

    public String formatPlayerTop(OfflinePlayer player, long sum) {
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

    public Map<UUID, Long> getTopBalancePlayers(int amount) {
        return onExitEventBalance.entrySet().stream()
                .map(entry -> {
                    UUID uuid = entry.getKey();
                    long finalBal = entry.getValue();
                    long initialBal = onJoinEventBalance.getOrDefault(uuid, 0L);
                    return Map.entry(uuid, finalBal - initialBal);
                })
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(amount)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public void giveAwards() {
        Map<UUID, Long> topBalancePlayers = getTopBalancePlayers(100);
        Currency hellic = SatanicRaids.getInstance().getHellic();

        int rank = 1;
        for (Map.Entry<UUID, Long> entry : topBalancePlayers.entrySet()) {
            UUID uuid = entry.getKey();
            long raidAmount = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) continue;

            int bonusHellic;
            String placeKey;

            switch (rank) {
                case 1 -> {
                    bonusHellic = 1000;
                    placeKey = "1";
                }
                case 2 -> {
                    bonusHellic = 700;
                    placeKey = "2";
                }
                case 3 -> {
                    bonusHellic = 400;
                    placeKey = "3";
                }
                default -> {
                    bonusHellic = 100;
                    placeKey = "other";
                }
            }

            hellic.addMoney(player, bonusHellic);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{place}", placeKey);
            placeholders.put("{bonus_amount}", Currency.formatPrice(bonusHellic));
            placeholders.put("{raid_amount}", Currency.formatPrice((int) raidAmount));

            new KeyedMessage(player, "messages.event-end-bonus", placeholders).send();
            rank++;
        }
    }

    public void giveAwarsAndBroadcastStats() {
        Map<UUID, Long> top = this.getTopBalancePlayers(3);

        if (top.isEmpty()) {
            new KeyedMessage(null, "messages.event-end-empty", null).broadcast();
            return;
        }
        String emptyLine = Utils.hexAndPAPI("%design_clr_primary%             ------------", null);
        List<String> lines = new ArrayList<>(List.of(emptyLine, emptyLine, emptyLine, emptyLine));

        int index = 0;
        for (Map.Entry<UUID, Long> entry : top.entrySet()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            lines.set(index, formatPlayerTop(op, entry.getValue()));
            index++;
        }

        String playersStatus = String.join("\n", lines);

        MessageInterface message = new KeyedMessage(null, "messages.event-end", Map.of("{players}", playersStatus));
        message.broadcast();
        giveAwards();
    }

    private long calculate(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .mapToInt(PriceChecker::getPriceForSellingItem)
                .sum();
    }
}
