package net.cmr.rtd.game.commands;

import java.util.HashSet;

public abstract class CommandHandler {
    
    public static HashSet<CommandHandler> handlers = new HashSet<CommandHandler>();

    public CommandHandler() {

    }

    public abstract void handleCommand(String command, String[] args);
    
    public boolean equals(Object obj) {
        return obj.getClass().equals(this.getClass());
    }

    public static void handleCommand(String input) {
        String[] split = input.split(" ");
        if (split.length == 0) {
            return;
        }
        String command = split[0];
        String[] args = new String[split.length - 1];
        for (int i = 1; i < split.length; i++) {
            args[i - 1] = split[i];
        }
        for (CommandHandler handler : handlers) {
            handler.handleCommand(command, args);
        }
    }
    public static void register(CommandHandler handler) {
        handlers.add(handler);
    }

}
