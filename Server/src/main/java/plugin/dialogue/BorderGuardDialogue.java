package plugin.dialogue;

import org.gielinor.game.content.dialogue.DialoguePlugin;
import org.gielinor.game.content.dialogue.FacialExpression;
import org.gielinor.game.content.dialogue.OptionSelect;
import org.gielinor.game.content.global.action.DoorActionHandler;
import org.gielinor.game.node.entity.npc.NPC;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.item.Item;
import org.gielinor.game.node.object.GameObject;
import org.gielinor.game.world.map.Location;
import org.gielinor.game.world.map.RegionManager;

/**
 * Represents the border guard dialogue plugin.
 *
 * @author 'Vexia
 * @version 1.0
 */
public final class BorderGuardDialogue extends DialoguePlugin {

    /**
     * Represents the array of object locations.
     */
    private static final Location[] LOCATIONS = new Location[]{ new Location(3267, 3228, 0), new Location(3267, 3229, 0), new Location(3268, 3228, 0), new Location(3268, 3228, 0), Location.create(3267, 3227, 0), Location.create(3268, 3227, 0), new Location(3268, 3227, 0) };

    /**
     * Represents the coins item.
     */
    private static final Item COINS = new Item(Item.COINS, 10);

    /**
     * Represents the door game object.
     */
    private GameObject door;

    /**
     * Constructs a new {@code BorderGuardDialogue} {@code Object}.
     */
    public BorderGuardDialogue() {
    }

    /**
     * Constructs a new {@code BorderGuardDialogue} {@code Object}.
     *
     * @param player the player.
     */
    public BorderGuardDialogue(Player player) {
        super(player);
    }

    @Override
    public DialoguePlugin newInstance(Player player) {
        return new BorderGuardDialogue(player);
    }

    @Override
    public boolean open(Object... args) {
        npc = (NPC) args[0];
        if (args.length == 2) {
            door = ((GameObject) args[1]);
        }
        if (player.getLocation().equals(LOCATIONS[0]) || player.getLocation().equals(LOCATIONS[1]) || player.getLocation().equals(LOCATIONS[2])) {
            door = RegionManager.getObject(LOCATIONS[3]);
        }
        if (player.getLocation().equals(LOCATIONS[4]) || player.getLocation().equals(LOCATIONS[5])) {
            door = RegionManager.getObject(LOCATIONS[6]);
        }
        if (door == null) {
            end();
            return true;
        }
        interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Can I come through this gate?");
        stage = 0;
        return true;
    }

    @Override
    public boolean handle(int interfaceId, OptionSelect optionSelect) {
        switch (stage) {
            case 0:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "You must pay a toll of 10 gold coins to pass.");
                stage = 1;
                break;
            case 1:
                interpreter.sendOptions("Select an Option", "Okay, I'll pay.", "Who does my money go to?", "No thanks, I'll walk around.");
                stage = 2;
                break;
            case 2:
                switch (optionSelect) {
                    case THREE_OPTION_ONE:
                        end();
                        if (!player.getInventory().containsItem(COINS)) {
                            end();
                            return true;
                        }
                        if (player.getInventory().remove(COINS)) {
                            DoorActionHandler.handleAutowalkDoor(player, door);
                        } else {
                            player.getActionSender().sendMessage("You need 10 gold coins to pay the toll.");
                        }
                        break;
                    case THREE_OPTION_TWO:
                        interpreter.sendDialogues(player, FacialExpression.NO_EXPRESSION, "Who does my money go to?");
                        stage = 20;
                        break;
                    case THREE_OPTION_THREE:
                        interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "As you wish. Don't go too near the scorpions.");
                        stage = 23;
                        break;

                }
                break;
            case 20:
                interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "The money goes to the city of Al-Kharid.", "Will you pay the toll?");
                stage = 21;
                break;
            case 21:
                interpreter.sendOptions("Select an Option", "Okay, I'll pay.", "No thanks, I'll walk around.");
                stage = 22;
                break;
            case 22:
                switch (optionSelect) {
                    case TWO_OPTION_ONE:
                        end();
                        if (player.getInventory().remove(new Item(Item.COINS, 10))) {
                            DoorActionHandler.handleAutowalkDoor(player, door);
                        } else {
                            player.getActionSender().sendMessage("You need 10 gold coins to pay the toll.");
                        }
                        break;
                    case TWO_OPTION_TWO:
                        interpreter.sendDialogues(npc, FacialExpression.NO_EXPRESSION, "As you wish. Don't go too near the scorpions.");
                        stage = 23;
                        break;
                }
                break;
            case 23:
                end();
                break;
            case 100:
                end();
                DoorActionHandler.handleAutowalkDoor(player, door);
                break;
        }
        return true;
    }

    @Override
    public int[] getIds() {
        return new int[]{ 925 };
    }
}