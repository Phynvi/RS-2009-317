package plugin.combat.special;

import org.gielinor.game.content.skill.Skills;
import org.gielinor.game.node.entity.Entity;
import org.gielinor.game.node.entity.combat.BattleState;
import org.gielinor.game.node.entity.combat.CombatStyle;
import org.gielinor.game.node.entity.combat.handlers.MeleeSwingHandler;
import org.gielinor.game.node.entity.impl.Animator.Priority;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.entity.player.link.audio.Audio;
import org.gielinor.game.node.entity.player.link.prayer.PrayerType;
import org.gielinor.game.world.World;
import org.gielinor.game.world.update.flag.context.Animation;
import org.gielinor.game.world.update.flag.context.Graphics;
import org.gielinor.rs2.plugin.Plugin;
import org.gielinor.utilities.misc.RandomUtil;

/**
 * Handles the Dragon warhammer special attack.
 *
 * TODO: find the correct sound
 *
 * reference:
 *  - http://oldschoolrunescape.wikia.com/wiki/Special_attacks
 *  - http://oldschoolrunescape.wikia.com/wiki/Dragon_warhammer
 *
 * @author Stan van der Bend
 */
public final class SmashSpecial extends MeleeSwingHandler implements Plugin<Object> {

    private static final int SPECIAL_ENERGY = 50;

    private static final Animation ANIMATION = new Animation(1378, Priority.HIGH);
    private static final Graphics GRAPHIC = new Graphics(1292, 96);
    private static final Audio SOUND = new Audio(1);

    @Override
    public Object fireEvent(String identifier, Object... args) {
        return null;
    }

    @Override
    public Plugin<Object> newInstance(Object arg) {
        CombatStyle.MELEE.getSwingHandler().register(13576, this);
        return this;
    }

    @Override
    public int swing(Entity entity, Entity victim, BattleState state) {
        if (!((Player) entity).getSettings().drainSpecial(SPECIAL_ENERGY)) {
            return -1;
        }
         int hit =0;
        if (isAccurateImpact(entity, victim, CombatStyle.MELEE)) {
            hit = calculateHit(entity, victim, 1.5D);
            victim.getSkills().drainLevel(Skills.DEFENCE, 0.33D, 0.33D);
        }
        state.setEstimatedHit(hit);
        entity.asPlayer().getAudioManager().send(SOUND, true);
        return 1;
    }

    @Override
    public void visualize(Entity entity, Entity victim, BattleState state) {
        entity.visualize(ANIMATION, GRAPHIC);
        victim.animate(victim.getProperties().getDefenceAnimation());
    }

}
