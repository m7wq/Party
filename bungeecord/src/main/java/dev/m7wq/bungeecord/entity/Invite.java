package dev.m7wq.bungeecord.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;


@Getter@Setter
@AllArgsConstructor
public class Invite {
    ProxiedPlayer inviter;
    ProxiedPlayer invited;
    long remainingTime;
}
