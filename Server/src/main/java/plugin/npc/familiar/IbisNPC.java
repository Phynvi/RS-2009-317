package plugin.npc.familiar;

import org.gielinor.game.content.skill.SkillBonus;
import org.gielinor.game.content.skill.Skills;
import org.gielinor.game.content.skill.free.fishing.Fish;
import org.gielinor.game.content.skill.member.summoning.familiar.Familiar;
import org.gielinor.game.content.skill.member.summoning.familiar.FamiliarSpecial;
import org.gielinor.game.content.skill.member.summoning.familiar.Forager;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.item.GroundItemManager;
import org.gielinor.game.node.item.Item;
import org.gielinor.game.world.World;
import org.gielinor.game.world.map.Location;
import org.gielinor.game.world.map.RegionManager;
import org.gielinor.game.world.update.flag.context.Animation;
import org.gielinor.rs2.pulse.Pulse;
import org.gielinor.utilities.misc.RandomUtil;

/**
 * Represents the Ibis familiar.
 *
 * @author Aero
 * @author Vexia
 */
public class IbisNPC extends Forager {

    /**
     * The randon fish during fish rain.
     */
    private static final Fish[] FISH = new Fish[]{ Fish.SHRIMP, Fish.BASS, Fish.COD, Fish.MACKEREL };

    /**
     * Constructs a new {@code IbisNPC} {@code Object}.
     */
    public IbisNPC() {
        this(null, 6991);
    }

    /**
     * Constructs a new {@code IbisNPC} {@code Object}.
     *
     * @param owner The owner.
     * @param id    The id.
     */
    public IbisNPC(Player owner, int id) {
        super(owner, id, 3800, 12531, 12, new Item(361), new Item(373));
        boosts.add(new SkillBonus(Skills.FISHING, 3));
    }

    @Override
    public void handlePassiveAction() {
        if (RandomUtil.random(15) < 4) {
            produceItem();
        }
    }

    @Override
    public boolean produceItem(Item item) {
        if (super.produceItem(item)) {
            if (item.getId() == 373) {
                owner.getSkills().addExperience(Skills.FISHING, 10);
            }
            return true;
        }
        return false;
    }

    @Override
    public Familiar construct(Player owner, int id) {
        return new IbisNPC(owner, id);
    }

    @Override
    protected boolean specialMove(FamiliarSpecial special) {
        animate(Animation.create(8201));
        World.submit(new Pulse(3, owner, this) {

            @Override
            public boolean pulse() {
                Location loc = null;
                for (int i = 0; i < 2; i++) {
                    loc = owner.getLocation().transform(RandomUtil.random(2), RandomUtil.random(2), 0);
                    if (RegionManager.getObject(loc) != null) {
                        continue;
                    }
                    GroundItemManager.create(FISH[RandomUtil.random(FISH.length)].getItem(), loc, owner);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public int[] getIds() {
        return new int[]{ 6991 };
    }

}
