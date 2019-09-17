package plugin.npc.familiar;

import org.gielinor.game.content.skill.member.summoning.familiar.Familiar;
import org.gielinor.game.content.skill.member.summoning.familiar.FamiliarSpecial;
import org.gielinor.game.node.entity.player.Player;

/**
 * Represents the Spirit Dagannoth familiar.
 * @author Aero
 */
public class SpiritDagannothNPC extends Familiar {

    /**
     * Constructs a new {@code SpiritDagannothNPC} {@code Object}.
     */
    public SpiritDagannothNPC() {
        this(null, 6804);
    }

    /**
     * Constructs a new {@code SpiritDagannothNPC} {@code Object}.
     * @param owner The owner.
     * @param id The id.
     */
    public SpiritDagannothNPC(Player owner, int id) {
        super(owner, id, 5700, 12017, 6);
    }

    @Override
    public Familiar construct(Player owner, int id) {
        return new SpiritDagannothNPC(owner, id);
    }

    @Override
    protected boolean specialMove(FamiliarSpecial special) {
        return false;
    }

    @Override
    public int[] getIds() {
        return new int[]{ 6804, 6805 };
    }

}