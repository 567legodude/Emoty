package com.ssplugins.emoty;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class EmotyCommand extends BukkitCommand {
    
    private Emoty main;
    private JsonObject data;
    
    private boolean requirePermission = false;
    private boolean enabled = true;
    
    public EmotyCommand(String name, Emoty main, JsonObject data) {
        super(name);
        this.main = main;
        this.data = data;
        
        if (data.has("requirePermission")) {
            requirePermission = data.get("requirePermission").getAsBoolean();
        }
    }
    
    public void register() {
        main.getManager().getCommandMap().register("emoty", this);
    }
    
    public void unregister() {
        this.unregister(main.getManager().getCommandMap());
    }
    
    public static String encode(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
    
    public static String decode(String str) {
        return ChatColor.stripColor(encode(str));
    }
    
    public static String replace(String source, String term, String replace) {
        return source.replace("{{" + term + "}}", replace);
    }
    
    public static String replaceFix(String source, String term, String replace) {
        return source.replace("{{" + term + "}}", fixCapital(source, "{{" + term + "}}", replace));
    }
    
    public static String fixCapital(String source, String term, String replace) {
        if (decode(source).startsWith(term)) return replace.substring(0, 1).toUpperCase() + replace.substring(1);
        return replace;
    }
    
    public static String colorsAt(String source, String term) {
        try {
            return ChatColor.getLastColors(source.substring(0, source.indexOf("{{" + term + "}}")));
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player command only.");
            return true;
        }
        if (!enabled) return true;
        if (requirePermission && !sender.hasPermission("emoty.cmd." + this.getName())) {
            sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.");
            return true;
        }
        
        String from = sender.getName();
        String who;
        if (args.length > 0) who = args[0];
        else who = from;
        
        String self = data.get("self").getAsString();
        String other = data.get("other").getAsString();
        String notWho;
        self = replaceFix(self, "player", "you");
        other = replace(other, "player", from);
        if (from.equalsIgnoreCase(who)) {
            self = replaceFix(self, "who", "yourself");
            notWho = replaceFix(other, "who", "themself");
        }
        else {
            self = replace(self, "who", who);
            notWho = replace(other, "who", who);
        }
        self = encode(self);
        other = encode(other);
    
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == sender) player.sendMessage(self);
            else if (player.getName().equalsIgnoreCase(who)) {
                String tmpOther = replaceFix(other, "who", "you");
                player.sendMessage(tmpOther);
            }
            else player.sendMessage(notWho);
        }
        
        return true;
    }
    
    public boolean canEnable() {
        return data.has("self") && data.has("other");
    }
    
    public String getData(String key) {
        if (data.has(key)) return data.get(key).getAsString();
        return "";
    }
    
    public void setData(String key, String value) {
        data.addProperty(key, value);
    }
    
    public void setData(String key, boolean value) {
        data.addProperty(key, value);
    }
    
    public JsonObject getData() {
        return data;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Command command = main.getManager().getCommandMap().getCommand(this.getName());
        if (enabled) {
            if (!canEnable()) return;
            if (command == null) register();
        }
        else {
            if (command != null) unregister();
        }
    }
    
    public boolean requiresPermission() {
        return requirePermission;
    }
    
    public void setRequirePermission(boolean requirePermission) {
        this.requirePermission = requirePermission;
    }
    
}
