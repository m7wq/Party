package dev.m7wq.spigot;

import dev.m7wq.spigot.abstraction.Addon;
import dev.m7wq.spigot.addons.bedwars.BedWarsAddon;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {

        Addon addon = null;

        if (isWorking("BedWars1058")){
            addon = new BedWarsAddon();
        }

        assert addon != null;
        addon.start(this);

    }

    boolean isWorking(String string){
        return getServer().getPluginManager().getPlugin(string)!=null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}
