package org.gielinor.game.node.entity.combat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.gielinor.game.content.skill.Skills;
import org.gielinor.game.content.skill.member.summoning.familiar.Familiar;
import org.gielinor.game.content.skill.member.summoning.pet.Pet;
import org.gielinor.game.node.entity.Entity;
import org.gielinor.game.node.entity.npc.NPC;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.entity.player.info.Ironman;
import org.gielinor.game.node.entity.player.link.prayer.PrayerType;
import org.gielinor.game.node.item.Item;
import org.gielinor.game.world.GameConstants;
import org.gielinor.game.world.World;
import org.gielinor.game.world.map.zone.ZoneType;
import org.gielinor.rs2.model.container.impl.Equipment;
import org.gielinor.rs2.pulse.Pulse;

import plugin.interaction.item.MaxCapePlugin;

/**
 * Class used for handling combat impacts.
 *
 * @author Emperor
 */
public final class ImpactHandler {

    /**
     * The entity.
     */
    private final Entity entity;

    /**
     * The amount of ticks impacts have been disabled for.
     */
    private int disabledTicks;

    /**
     * The impact log.
     */
    private final Map<Entity, Integer> impactLog = new HashMap<>();

    /**
     * Gets the current hitsplats to show.
     */
    private final Queue<Impact> impactQueue = new LinkedList<Impact>();//Impact[] hitsplats = new Impact[2];

    /**
     * The last entity to impact this entity
     */
    private Entity lastImpactEntity;

    /**
     * If this is a cannon impact.
     */
    private boolean cannon;

    /**
     * Constructs a new {@code ImpactHandler} {@code Object}.
     *
     * @param entity The entity.
     */
    public ImpactHandler(Entity entity) {
        this.entity = entity;
    }

    /**
     * Manually hits the entity.
     *
     * @param source The entity dealing the hit.
     * @param hit    The amount to hit.
     * @param type   The hitsplat type.
     * @return The impact object.
     */
    public Impact manualHit(Entity source, int hit, HitsplatType type) {
        if (hit > entity.getSkills().getLifepoints()) {
            hit = entity.getSkills().getLifepoints();
        }
        return handleImpact(source, hit, null, null, type);
    }

    /**
     * Manually hits the entity.
     *
     * @param source The entity dealing the hit.
     * @param hit    The amount to hit.
     * @param type   The hitsplat type.
     * @param ticks  The delay before handling the hit.
     * @return The impact object (or null if a pulse got submitted).
     */
    public Impact manualHit(final Entity source, int hit, final HitsplatType type, int ticks) {
        if (ticks > 0) {
            final int damage = hit;
            World.submit(new Pulse(ticks, entity) {

                @Override
                public boolean pulse() {
                    manualHit(source, damage, type);
                    return true;
                }
            });
            return null;
        }
        return manualHit(source, hit, type);
    }

    /**
     * Handles an impact.
     *
     * @param source The impact-dealing entity.
     * @param hit    The hit amount.
     * @param style  The combat style used to deal the impact.
     * @return The impact object created.
     */
    public Impact handleImpact(Entity source, int hit, CombatStyle style, boolean cannon) {
        this.cannon = cannon;
        return handleImpact(source, hit, style, null, null, false);
    }

    /**
     * Handles an impact.
     *
     * @param source The impact-dealing entity.
     * @param hit    The hit amount.
     * @param style  The combat style used to deal the impact.
     * @return The impact object created.
     */
    public Impact handleImpact(Entity source, int hit, CombatStyle style) {
        return handleImpact(source, hit, style, null, null, false);
    }

    /**
     * Handles an impact.
     *
     * @param source The impact-dealing entity.
     * @param hit    The hit amount.
     * @param style  The combat style used to deal the impact.
     * @param state  The battle state.
     * @return The impact object created.
     */
    public Impact handleImpact(Entity source, int hit, CombatStyle style, BattleState state) {
        return handleImpact(source, hit, style, state, null, false);
    }

    /**
     * Handles an impact.
     *
     * @param source The impact-dealing entity.
     * @param hit    The hit amount.
     * @param style  The combat style used to deal the impact.
     * @param state  The battle state.
     * @param type   The hitsplat type.
     * @return The impact object created.
     */
    public Impact handleImpact(Entity source, int hit, CombatStyle style, BattleState state, HitsplatType type) {
        return handleImpact(source, hit, style, state, type, false);
    }

    /**
     * Handles an impact.
     *
     * @param source The impact-dealing entity.
     * @param hit    The hit amount.
     * @param style  The combat style used to deal the impact.
     * @param state  The battle state.
     * @param type   The hitsplat type.
     * @return The impact object created.
     */
    public Impact handleImpactOld(Entity source, int hit, final CombatStyle style, final BattleState state, HitsplatType type, boolean secondary) {
        if (source instanceof Familiar) {
            source = ((Familiar) source).getOwner();
        }
        if (disabledTicks > World.getTicks()) {
            return null;
        }
        if (entity instanceof Player) {
            if (entity.asPlayer().specialDetails()) {
                if (hit >= entity.getSkills().getLifepoints()) {
                    hit = 0;
                }
            }
        }
        hit -= entity.getSkills().hit(hit);
        if (type == null || type == HitsplatType.NORMAL) {
            if (hit == 0) {
                type = HitsplatType.MISS;
            } else {
                type = HitsplatType.NORMAL;
            }
        } else if (hit == 0 && type == HitsplatType.POISON) {
            return null;
        }
        if (hit > 0) {
            Integer value = impactLog.get(source);
            if (value == null) {
                value = 0;
            }
            impactLog.put(source, hit + value);
        }
        if (style != null && style.getSwingHandler() != null && source instanceof Player) {
            style.getSwingHandler().addExperience(source, entity, state);
        }
        if (entity.getSkills().getLifepoints() < 1) {
            entity.getProperties().getCombatPulse().stop();
            entity.face(source);
            DeathTask.startDeath(entity, getMostDamageEntity());
        } else if (entity.getSkills().getLifepoints() < (entity.getSkills().getMaximumLifepoints() * 0.1)) {
            if (entity instanceof Player && ((Player) entity).getPrayer().get(PrayerType.REDEMPTION)) {
                ((Player) entity).getPrayer().startRedemption();
            }
        }
        Impact impact = new Impact(source, hit, style, type);
        impactQueue.add(impact);
        return impact;
    }

    public Impact handleImpact(Entity source, int hit, final CombatStyle style, final BattleState state, HitsplatType type, boolean secondary) {
        boolean fam = source instanceof Familiar;
        if (fam) {
            source = ((Familiar) source).getOwner();
        }
        if (disabledTicks > World.getTicks()) {
            return null;
        }
        if (entity instanceof Player) {
            if (entity.asPlayer().specialDetails()) {
                if (hit >= entity.getSkills().getLifepoints()) {
                    hit = entity.getSkills().getLifepoints();
                }
            }
        }
        hit -= entity.getSkills().hit(hit);
        if (type == null || type == HitsplatType.NORMAL) {
            if (hit == 0) {
                type = HitsplatType.MISS;
            } else {
                type = HitsplatType.NORMAL;
            }
        } else if (hit == 0 && type == HitsplatType.POISON) {
            return null;
        }
        if (hit > 0) {
            Integer value = impactLog.get(source);
            if (value == null) {
                value = 0;
            }
            impactLog.put(source, hit + value);
        }
        if (style != null && style.getSwingHandler() != null && source instanceof Player) {
            Player player = source.asPlayer();
            if (fam && player.getFamiliarManager().hasFamiliar() &&
                !(player.getFamiliarManager().getFamiliar() instanceof Pet)) {
                source.setAttribute("fam-exp", true);
            }
            style.getSwingHandler().setCannon(true);
            style.getSwingHandler().addExperience(source, entity, state);
            source.removeAttribute("fam-exp");
        }
        boolean dead = false;
        if (entity.getSkills().getLifepoints() < 1) {
            entity.getProperties().getCombatPulse().stop();
            entity.face(source);
            Entity killer = getMostDamageEntity();
            DeathTask.startDeath(entity, killer);
            dead = true;
        } else if (entity.getSkills().getLifepoints() < (entity.getSkills().getMaximumLifepoints() * 0.1)) {
            if (entity instanceof Player && ((Player) entity).getPrayer().get(PrayerType.REDEMPTION)) {
                ((Player) entity).getPrayer().startRedemption();
            }
        }
        Impact impact = new Impact(source, hit, style, type);
        impactQueue.add(impact);
        lastImpactEntity = source;
        if (entity instanceof Player && !dead) {
            Player p = entity.asPlayer();
            if (p.getZoneMonitor().getType() != ZoneType.SAFE.getId() && p.getSkullManager().getLevel() <= 30
                && (p.getEquipment().contains(2570, 1) || MaxCapePlugin.isWearing(p))) {
                int percentage = (int) (entity.getSkills().getStaticLevel(Skills.HITPOINTS) * 0.10);
                if (p.getSkills().getLifepoints() <= percentage) {
                    if (!MaxCapePlugin.isWearing(p)) {
                        p.getEquipment().remove(new Item(2570));
                    }
                    p.setTeleportTarget(GameConstants.HOME_LOCATION);
                    if (MaxCapePlugin.isWearing(p)) {
                        p.getActionSender().sendMessage("Your Max cape saves you.");
                    } else {
                        p.getActionSender().sendMessage("Your ring of life saves you and in the process is destroyed.");
                    }
                }
            }
        }
        return impact;
    }

    /**
     * Handles the recoil effect.
     *
     * @param attacker The attacker.
     * @param hit      The hit to handle.
     */
    public void handleRecoilEffect(Entity attacker, int hit) {
        int damage = hit / 10;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int current = player.getSavedData().getGlobalData().getRecoilDamage();
            if (damage >= current) {
                damage = current;
                player.getActionSender().sendMessage("Your Ring of Recoil has shattered.");
                player.getEquipment().replace(null, Equipment.SLOT_RING);
                player.getSavedData().getGlobalData().setRecoilDamage(40);
            } else {
                player.getSavedData().getGlobalData().setRecoilDamage(current - damage);
            }
        }
        if (damage > 0) {
            attacker.getImpactHandler().manualHit(entity, damage, HitsplatType.NORMAL);
        }
    }


    /**
     * Checks if the entity is the only attacker.
     *
     * @return <code>True</code> if so.
     */
    public boolean isOnlyAttacker(Entity killer) {
        return impactLog.size() == 1 && impactLog.containsKey(killer);
    }


    /**
     * Gets the player who's dealt the most damage.
     *
     * @return The player.
     */
    public Entity getMostDamageEntity() {
        Entity entity = this.entity;
        int damage = -1;
        for (Entity e : impactLog.keySet()) {
            if (e == this.entity) {
                continue;
            }
            if (e instanceof Player) {
                Player player = (Player) e;
                if (!isOnlyAttacker(player) && Ironman.isIronman(player)) {
                    continue;
                }
            }
            int amount = impactLog.get(e);
            if (amount > damage || (entity instanceof NPC && e instanceof Player)) {
                damage = amount;
                entity = e;
            }
        }
        return entity;
    }

    /**
     * Gets the impact log.
     *
     * @return The impact log.
     */
    public Map<Entity, Integer> getImpactLog() {
        return impactLog;
    }

    /**
     * Checks if the entity needs a hit update.
     *
     * @return <code>True</code> if so.
     */
    public boolean isHitUpdate() {
        return impactQueue.peek() != null;
    }

    /**
     * Gets the hitsplats.
     *
     * @return The hitsplats.
     */
    public Queue<Impact> getImpactQueue() {
        return impactQueue;
    }

    /**
     * Gets the disabledTicks.
     *
     * @return The disabledTicks.
     */
    public int getDisabledTicks() {
        return disabledTicks;
    }

    /**
     * Sets the disabledTicks.
     *
     * @param ticks The amount of ticks to disable impacts for..
     */
    public void setDisabledTicks(int ticks) {
        this.disabledTicks = World.getTicks() + ticks;
    }

    /**
     * Gets if this is a cannon impact.
     *
     * @return <code>True</code> if so.
     */
    public boolean isCannon() {
        return cannon;
    }

    /**
     * Gets the last entity to impact this entity.
     * @return the entity
     */
    public Entity getLastImpactEntity() {
        return lastImpactEntity;
    }


    /**
     * Represents an impact.
     *
     * @author Emperor
     */
    public static class Impact {

        /**
         * The impact-dealing entity.
         */
        private final Entity source;

        /**
         * The hit amount.
         */
        private final int amount;

        /**
         * The combat style used to deal the hit.
         */
        private final CombatStyle style;

        /**
         * The hitsplat type.
         */
        private final HitsplatType type;

        /**
         * Constructs a new {@code ImpactHandler} {@code Object}.
         *
         * @param source The impact-dealing entity.
         * @param amount The hit amount.
         * @param style  The combat style used to deal the hit.
         * @param type   The hitsplat type.
         */
        public Impact(Entity source, int amount, CombatStyle style, HitsplatType type) {
            this.source = source;
            this.amount = amount;
            this.style = style;
            this.type = type;
        }

        /**
         * Gets the source.
         *
         * @return The source.
         */
        public Entity getSource() {
            return source;
        }

        /**
         * Gets the amount.
         *
         * @return The amount.
         */
        public int getAmount() {
            return amount;
        }

        /**
         * Gets the style.
         *
         * @return The style.
         */
        public CombatStyle getStyle() {
            return style;
        }

        /**
         * Gets the type.
         *
         * @return The type.
         */
        public HitsplatType getType() {
            return type;
        }
    }

    /**
     * Represents the hitsplat types.
     *
     * @author Emperor
     */
    public static enum HitsplatType {
        MISS,
        NORMAL,
        POISON,
        DISEASE,
        VENOM
    }
}