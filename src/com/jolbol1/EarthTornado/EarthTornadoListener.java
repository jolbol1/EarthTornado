package com.jolbol1.EarthTornado;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthTornadoListener implements Listener {



    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeftClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
      if(canUse(player)){
            new EarthTornado(player);


        }
    }

    @EventHandler
    public void onBlockPlace(EntityChangeBlockEvent e) {
        if(e.getEntity().hasMetadata("vortex")) {
            e.getEntity().remove();

            e.setCancelled(true);
        }
    }




    private boolean canUse(Player player){
        if(Methods.getBoundAbility(player) == null)
            return false;
        if(!Methods.canBend(player.getName(), "EarthTornado"))
            return false;
        if(Methods.isRegionProtectedFromBuild(player, "EarthTornado", player.getLocation()))
            return false;
        if(Methods.getBendingPlayer(player.getName()).isOnCooldown("EarthTornado"))
            return false;
        if(Methods.getBoundAbility(player).equalsIgnoreCase("EarthTornado"))
            return true;
        return false;
    }

}

