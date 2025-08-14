package dev.m7wq.bungeecord.manager;


import dev.m7wq.bungeecord.Plugin;
import dev.m7wq.bungeecord.entity.Invite;
import dev.m7wq.bungeecord.entity.PartyMember;
import dev.m7wq.bungeecord.enums.Permission;
import dev.m7wq.bungeecord.enums.Role;
import dev.m7wq.bungeecord.utils.TextHelper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class PartyManager {

    Plugin plugin;

    public PartyManager(Plugin plugin){
        this.plugin = plugin;

    }

    Set<Invite> invites = new HashSet<>();
    HashSet<List<PartyMember>> parties = new HashSet<>();

    public void warp(ProxiedPlayer player){

        List<PartyMember> partyMemberList = getPartyMemberList(player);
        PartyMember partyMember = getPartyMember(player);

        if (partyMember==null){
            TextHelper.sendMessage(player,"&cYou arent in any party");
            return;
        }

        if (!partyMember.getRole().getPermissions().contains(Permission.WARP))
        {
            TextHelper.sendMessage(player,"&cYou dont have enough permission.");
            return;
        }


        List<String> warps = warpUsage(player);

        if (!warps.isEmpty()){
            warps.forEach( name ->
                    TextHelper.sendMessage(player, "&eWarped: &f"+name)
            );
        }





    }

    public List<String> warpUsage(ProxiedPlayer player){

        List<PartyMember> partyMemberList = getPartyMemberList(player);
        PartyMember partyMember = getPartyMember(player);

        if (partyMember==null){

            return null;
        }

        if (!partyMember.getRole().getPermissions().contains(Permission.WARP))
        {

            return null;
        }

        List<String> warpedNames = new ArrayList<>();

        ServerInfo serverInfo = player.getServer().getInfo();
        try {


            if (serverInfo.getName().equals("BedWars")) {


                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DataOutputStream data = new DataOutputStream(output);


                List<ProxiedPlayer> proxiedPlayers = partyMemberList.stream()
                        .map(PartyMember::getPlayer).collect(Collectors.toList());

                proxiedPlayers.forEach(
                        proxiedPlayer ->
                        {
                            if (!proxiedPlayer.getServer().getInfo().getName().equals(serverInfo.getName()))
                                proxiedPlayer.connect(serverInfo);

                        }

                );

                data.writeUTF("BEDWARS_WARP");

                String players = proxiedPlayers.stream().map(proxiedPlayer->proxiedPlayer.getUniqueId().toString())
                        .collect(Collectors.joining(","));

                data.writeUTF(players);

                data.writeUTF(player.getUniqueId().toString());

                player.getServer().sendData("BungeeCord", output.toByteArray());


            }else {

                List<ProxiedPlayer> proxiedPlayers = partyMemberList.stream()
                        .map(PartyMember::getPlayer).collect(Collectors.toList());

                proxiedPlayers.forEach(
                        proxiedPlayer ->
                        {
                            if (!proxiedPlayer.getServer().getInfo().getName().equals(serverInfo.getName())) {
                                proxiedPlayer.connect(serverInfo);
                                warpedNames.add(proxiedPlayer.getName());
                            }



                        }

                );

            }
        }catch (IOException e){
            e.printStackTrace();;
        }


        return warpedNames;


    }

    public void invite(ProxiedPlayer inviter, ProxiedPlayer invited, Audience invitedAudience) {

        if (parties.stream()
                .anyMatch(list -> list.stream()
                        .anyMatch(partyMember -> partyMember.getPlayer().equals(inviter) && !(partyMember.getRole() == Role.LEADER || partyMember.getRole() == Role.MODERATOR)))) {
            TextHelper.sendMessage(inviter, "&cYou dont have permission");
            return;
        }


        if (invites.stream().anyMatch(invite -> invite.getInviter().equals(inviter) && invite.getInvited().equals(invited))) {
            TextHelper.sendMessage(inviter, "&cYou already invited this ProxiedPlayer to your party");
            return;
        }

        if (invites.stream().anyMatch(invite -> invite.getInviter().equals(invited))) {
            TextHelper.sendMessage(inviter, "&cThis ProxiedPlayer already sent an invite to another ProxiedPlayer");
            return;
        }

        if (isPartyMember(invited)) {
            TextHelper.sendMessage(inviter, "&cThis ProxiedPlayer is already into a party");
            return;
        }

        String inviteText = TextHelper.prefixed(inviter) + " &7has invited " + TextHelper.prefixed(invited) + " &7he has 60 seconds to accept";
        TextHelper.sendMessage(inviter, inviteText);

        String invitedText = TextHelper.colorize(TextHelper.prefixed(inviter) + " has sent to you an invite to his party");

        String buttonText = TextHelper.colorize("&r    &a&l(ACCEPT THE INVITE)");
        Component button = Component.text(buttonText)
                .hoverEvent(HoverEvent.showText(Component.text(TextHelper.colorize("&aAccept the party"))))
                .clickEvent(ClickEvent.runCommand("/party accept " + inviter.getName()));

        Component invitedComponent = Component.text(invitedText).appendNewline().append(button);

        invitedAudience.sendMessage(invitedComponent);


        invites.add(new Invite(inviter, invited, 60));

        final ScheduledTask[] holder = new ScheduledTask[1];
        holder[0] = ProxyServer.getInstance().getScheduler().schedule(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        Optional<Invite> inviteOptional = invites.stream()
                                .filter(invite1 -> invite1.getInviter().equals(inviter)
                                        && invite1.getInvited().equals(invited))
                                .findFirst();

                        if (!inviteOptional.isPresent()) {
                            holder[0].cancel(); // cancel here
                            return;
                        }

                        Invite invite = inviteOptional.get();
                        long time = invite.getRemainingTime();

                        if (time > 0) {
                            invite.setRemainingTime(time - 1);
                        } else {
                            invites.remove(invite);
                            holder[0].cancel();
                        }
                    }
                },
                0, 1, TimeUnit.SECONDS
        );





    }


    public void accept(ProxiedPlayer accepter, ProxiedPlayer target) {


        if (!invites.stream().anyMatch(invite -> invite.getInviter().equals(target) && invite.getInvited().equals(accepter))) {
            TextHelper.sendMessage(accepter, "&cYou dont have an invite from this ProxiedPlayer");
            return;
        }

        if (!isPartyMember(target)) {
            List<PartyMember> list = new ArrayList<>();
            list.add(new PartyMember(target, Role.LEADER));
            parties.add(list);

        }

        getPartyMemberList(target).add(new PartyMember(accepter, Role.MEMBER));

        invites.removeIf(invite->invite.getInviter().equals(target) && invite.getInvited().equals(accepter));

        for (PartyMember member : getPartyMemberList(target)) {
            TextHelper.sendMessage(member.getPlayer(), "&a[+]  " + TextHelper.prefixed(accepter) + " &ahas joined the party");
        }
    }

    public void chat(ProxiedPlayer ProxiedPlayer, String message) {

        String prefix = TextHelper.colorize("&5Party &8» ");

        if (!isPartyMember(ProxiedPlayer)) {
            TextHelper.sendMessage(ProxiedPlayer, "&cYou aren't in any party");
            return;
        }

        getPartyMemberList(ProxiedPlayer).forEach(member -> {
            member.getPlayer().sendMessage(prefix + " " + TextHelper.prefixed(ProxiedPlayer) + TextHelper.colorize("&r: " + message));
        });

    }

    public void disband(ProxiedPlayer ProxiedPlayer){


        PartyMember member = getPartyMember(ProxiedPlayer);
        List<PartyMember> partyMemberList = getPartyMemberList(ProxiedPlayer);

        if (member == null) {
            TextHelper.sendMessage(ProxiedPlayer, "&cYou aren't in any party");
            return;
        }

        if (!member.getRole().getPermissions().contains(Permission.DISBAND)){
            TextHelper.sendMessage(ProxiedPlayer,"&cYou dont have enough permission.");
            return;
        }

        partyMemberList.forEach(
                partyMember -> TextHelper.sendMessage(partyMember.getPlayer(),"&cYour party has been disbanded!")
        );


        parties.removeIf(party->party.stream().anyMatch(partyMember -> partyMember.getPlayer().equals(ProxiedPlayer)));


    }

    public void disbandUsage(ProxiedPlayer ProxiedPlayer){
        PartyMember member = getPartyMember(ProxiedPlayer);
        List<PartyMember> partyMemberList = getPartyMemberList(ProxiedPlayer);

        if (member == null) {

            return;
        }

        if (!member.getRole().getPermissions().contains(Permission.DISBAND)){

            return;
        }

        partyMemberList.forEach(
                partyMember -> TextHelper.sendMessage(partyMember.getPlayer(),"&cYour party has been disbanded because your leader has left the server!")
        );


        parties.removeIf(party->party.stream().anyMatch(partyMember -> partyMember.getPlayer().equals(ProxiedPlayer)));
    }

    public void leave(ProxiedPlayer leaver){
        List<PartyMember> list = getPartyMemberList(leaver);

        if (list==null) {
            TextHelper.sendMessage(leaver, "&cYou arent in any party");
            return;
        }

        list.removeIf(partyMember -> partyMember.getPlayer().equals(leaver));

        list.forEach(member->{
            TextHelper.sendMessage(member.getPlayer(), "&c[-] "+TextHelper.prefixed(leaver)+" &ahas left the party");
        });
    }
    
    public void leaveUsage(ProxiedPlayer leaver){
        List<PartyMember> list = getPartyMemberList(leaver);

        if (list==null) {
       
            return;
        }

        list.removeIf(partyMember -> partyMember.getPlayer().equals(leaver));

        list.forEach(member->{
            TextHelper.sendMessage(member.getPlayer(), "&c[-] "+TextHelper.prefixed(leaver)+" &ahas left the party");
        });
    }

    public void list(ProxiedPlayer ProxiedPlayer){

        List<PartyMember> list = getPartyMemberList(ProxiedPlayer);

        if (list==null) {
            TextHelper.sendMessage(ProxiedPlayer,"&cYou arent in any party");
            return;
        }

        String leader = TextHelper.prefixed(list.stream().filter(partyMember -> partyMember.getRole()==Role.LEADER).findFirst().get().getPlayer());

        StringBuilder moderators = new StringBuilder();
        StringBuilder members = new StringBuilder();

        for (PartyMember member : list) {
            if (member.getRole()==Role.MODERATOR) {
                moderators.append(TextHelper.prefixed(member.getPlayer()));
                if (!list.get(list.size() - 1).equals(member))
                    moderators.append(TextHelper.colorize("&f, "));
            }else if (member.getRole()==Role.MEMBER){
                members.append(TextHelper.prefixed(member.getPlayer()));
                if (!list.get(list.size() - 1).equals(member))
                    members.append(TextHelper.colorize("&f, "));
            }



        }





        TextHelper.sendMessage(ProxiedPlayer, "&8&m------------------------------");
        TextHelper.sendMessage(ProxiedPlayer, "&r ");
        TextHelper.sendMessage(ProxiedPlayer, "&eLeader&f: " + leader);
        TextHelper.sendMessage(ProxiedPlayer, "&r ");
        TextHelper.sendMessage(ProxiedPlayer, "&eModerators&f: " + moderators);
        TextHelper.sendMessage(ProxiedPlayer, "&r ");
        TextHelper.sendMessage(ProxiedPlayer, "&eMembers&f: " + members);
        TextHelper.sendMessage(ProxiedPlayer, "&r ");
        TextHelper.sendMessage(ProxiedPlayer, "&8&m------------------------------");



    }

    public void kick(ProxiedPlayer kicker, ProxiedPlayer target){


        PartyMember kickerMember = getPartyMember(kicker);

        if (kickerMember==null){

            TextHelper.sendMessage(kicker,"&cYou aren't in any party");
            return;

        }

        if (!kickerMember.getRole().getPermissions().contains(Permission.KICK)){
            TextHelper.sendMessage(kicker,"&cYou dont enough have permission");
            return;
        }

        PartyMember ProxiedPlayerMember = getPartyMember(target);

        if (ProxiedPlayerMember == null)
        {
            TextHelper.sendMessage(kicker,"&cThis ProxiedPlayer isn't in any party");
            return;
        }



        if (!getPartyMemberList(kicker).contains(ProxiedPlayerMember)){
            TextHelper.sendMessage(kicker,"&cThis ProxiedPlayer isn't in your party");
            return;
        }

        getPartyMemberList(kicker).removeIf(member->member.getPlayer().equals(target));

        TextHelper.sendMessage(kicker,"&aProxiedPlayer has been kicked successfully!");
        TextHelper.sendMessage(target,"&cYou have been kicked from the party.");
    }




    public void promote(ProxiedPlayer promoter, ProxiedPlayer ProxiedPlayer){

        PartyMember promoterMember = getPartyMember(promoter);

        if (promoterMember==null){

            TextHelper.sendMessage(promoter,"&cYou aren't in any party");
            return;

        }

        if (!promoterMember.getRole().getPermissions().contains(Permission.PROMOTE)){
            TextHelper.sendMessage(promoter,"&cYou dont enough have permission");
            return;
        }

        PartyMember ProxiedPlayerMember = getPartyMember(ProxiedPlayer);

        if (ProxiedPlayerMember == null)
        {
            TextHelper.sendMessage(promoter,"&cThis ProxiedPlayer isn't in any party");
            return;
        }



        if (!getPartyMemberList(promoter).contains(ProxiedPlayerMember)){
            TextHelper.sendMessage(promoter,"&cThis ProxiedPlayer isn't in your party");
            return;
        }

        Role promotedRole = ProxiedPlayerMember.getRole().upper();

        if (promotedRole==null){
            TextHelper.sendMessage(promoter,"&cThis ProxiedPlayer cannot be promoted anymore");
            return;
        }


        ProxiedPlayerMember.setRole(promotedRole);

        TextHelper.sendMessage(promoter,"&aProxiedPlayer has been promoted successfully");
        TextHelper.sendMessage(ProxiedPlayer,"&aYou have been promoted to "+promotedRole.getName());


    }
    public void demote(ProxiedPlayer demoter,ProxiedPlayer ProxiedPlayer){
        PartyMember demoterMember = getPartyMember(demoter);

        if (demoterMember==null){

            TextHelper.sendMessage(demoter,"&cYou aren't in any party");
            return;

        }

        if (!demoterMember.getRole().getPermissions().contains(Permission.DEMOTE)){
            TextHelper.sendMessage(demoter,"&cYou dont enough have permission");
            return;
        }

        PartyMember ProxiedPlayerMember = getPartyMember(ProxiedPlayer);

        if (ProxiedPlayerMember == null)
        {
            TextHelper.sendMessage(demoter,"&cThis ProxiedPlayer isn't in any party");
            return;
        }



        if (!getPartyMemberList(demoter).contains(ProxiedPlayerMember)){
            TextHelper.sendMessage(demoter,"&cThis ProxiedPlayer isn't in your party");
            return;
        }

        Role demotedRole = ProxiedPlayerMember.getRole().lower();



        if (demotedRole==null){
            TextHelper.sendMessage(demoter,"&cThis ProxiedPlayer cannot be demoted anymore");
            return;
        }

        ProxiedPlayerMember.setRole(demotedRole);

        TextHelper.sendMessage(demoter,"&aProxiedPlayer has been demoted successfully");
        TextHelper.sendMessage(ProxiedPlayer,"&aYou have been demoted to "+demotedRole.getName());
    }


    boolean isPartyMember(ProxiedPlayer ProxiedPlayer){
        return parties.stream()
                .anyMatch(list->list.stream()
                        .anyMatch(partyMember -> partyMember.getPlayer().equals(ProxiedPlayer)));
    }

    public PartyMember getPartyMember(ProxiedPlayer ProxiedPlayer){

        for (List<PartyMember> partyMemberList : parties){
            for (PartyMember partyMember : partyMemberList){
                if (partyMember.getPlayer().equals(ProxiedPlayer))
                    return partyMember;
            }
        }
        return null;
    }

    public List<PartyMember> getPartyMemberList(ProxiedPlayer contained){
        for (List<PartyMember> partyMemberList : parties){
            for (PartyMember partyMember : partyMemberList){
                if (partyMember.getPlayer().equals(contained))
                    return partyMemberList;
            }
        }
        return null;
    }

}
