package plugin.dialogue;

import org.gielinor.game.content.dialogue.DialoguePlugin;
import org.gielinor.game.content.dialogue.FacialExpression;
import org.gielinor.game.content.dialogue.OptionSelect;
import org.gielinor.game.node.entity.npc.NPC;
import org.gielinor.game.node.entity.player.Player;

/**
 * Represent the hild dialogue plugin.
 *
 * @author 'Vexia
 * @version 1.0
 */
public final class HildDialogue extends DialoguePlugin {

    /**
     * Constructs a new {@code HildDialogue} {@code Object}.
     */
    public HildDialogue() {
        /**
         * empty.
         */
    }

    /**
     * Constructs a new {@code HildDialogue} {@code Object}.
     *
     * @param player the player.
     */
    public HildDialogue(Player player) {
        super(player);
    }

    @Override
    public DialoguePlugin newInstance(Player player) {
        return new HildDialogue(player);
    }

    @Override
    public boolean open(Object... args) {
        npc = (NPC) args[0];
        interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Hi!");
        stage = 0;
        return true;
    }

    @Override
    public boolean handle(int interfaceId, OptionSelect optionSelect) {
        switch (stage) {
            case 0:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Hi!");
                stage = 1;
                break;
            case 1:
                end();
                break;
        }
        return true;
    }

    @Override
    public int[] getIds() {
        return new int[]{ 1090 };
    }
}
