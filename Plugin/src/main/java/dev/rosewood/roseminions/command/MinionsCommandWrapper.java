package dev.rosewood.roseminions.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MinionsCommandWrapper extends RoseCommandWrapper {

    public MinionsCommandWrapper(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public String getDefaultName() {
        return "rm";
    }

    @Override
    public List<String> getDefaultAliases() {
        return Arrays.asList("roseminions", "minions");
    }

    @Override
    public List<String> getCommandPackages() {
        return Collections.singletonList("dev.rosewood.roseminions.command.command");
    }

    @Override
    public boolean includeBaseCommand() {
        return true;
    }

    @Override
    public boolean includeHelpCommand() {
        return true;
    }

    @Override
    public boolean includeReloadCommand() {
        return true;
    }

}
