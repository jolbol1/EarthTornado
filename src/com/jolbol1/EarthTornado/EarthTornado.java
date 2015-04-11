package com.jolbol1.EarthTornado;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;


public class EarthTornado {

    public static ConcurrentHashMap<Player, EarthTornado> instances = new ConcurrentHashMap<Player, EarthTornado>();
    private Player player;
    private Location location;
    private Vector direction;
    private double distanceTravelled;
    private boolean hasHit;
    private static int id;

    private double speed = ProjectKorra.plugin.getConfig().getDouble("ExtraAbilities.jolbol1.EarthTornado.Speed");
    private static double DAMAGE = ProjectKorra.plugin.getConfig().getDouble("ExtraAbilities.jolbol1.EarthTornado.Damage");
    private final long defaultchargetime = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.jolbol1.EarthTornado.ChargeTime");
    private int blocks = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.jolbol1.EarthTornado.Blocks");
    private long duration = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.jolbol1.EarthTornado.Duration");
    private long COOLDOWN = ProjectKorra.plugin.getConfig().getLong("ExtraAbilities.jolbol1.EarthTornado.Cooldown");

    private static int ids;
    private static int ID = Integer.MIN_VALUE;

    public  long starttime;
    private long chargetime = defaultchargetime;
    private boolean charged = false;


    public EarthTornado(Player player){
        if (instances.containsKey(player))
            return;
        starttime = System.currentTimeMillis();
        if (AvatarState.isAvatarState(player))
            chargetime = 0;
        this.player = player;
        instances.put(player, this);


    }



    private void progress(){
        if (System.currentTimeMillis() > starttime + chargetime ) {
            cancel();
            return;


        }


        if(!player.isSneaking()) {
            cancel();
            return;
        }

        if(player.isDead() || !player.isOnline()){
            cancel();
            return;
        }

        progressEarthTornado();
    }

    private void progressEarthTornado(){

        if(!player.isSneaking()) return;
        Block b = getTarget(player, 10);

          if(!(ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks").contains(b.getType().toString()))) return;

        if(Methods.getBendingPlayer(player.getName()).isOnCooldown("EarthTornado")) {
            cancel();
            return;
        }
       spawnTornado(ProjectKorra.plugin, b.getLocation(), b.getType(), (byte) 0, null, speed, blocks, duration, false, false);
        Methods.getBendingPlayer(player.getName()).addCooldown("EarthTornado", COOLDOWN);

    }

    private void doDamage(LivingEntity entity){
        hasHit = true;
        Methods.damageEntity(player, entity, DAMAGE);
        entity.setFireTicks(40);
    }

    private void cancel(){
        instances.remove(player);


    }

    public static void progressAll(){
        for(Player player : instances.keySet()){
            instances.get(player).progress();
        }
    }

    public static void removeAll(){
        instances.clear();
    }
    public void spawnTornado(
            final Plugin plugin,
            final Location  location,
            final Material material,
            final byte      data,
            final Vector direction,
            final double    speed,
            final int        amount_of_blocks,
            final long      time,
            final boolean    spew,
            final boolean    explode

    ) {

        class VortexBlock {

            private Entity entity;

            public boolean removable = true;

            private float ticker_vertical = 0.0f;
            private float ticker_horisontal = (float) (Math.random() * 2 * Math.PI);


            @SuppressWarnings("deprecation")
            public VortexBlock(Location l, Material m, byte d) {

                if (l.getBlock().getType() != Material.AIR) {

                    Block b = l.getBlock();
                    entity = l.getWorld().spawnFallingBlock(l, b.getType(), b.getData());

                    if (b.getType() != Material.WATER)
                        b.setType(Material.AIR);

                    removable = !spew;
                }
                else {
                    entity = l.getWorld().spawnFallingBlock(l, m, d);

                    removable = !explode;
                }

                addMetadata();
            }

            public VortexBlock(Entity e) {
                entity    = e;
                removable = false;
                addMetadata();
            }

            private void addMetadata() {
                entity.setMetadata("vortex", new FixedMetadataValue(plugin, "protected"));
            }

            public void remove() {
                if(removable) {
                    entity.remove();
                }
                entity.removeMetadata("vortex", plugin);
            }

            @SuppressWarnings("deprecation")
            public HashSet<VortexBlock> tick() {

                double radius    = Math.sin(verticalTicker()) * 2;
                float  horisontal = horisontalTicker();

                Vector v = new Vector(radius * Math.cos(horisontal), 0.5D, radius * Math.sin(horisontal));

                HashSet<VortexBlock> new_blocks = new HashSet<VortexBlock>();

                // Pick up blocks
                // Pick up blocks



                // Pick up other entities
                List<Entity> entities = entity.getNearbyEntities(1.0D, 1.0D, 1.0D);
                for(Entity e : entities) {

                    if(!e.hasMetadata("vortex")) {


                        if(e instanceof LivingEntity) {
                            if (((LivingEntity) e).getHealth() != 0) {
                                LivingEntity pl = (LivingEntity) e;
                                double health = pl.getHealth();
                                double newHealth = health - DAMAGE;
                                if (pl instanceof Player) {
                                    if(newHealth <= 0.5) {
                                        pl.setHealth(0.5);

                                    } else {
                                        Methods.damageEntity(player, e, DAMAGE);
                                    }


                                } else {
                                    if(newHealth <= 0) {
                                        ((LivingEntity) e).setHealth(0.0);
                                    } else {
                                        ((LivingEntity) e).setHealth(newHealth);
                                    }
                                }

                                if (pl instanceof Player) {

                                    Player ps = (Player) pl;
                                    ps.playSound(pl.getLocation(), Sound.FALL_BIG, 1, 1);
                                }
                                new_blocks.add(new VortexBlock(e));

                            }
                        }


                    }
                }

                setVelocity(v);

                return new_blocks;
            }

            private void setVelocity(Vector v) {
                entity.setVelocity(v);
            }

            private float verticalTicker() {
                if (ticker_vertical < 1.0f) {
                    ticker_vertical += 0.05f;
                }
                return ticker_vertical;
            }

            private float horisontalTicker() {
//                ticker_horisontal = (float) ((ticker_horisontal + 0.8f) % 2*Math.PI);
                return (ticker_horisontal += 0.8f);
            }
        }

        // Modify the direction vector using the speed argument.
        if (direction != null) {
            direction.normalize().multiply(speed);
        }

        // This set will contain every block created to make sure the metadata for each and everyone is removed.
        final HashSet<VortexBlock> clear = new HashSet<VortexBlock>();

        id = new BukkitRunnable() {

            private ArrayDeque<VortexBlock> blocks = new ArrayDeque<VortexBlock>();

            public void run() {

                if (direction != null) {
                    location.add(direction);
                }

                // Spawns 10 blocks at the time.
                for (int i = 0; i < 10; i++) {
                    checkListSize();
                    VortexBlock vb = new VortexBlock(location, material, data);
                    blocks.add(vb);
                    clear.add(vb);
                }

                // Make all blocks in the list spin, and pick up any blocks that get in the way.
                ArrayDeque<VortexBlock> que = new ArrayDeque<VortexBlock>();

                for (VortexBlock vb : blocks) {

                    HashSet<VortexBlock> new_blocks = vb.tick();

                    for(VortexBlock temp : new_blocks) {
                        que.add(temp);
                    }
                }

                // Add the new blocks
                for(VortexBlock vb : que) {
                    checkListSize();
                    blocks.add(vb);
                    clear.add(vb);
                }
            }

            // Removes the oldest block if the list goes over the limit.
            private void checkListSize() {
                while(blocks.size() >= amount_of_blocks) {
                    VortexBlock vb = blocks.getFirst();
                    vb.remove();

                    blocks.remove(vb);
                    clear.remove(vb);
                }
            }
        }.runTaskTimer(plugin, 5L, 5L).getTaskId();



        // Stop the "tornado" after the given time.

        new BukkitRunnable() {
            public void run() {
                for(VortexBlock vb : clear) {
                    vb.remove();

                }
                plugin.getServer().getScheduler().cancelTask(id);

            }
        }.runTaskLater(plugin, time);
    }

    public final Block getTarget(Player player, Integer range) {
        BlockIterator bi= new BlockIterator(player, range);
        Block lastBlock = bi.next();
        while (bi.hasNext()) {
            lastBlock = bi.next();
            if (lastBlock.getType() == Material.AIR)
                continue;
            break;
        }
        return lastBlock;
    }

    public static void removeTornadoBlocks() {
        ProjectKorra.plugin.getServer().getScheduler().cancelTask(id);

        for(World w : Bukkit.getWorlds()) {
            for(Entity e : w.getEntities()){
                if(e.hasMetadata("vortex")) {
                    if(e instanceof LivingEntity) {
                        e.removeMetadata("vortex", ProjectKorra.plugin);
                    } else {
                        e.remove();
                        e.removeMetadata("vortex", ProjectKorra.plugin);
                    }

                }
            }
        }
    }








}