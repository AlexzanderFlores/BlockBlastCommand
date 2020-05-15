package net.blockblast.command;

import org.bukkit.plugin.java.JavaPlugin;

public class BlockBlastCommand extends JavaPlugin {
    private static BlockBlastCommand instance = null;

    @Override
    public void onEnable() {
        instance = this;
    }

    public static BlockBlastCommand getInstance() {
        return instance;
    }
}
