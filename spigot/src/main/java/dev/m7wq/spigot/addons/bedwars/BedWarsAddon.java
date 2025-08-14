package dev.m7wq.spigot.addons.bedwars;

import com.andrei1058.bedwars.api.BedWars;
import dev.m7wq.spigot.Plugin;
import dev.m7wq.spigot.abstraction.Addon;
import dev.m7wq.spigot.addons.bedwars.listeners.BedWarsMessagingListener;
import dev.m7wq.spigot.addons.bedwars.listeners.MainListener;
import dev.m7wq.spigot.addons.bedwars.managers.BedWarsManager;
import lombok.Getter;

public class BedWarsAddon implements Addon {


    @Getter
    static BedWarsAddon instance;

    @Getter
    BedWarsManager manager;

    @Getter
    BedWars API;

    @Override
    public void start(Plugin plugin) {

        instance=this;
        manager = new BedWarsManager();
        API = plugin.getServer().getServicesManager().getRegistration(BedWars.class).getProvider();

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,"BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,"BungeeCord",new BedWarsMessagingListener());

        plugin.getServer().getPluginManager().registerEvents(new MainListener(plugin),plugin);
    }
}
