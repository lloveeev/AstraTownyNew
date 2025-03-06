package dev.loveeev.astraTowny.chat;

import dev.loveeev.astraTowny.Core;
import dev.loveeev.astratowny.config.TranslateYML;
import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.Town;
import dev.loveeev.astratowny.utils.ChatUtil;
import dev.loveeev.astrawars.data.AstraWarsData;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class Messages {
    private final Player player;
    private final Town town;
    @Getter
    private static Messages instance;

    private Messages(Player player) {
        instance = this;
        this.player = player;
        this.town = TownManager.getInstance().getTown(player);
    }

    public String getMessage(String key) {
        String message = TranslateYML.get(this.player).getString(key);
        if (message != null) {
            message = replacePlaceholders(message);
        }
        return message;
    }

    public List<String> getMessageList(String key) {
        List<String> messages = TranslateYML.get(this.player).getStringList(key);
        List<String> processedMessages = new ArrayList<>();
        for (String message : messages) {
            processedMessages.add(replacePlaceholders(message));
        }
        return processedMessages;
    }

    private String replacePlaceholders(String message) {
        String townName = Objects.toString(Optional.ofNullable(town).map(Town::getName).orElse(TranslateYML.get(player).getString("no")), "");
        String nationName = Objects.toString(Optional.ofNullable(town).map(Town::getNation).map(Nation::getName).orElse(TranslateYML.get(player).getString("no")), "");
        String mayorName = Objects.toString(Optional.ofNullable(town).map(Town::getMayor).map(Resident::getPlayerName).orElse(TranslateYML.get(player).getString("no")), "");
        String kingName = Objects.toString(Optional.ofNullable(town).map(Town::getNation).map(Nation::getKing).map(Resident::getPlayerName).orElse(TranslateYML.get(player).getString("no")), "");
        String capitalName = Objects.toString(Optional.ofNullable(town).map(Town::getNation).map(Nation::getCapital).map(Town::getName).orElse(TranslateYML.get(player).getString("no")), "");
        String mapcolor = Objects.toString(Optional.ofNullable(town).map(Town::getMapColor).orElse(TranslateYML.get(player).getString("no")), "");
        String language = Objects.toString(TownManager.getInstance().getResident(player).getLanguage(), "");

        return message
                .replace("{player}", Objects.toString(player.getName(), ""))
                .replace("{town}", townName)
                .replace("{nation}", nationName)
                .replace("{mayor}", mayorName)
                .replace("{king}", kingName)
                .replace("{capital}", capitalName)
                .replace("{mapcolor}", mapcolor)
                .replace("{language}", language);
    }


    public static String flagSuc(Player player) {
        return new Messages(player).getMessage("flagSuc");
    }

    public static String newDay(Player player) {
        return new Messages(player).getMessage("newDay");
    }

    public static String newHour(Player player) {
        return new Messages(player).getMessage("newHour");
    }

    public static String compArmy(Player player) {
        return new Messages(player).getMessage("compArmy");
    }

    public static String flagControl(Player player) {
        return new Messages(player).getMessage("flagControl");
    }

    public static String flagExist(Player player) {
        return new Messages(player).getMessage("flagExist");
    }

    public static void info(Town town, Player player, List<String> message) {
        for (String line : replaceInfo(message, town, null, player)) {
            player.sendMessage(ChatUtil.colorize(line));
        }
        if (Core.getInstance().checkAddons("AstraBuilds") && Core.getInstance().checkAddons("AstraRanks")) {
            // Сбор информации о жителях
            List<String> residentsList = town.getResidents().stream()
                    .map(Resident::getPlayerName)
                    .toList();
            StringBuilder residentsInfo = new StringBuilder("§6Жители (" + residentsList.size() + "):\n");
            for (String resident : residentsList) {
                residentsInfo.append(" - §f").append(resident).append("\n");
            }
            Component residentsHoverText = Component.text(residentsInfo.toString());

            // Сбор информации о званиях
            List<String> ranksList = town.getRanks().stream()
                    .map(Rank::getName)
                    .toList();
            StringBuilder ranksInfo = new StringBuilder("§6Звания: \n");
            for (String rank : ranksList) {
                int rankCount = TownManager.getInstance().getRankResident(TownManager.getInstance().getRank(rank)).size();
                ranksInfo.append(" - §f").append(rank).append(": ").append(rankCount).append("\n");
            }
            Component ranksHoverText = Component.text(ranksInfo.toString());

            // Сбор информации о зданиях
            List<String> buildsList = new ArrayList<>();
            buildsList.add("test");
            StringBuilder buildsInfo = new StringBuilder("§6Здания: \n");
            for (String build : buildsList) {
                buildsInfo.append(" - §f").append(build).append("\n");
            }
            Component buildsHoverText = Component.text(buildsInfo.toString());

            // Создание компонентов текста с ховер-событиями
            Component residentsComponent = Component.text(ChatUtil.colorize("&#DDA840Жители"))
                    .hoverEvent(HoverEvent.showText(residentsHoverText));

            Component ranksComponent = Component.text(ChatUtil.colorize("&#DDA840Звания"))
                    .hoverEvent(HoverEvent.showText(ranksHoverText));

            Component buildsComponent = Component.text(ChatUtil.colorize("&#DDA840Здания"))
                    .hoverEvent(HoverEvent.showText(buildsHoverText));

            // Соединение компонентов в одно сообщение
            Component combinedMessage = Component.text()
                    .append(residentsComponent)
                    .append(Component.text(" §f| ")) // Разделитель
                    .append(ranksComponent)
                    .append(Component.text(" §f| ")) // Разделитель
                    .append(buildsComponent)
                    .build();
            player.sendMessage(combinedMessage);
        }
    }

    public static List<String> replaceInfo(List<String> message, Town town, Nation nation, Player player) {
        List<String> m = new ArrayList<>();

        boolean isAstraWarsEnabled = Core.getInstance().checkAddons("AstraWars");
        boolean isAstraUnionsEnabled = Core.getInstance().checkAddons("AstraUnions");

        String townName = town != null ? town.getName() : "";
        String mayorName = (town != null && town.getMayor() != null) ? town.getMayor().getPlayerName() : "";
        String nationName = (town != null && town.getNation() != null) ? town.getNation().getName() : "";
        String townManpower = (isAstraWarsEnabled && town != null) ?
                (AstraWarsData.getTownWar(town.getName()) != null ?
                        String.valueOf(AstraWarsData.getTownWar(town.getName()).getPeople()) : "0") : "0";

        String nationKingName = (nation != null && nation.getKing() != null) ? nation.getKing().getPlayerName() : "";
        String capitalName = (nation != null && nation.getCapital() != null) ? nation.getCapital().getName() : "";
        /*String nationUnionName = (isAstraUnionsEnabled && nation != null) ?
               (Data.getUnion(nation.getName()) != null ? Data.getUnion(nation.getName()).getName() : "нет") : "нет";
         */
        for (String sm : message) {
            sm = sm.replace("{town}", townName)
                    .replace("{mayor}", mayorName)
                    .replace("{nation}", nationName);

            if (isAstraWarsEnabled) {
                sm = sm.replace("{town_manpower}", townManpower);
            }

            if (nation != null) {
                sm = sm.replace("{nation}", nation.getName())
                        .replace("{king}", nationKingName)
                        .replace("{capital}", capitalName);
            }

            if (isAstraUnionsEnabled) {
                //sm = sm.replace("{nation_union}", nationUnionName);
            }

            m.add(sm);
        }

        return m;
    }

    public static void info(Nation nation,Player player,List<String> message){
        for (String line : replaceInfo(message, null, nation,player)) {
            player.sendMessage(ChatUtil.colorize(line));
        }
        if (Core.getInstance().checkAddons("AstraWars") && Core.getInstance().checkAddons("AstraRanks")) {
            List<String> residentsList = nation.getTowns().stream()
                    .map(Town::getName)
                    .toList();
            StringBuilder residentsInfo = new StringBuilder("§6Города (" + residentsList.size() + "):\n");
            for (String resident : residentsList) {
                residentsInfo.append(" - §f").append(resident).append("\n");
            }
            Component residentsHoverText = Component.text(residentsInfo.toString());

            // Сбор информации о званиях
            List<String> ranksList = nation.getRanks().stream()
                    .map(Rank::getName)
                    .toList();
            StringBuilder ranksInfo = new StringBuilder("§6Звания: \n");
            for (String rank : ranksList) {
                int rankCount = TownManager.getInstance().getRankResident(TownManager.getInstance().getRank(rank)).size();
                ranksInfo.append(" - §f").append(rank).append(": ").append(rankCount).append("\n");
            }
            Component ranksHoverText = Component.text(ranksInfo.toString());

            // Сбор информации о людский ресурсах
            List<String> buildsList = new ArrayList<>();
            buildsList.add("test");
            StringBuilder buildsInfo = new StringBuilder("§6Людский ресурсы: \n");
            for (String build : buildsList) {
                buildsInfo.append(" - §f").append(build).append("\n");
            }
            Component buildsHoverText = Component.text(buildsInfo.toString());


            List<String> armyList = new ArrayList<>();
            armyList.add("test"); //  список данных о армии

            StringBuilder armyInfo = new StringBuilder("§6Армия: \n");
            for (String army : armyList) {
                armyInfo.append(" - §f").append(army).append("\n");
            }

            Component armyHoverText = Component.text(armyInfo.toString());

            // Создание компонентов текста с ховер-событиями
            Component residentsComponent = Component.text(ChatUtil.colorize("&#DDA840Жители"))
                    .hoverEvent(HoverEvent.showText(residentsHoverText));

            Component ranksComponent = Component.text(ChatUtil.colorize("&#DDA840Звания"))
                    .hoverEvent(HoverEvent.showText(ranksHoverText));

            Component buildsComponent = Component.text(ChatUtil.colorize("§#DDA840Людские ресурсы"))
                    .hoverEvent(HoverEvent.showText(buildsHoverText));

            Component armyComponent = Component.text(ChatUtil.colorize("&#DDA840Армия"))
                    .hoverEvent(HoverEvent.showText(armyHoverText));

            // Соединение компонентов в одно сообщение
            Component combinedMessage = Component.text()
                    .append(residentsComponent)
                    .append(Component.text(" §f| ")) // Разделитель
                    .append(ranksComponent)
                    .append(Component.text(" §f| ")) // Разделитель
                    .append(buildsComponent)
                    .append(Component.text(" §f| "))
                    .append(armyComponent)
                    .build();
            player.sendMessage(combinedMessage);
        }
    }




    public static String no(Player player){
        return TranslateYML.get(player).getString("no");
    }

    public static String interactBlock(Player player){
        return new Messages(player).getMessage("interactBlock");
    }
    public static String breakBlock(Player player){
        return new Messages(player).getMessage("breakBlock");
    }
    public static String playernotthisnation(Player player){
        return new Messages(player).getMessage("playernotthisnation");
    }
    public static String alrnation(Player player){
        return new Messages(player).getMessage("alrnation");
    }
    public static List<String> t(Player player){
        return new Messages(player).getMessageList("t");
    }
    public static List<String> n(Player player){
        return new Messages(player).getMessageList("n");
    }
    public static String leavenation(Player player){
        return new Messages(player).getMessage("leavenation");
    }
    public static String townhasnation(Player player){
        return new Messages(player).getMessage("townhasnation");
    }
    public static String selfinvite(Player player){
        return new Messages(player).getMessage("selfinvite");
    }
    public static String leave(Player player){
        return new Messages(player).getMessage("leave");
    }
    public static String capitalerror(Player player){
        return new Messages(player).getMessage("capitalerror");
    }
    public static String bcaddnation(Player player){
        return new Messages(player).getMessage("bcaddnation");
    }
    public static String bcaddtown(Player player){
        return new Messages(player).getMessage("bcaddtown");
    }
    public static String bcremovenation(Player player){
        return new Messages(player).getMessage("bcremovenation");
    }
    public static String bcremovetown(Player player){
        return new Messages(player).getMessage("bcremovetown");
    }
    public static String kicksuc(Player player){
        return new Messages(player).getMessage("kicksuc");
    }
    public static String bccreatetown(Player player,Town town){
        return new Messages(player).getMessage("bccreatetown").replace("{townall}",town.getName());
    }
    public static String townnotisnation(Player player){
        return new Messages(player).getMessage("townnotisnation");
    }
    public static String bccreatenation(Player player, Nation nation){
        return new Messages(player).getMessage("bccreatenation").replace("{nationall}",nation.getName());
    }
    public static String bcremovetownall(Player player,Town town){
        return new Messages(player).getMessage("bcremovetownall").replace("{townall}",town.getName());
    }
    public static String bcremovenationall(Player player,Nation nation){
        return new Messages(player).getMessage("bcremovenationall").replace("{nationall}",nation.getName());
    }
    public static String kicktownSuccess(Player player){
        return new Messages(player).getMessage("kicktownSuccess");
    }
    public static String language(Player player){
        return new Messages(player).getMessage("language");
    }
    public static String playererror(Player player){
        return new Messages(player).getMessage("playererror");
    }
    public static String rankalrcomback(Player player){
        return new Messages(player).getMessage("rankalrcomback");
    }
    public static String transfersuc(Player player){
        return new Messages(player).getMessage("transfersuc");
    }
    public static String rankalrgive(Player player){
        return new Messages(player).getMessage("rankalrgive");
    }
    public static String transfert(Player player){
        return new Messages(player).getMessage("transfert");
    }
    public static String transfern(Player player){
        return new Messages(player).getMessage("transfern");
    }
    public static String args(Player player){
        return new Messages(player).getMessage("args");
    }
    public static String succreaterank(Player player){
        return new Messages(player).getMessage("succreaterank");
    }
    public static String townjoin(Player player){
        return new Messages(player).getMessage("townjoin");
    }
    public static String townleave(Player player){
        return new Messages(player).getMessage("townleave");
    }
    public static String suc(Player player){
        return new Messages(player).getMessage("suc");
    }
    public static String alrclaim(Player player){return  new Messages(player).getMessage("alrclaim");}
    public static String nationexist(Player player){return new Messages(player).getMessage("nationexist");}
    public static String changecapital(Player player){return new Messages(player).getMessage("nationexist");}
    public static String setnationname(Player player){return new Messages(player).getMessage("setnationname");}
    public static String kicktown(Player player){return new Messages(player).getMessage("kicktown");}
    public static String townnonation(Player player){return new Messages(player).getMessage("townnonation");}
    public static String notkickcapital(Player player){return new Messages(player).getMessage("notkickcapital");}
    public static String asksendtown(Player player){
        return new Messages(player).getMessage("asksendtown");
    }
    public static String rankg(Player player){
        return new Messages(player).getMessage("rankg");
    }
    public static String rankf(Player player){
        return new Messages(player).getMessage("rankf");
    }
    public static String rankalrexist(Player player){
        return new Messages(player).getMessage("rankalrexist");
    }
    public static String rankgivesuc(Player player){
        return new Messages(player).getMessage("rankgivesuc");
    }
    public static String rankexist(Player player){
        return new Messages(player).getMessage("rankexist");
    }
    public static String rankcombacksuc(Player player){
        return new Messages(player).getMessage("rankcombacksuc");
    }
    public static String townexist(Player player){
        return new Messages(player).getMessage("townexist");
    }
    public static String asksend(Player player){
        return new Messages(player).getMessage("asksend");
    }
    public static String nationdel(Player player){
        return new Messages(player).getMessage("nationdel");
    }
    public static String nationmayor(Player player){
        return new Messages(player).getMessage("nationmayor");
    }
    public static String nationalr(Player player){
        return new Messages(player).getMessage("nationalr");
    }
    public static String townfornation(Player player){
        return new Messages(player).getMessage("townfornation");
    }
    public static String createnationsuc(Player player){
        return new Messages(player).getMessage("createnationsuc");
    }
    public static String nationalrexist(Player player){
        return new Messages(player).getMessage("nationalrexist");
    }
    public static String createnationt(Player player){
        return new Messages(player).getMessage("createnationt");
    }
    public static String createnation(Player player){
        return new Messages(player).getMessage("createnation");
    }
    public static String nationnull(Player player){
        return new Messages(player).getMessage("nationnull");
    }
    public static String messageconsole(){
        return getInstance().getMessage("messageconsole");
    }
    public static String spawnset(Player player){
        return new Messages(player).getMessage("spawnset");
    }
    public static String create(Player player){
        return new Messages(player).getMessage("create");
    }
    public static String messagetownexist(Player player){
        return new Messages(player).getMessage("messagetownexist");
    }
    public static String commandundefined(Player player){
        return new Messages(player).getMessage("commandundefined");
    }
    public static String createtown(Player player){
        return new Messages(player).getMessage("createtown");
    }
    public static String townalrexist(Player player){
        return new Messages(player).getMessage("townalrexist");
    }
    public static String alrintown(Player player){
        return new Messages(player).getMessage("alrintown");
    }
    public static String rank(Player player){
        return new Messages(player).getMessage("rank");
    }
    public static String residentjoin(Player player){
        return new Messages(player).getMessage("residentjoin");
    }
    public static String residentleave(Player player){
        return new Messages(player).getMessage("residentleave");
    }
    public static String invite(Player player){
        return new Messages(player).getMessage("invite");
    }
    public static String alrinvite(Player player){
        return new Messages(player).getMessage("alrinvite");
    }
    public static String invitesend(Player player){
        return new Messages(player).getMessage("invitesend");
    }
    public static String inviteaprove(Player player){
        return new Messages(player).getMessage("inviteaprove");
    }
    public static String playeroffline(Player player){
        return new Messages(player).getMessage("playeroffline");
    }
    public static String alrinvitetown(Player player){
        return new Messages(player).getMessage("alrinvitetown");
    }
    public static String playernottown(Player player){
        return new Messages(player).getMessage("playernottown");
    }
    public static String setmapcolorsuc(Player player){
        return new Messages(player).getMessage("setmapcolorsuc");
    }
    public static String setname(Player player){
        return new Messages(player).getMessage("setname");
    }
    public static String setnamesuc(Player player){
        return  new Messages(player).getMessage("setnamesuc");
    }
    public static String setmapcolor(Player player){
        return new Messages(player).getMessage("setmapcolor");
    }
    public static String notkickmayor(Player player){
        return new Messages(player).getMessage("notkickmayor");
    }
    public static String kickplayer(Player player){
        return new Messages(player).getMessage("kickplayer");
    }
    public static String confirmation(Player player){
        return new Messages(player).getMessage("confirmation");
    }
    public static String permissionerror(Player player){
        return new Messages(player).getMessage("permissionerror");
    }
    public static String accept(Player player){
        return new Messages(player).getMessage("accept");
    }
    public static String accepterror(Player player){
        return new Messages(player).getMessage("accepterror");
    }
    public static String acceptconfirm(Player player){
        return new Messages(player).getMessage("acceptconfirm");
    }
    public static String deleteconfirm(Player player){
        return new Messages(player).getMessage("deleteconfirm");
    }
    public static String leaveconfirm(Player player){
        return new Messages(player).getMessage("leaveconfirm");
    }
    public static String leaveerror(Player player){
        return new Messages(player).getMessage("leaveerror");
    }
    public static String claimerror(Player player){
        return new Messages(player).getMessage("claimerror");
    }
    public static String goldnotenough(Player player){
        return new Messages(player).getMessage("goldnotenough");
    }
    public static String claimconfirm(Player player){
        return new Messages(player).getMessage("claimconfirm");
    }
    public static String error(Player player){
        return new Messages(player).getMessage("error");
    }
    public static String homeblockerror(Player player){
        return new Messages(player).getMessage("homeblockerror");
    }
    public static String chunkerror(Player player) {
        return new Messages(player).getMessage("chunkerror");
    }
    public static String unclaimconfirm(Player player){
        return new Messages(player).getMessage("unclaimconfirm");
    }
    public static String homeblockalr(Player player){
        return new Messages(player).getMessage("homeblockalr");
    }
    public static String homeblockconfirm(Player player){
        return new Messages(player).getMessage("homeblockconfirm");
    }
}
