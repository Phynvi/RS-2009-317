package org.gielinor.game.content.skill.member.agility;

import org.gielinor.game.content.skill.Skills;
import org.gielinor.game.node.entity.combat.ImpactHandler.HitsplatType;
import org.gielinor.game.node.entity.impl.ForceMovement;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.world.World;
import org.gielinor.game.world.map.Direction;
import org.gielinor.game.world.map.Location;
import org.gielinor.game.world.update.flag.context.Animation;
import org.gielinor.rs2.pulse.Pulse;
import org.gielinor.rs2.pulse.impl.MovementPulse;
import org.gielinor.rs2.task.impl.LocationLogoutTask;
import org.gielinor.rs2.task.impl.LogoutTask;
import org.gielinor.utilities.misc.RandomUtil;

/**
 * Holds agility-related utility methods.
 *
 * @author Emperor
 */
public final class AgilityHandler {

    /**
     * Checks if the player has failed crossing an obstacle.
     *
     * @param player     The player.
     * @param level      The level requirement for the obstacle.
     * @param failChance The chance of failing (0.0 - 0.99).
     * @return <code>True</code> if the player has failed.
     */
    public static boolean hasFailed(Player player, int level, double failChance) {
        int levelDiff = player.getSkills().getLevel(Skills.AGILITY) - level;
        if (levelDiff > 69) {
            return false;
        }
        double chance = (1 + levelDiff) * 0.01;
        chance *= Math.random();
        return chance <= (Math.random() * failChance);
    }

    /**
     * Handles failing an obstacle (by using force movement).
     *
     * @param player  The player.
     * @param delay   The amount of ticks before teleporting to the destination and hitting.
     * @param start   The start location.
     * @param end     The end location.
     * @param dest    The destination.
     * @param anim    The animation.
     * @param hit     The amount of damage to hit.
     * @param message The message to send.
     */
    public static ForceMovement failWalk(final Player player, int delay, Location start, Location end, final Location dest, Animation anim, int speed, final int hit, final String message, Direction direction) {
        ForceMovement movement = new ForceMovement(player, start, end, anim, speed) {

            @Override
            public void stop() {
                super.stop();
                player.getProperties().setTeleportLocation(dest);
                if (hit > 0) {
                    player.getImpactHandler().setDisabledTicks(0);
                    player.getImpactHandler().manualHit(player, hit, HitsplatType.NORMAL);
                }
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
            }
        };
        if (direction != null) {
            movement.setDirection(direction);
        }
        movement.start();
        movement.setDelay(delay);
        World.submit(movement);
        return movement;
    }

    /**
     * Handles failing an obstacle (by using force movement).
     *
     * @param player  The player.
     * @param delay   The amount of ticks before teleporting to the destination and hitting.
     * @param start   The start location.
     * @param end     The end location.
     * @param dest    The destination.
     * @param anim    The animation.
     * @param hit     The amount of damage to hit.
     * @param message The message to send.
     */
    public static ForceMovement failWalk(final Player player, int delay, Location start, Location end, final Location dest, Animation anim, int speed, final int hit, final String message) {
        return failWalk(player, delay, start, end, dest, anim, speed, hit, message, null);
    }


    /**
     * Handles failing an obstacle.
     *
     * @param player  The player.
     * @param delay   The amount of ticks before teleporting to the destination and hitting.
     * @param dest    The destination.
     * @param anim    The animation.
     * @param hit     The amount of damage to hit.
     * @param message The message to send.
     */
    public static void fail(final Player player, int delay, final Location dest, Animation anim, final int hit, final String message) {
        if (anim != null) {
            player.animate(anim);
        }
        World.submit(new Pulse(delay, player) {

            @Override
            public boolean pulse() {
                player.getProperties().setTeleportLocation(dest);
                player.animate(Animation.RESET);
                if (hit > 0) {
                    player.getImpactHandler().setDisabledTicks(0);
                    player.getImpactHandler().manualHit(player, hit, HitsplatType.NORMAL);
                }
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
                return true;
            }
        });
    }

    /**
     * Walks across an obstacle using the force movement update mask.
     *
     * @param start      The start location.
     * @param end        The end location.
     * @param animation  The animation.
     * @param speed      The force movement speed.
     * @param experience The amount of agility experience to give as reward.
     * @param message    The message to send upon completion.
     * @return The force movement instance, if force movement is used.
     */
    public static ForceMovement forceWalk(final Player player, final int courseIndex, Location start, Location end, Animation animation, int speed, final double experience, final String message) {
        ForceMovement movement = new ForceMovement(player, start, end, animation, speed) {

            @Override
            public void stop() {
                super.stop();
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
                if (experience > 0.0) {
                    player.getSkills().addExperience(Skills.AGILITY, experience);
                }
                setObstacleFlag(player, courseIndex);
            }
        };
        movement.start();
        World.submit(movement);
        return movement;
    }


    /**
     * Walks across an obstacle using the force movement update mask.
     *
     * @param start      The start location.
     * @param end        The end location.
     * @param animation  The animation.
     * @param speed      The force movement speed.
     * @param experience The amount of agility experience to give as reward.
     * @param message    The message to send upon completion.
     * @return The force movement instance, if force movement is used.
     */
    public static ForceMovement forceWalk(final Player player, final int courseIndex, Location start, Location end, Animation animation, int speed, final double experience, final String message, int delay) {
        if (delay < 1) {
            return forceWalk(player, courseIndex, start, end, animation, speed, experience, message);
        }
        final ForceMovement movement = new ForceMovement(player, start, end, animation, speed) {

            @Override
            public void stop() {
                super.stop();
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
                if (experience > 0.0) {
                    player.getSkills().addExperience(Skills.AGILITY, experience);
                }
                setObstacleFlag(player, courseIndex);
            }
        };
        World.submit(new Pulse(delay, player) {

            @Override
            public boolean pulse() {
                movement.start();
                World.submit(movement);
                return true;
            }
        });
        return movement;
    }

    /**
     * Executes the climbing action.
     *
     * @param player      The player.
     * @param animation   The climbing animation.
     * @param destination The destination.
     * @param experience  The amount of agility experience to give as reward.
     * @param message     The message to send upon completion.
     */
    public static void climb(final Player player, final int courseIndex, Animation animation, final Location destination, final double experience, final String message) {
        climb(player, courseIndex, animation, destination, experience, message, 2);
    }

    /**
     * Executes the climbing action.
     *
     * @param player      The player.
     * @param animation   The climbing animation.
     * @param destination The destination.
     * @param experience  The amount of agility experience to give as reward.
     * @param message     The message to send upon completion.
     */
    public static void climb(final Player player, final int courseIndex, Animation animation, final Location destination, final double experience, final String message, int delay) {
        player.lock(delay + 1);
        player.animate(animation);
        World.submit(new Pulse(delay) {

            @Override
            public boolean pulse() {
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
                if (experience > 0.0) {
                    player.getSkills().addExperience(Skills.AGILITY, experience);
                }
                player.getProperties().setTeleportLocation(destination);
                setObstacleFlag(player, courseIndex);
                return true;
            }
        });
    }

    /**
     * Uses the walking queue to walk across an obstacle.
     *
     * @param player      The player.
     * @param courseIndex The obstacle index for the course, {@code -1} if no course.
     * @param start       The start location.
     * @param end         The end location.
     * @param animation   The animation.
     * @param experience  The agility experience.
     * @param message     The message to send upon completion.
     */
    public static void walk(final Player player, final int courseIndex, final Location start, final Location end, final Animation animation, final double experience, final String message) {
        if (!player.getLocation().equals(start)) {
            player.getPulseManager().run(new MovementPulse(player, start) {

                @Override
                public boolean pulse() {
                    walk(player, courseIndex, start, end, animation, experience, message);
                    return true;
                }
            }, "movement");
            return;
        }
        player.getWalkingQueue().reset();
        player.getWalkingQueue().addPath(end.getX(), end.getY(), true);
        int ticks = player.getWalkingQueue().getQueue().size();
        player.getImpactHandler().setDisabledTicks(ticks);
        player.lock(1 + ticks);
        player.addExtension(LogoutTask.class, new LocationLogoutTask(1 + ticks, start));
        if (animation != null) {
            player.getAppearance().setAnimations(animation);
        }
        World.submit(new Pulse(ticks, player) {

            @Override
            public boolean pulse() {
                if (animation != null) {
                    player.getAppearance().setAnimations();
                    player.getAppearance().sync();
                }
                if (message != null) {
                    player.getActionSender().sendMessage(message);
                }
                if (experience > 0.0) {
                    player.getSkills().addExperience(Skills.AGILITY, experience);
                }
                setObstacleFlag(player, courseIndex);
                return true;
            }
        });
    }

    /**
     * Sets the obstacle flag for the agility course.
     *
     * @param player      The player.
     * @param courseIndex The course index.
     */
    public static void setObstacleFlag(Player player, int courseIndex) {
        if (courseIndex < 0) {
            return;
        }
        AgilityCourse agilityCourse = player.getExtension(AgilityCourse.class);
        if (agilityCourse != null && courseIndex < agilityCourse.getPassedObstacles().length) {
            agilityCourse.flag(courseIndex);
        }
    }

    /**
     * Checks if the course should spawn a mark of grace.
     *
     * @param courseName The name of the course for the attribute.
     * @param level      The level of the course.
     * @return <code>True</code> if so.
     */
    public static boolean spawnMarkOfGrace(Player player, String courseName, int level) {
        long lastSpawn = player.getAttribute("grace-spawn-" + courseName, 0L);
        int playerLevel = RandomUtil.random(1 + player.getSkills().getLevel(Skills.AGILITY)) + RandomUtil.getRandom(110);
        int courseLevel = (level + RandomUtil.getRandom(100) - player.getSkills().getLevel(Skills.AGILITY));
        int levelDiff = player.getSkills().getLevel(Skills.AGILITY) - level;
        if (levelDiff >= 45) {
            courseLevel = 20;
        }
        boolean spawn = playerLevel < courseLevel && (lastSpawn < System.currentTimeMillis());
        if (spawn) {
            player.saveAttribute("grace-spawn-" + courseName, System.currentTimeMillis() + 120_000);
        }
        return spawn;
    }
}
