package dev.m7wq.bungeecord.entity;

import dev.m7wq.bungeecord.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;


@AllArgsConstructor
@Getter@Setter
public class PartyMember {
    ProxiedPlayer player;
    Role role;
}
