package plugin.dialogue;

import org.gielinor.game.content.dialogue.DialoguePlugin;
import org.gielinor.game.content.dialogue.FacialExpression;
import org.gielinor.game.content.dialogue.OptionSelect;
import org.gielinor.game.content.global.shop.Shops;
import org.gielinor.game.node.entity.npc.NPC;
import org.gielinor.game.node.entity.player.Player;

/**
 * Represents the dialogue plugin used for the lilly npc.
 *
 * @author 'Vexia
 * @version 1.0
 */
public final class LillyDialogue extends DialoguePlugin {

    /**
     * Constructs a new {@code LillyDialogue} {@code Object}
     */
    public LillyDialogue() {
        /**
         * empty.
         */
    }

    /**
     * Constructs a new {@code LillyDialogue} {@code Object}.
     *
     * @param player the player.
     */
    public LillyDialogue(Player player) {
        super(player);
    }

    @Override
    public DialoguePlugin newInstance(Player player) {
        return new LillyDialogue(player);
    }

    @Override
    public boolean open(Object... args) {
        npc = (NPC) args[0];
        interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Uh..... hi... didn't see you there. Can.... I help?");
        stage = 0;
        return true;
    }

    @Override
    public boolean handle(int interfaceId, OptionSelect optionSelect) {
        switch (stage) {
            case 0:
                interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Umm... do you sell potions?");
                stage = 1;
                break;
            case 1:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Erm... yes. When I'm not drinking them.");
                stage = 2;
                break;
            case 2:
                interpreter.sendOptions("What would you like to say?", "I'd like to see what you have for sale.", "That's a pretty wall hanging.", "Bye!");
                stage = 3;
                break;
            case 3:
                if (optionSelect.getButtonId() == 1) {
                    interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "I'd like to see what you have for sale.");
                    stage = 4;
                } else if (optionSelect.getButtonId() == 2) {
                    interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "That's a pretty wall hanging.");
                    stage = 5;
                } else if (optionSelect.getButtonId() == 3) {
                    interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Bye!");
                    stage = 9;
                }
                break;
            case 4:
                end();
                Shops.WARRIORS_GUILD_POTION_SHOP.open(player);
                break;
            case 5:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Do you think so? I made it my self.");
                stage = 6;
                break;
            case 6:
                interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Really? Is that why there's all this cloth and dye around?");
                stage = 7;
                break;
            case 7:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Yes, it's a hobby of mine when I'm.... relaxing.");
                stage = 8;
                break;
            case 8:
                end();
                break;
            case 9:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "Have fun and come back soon!");
                stage = 8;
                break;
        }
        return true;
    }

    @Override
    public int[] getIds() {
        return new int[]{ 4294 };
    }
}
