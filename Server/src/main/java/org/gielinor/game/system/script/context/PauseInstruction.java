package org.gielinor.game.system.script.context;

import org.gielinor.game.system.script.ScriptContext;
import org.gielinor.game.system.script.ScriptManager;
import org.gielinor.game.world.World;
import org.gielinor.rs2.pulse.Pulse;

/**
 * Used for pausing the script.
 * @author Emperor
 *
 */
public final class PauseInstruction extends ScriptContext {

    /**
     * The amount of ticks to pause for.
     */
    private final int ticks;

    /**
     * Constructs a new {@code PauseInstruction} {@code Object}.
     */
    public PauseInstruction() {
        this(-1);
    }

    /**
     * Constructs a new {@code PauseInstruction} {@code Object}.
     * @param ticks The amount of ticks to pause for.
     */
    public PauseInstruction(int ticks) {
        super("pause");
        super.setInstant(false);
        this.ticks = ticks;
    }

    @Override
    public ScriptContext parse(Object... params) {
        int ticks = -1;
        if (params[0] instanceof Integer) {
            ticks = (Integer) params[0];
        }
        PauseInstruction context = new PauseInstruction(ticks);
        context.parameters = params;
        return context;
    }

    @Override
    public boolean execute(final Object... args) {
        World.submit(new Pulse(ticks) {

            @Override
            public boolean pulse() {
                ScriptManager.run(PauseInstruction.this, args);
                return true;
            }
        });
        return true;
    }

}
