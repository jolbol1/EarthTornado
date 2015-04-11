package com.jolbol1.EarthTornado;


import org.bukkit.permissions.PermissionDefault;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModule;

public class EarthTornadoAddon extends AbilityModule{

    public static EarthTornadoAddon ability;

    public EarthTornadoAddon() {
        super("EarthTornado");
    }

    public String getAuthor(){
        return "jolbol1";
    }

    public String getVersion(){
        return "V1.0";
    }

    public boolean isHarmlessAbility(){
        return false;
    }

    public boolean isShiftAbility(){
        return true;
    }

    public String getElement(){
        return Element.Fire.toString();
    }

    public void onThisLoad(){
        loadConfig();
        ProjectKorra.plugin.getLogger().info((new StringBuilder()).append(getName() + " ").append(getVersion()).append(" developed by ").append(getAuthor()).append(" has been loaded!").toString());
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new EarthTornadoListener(), ProjectKorra.plugin);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(new EarthTornadoPermissions().permission);
        ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.EarthTornado").setDefault(PermissionDefault.TRUE);
        ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new EarthTornadoManager(), 0L, 1L);
    }

    public void loadConfig(){
        ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.Speed", Double.valueOf(0.3));
        ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.Damage", Double.valueOf(5.0));
         ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.ChargeTime", Long.valueOf(200));
         ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.Blocks", Integer.valueOf(60));
         ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.Duration", Long.valueOf(100));
         ProjectKorra.plugin.getConfig().addDefault("ExtraAbilities.jolbol1.EarthTornado.Cooldown", Long.valueOf(2000));
    }

    public String getDescription(){
        return "Developed by: " + getAuthor()
                + "\nTo use, simply Left-Click in the direction you are looking to shoot a fireball!";
    }

    public void stop(){
        EarthTornado.removeTornadoBlocks();
        ProjectKorra.plugin.getLogger().info((new StringBuilder()).append(getName()).append(" addon ability disabled.").toString());
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(new EarthTornadoPermissions().permission);
        EarthTornado.removeAll();
    }
}
