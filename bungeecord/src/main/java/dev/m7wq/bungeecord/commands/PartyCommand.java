package dev.m7wq.bungeecord.commands;

import dev.m7wq.bungeecord.Plugin;
import dev.m7wq.bungeecord.manager.PartyManager;
import dev.m7wq.bungeecord.utils.TextHelper;
import lombok.AllArgsConstructor;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class PartyCommand extends Command implements TabExecutor {

    private final PartyManager manager;
    private final BungeeAudiences audiences;

    public PartyCommand(Plugin plugin, PartyManager manager, BungeeAudiences audiences) {
        super("party", null, "p");
        this.manager = manager;
        this.audiences = audiences;
        plugin.getProxy().getPluginManager().registerCommand(plugin,
                new PartyChatCommand(manager));
    }


    public static class PartyChatCommand extends Command{

        private final PartyManager manager;

        public PartyChatCommand(PartyManager manager) {
            super("pc");
            this.manager = manager;
        }

        @Override
        public void execute(CommandSender commandSender, String[] strings) {

            String message = String.join(" ",strings);

            if (!(commandSender instanceof ProxiedPlayer player))
                return;

            manager.chat(player,message);


        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Players only!"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            // Show help if no args
            sendHelp(player);
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(player);
                break;
            case "invite":
                if (args.length < 2) {
                    TextHelper.sendMessage(player,"&cUsage: /party invite <player>");
                    return;
                }
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    TextHelper.sendMessage(player,"&cPlayer not found!");
                    return;
                }
                if (target.equals(player)) {
                    TextHelper.sendMessage(player,"&cYou can't invite yourself!");
                    return;
                }
                manager.invite(player, target, audiences.player(target));
                break;

            case "list":
                manager.list(player);
                break;
            case "disband":
                manager.disband(player);
                break;
            case "accept":
                if (args.length < 2) {
                    TextHelper.sendMessage(player, "&cUsage: /party accept <player>");
                    return;
                }
                target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target != null) {
                    manager.accept(player, target);
                } else {
                    TextHelper.sendMessage(player, "&cPlayer not found!");
                }
                break;

            case "chat":
                if (args.length< 2) {
                    TextHelper.sendMessage(player, "&cUsage: /party chat <message>");
                    return;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                manager.chat(player, message);
                break;

            case "promote":
                doSimplePlayerAction(args, player, manager::promote);
                break;

            case "demote":
                doSimplePlayerAction(args, player, manager::demote);
                break;

            case "kick":
                doSimplePlayerAction(args, player, manager::kick);
                break;

            case "leave":
                manager.leave(player);
                break;

            case "warp":
                manager.warp(player);
                break;

            default:
                TextHelper.sendMessage(player, "&cUnknown subcommand! Use /party help");
        }
    }

    private void doSimplePlayerAction(String[] args, ProxiedPlayer player, BiAction action) {
        if (args.length < 2) {
            TextHelper.sendMessage(player, "&cUsage: /party " + args[0] + " <player>");
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
        if (target == null) {
            TextHelper.sendMessage(player, "&cPlayer not found!");
            return;
        }
        action.run(player, target);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        
        List<String> subcommands = Arrays.asList(
                "help", "invite", "list", "disband",
                "accept", "chat", "promote", "demote",
                "kick", "leave", "warp"
        );

        if (args.length == 1) {
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }


        if (args.length==2 && Arrays.asList("invite","accept","promote","demote","kick").contains(args[0].toLowerCase())) {
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name->name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private interface BiAction {
        void run(ProxiedPlayer sender, ProxiedPlayer target);
    }

    private void sendHelp(ProxiedPlayer player) {
        TextHelper.sendMessage(player, "&8&m--------------------");
        TextHelper.sendMessage(player, "&e/party invite <player> &7- Invite a player");
        TextHelper.sendMessage(player, "&e/party accept <player> &7- Accept an invite");
        TextHelper.sendMessage(player, "&e/party list &7- Show party members");
        TextHelper.sendMessage(player, "&e/party disband &7- Disband your party");
        TextHelper.sendMessage(player, "&e/party leave &7- Leave your party");
        TextHelper.sendMessage(player, "&e/party chat <msg> &7- Party chat");
        TextHelper.sendMessage(player, "&e/party promote <player>");
        TextHelper.sendMessage(player, "&e/party demote <player>");
        TextHelper.sendMessage(player, "&e/party kick <player>");
        TextHelper.sendMessage(player, "&e/party warp &7- Warp party");
        TextHelper.sendMessage(player, "&8&m--------------------");
    }
}

