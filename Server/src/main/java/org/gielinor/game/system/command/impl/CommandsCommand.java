package org.gielinor.game.system.command.impl;

import org.gielinor.game.node.entity.player.Player;
import org.gielinor.game.node.entity.player.info.Rights;
import org.gielinor.game.system.command.Command;
import org.gielinor.net.packet.PacketRepository;
import org.gielinor.net.packet.context.InterfaceScrollPositionContext;
import org.gielinor.net.packet.out.InterfaceScrollPosition;

import plugin.interaction.inter.CommandsInterfacePlugin;

/**
 * Displays commands available.
 *
 * @author <a href="https://Gielinor.org">Gielinor Logan G.</a>
 */
public class CommandsCommand extends Command {

    @Override
    public Rights getRights() {
        return Rights.REGULAR_PLAYER;
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

    @Override
    public String[] getCommands() {
        return new String[]{ "cmds", "commands" };
    }

    @Override
    public void execute(Player player, String[] args) {
        PacketRepository.send(InterfaceScrollPosition.class, new InterfaceScrollPositionContext(player, 26603, 0));
        CommandsInterfacePlugin.openPage(player, 0, true);
    }
}
