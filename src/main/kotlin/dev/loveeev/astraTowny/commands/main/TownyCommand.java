package dev.loveeev.astraTowny.commands.main;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.objects.townblocks.HomeBlock;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.config.ConfigYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.objects.townblocks.WorldCoord;
import dev.loveeev.astratowny.response.NoPermissionException;
import dev.loveeev.astratowny.response.TownyResponse;
import dev.loveeev.astratowny.utils.BukkitUtils;
import dev.loveeev.astratowny.utils.ChatUtil;
import dev.loveeev.astratowny.utils.TownyUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TownyCommand implements TabExecutor{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.messageconsole());
            return true;
        }
        Resident resident = TownManager.getInstance().getResident(player);
        if (args.length == 0) {
            if (!TownManager.getInstance().getResident(player).hasTown()) {
                ChatUtil.sendSuccessNotification(player, Messages.messagetownexist(player));
            } else {
                Messages.info(TownManager.getInstance().getTown(player), player, TranslateYML.get(player).getStringList("t"));
            }
            return true;
        }


        try {
            switch (args[0]) {

                case "create", "new" -> handleCreateTown(args, player, resident);
                case "delete" -> handleDeleteTown(args, resident, player);
                case "leave" -> handleLeaveTown(player);
                case "spawn" -> {
                    if (!resident.hasTown()) {
                        ChatUtil.sendSuccessNotification(player, Messages.messagetownexist(player));
                        return true;
                    }
                    player.teleport(resident.getTown().getSpawnLocation());
                }
                case "accept" -> handleAcceptInvite(args, resident, player);
                case "invite" -> handleInvite(args, resident, player);
                case "claim" -> handleClaim(resident);
                case "unclaim" -> handleUnClaim(resident);
                case "set" -> handleSet(args, resident, player);
                case "rank" -> handleRank(resident, args);
                case "kick" -> handleKick(resident, args);
                case "transfer" -> handleTransfer(resident, args);
                case "army" -> handleArmy(resident, args);
                case "toggle" -> flagControl(args, resident);
                default -> {
                    CommandExecutor executor = commandExecutors.get(args[0]);
                    if (executor != null) {
                        return executor.onCommand(sender, command, label, args);
                    } else {
                        String cityName = args[0];
                        Town nation = TownManager.getInstance().getTown(cityName);
                        if (nation != null) {
                            Messages.info(nation, player, TranslateYML.get(player).getStringList("t"));
                        } else {
                            ChatUtil.sendSuccessNotification(player, Messages.commandundefined(player));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void handleArmy(Resident resident, String[] args) {
        if(args.length < 3) {
            ChatUtil.sendSuccessNotification(resident, Messages.compArmy(resident.getPlayer()));
            return;
        }
        if(Core.getInstance().checkAddons("AstraWars")) {
            Bukkit.dispatchCommand(resident.getPlayer(), "astraWarsAdd " + args[1] + " " + args[2]);
        }else {
            ChatUtil.sendSuccessNotification(resident,"У вас не установлен AstraWars, покупка на https://plugin.astraworld.su");
        }
    }



    public void handleTransfer(Resident resident,String[] args) {
        if(args.length < 2){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.transfert(resident.getPlayer()));
            return;
        }
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if (!resident.isMayor()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.permissionerror(resident.getPlayer()));
            return;
        }
        Resident res = TownManager.getInstance().getResident(args[1]);
        if(res == null){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.playererror(resident.getPlayer()));
            return;
        }
        if(!res.hasTown()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if(res.getTown() != resident.getTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.error(resident.getPlayer()));
            return;
        }
        TownyUtil.removeResidentInTown(resident);
        TownyUtil.setMayor(res);

        TownyUtil.addResidentInTown(resident,res.getTown());
        ChatUtil.sendSuccessNotification(resident.getPlayer(),Messages.transfersuc(resident.getPlayer()));
    }
    public void handleSet(String[] args,Resident resident,Player player) throws NoPermissionException {
        if(args.length < 2){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return;
        }
        switch (args[1]){
            case "homeblock" -> setHomeBlock(resident);
            case "spawn" -> setspawn(resident);
            case "mapcolor" -> setMapColor(resident,args);
            case "name" -> setName(resident,args);
            default -> ChatUtil.sendSuccessNotification(player, Messages.commandundefined(player));
        }
    }

    public void flagControl(String[] args,Resident resident){
        if(args.length < 3){
            ChatUtil.sendSuccessNotification(resident,Messages.flagControl(resident.getPlayer()));
            return;
        }
        if(!resident.hasTown()){
            ChatUtil.sendSuccessNotification(resident,Messages.messagetownexist(resident.getPlayer()));
            return;
        }
        if(!resident.isMayor()){
            ChatUtil.sendSuccessNotification(resident,Messages.permissionerror(resident.getPlayer()));
            return;
        }

        try{
            Town.PermsType permsType = Town.PermsType.valueOf(args[1]);
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                ChatUtil.sendSuccessNotification(resident,"Вы неверно укахали true/false");
            }
            Town town = resident.getTown();
            town.setEventStatus(permsType,Boolean.parseBoolean(args[2]));
            ChatUtil.sendSuccessNotification(resident,Messages.flagSuc(resident.getPlayer()).replace("{flag}",permsType.name()).replace("{bool}",args[2]));
        } catch (IllegalArgumentException e) {
            ChatUtil.sendSuccessNotification(resident,Messages.flagExist(resident.getPlayer()));
        }




    }


    public void handleKick(Resident resident,String[] args) throws NoPermissionException {
        if (args.length == 1) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.kickplayer(resident.getPlayer()));
            return;
        }

        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_KICK");
        Resident kickres = TownManager.getInstance().getResident(args[1]);
        if(kickres == resident){
            ChatUtil.sendSuccessNotification(resident,"Вы не можете кикнуть самого себя!");
            return;
        }
        if (kickres != null) {
            if (!kickres.hasTown()) {
                if (resident.getTown() == kickres.getTown()) {
                    if (!kickres.isMayor()) {

                        TownyUtil.removeResidentInTown(kickres);
                        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.kicksuc(resident.getPlayer()));
                        ChatUtil.sendSuccessNotification(kickres,"Вы были и́згнаны.");
                    } else {
                        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.notkickmayor(resident.getPlayer()));
                    }
                } else {
                    ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.playernottown(resident.getPlayer()));
                }
            }
        }
    }
    public void setMapColor(Resident resident,String[] args) throws NoPermissionException {
        if(!resident.hasTown()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if (args.length == 2) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setmapcolor(resident.getPlayer()));
            return;
        }
        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_SET_MAPCOLOR");
        resident.getTown().setMapColor(args[2]);
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setmapcolorsuc(resident.getPlayer()));
    }
    public void setName(Resident resident,String[] args) throws NoPermissionException {
        if (args.length == 2) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setname(resident.getPlayer()));
            return;
        }
        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_SET_NAME");
        if(!resident.hasTown()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        resident.getTown().setName(args[2]);
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setnamesuc(resident.getPlayer()));
    }

    public void handleCreateTown(String[] args, Player player, Resident resident) throws NoPermissionException {
        if (args.length == 1) {
            ChatUtil.sendSuccessNotification(player, Messages.createtown(player));
            return;
        }
        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_CREATE");

        Chunk chunk = player.getChunk();
        if (TownManager.getInstance().getTownBlock(chunk) != null) {
            ChatUtil.sendSuccessNotification(player, Messages.alrclaim(player)) ;
            return;
        }

        if (!resident.hasTown()) {
            Town existingTown = TownManager.getInstance().getTown(args[0]);
            if (existingTown != null) {
                ChatUtil.sendSuccessNotification(player, Messages.townalrexist(player));
                return;
            }
            TownyUtil.createTown(args[1],UUID.randomUUID(),resident,new HomeBlock(player.getChunk()),player.getLocation());
        }
    }

    public void handleInvite(String[] args, Resident resident, Player player) throws NoPermissionException {
        if (resident.hasTown()) {
            TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_INVITE");
            if (args.length == 1) {
                ChatUtil.sendSuccessNotification(player, Messages.invite(player));
                return;
            }
            if (args.length == 2) {
                String invitedPlayerName = args[1];

                Player invitedPlayer = Bukkit.getPlayer(invitedPlayerName);
                if (invitedPlayer == null || !invitedPlayer.isOnline()) {
                    ChatUtil.sendSuccessNotification(player, Messages.playeroffline(player));
                    return;
                }

                Resident invitedResident = TownManager.getInstance().getResident(invitedPlayer);
                if (invitedResident.hasTown()) {
                    ChatUtil.sendSuccessNotification(player, Messages.alrinvitetown(player));
                    return;
                }
                if (invitedResident.hasInvitation(resident.getTown().getName())) {
                    ChatUtil.sendSuccessNotification(player, Messages.alrinvite(player));
                    return;
                }
                if (invitedResident == resident) {
                    ChatUtil.sendSuccessNotification(player, Messages.selfinvite(player));
                    return;
                }

                invitedResident.addInvitation(resident.getTown().getName());

                ChatUtil.sendSuccessNotification(player, Messages.invitesend(player) + invitedPlayerName + ".");
                TextComponent message = new TextComponent(ChatUtil.getInstance().colorize(Messages.inviteaprove(player) + resident.getTown().getName()));
                TextComponent confirmCommand = new TextComponent(ChatColor.GREEN + " [/t accept " + resident.getTown().getName() + "]");
                confirmCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t accept " + resident.getTown().getName()));
                confirmCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.confirmation(player)).create()));
                message.addExtra(confirmCommand);

                invitedPlayer.spigot().sendMessage(ChatMessageType.CHAT, message);
            }
        } else {
            ChatUtil.sendSuccessNotification(player, Messages.permissionerror(player));
        }
    }

    public void handleAcceptInvite(String[] args, Resident resident, Player player) {
        if (args.length != 2) {
            ChatUtil.sendSuccessNotification(player, Messages.accept(player));
            return;
        }

        String townName = args[1];
        acceptInvitation(resident, townName, player);
    }

    public static void acceptInvitation(Resident resident, String townName, Player player) {
        if (!resident.hasInvitation(townName)) {
            ChatUtil.sendSuccessNotification(player, Messages.accepterror(player));
            return;
        }
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null){
           ChatUtil.sendSuccessNotification(player,Messages.townexist(player));
           return;
        }
        TownyUtil.addResidentInTown(resident,town);
        resident.removeInvitation(townName);
        ChatUtil.sendSuccessNotification(player, Messages.acceptconfirm(player));
    }
    public void handleDeleteTown(String[] args,Resident resident,Player player) throws NoPermissionException {
        if(!resident.hasTown()){
            ChatUtil.sendSuccessNotification(player,Messages.messagetownexist(player));
            return;
        }



        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_DELETE");



        if(args.length == 1){
            if(resident.isMayor()){
                TextComponent message = new TextComponent(ChatUtil.getInstance().colorize(Messages.confirmation(player)));
                TextComponent confirmCommand = new TextComponent(ChatColor.GREEN + "[/t delete confirm]");
                confirmCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t delete confirm"));
                confirmCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.confirmation(player)).create()));
                message.addExtra(confirmCommand);
                player.spigot().sendMessage(ChatMessageType.CHAT, message);
            }else {
                ChatUtil.sendSuccessNotification(player,Messages.permissionerror(player));
            }

        }
        if(args.length == 2) {
            if (args[1].equalsIgnoreCase("confirm")) {
                deleteTown(resident);
                ChatUtil.sendSuccessNotification(player, Messages.deleteconfirm(player));
            } else {
                ChatUtil.sendSuccessNotification(player, Messages.commandundefined(player));
            }
        }
    }

    public void deleteTown(Resident resident) {
        if (resident.hasTown() && resident.isMayor()) {
            TownyUtil.deleteTown(resident.getTown());
        } else {
            ChatUtil.sendSuccessNotification(resident, Messages.permissionerror(resident.getPlayer()));
        }
    }

    public void handleLeaveTown(Player player){
        Resident resident = TownManager.getInstance().getResident(player);
        if (!resident.hasTown()){
            Messages.messagetownexist(player);
            return;
        }
        if(!resident.isMayor()) {
            TownyUtil.removeResidentInTown(resident);
            ChatUtil.sendSuccessNotification(player, Messages.leaveconfirm(player));
        } else {
            ChatUtil.sendSuccessNotification(player,Messages.leaveerror(resident.getPlayer()));
        }
    }

    //ДОПИШИ КАК ПРОТЕСТИТЕ ЭКОНОМИКУ
    public void handleClaim(Resident resident) throws NoPermissionException {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.messagetownexist(resident.getPlayer()));
            return;
        }

        Town town = resident.getTown();
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_CLAIM");
        Chunk chunk = resident.getPlayer().getLocation().getChunk();
        WorldCoord claimCoord = new WorldCoord(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());

        // Проверка на наличие соседних чанков
        if (!hasNeighbouringTownChunk(town, claimCoord)) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.claimerror(resident.getPlayer()));
            return;
        }

        if (TownManager.getInstance().getTownByChunk(chunk) != null) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.alrclaim(resident.getPlayer()));
            return;
        }

        if (ConfigYML.moneygold) {
            if (TownManager.getInstance().getTownBlock(chunk) != null) {
                ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.alrclaim(resident.getPlayer()));
                return;
            }
            if (Core.getInstance().getItemAmount(resident.getPlayer(), Material.GOLD_INGOT) < ConfigYML.priceTownBlocks) {
                ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.goldnotenough(resident.getPlayer()));
                return;
            }

            resident.getPlayer().getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, (int) Math.floor(ConfigYML.priceTownBlocks)));

            TownyUtil.createTownBlock(chunk.getWorld(), town, chunk.getX(), chunk.getZ());
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.claimconfirm(resident.getPlayer()) + " X: " + chunk.getX() + " Z: " + chunk.getZ());
        } else {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.error(resident.getPlayer()));
        }
    }

    // Метод для проверки наличия соседних чанков
    private boolean hasNeighbouringTownChunk(Town town, WorldCoord claimCoord) {
        // Получаем все чанки города
        ConcurrentHashMap<WorldCoord, TownBlocks> townBlocks = town.getTownBlocks();

        // Координаты для соседних чанков (слева, справа, сверху, снизу)
        int[][] neighbours = {
                {1, 0},   // справа
                {-1, 0},  // слева
                {0, 1},   // впереди
                {0, -1}   // сзади
        };

        // Проверяем каждый соседний чанк
        for (int[] offset : neighbours) {
            WorldCoord neighbourCoord = new WorldCoord(
                    claimCoord.getWorldName(),
                    claimCoord.getX() + offset[0],
                    claimCoord.getZ() + offset[1]
            );

            // Если соседний чанк принадлежит городу, возвращаем true
            if (townBlocks.containsKey(neighbourCoord)) {
                return true;
            }
        }

        // Если ни один соседний чанк не принадлежит городу, возвращаем false
        return false;
    }





    public void handleUnClaim(Resident resident) throws NoPermissionException {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.messagetownexist(resident.getPlayer()));
            return;
        }

        Town town = resident.getTown();
        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_UNCLAIM");
        Chunk chunk = resident.getPlayer().getLocation().getChunk();
        if(town.getHomeblock().getX() == chunk.getX() && town.getHomeblock().getZ() == chunk.getZ()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.homeblockerror(resident.getPlayer()));
            return;
        }

        if(TownManager.getInstance().getTownByChunk(chunk) == null){
            ChatUtil.sendSuccessNotification(resident,Messages.chunkerror(resident.getPlayer()));
            return;
        }

        if(resident.getTown() != TownManager.getInstance().getTownByChunk(chunk)){
            ChatUtil.sendSuccessNotification(resident,Messages.chunkerror(resident.getPlayer()));
            return;
        }
        TownBlocks townBlocks = TownManager.getInstance().getTownBlock(chunk);
        TownyResponse response = TownyUtil.deleteTownBlock(townBlocks);
        BukkitUtils.logToConsole(response.getMessage());
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.unclaimconfirm(resident.getPlayer()) + " X: " + townBlocks.getX() + " Z: " + townBlocks.getZ());
    }

    public void setspawn(Resident resident) throws NoPermissionException {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.messagetownexist(resident.getPlayer()));
            return;
        }
        TownyUtil.checkTownPermission(resident, "ASTRATOWN_TOWN_SET_SPAWN");
        Town town = TownManager.getInstance().getTown(resident);
        Chunk chunk = resident.getPlayer().getLocation().getChunk();
        if (town.getTownBlocks().keySet().stream().noneMatch(townBlock -> townBlock.getX() == chunk.getX() && townBlock.getZ() == chunk.getZ())) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.chunkerror(resident.getPlayer()));
            return;
        }
        town.setSpawnLocation(resident.getPlayer().getLocation());
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.spawnset(resident.getPlayer()));
    }

    public void setHomeBlock(Resident resident) throws NoPermissionException {
        if (!resident.hasTown()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.messagetownexist(resident.getPlayer()));
            return;
        }
        TownyUtil.checkTownPermission(resident,"ASTRATOWN_TOWN_SET_HOMEBLOCK");
        Town town = TownManager.getInstance().getTown(resident);
        Chunk chunk = resident.getPlayer().getLocation().getChunk();
        if (town.getTownBlocks().keySet().stream().noneMatch(townBlock -> townBlock.getX() == chunk.getX() && townBlock.getZ() == chunk.getZ())) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.chunkerror(resident.getPlayer()));
            return;
        }
        if(town.getHomeblock().getX() == chunk.getX() && town.getHomeblock().getZ() == chunk.getZ()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.homeblockalr(resident.getPlayer()));
            return;
        }
        town.setHomeblock(new HomeBlock(chunk.getX(),chunk.getZ(),chunk.getWorld()));
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.homeblockconfirm(resident.getPlayer()));
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Resident resident = TownManager.getInstance().getResident(player);

        if (args.length == 1) {
            List<String> commands = new ArrayList<>(List.of("create", "delete", "leave", "spawn", "accept", "invite", "claim", "unclaim", "set", "rank", "new", "kick", "transfer","army"));
            List<String> townNames = TownManager.getInstance().getTownNames();
            commands.addAll(townNames);
            return getPartialMatches(args[0], commands);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                return getPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("rank")) {
                return getPartialMatches(args[1], List.of("create", "new", "set", "give", "delete","my"));
            } else if (args[0].equalsIgnoreCase("set")) {
                return getPartialMatches(args[1], List.of("spawn", "homeblock", "mapcolor", "name", "mapcolorall"));
            } else if (args[0].equalsIgnoreCase("kick") && resident.hasTown()) {
                return getPartialMatches(args[1], resident.getTown().getResidents().stream().map(Resident::getPlayerName).collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("transfer")) {
                return getPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            }else if (args[0].equalsIgnoreCase("accept")) {
                return getPartialMatches(args[1],resident.getInvitations());
            }else if (args[0].equalsIgnoreCase("army")) {
                return getPartialMatches(args[1],List.of("add","remove"));
            }
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                if (resident.hasTown()) {
                    return getPartialMatches(args[2], resident.getTown().getResidents().stream().map(Resident::getPlayerName).collect(Collectors.toList()));
                } else {
                    return new ArrayList<>();
                }
            } else if (args[1].equalsIgnoreCase("delete")) {
                List<String> list = new ArrayList<>();
                for (ConcurrentHashMap.Entry<UUID,Rank> entry : TownManager.getInstance().getRanks().entrySet()) {
                    Rank rank = entry.getValue();
                    String rankName = rank.getName();
                    if (resident.hasNation()) {
                        if (rank.getTown() == resident.getTown()) {
                            list.add(rankName);
                        }
                    }
                }
                list.remove(ConfigYML.mayorname);
                list.remove(ConfigYML.defaultPerm);
                return getPartialMatches(args[2], list);
            }else if (args[0].equalsIgnoreCase("army")) {
                return getPartialMatches(args[1],resident.getTown().getResidents().stream().map(Resident::getPlayerName).collect(Collectors.toList()));
            }
        }

        if (args.length == 4) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                List<String> list = new ArrayList<>();
                for (ConcurrentHashMap.Entry<UUID, Rank> entry : TownManager.getInstance().getRanks().entrySet()) {
                    Rank rank = entry.getValue();
                    String rankName = rank.getName();
                    if (resident.hasNation()) {
                        if (rank.getTown() == resident.getTown()) {
                            list.add(rankName);
                        }
                    }
                }
                list.remove(ConfigYML.mayorname);
                list.remove(ConfigYML.defaultPerm);
                return getPartialMatches(args[3], list);
            }
        }

        TabCompleter completer = tabCompleters.get(args[0]);
        if (completer != null) {
            return completer.onTabComplete(sender, command, label, args);
        }

        return new ArrayList<>();
    }
    private List<String> getPartialMatches(String arg, List<String> options) {
        return options.stream().filter(option -> option.startsWith(arg)).collect(Collectors.toList());
    }
}
