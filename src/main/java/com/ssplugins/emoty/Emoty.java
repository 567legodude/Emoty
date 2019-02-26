package com.ssplugins.emoty;

import com.google.gson.JsonObject;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Emoty extends JavaPlugin {
    
    private JSONConfig config;
    
    private CommandManager manager;
    private Map<String, EmotyCommand> commands;
    
    @Override
    public void onEnable() {
        config = new JSONConfig(this, "commands");
        addDefault();
    
        manager = new CommandManager();
        PluginCommand emoty = this.getCommand("emoty");
        ManageCommand manageCommand = new ManageCommand(this);
        emoty.setExecutor(manageCommand);
        emoty.setTabCompleter(manageCommand);
    
        commands = new HashMap<>();
        // Load commands
        config.getJson().entrySet().forEach(entry -> {
            commands.put(entry.getKey(), new EmotyCommand(entry.getKey(), this, entry.getValue().getAsJsonObject()));
        });
        // Register commands
        commands.forEach((name, command) -> {
            command.setEnabled(command.isEnabled());
        });
    }
    
    private void addDefault() {
        JsonObject json = config.getJson();
        if (json.has("slap")) return;
        JsonObject slap = new JsonObject();
        slap.addProperty("self", "&e{{player}} &fslap {{who}} in the face.");
        slap.addProperty("other", "&e{{player}} &fslaps {{who}} in the face.");
        slap.addProperty("requirePermission", false);
        slap.addProperty("enabled", true);
        slap.addProperty("comment", "The requirePermission property is optional. If true, player requires permission emoty.cmd.<name>");
        json.add("slap", slap);
        config.save();
    }
    
    public void makeCommand(String name) {
        EmotyCommand emotyCommand = new EmotyCommand(name, this, new JsonObject());
        emotyCommand.setEnabled(false);
        commands.put(name, emotyCommand);
        emotyCommand.register();
        //
        config.getJson().add(name, emotyCommand.getData());
        config.save();
    }
    
    public boolean removeCommand(String name) {
        if (commands.containsKey(name)) {
            commands.remove(name);
            config.getJson().remove(name);
            config.save();
            return true;
        }
        return false;
    }
    
    public Optional<EmotyCommand> useCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }
    
    public boolean updateCommand(String name, Consumer<EmotyCommand> consumer) {
        if (commands.containsKey(name)) {
            EmotyCommand emotyCommand = commands.get(name);
            consumer.accept(emotyCommand);
            config.getJson().add(name, emotyCommand.getData());
            config.save();
            return true;
        }
        return false;
    }
    
    public CommandManager getManager() {
        return manager;
    }
    
    public Map<String, EmotyCommand> getCommands() {
        return commands;
    }
    
}
