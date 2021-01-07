package net.blockblast.command;

import net.blockblastapi.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandBase extends BukkitCommand implements CommandExecutor {
    private List<String> delayedPlayers = null;
    private int delay = 0;
    private int minArguments;
    private int maxArguments;
    private boolean playerOnly;

    public CommandBase(String command) {
        this(command, 0);
    }

    public CommandBase(String command, boolean playerOnly) {
        this(command, 0, playerOnly);
    }

    public CommandBase(String command, int requiredArguments) {
        this(command, requiredArguments, requiredArguments);
    }

    public CommandBase(String command, int minArguments, int maxArguments) {
        this(command, minArguments, maxArguments, false);
    }

    public CommandBase(String command, int requiredArguments, boolean playerOnly) {
        this(command, requiredArguments, requiredArguments, playerOnly);
    }

    public CommandBase(String command, int minArguments, int maxArguments, boolean playerOnly) {
        super(command);

        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.playerOnly = playerOnly;

        ((CraftServer) BlockBlastCommand.getInstance().getServer()).getCommandMap().register(command, this);
    }

    public CommandBase enableDelay(int delay) {
        this.delay = delay;
        this.delayedPlayers = new ArrayList<String>();
        return this;
    }

    protected void removeDelay(Player player) {
        delayedPlayers.remove(player.getName());
    }

    private void sendUsage(CommandSender sender) {
        String usage = getUsage();

        for (String line : usage.split("\n")) {
            Msg.send(sender, "&f" + line);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String [] arguments) {
        if (arguments.length < minArguments || (arguments.length > maxArguments && maxArguments != -1)) {
            sendUsage(sender);
        } else {
            if (playerOnly && !(sender instanceof Player)) {
                Msg.sendPlayersOnly(sender);
            } else {
                String permission = getPermission();
                if (permission == null || sender.hasPermission(permission)) {
                    if (delayedPlayers != null && sender instanceof Player) {
                        Player player = (Player) sender;
                        if (delayedPlayers.contains(player.getName())) {
                            if (delay == 1) {
                                Msg.send(player, "&cThis command has a delay of &e" + delay + "&c second");
                            } else {
                                Msg.send(player, "&cThis command has a delay of &e" + delay + "&c seconds");
                            }
                        } else {
                            delayedPlayers.add(player.getName());
                            Bukkit.getScheduler().scheduleSyncDelayedTask(BlockBlastCommand.getInstance(),
                                    () -> delayedPlayers.remove(player.getName()), 20 * delay);
                            if (!onCommand(sender, arguments)) {
                                sendUsage(sender);
                            }
                        }
                    } else {
                        if (!onCommand(sender, arguments)) {
                            sendUsage(sender);
                        }
                    }
                } else {
                    Msg.send(sender, "&cYou do not have permission to run this command.");
                }
            }
        }

        return true;
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String [] arguments) {
        return this.onCommand(sender, arguments);
    }

    public abstract boolean onCommand(CommandSender sender, String [] arguments);

    public abstract String getUsage();
}
