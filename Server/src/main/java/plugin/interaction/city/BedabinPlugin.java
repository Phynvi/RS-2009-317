package plugin.interaction.city;

import org.gielinor.cache.def.impl.NPCDefinition;
import org.gielinor.cache.def.impl.ObjectDefinition;
import org.gielinor.game.interaction.OptionHandler;
import org.gielinor.game.node.Node;
import org.gielinor.game.node.entity.npc.NPC;
import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.object.GameObject;
import org.gielinor.game.node.object.ObjectBuilder;
import org.gielinor.game.world.map.Location;
import org.gielinor.game.world.map.RegionManager;
import org.gielinor.rs2.plugin.Plugin;

/**
 * The plugin used to handle bedabin interactions.
 * @author 'Vexia
 * @version 1.0
 */
public final class BedabinPlugin extends OptionHandler {

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
        ObjectDefinition.forId(2700).getConfigurations().put("option:walk-through", this);
        ObjectDefinition.forId(2672).getConfigurations().put("option:use", this);
        NPCDefinition.forId(834).getConfigurations().put("option:talk-to", this);
        return this;
    }

    @Override
    public boolean handle(Player player, Node node, String option) {
        final int id = node.getId();
        switch (option) {
            case "talk-to":
                player.getDialogueInterpreter().open(node.getId(), node);
                break;
            case "walk-through":
                switch (id) {
                    case 2700:
                        if (player.getLocation().getY() >= 3046) {
                            final GameObject door = RegionManager.getObject(new Location(3169, 3046, 0));
                            ObjectBuilder.replace(door, door.transform(2701), 2);
                            player.getWalkingQueue().reset();
                            player.getWalkingQueue().addPath(3169, 3045);
                            player.getActionSender().sendMessage("You walk back out the tent.");
                            break;
                        }
                        player.getDialogueInterpreter().open(834, RegionManager.getNpc(player, 834));
                        break;
                }
                break;
            case "use":
                switch (id) {
                    case 2672:
                        player.getActionSender().sendMessage("To forge items use the metal you wish to work with the anvil.");
                        break;
                }
                break;
        }
        return true;
    }

    @Override
    public Location getDestination(Node n, Node node) {
        if (node instanceof NPC) {
            if (node.getId() == 834) {
                return new Location(3169, 3045, 0);
            }
        }
        return null;
    }

}