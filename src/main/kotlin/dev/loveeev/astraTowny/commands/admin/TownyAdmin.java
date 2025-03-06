package dev.loveeev.astraTowny.commands.admin;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.utils.BukkitUtils;
import dev.loveeev.astratowny.utils.TownyUtil;
import dev.loveeev.astratowny.utils.map.BorderUtil;
import dev.loveeev.astratowny.utils.map.MapHud;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.timers.NewDayEvent;
import dev.loveeev.astratowny.objects.townblocks.HomeBlock;
import dev.loveeev.astratowny.config.ConfigYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.events.nation.NationCreateEvent;
import dev.loveeev.astratowny.events.nation.NationDeleteEvent;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.events.town.TownCreateEvent;
import dev.loveeev.astratowny.events.town.TownDeleteEvent;
import dev.loveeev.astratowny.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TownyAdmin implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Messages.messageconsole());
            return true;
        }
        if(args.length < 1){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return true;
        }
        switch (args[0]){
            case "nation" -> nation(args,player);
            case "newday" -> newday(player);
            case "reload" -> reload(args,player);
            case "resetbanks" -> resetbanks(args,player);
            case "town" -> town(args,player);
            case "map" -> MapHud.toggleMapHud(player);
            case "fill" -> fill(args,player);
            case "load" -> load(args,player);
        }
        return false;
    }


    public void fill(String[] args, Player player){
        Town town = TownManager.getInstance().getTown(player);

        if (town == null) {
            ChatUtil.sendSuccessNotification(player, Messages.townexist(player));
            return;
        }
        final List<WorldCoord> selection = getTownClaimSelectionOrThrow(player, args, town);
        for (WorldCoord coord : selection) {
            TownBlocks townBlocks = new TownBlocks(coord.getX(),coord.getZ(),town,coord.getBukkitWorld());
            TownManager.getInstance().addTownBlock(townBlocks);
            town.addClaimedChunk(townBlocks);
        }


        ChatUtil.sendSuccessNotification(player, "Все внутренние блоки территории города были успешно заполнены!");
    }

    private void load(String[] args, Player player){
        for (Town town : TownManager.getInstance().getTowns().values()){
            Rank ranktown = TownManager.getInstance().getRank(ConfigYML.defaultPerm,town);
            Rank rankMayor = TownManager.getInstance().getRank(ConfigYML.mayorname,town);
            Bukkit.getLogger().info(town.getName() + " " + ranktown + " " + rankMayor);
            if(ranktown == null){
                TownyUtil.createRank(ConfigYML.defaultPerm,town,UUID.randomUUID());
            }
            if(rankMayor == null){
                TownyUtil.createRank(ConfigYML.mayorname,town,UUID.randomUUID());
            }
        }
        for (Nation nation : TownManager.getInstance().getNations().values()){
            Rank rank = TownManager.getInstance().getRank(ConfigYML.defaultnationname,nation);
            Rank rankNation = TownManager.getInstance().getRank(ConfigYML.kingname,nation);
            Bukkit.getLogger().info(nation.getName() + " " + rank + " " + rankNation);
            if(rank == null){
                TownyUtil.createRank(ConfigYML.defaultnationname,nation,UUID.randomUUID());
            }
            if(rankNation == null){
                TownyUtil.createRank(ConfigYML.kingname,nation,UUID.randomUUID());
            }
        }
    }

    private static List<WorldCoord> getTownClaimSelectionOrThrow(Player player, String[] split, Town town) {
        List<WorldCoord> selection;
        WorldCoord playerWorldCoord = WorldCoord.parseWorldCoord(player);
        final World world = playerWorldCoord.getBukkitWorld();

        if (world == null) {
            ChatUtil.sendSuccessNotification(player, Messages.error(player));
            return new ArrayList<>();
        }

        final BorderUtil.FloodfillResult result = BorderUtil.getFloodFillableCoords(town, playerWorldCoord);

        selection = new ArrayList<>(result.coords());
        return selection;
    }






    public void nation(String @NotNull [] args, Player player){
        if(args.length < 2){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return;
        }
        if(args[1].equalsIgnoreCase("new") || args[1].equalsIgnoreCase("create")){
            if(args.length < 3){
                ChatUtil.sendSuccessNotification(player,Messages.args(player));
                return;
            }
            TownyUtil.createNation(args[2],UUID.randomUUID(),null,null);
        } else if(TownManager.getInstance().getNation(args[1]) != null){
            if(args.length < 4){
                ChatUtil.sendSuccessNotification(player,Messages.args(player));
                return;
            }
            switch (args[2]){
                case "set" -> {
                    if(args.length < 5){
                        ChatUtil.sendSuccessNotification(player,Messages.args(player));
                        return;
                    }
                    switch (args[3]){
                        case "capital" ->{
                            Nation nation = TownManager.getInstance().getNation(args[1]);
                            if(nation == null){
                                ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                                return;
                            }
                            Town town = TownManager.getInstance().getTown(args[4]);
                            if(town == null){
                                ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                                return;
                            }
                            if(town.getNation() != nation){
                                ChatUtil.sendSuccessNotification(player,Messages.alrnation(player));
                                return;
                            }
                            nation.setCapital(town);
                        }
                        case "king" -> {
                            Nation nation = TownManager.getInstance().getNation(args[1]);
                            if(nation == null){
                                ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                                return;
                            }
                            Resident resident = TownManager.getInstance().getResident(args[4]);
                            if(resident == null){
                                ChatUtil.sendSuccessNotification(player,Messages.playererror(player));
                                return;
                            }
                            if(resident.getNation() != nation){
                                ChatUtil.sendSuccessNotification(player,Messages.playernotthisnation(player));
                                return;
                            }
                            if(nation.getKing() != null){
                                TownyUtil.removeResidentInNation(resident);
                                TownyUtil.addResidentInNation(resident,nation);
                            }
                        }
                        case "mapcolor" -> {
                            Nation nation = TownManager.getInstance().getNation(args[1]);
                            if(nation == null){
                                ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                                return;
                            }
                            nation.setMapColor(args[4]);

                            ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                        }
                        case "fullmapcolor" ->{
                            Nation nation = TownManager.getInstance().getNation(args[1]);
                            if(nation == null){
                                ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                                return;
                            }

                            for (Town town : nation.getTowns()){
                                town.setMapColor(args[4]);
                            }
                            ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                        }
                        case "name" ->{
                            Nation nation = TownManager.getInstance().getNation(args[1]);
                            if(nation == null){
                                ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                                return;
                            }
                            nation.setName(args[4]);
                        }
                    }
                }
                case "add" -> {
                    Nation nation = TownManager.getInstance().getNation(args[1]);
                    if(nation == null){
                        ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                        return;
                    }
                    Town town = TownManager.getInstance().getTown(args[3]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                        return;
                    }
                    if(town.getNation() != null){
                        ChatUtil.sendSuccessNotification(player,Messages.townhasnation(player));
                        return;
                    }

                    TownyUtil.addTownInNation(town,nation);
                    ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                }
                case "delete" -> {
                    Nation nation = TownManager.getInstance().getNation(args[1]);
                    if(nation == null){
                        ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                        return;
                    }
                    TownyUtil.deleteNation(nation);
                    ChatUtil.sendSuccessNotification(player,Messages.nationdel(player));
                }
                case "deposit" ->{
                    ChatUtil.sendSuccessNotification(player,Messages.error(player));
                }
                case "withdraw" -> {
                    ChatUtil.sendSuccessNotification(player,Messages.error(player));
                }
                case "kick" -> {
                    Nation nation = TownManager.getInstance().getNation(args[1]);
                    if(nation == null){
                        ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
                        return;
                    }
                    Town town = TownManager.getInstance().getTown(args[3]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                        return;
                    }
                    if(nation.getCapital() != town) {
                        TownyUtil.removeTownInNation(town);
                    }
                    ChatUtil.sendSuccessNotification(player,Messages.kicktownSuccess(player));
                }
                case "ranksettings" -> ChatUtil.sendSuccessNotification(player,Messages.error(player));

                case "toggle" -> ChatUtil.sendSuccessNotification(player,Messages.error(player));
            }

        }else {
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
        }
    }
    public void newday(Player player){
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        LocalDateTime now = LocalDateTime.now(zoneId);
        int year = now.getYear();
        int month = now.getMonthValue();
        int dayOfMonth = now.getDayOfMonth();
        String dayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

        BukkitUtils.fireEvent(new NewDayEvent(dayOfWeek, year, month, dayOfMonth));
        ChatUtil.sendSuccessNotification(player,"New day");
    }
    public void resetbanks(String[] args,Player player){

    }
    public void town(String[] args,Player player){
        if(args.length < 2){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return;
        }
        if(args[1].equalsIgnoreCase("new") || args[1].equalsIgnoreCase("create")){
            TownyUtil.createTown(args[2],UUID.randomUUID(),null,null,null);
        } else if(TownManager.getInstance().getTown(args[1]) != null){
            if(args.length < 3){
                ChatUtil.sendSuccessNotification(player,Messages.args(player));
                return;
            }
            switch (args[2]){
                case "set" -> {
                    switch (args[3]){
                        case "mayor" -> {
                            Town town = TownManager.getInstance().getTown(args[1]);
                            if(town == null){
                                ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                                return;
                            }
                            Resident resident = TownManager.getInstance().getResident(args[4]);
                            if(resident == null){
                                ChatUtil.sendSuccessNotification(player,Messages.playererror(player));
                                return;
                            }
                            if(resident.getTown() != town){
                                ChatUtil.sendSuccessNotification(player,Messages.playernottown(player));
                                return;
                            }
                            if(resident.getTown().getMayor() != null) {
                                TownyUtil.removeResidentInTown(resident);
                                TownyUtil.addResidentInTown(resident,town);
                            }
                            TownyUtil.setMayor(resident);
                            ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                        }
                        case "mapcolor" -> {
                            Town town = TownManager.getInstance().getTown(args[1]);
                            if(town == null){
                                ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                                return;
                            }
                            town.setMapColor(args[4]);
                            ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                        }
                        case "name" ->{
                            Town town = TownManager.getInstance().getTown(args[1]);
                            if(town == null){
                                ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                                return;
                            }
                            town.setName(args[4]);
                        }
                        case "homeblock" -> {
                            Town town = TownManager.getInstance().getTown(args[1]);
                            Chunk chunk = player.getChunk();
                            if (town.getTownBlocks().keySet().stream().noneMatch(coord -> coord.getX() == chunk.getX() && coord.getZ() == chunk.getZ())) {
                                ChatUtil.sendSuccessNotification(player, Messages.chunkerror(player));
                                return;
                            }

                            if(TownManager.getInstance().getTownByChunk(chunk) == null){
                                ChatUtil.sendSuccessNotification(player,Messages.chunkerror(player));
                                return;
                            }
                            town.setHomeblock(new HomeBlock(chunk.getX(),chunk.getZ(),chunk.getWorld()));
                        }
                        case "spawn" -> {
                            Town town = TownManager.getInstance().getTown(args[1]);
                            Chunk chunk = player.getChunk();
                            if (town.getTownBlocks().keySet().stream().noneMatch(coord -> coord.getX() == chunk.getX() && coord.getZ() == chunk.getZ())) {
                                ChatUtil.sendSuccessNotification(player, Messages.chunkerror(player));
                                return;
                            }
                            if(TownManager.getInstance().getTownByChunk(chunk) == null){
                                ChatUtil.sendSuccessNotification(player,Messages.chunkerror(player));
                                return;
                            }
                            town.setSpawnLocation(player.getLocation());
                        }
                    }
                }
                case "add" -> {
                    Town town = TownManager.getInstance().getTown(args[1]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                        return;
                    }
                    Resident resident = TownManager.getInstance().getResident(args[3]);
                    if(resident == null){
                        ChatUtil.sendSuccessNotification(player,Messages.playererror(player));
                        return;
                    }
                    if(resident.getTown() == town){
                        ChatUtil.sendSuccessNotification(player,Messages.alrinvitetown(player));
                        return;
                    }
                    if(resident.getTown() != null){
                        ChatUtil.sendSuccessNotification(player,Messages.townalrexist(player));
                        return;
                    }
                    TownyUtil.addResidentInTown(resident,town);
                    if(town.hasNation()){
                        TownyUtil.addResidentInNation(resident,town.getNation());
                    }
                    ChatUtil.sendSuccessNotification(player,Messages.suc(player));
                }
                case "delete" -> {
                    Town town = TownManager.getInstance().getTown(args[1]);
                    TownyUtil.deleteTown(town);
                }
                case "deposit" ->{
                    ChatUtil.sendSuccessNotification(player,Messages.error(player));
                }
                case "withdraw" -> {
                    ChatUtil.sendSuccessNotification(player,Messages.error(player));
                }
                case "forcemerge" -> {
                    if(args.length < 4){
                        ChatUtil.sendSuccessNotification(player, Messages.args(player));
                        return;
                    }

                    // Получаем первый город
                    Town town = TownManager.getInstance().getTown(args[1]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player, Messages.townexist(player));
                        return;
                    }

                    // Получаем второй город для слияния
                    Town addTown = TownManager.getInstance().getTown(args[3]);
                    if(addTown == null){
                        ChatUtil.sendSuccessNotification(player, Messages.townexist(player));
                        return;
                    }

                    // Синхронизация для предотвращения ошибок с потоками
                    synchronized (town) {
                        synchronized (addTown) {
                            // Очистка мэра второго города
                            if(addTown.getMayor() != null){
                                addTown.getMayor().clear();
                            }

                            // Перенос всех резидентов из второго города в первый
                            for (Resident resident : addTown.getResidents()) {
                                TownyUtil.removeResidentInTown(resident);
                                TownyUtil.addResidentInTown(resident, town);
                            }

                            // Перенос всех TownBlocks из второго города в первый
                            for (TownBlocks tb : addTown.getTownBlocks().values()) {
                                tb.setTown(town);
                                town.addClaimedChunk(tb);
                            }

                            // Очистка TownBlocks второго города
                            addTown.getTownBlocks().clear();
                        }
                    }
                    TownyUtil.deleteTown(addTown);
                    // Уведомление об успешном слиянии
                    ChatUtil.sendSuccessNotification(player, Messages.suc(player));
                }

                case "kick" -> {
                    Town town = TownManager.getInstance().getTown(args[1]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                        return;
                    }
                    Resident resident = TownManager.getInstance().getResident(args[3]);
                    if(resident == null){
                        ChatUtil.sendSuccessNotification(player, Messages.playererror(player));
                        return;
                    }
                    if(resident.isMayor()){
                        ChatUtil.sendSuccessNotification(player,Messages.notkickmayor(player));
                        return;
                    }
                    if(resident.hasTown()) {
                        TownyUtil.removeResidentInTown(resident);
                        ChatUtil.sendSuccessNotification(player, Messages.kicksuc(player));
                    }else {
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                    }
                }
                case "ranksettings" -> ChatUtil.sendSuccessNotification(player,Messages.error(player));
                case "spawn" ->{
                    Town town = TownManager.getInstance().getTown(args[1]);
                    if(town == null){
                        ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
                        return;
                    }
                    player.teleport(town.getSpawnLocation());
                }
                case "toggle" -> {
                    ChatUtil.sendSuccessNotification(player,Messages.error(player));
                }
            }

        }else {
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
        }
    }

    public void reload (String[] args,Player player){
        Core.getInstance().reloadConfig();
        TranslateYML.getCachedConfigurations().clear();
        new TranslateYML();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Resident resident = TownManager.getInstance().getResident(player);

        if (args.length == 1) {
            List<String> commands = new ArrayList<>(List.of("nation","newday","reload","resetbanks","town","map","fill"));
            return Core.getPartialMatches(args[0], commands);
        }

        if (args.length == 2) {
            if(args[0].equalsIgnoreCase("town")){
                List<String> towns = new ArrayList<>(TownManager.getInstance().getTownNames());
                towns.add("create");
                towns.add("new");
                return Core.getPartialMatches(args[1],towns);
            }
            else if(args[0].equalsIgnoreCase("nation")){
                List<String> nations = new ArrayList<>(TownManager.getInstance().getNationNames());
                nations.add("create");
                nations.add("new");
                return Core.getPartialMatches(args[1],nations);
            }
        }

        if (args.length == 3) {
            if(args[0].equalsIgnoreCase("nation") && (TownManager.getInstance().getNation(args[1]) != null)){
                return Core.getPartialMatches(args[2],List.of("add","delete","deposit","kick","ranksettings","set","withdraw","toggle","fillcolor"));
            }
            if(args[0].equalsIgnoreCase("town") && (TownManager.getInstance().getTown(args[1]) != null)){
                return Core.getPartialMatches(args[2],List.of("add","delete","deposit","kick","ranksettings","set","spawn","withdraw","toggle","forcemerge"));
            }
        }

        if (args.length == 4) {
            if(args[0].equalsIgnoreCase("nation") && (TownManager.getInstance().getNation(args[1]) != null)) {
                if(args[2].equalsIgnoreCase("set")) {
                    return Core.getPartialMatches(args[3], List.of("capital", "king", "mapcolor", "name","fullmapcolor"));
                }else if(args[2].equalsIgnoreCase("kick")){
                    return Core.getPartialMatches(args[3],TownManager.getInstance().getNation(args[1]).getTowns().stream().map(Town::getName).toList());
                }else if(args[2].equalsIgnoreCase("add")){
                    return Core.getPartialMatches(args[0],TownManager.getInstance().getTownNames());
                }
            }else if(args[0].equalsIgnoreCase("town") && (TownManager.getInstance().getTown(args[1]) != null)) {
                if ((args[2].equalsIgnoreCase("set"))) {
                    return Core.getPartialMatches(args[3], List.of("homeblock", "mayor", "mapcolor", "name", "spawn"));
                }else if(args[2].equalsIgnoreCase("kick")){
                    return Core.getPartialMatches(args[3],TownManager.getInstance().getTown(args[1]).getResidents().stream().map(Resident::getPlayerName).toList());
                }else if(args[2].equalsIgnoreCase("add")){
                    return Core.getPartialMatches(args[3],TownManager.getInstance().getResidentNames());
                }
            }
        }
        if(args.length == 5){
            if(args[0].equalsIgnoreCase("town") && (TownManager.getInstance().getTown(args[1]) != null) && args[2].equalsIgnoreCase("set") && args[3].equalsIgnoreCase("mayor")){
                return Core.getPartialMatches(args[4],TownManager.getInstance().getTown(args[1]).getResidents().stream().map(Resident::getPlayerName).toList());
            }
            if(args[0].equalsIgnoreCase("nation") && (TownManager.getInstance().getNation(args[1]) != null) && args[2].equalsIgnoreCase("set") && args[3].equalsIgnoreCase("capital")){
                return Core.getPartialMatches(args[4],TownManager.getInstance().getNation(args[1]).getTowns().stream().map(Town::getName).toList());
            }
        }
        return new ArrayList<>();
    }
}
