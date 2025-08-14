package dev.m7wq.bungeecord.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;



@UtilityClass
public class TextHelper {



    public void sendMessage(ProxiedPlayer ProxiedPlayer, String message){
        ProxiedPlayer.sendMessage(colorize(message));
    }

    public void sendComponent(Audience audience, Component component){

        audience.sendMessage(component);

    }

    public String colorize(String message){
        return ChatColor.translateAlternateColorCodes('&',message);
    }

    public String prefixed(ProxiedPlayer player){
        LuckPerms luckPerms = LuckPermsProvider.get();

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user==null)
            throw new IllegalStateException("user is null");

        String prefix = user.getCachedData().getMetaData().getPrefix();

        return colorize(prefix+player.getName());
    }

}
