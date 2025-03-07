package dev.loveeev.astraTowny.commands.main;

import dev.loveeev.astratowny.chat.Messages;
import dev.loveeev.astratowny.config.ConfigYML;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.response.NoPermissionException;
import dev.loveeev.astratowny.utils.ChatUtil;
import dev.loveeev.astratowny.utils.TownyUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
 import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NationCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только в игре.");
            return true;
        }
        Resident resident = TownManager.getInstance().getResident(player);
        if (args.length == 0) {
            if (!TownManager.getInstance().getResident(player).hasNation()) {
                ChatUtil.sendSuccessNotification(player, Messages.nationnull(player));
            } else {
                Messages.info(TownManager.getInstance().getNation(player), player, TranslateYML.get(player).getStringList("n"));
            }
            return true;
        }
        try {
            switch (args[0]) {
                case "create", "new" -> createNation(args, resident, player);
                case "delete" -> deleteNation(args, resident, player);
                case "invite" -> invite(args, resident, player);
                case "accept" -> accept(args, resident, player);
                case "kick" -> kickTown(args, resident, player);
                case "transfer" -> transfer(resident, args);
                case "set" -> set(args, resident, player);
                case "leave" -> leave(args, resident, player);
                default -> {
                    String cityName = args[0];
                    Nation nation = TownManager.getInstance().getNation(cityName);
                    if (nation != null) {
                        Messages.info(nation, player, TranslateYML.get(player).getStringList("n"));
                    } else {
                        ChatUtil.sendSuccessNotification(player, Messages.commandundefined(resident.getPlayer()));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            List<String> commands = new ArrayList<>(List.of("create","delete","invite","accept","kick","rank","set","transfer","leave"));
            List<String> townNames = TownManager.getInstance().getTownNames();
            commands.addAll(townNames);
            return getPartialMatches(args[0], commands);
        }
        Resident resident = TownManager.getInstance().getResident((Player) sender);
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("set")){
                return getPartialMatches(args[1],List.of("mapcolor","name","capital"));
            }else if (args[0].equalsIgnoreCase("kick")){
                if(resident.hasNation()){
                    List<Town> towns = resident.getNation().getTowns();
                    towns.remove(resident.getNation().getCapital());
                    return getPartialMatches(args[1],towns.stream().map(town -> town.getName()).collect(Collectors.toList()));
                }
            }else if(args[0].equalsIgnoreCase("rank")){
                return getPartialMatches(args[1],List.of("create","new","add","remove","delete","list","my"));
            }else if(args[0].equalsIgnoreCase("invite")){
                return getPartialMatches(args[1],TownManager.getInstance().getTownNames());
            }else if (args[0].equalsIgnoreCase("accept")) {
                if(resident.hasTown()) {
                    return getPartialMatches(args[1], resident.getTown().getInvitations());
                }else {
                    return new ArrayList<>();
                }
            }
        }
        if(args.length == 3){
            if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                if (resident.hasNation()) {
                    return getPartialMatches(args[2], resident.getTown().getResidents().stream().map(Resident::getPlayerName).collect(Collectors.toList()));
                }else{
                    return new ArrayList<>();
                }
            }else if (args[1].equalsIgnoreCase("delete")){
                List<String> list = new ArrayList<>();
                for (ConcurrentHashMap.Entry<UUID, Rank> entry : TownManager.getInstance().getRanks().entrySet()) {
                    Rank rank = entry.getValue();
                    String rankName = rank.getName();
                    if (resident.hasNation()) {
                        if (rank.getNation() == resident.getNation()) {
                            list.add(rankName);
                        }
                    }
                }
                list.remove(ConfigYML.kingname);
                list.remove(ConfigYML.defaultnationname);
                return getPartialMatches(args[2],list);
            }
        }
        if(args.length == 4){
            if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")){
                List<String> list = new ArrayList<>();
                for (ConcurrentHashMap.Entry<UUID, Rank> entry : TownManager.getInstance().getRanks().entrySet()) {
                    Rank rank = entry.getValue();
                    String rankName = rank.getName();
                    if (resident.hasNation()) {
                        if (rank.getNation() == resident.getNation()) {
                            list.add(rankName);
                        }
                    }
                }
                list.remove(ConfigYML.kingname);
                list.remove(ConfigYML.defaultnationname);
                return getPartialMatches(args[3],list);
            }
        }
        return new ArrayList<>();
    }


    public void  leave(String[] args,Resident resident,Player player) throws NoPermissionException {
        if(!resident.hasNation()){
            ChatUtil.sendSuccessNotification(resident, Messages.nationexist(resident.getPlayer()));
            return;
        }
        if (!resident.isMayor()) {
            ChatUtil.sendSuccessNotification(resident, Messages.permissionerror(resident.getPlayer()));
            return;
        }
        if(args.length < 1){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return;
        }
        if(args[0] == null || args[0].isEmpty()){
            ChatUtil.sendSuccessNotification(player,Messages.args(player));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_LEAVE");
        if(resident.getTown().isCapital()){
            ChatUtil.sendSuccessNotification(player,Messages.capitalerror(player));
            return;
        }
        TownyUtil.removeTownInNation(resident.getTown());
        ChatUtil.sendSuccessNotification(player,Messages.leavenation(player));
    }


    public void transfer(Resident resident,String[] args) {
        if(args.length < 2){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.transfern(resident.getPlayer()));
            return;
        }
        if(TownManager.getInstance().getTown(args[1]) == null){
            ChatUtil.sendSuccessNotification(resident,Messages.townexist(resident.getPlayer()));
        }
        if (!resident.hasNation()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if (!resident.isKing()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.permissionerror(resident.getPlayer()));
            return;
        }
        Resident res = TownManager.getInstance().getResident(args[1]);
        if(res == null){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.playererror(resident.getPlayer()));
            return;
        }
        if(!res.hasNation()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if(res.getNation() != resident.getNation()) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.error(resident.getPlayer()));
            return;
        }
        TownyUtil.setKing(res);
        TownyUtil.removeResidentInNation(resident);
        TownyUtil.addResidentInTown(resident,res.getTown());
        ChatUtil.sendSuccessNotification(resident,Messages.transfersuc(resident.getPlayer()));
    }

    public void accept(String[] args, Resident resident, Player player) throws NoPermissionException {
        if (args.length != 2) {
            ChatUtil.sendSuccessNotification(player, Messages.accept(player));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_TOWN_ACCEPTNATION");
        String townName = args[1];
        acceptInvitation(resident, townName, player);
    }

    public static void acceptInvitation(Resident resident, String nationName, Player player) {
        if (!resident.getTown().hasInvitation(nationName)) {
            ChatUtil.sendSuccessNotification(player, Messages.accepterror(player));
            return;
        }
        TownyUtil.addTownInNation(resident.getTown(),TownManager.getInstance().getNation(nationName));
        resident.getTown().getInvitations().remove(nationName);
        ChatUtil.sendSuccessNotification(player, Messages.acceptconfirm(player));
    }


    public void invite(String[] args, Resident resident, Player player) throws NoPermissionException {
        if(resident.hasTown() && resident.hasNation()) {
            TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_INVITE");
            if (args.length == 1) {
                ChatUtil.sendSuccessNotification(player, Messages.invite(player));
                return;
            }
            if (args.length == 2) {
                String invitedsTown = args[1];
                Town invitedTown = TownManager.getInstance().getTown(invitedsTown);
                if(invitedTown.hasInvitation(resident.getNation().getName())){
                    ChatUtil.sendSuccessNotification(player,Messages.alrinvite(player));
                    return;
                }
                if(Objects.equals(invitedTown, resident.getTown())){
                    ChatUtil.sendSuccessNotification(player,Messages.selfinvite(player));
                    return;
                }
                if(invitedTown.hasNation()){
                    ChatUtil.sendSuccessNotification(player,Messages.townhasnation(player));
                    return;
                }
                if(!invitedTown.getMayor().isOnline()){
                    ChatUtil.sendSuccessNotification(player,"Мэр не в сети.");
                }
                invitedTown.getInvitations().add(resident.getNation().getName());
                ChatUtil.sendSuccessNotification(player, Messages.invitesend(player) + invitedsTown + ".");
                TextComponent message = new TextComponent(ChatUtil.getInstance().colorize(Messages.inviteaprove(player) + resident.getNation().getName()));
                TextComponent confirmCommand = new TextComponent(ChatColor.GREEN + " [/n accept " + resident.getNation().getName() + "]");
                confirmCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/n accept " + resident.getNation().getName()));
                confirmCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.confirmation(player)).create()));
                message.addExtra(confirmCommand);
                invitedTown.getMayor().getPlayer().spigot().sendMessage(ChatMessageType.CHAT, message);
            }

        }else {
            ChatUtil.sendSuccessNotification(player,Messages.nationexist(player));
        }
    }
    private List<String> getPartialMatches(String arg, List<String> options) {
        return options.stream().filter(option -> option.startsWith(arg)).collect(Collectors.toList());
    }
    public void kickTown(String[] args, Resident resident, Player player) throws NoPermissionException {
        if (args.length == 1) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.kicktown(resident.getPlayer()));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_KICKTOWN");
        TownManager townManager = TownManager.getInstance();
        Town targetTown = townManager.getTown(args[1]);

        if (targetTown == null) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.playeroffline(resident.getPlayer()));
            return;
        }
        if(targetTown == resident.getTown()){
            ChatUtil.sendSuccessNotification(player,"Вы не можете кикнуть самого себя!");
            return;
        }

        Town residentTown = townManager.getTown(resident.getPlayer());
        Nation targetNation = targetTown.getNation();
        Nation residentNation = residentTown.getNation();

        if (targetNation != residentNation) {
            ChatUtil.sendSuccessNotification(player, Messages.townnonation(player));
            return;
        }

        if (targetNation.getCapital() == targetTown) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.notkickcapital(player));
            return;
        }


        TownyUtil.removeTownInNation(targetTown);
        for (Resident r : targetTown.getResidents()){
            ChatUtil.sendSuccessNotification(r,"Вы были и́згнаны из нации.");
        }
        ChatUtil.sendSuccessNotification(resident.getPlayer(),Messages.kicksuc(resident.getPlayer()));
    }


    public void set(String[] args,Resident resident,Player player) throws NoPermissionException {
        if(!resident.hasNation()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.nationexist(resident.getPlayer()));
            return;
        }
        switch (args[1]){
            case "name" -> setName(args,resident);
            case "capital" -> setCapital(args,resident);
            case "mapcolor" -> setMapColor(args,resident);
            default -> ChatUtil.sendSuccessNotification(player, Messages.commandundefined(player));
        }
    }
    public void setName(String[] args, Resident resident) throws NoPermissionException {
        if (args.length == 2) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setnationname(resident.getPlayer()));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_SET_NAME");
        resident.getNation().setName(args[2]);
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setnamesuc(resident.getPlayer()));
    }
    public void setCapital(String[] args, Resident resident){
        if (args.length == 2) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setnationname(resident.getPlayer()));
            return;
        }
        if(!resident.isKing()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(),Messages.permissionerror(resident.getPlayer()));
            return;
        }
        if(TownManager.getInstance().getTown(args[2] )== null){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townexist(resident.getPlayer()));
            return;
        }
        if(TownManager.getInstance().getTown(args[2]).getNation() != resident.getNation()){
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.townnotisnation(resident.getPlayer()));
            return;
        }
        resident.getNation().setCapital(TownManager.getInstance().getTown(args[2]));
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.changecapital(resident.getPlayer()));
    }

    public void setMapColor(String[] args, Resident resident) throws NoPermissionException {
        if (args.length == 2) {
            ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.setnationname(resident.getPlayer()));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_SET_MAPCOLOR");
        for (Town town : resident.getNation().getTowns()){
            town.setMapColor(args[2]);
        }
        ChatUtil.sendSuccessNotification(resident.getPlayer(), Messages.changecapital(resident.getPlayer()));
    }
    public void createNation(String[] args,Resident resident,Player player) throws NoPermissionException {
        if (args.length == 1) {
            ChatUtil.sendSuccessNotification(player,Messages.createnation(resident.getPlayer()));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_CREATE");
            if (!resident.hasTown()) {
                ChatUtil.sendSuccessNotification(player, Messages.createnationt(resident.getPlayer()));
                return;
            }
            if (!resident.isMayor()) {
                ChatUtil.sendSuccessNotification(player, Messages.nationmayor(resident.getPlayer()));
                return;
            }
            if(args[1] == null || args[1].isEmpty()){
                ChatUtil.sendSuccessNotification(resident,Messages.args(player));
                return;
            }
            if (!resident.hasNation()) {
                Town town = TownManager.getInstance().getTown(resident);
                Nation existingNation = TownManager.getInstance().getNation(args[1]);
                if (existingNation != null) {
                    ChatUtil.sendSuccessNotification(player, Messages.nationalrexist(resident.getPlayer()));
                    return;
                }
                TownyUtil.createNation(args[1],UUID.randomUUID(),resident,town);
            } else {
                ChatUtil.sendSuccessNotification(player, Messages.nationalr(resident.getPlayer()));
            }
    }



    public void deleteNation(String[] args,Resident resident,Player player) throws NoPermissionException {
        if(!resident.hasTown()){
            ChatUtil.sendSuccessNotification(player,Messages.townfornation(resident.getPlayer()));
            return;
        }
        TownyUtil.checkNationPermission(resident,"ASTRATOWN_NATION_DELETE");
        if(resident.hasNation() && resident.isKing()) {
            Nation nation = TownManager.getInstance().getNation(resident);
            TownyUtil.deleteNation(nation);
            ChatUtil.sendSuccessNotification(player,Messages.nationdel(resident.getPlayer()) );
        }else {
            ChatUtil.sendSuccessNotification(player,Messages.permissionerror(resident.getPlayer()));
        }
    }

}
