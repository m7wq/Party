package dev.m7wq.bungeecord.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Role {
    LEADER("&eLeader",Arrays.asList(
            Permission.DISBAND,
            Permission.INVITE,
            Permission.PROMOTE,
            Permission.DEMOTE,
            Permission.WARP,
            Permission.KICK
    )),
    MODERATOR("&2Moderator",Arrays.asList(
            Permission.INVITE,
            Permission.WARP

    )),
    MEMBER("&9Member",new ArrayList<>());

    public String name;
    public List<Permission> permissions;


    public Role lower(){
        if (this==MODERATOR)
            return MEMBER;

        return null;

    }

    public Role upper(){
        if (this==MEMBER){
            return MODERATOR;
        }

        return null;
    }





}
