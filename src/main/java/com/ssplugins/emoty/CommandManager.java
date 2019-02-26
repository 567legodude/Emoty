package com.ssplugins.emoty;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;

public class CommandManager {
    
    private CommandMap commandMap;
    
    public CommandManager() {
        try {
            Field cm = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            cm.setAccessible(true);
            commandMap = (CommandMap) cm.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to load Bukkit command system.");
        }
    }
    
    public CommandMap getCommandMap() {
        return commandMap;
    }
    
}
