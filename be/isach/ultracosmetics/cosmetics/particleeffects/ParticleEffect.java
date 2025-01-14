package be.isach.ultracosmetics.cosmetics.particleeffects;

import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.Core;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.util.UtilParticles;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Created by sacha on 03/08/15.
 */
public abstract class ParticleEffect implements Listener {

    private Material material;
    private Byte data;
    private String name;

    boolean moving;

    private ParticleEffectType type = ParticleEffectType.DEFAULT;

    private String permission;

    int repeatDelay = 1;

    private UUID owner;

    private Listener listener;

    private Effect effect;

    public ParticleEffect(final Effect effect, Material material, Byte data, String configName, String permission, final UUID owner, final ParticleEffectType type, int repeatDelay) {
        this.material = material;
        this.data = data;
        this.name = configName;
        this.permission = permission;
        this.type = type;
        this.effect = effect;
        this.repeatDelay = repeatDelay;
        if (owner != null) {
            this.owner = owner;
            if (!getPlayer().hasPermission(permission)) {
                getPlayer().sendMessage(MessageManager.getMessage("No-Permission"));
                return;
            }
            if (Core.getCustomPlayer(getPlayer()).currentParticleEffect != null)
                Core.getCustomPlayer(getPlayer()).removeParticleEffect();
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (Bukkit.getPlayer(owner) != null
                                && Core.getCustomPlayer(Bukkit.getPlayer(owner)).currentParticleEffect != null
                                && Core.getCustomPlayer(Bukkit.getPlayer(owner)).currentParticleEffect.getType() == type) {
                            if (getType() != ParticleEffectType.FROZENWALK
                                    && getType() != ParticleEffectType.ENCHANTED
                                    && getType() != ParticleEffectType.MUSIC) {
                                if (moving) {
                                    UtilParticles.play(getPlayer().getLocation().add(0, 1, 0), effect, 0, 0, .4f, .3f, .4f, 0, 3);
                                    moving = false;
                                } else {
                                    if (!moving)
                                        onUpdate();
                                }
                            } else
                                onUpdate();
                        } else {
                            cancel();
                        }

                    } catch (NullPointerException exc) {
                        clear();
                        cancel();
                    }
                }
            };
            runnable.runTaskTimerAsynchronously(Core.getPlugin(), 0, repeatDelay);
            listener = new ParticleEffectListener(this);

            getPlayer().sendMessage(MessageManager.getMessage("Particle-Effects.Summon").replace("%effectname%", (Core.placeHolderColor) ? getName() : Core.filterColor(getName())));
            Core.getCustomPlayer(getPlayer()).currentParticleEffect = this;
        }
    }

    public Effect getEffect() {
        return effect;
    }

    public String getConfigName() {
        return name;
    }

    public String getName() {
        return MessageManager.getMessage("Particle-Effects." + name + ".name");
    }

    public Material getMaterial() {
        return this.material;
    }


    public ParticleEffectType getType() {
        return this.type;
    }

    public Byte getData() {
        return this.data;
    }

    abstract void onUpdate();

    public void clear() {
        Core.getCustomPlayer(getPlayer()).currentParticleEffect = null;
        try {
            HandlerList.unregisterAll(this);
            HandlerList.unregisterAll(listener);
        } catch (Exception exc) {
        }
        if (getPlayer() != null)
            getPlayer().sendMessage(MessageManager.getMessage("Particle-Effects.Unsummon").replace("%mountname%", (Core.placeHolderColor) ? getName() : Core.filterColor(getName())));
        owner = null;
    }

    protected UUID getOwner() {
        return owner;
    }

    protected Player getPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public class ParticleEffectListener implements Listener {
        private ParticleEffect particleEffect;

        public ParticleEffectListener(ParticleEffect particleEffect) {
            this.particleEffect = particleEffect;
            Core.registerListener(this);
        }

        @EventHandler
        public void onMove(PlayerMoveEvent event) {
            if (getPlayer() == event.getPlayer()
                    && (event.getFrom().getX() != event.getTo().getX()
                    || event.getFrom().getY() != event.getTo().getY()
                    || event.getFrom().getZ() != event.getTo().getZ()))
                particleEffect.moving = true;
        }

    }

    public enum ParticleEffectType {

        DEFAULT("", ""),
        RAINCLOUD("ultracosmetics.particleeffects.raincloud", "RainCloud"),
        SNOWCLOUD("ultracosmetics.particleeffects.snowcloud", "SnowCloud"),
        BLOODHELIX("ultracosmetics.particleeffects.bloodhelix", "BloodHelix"),
        FROSTLORD("ultracosmetics.particleeffects.frostlord", "FrostLord"),
        FLAMERINGS("ultracosmetics.particleeffects.flamerings", "FlameRings"),
        INLOVE("ultracosmetics.particleeffects.inlove", "InLove"),
        GREENSPARKS("ultracosmetics.particleeffects.greensparks", "GreenSparks"),
        FROZENWALK("ultracosmetics.particleeffects.frozenwalk", "FrozenWalk"),
        MUSIC("ultracosmetics.particleeffects.music", "Music"),
        ENCHANTED("ultracosmetics.particleeffects.enchanted", "Enchanted"),
        INFERNO("ultracosmetics.particleeffects.inferno", "Inferno");


        String permission;
        String configName;

        ParticleEffectType(String permission, String configName) {
            this.permission = permission;
            this.configName = configName;
        }

        public String getPermission() {
            return permission;
        }

        public boolean isEnabled() {
            return SettingsManager.getConfig().get("Particle-Effects." + configName + ".Enabled");
        }

    }

}
