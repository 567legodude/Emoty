package com.ssplugins.emoty;

import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManageCommand implements CommandExecutor, TabCompleter {
    
    private Emoty main;
    
    private final List<String> subs = Arrays.asList("add", "remove", "enable", "disable", "edit");
    private final List<String> options = Arrays.asList("self", "other", "permission");
    
    public ManageCommand(Emoty main) {
        this.main = main;
    }
    
    private void sendUsage(CommandSender sender) {
        TextBuilder builder = new TextBuilder();
        builder.append("/emoty ", style -> style.color(ChatColor.GREEN));
        
        String msg = "Click to insert";
        
        builder.append("add", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty add ").hover(msg));
        builder.append("|", style -> style.color(ChatColor.RED));
        builder.append("remove", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty remove ").hover(msg));
        builder.append("|", style -> style.color(ChatColor.RED));
        builder.append("enable", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty enable ").hover(msg));
        builder.append("|", style -> style.color(ChatColor.RED));
        builder.append("disable", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty disable ").hover(msg));
        builder.append("|", style -> style.color(ChatColor.RED));
        builder.append("edit", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty edit ").hover(msg));
        builder.append("|", style -> style.color(ChatColor.RED));
        builder.append("list", style -> style.color(ChatColor.AQUA).click(Action.SUGGEST_COMMAND, "/emoty list").hover(msg));
        
        builder.append(" [<name>] ", style -> style.color(ChatColor.YELLOW));
        
        builder.sendTo(sender);
    }
    
    private void msgExists(boolean exists, CommandSender sender, String msg) {
        if (exists) sender.sendMessage(msg);
        else sender.sendMessage("Command doesn't exist.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.isOp() && !sender.hasPermission("emoty.admin")) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission to use this command.");
                return true;
            }
        }
        if (args.length < 1) {
            sendUsage(sender);
        }
        else if (args.length == 1) {
            String sub = args[0].toLowerCase();
            if (sub.equals("list")) {
                String commands = String.join(", ", main.getCommands().keySet());
                sender.sendMessage(commands);
            }
            else sendUsage(sender);
        }
        else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String name = args[1].toLowerCase();
            if (sub.equals("add")) {
                if (main.getCommands().containsKey(name)) {
                    sender.sendMessage("Command already exists.");
                }
                else {
                    main.makeCommand(name);
                    sender.sendMessage("Command added: " + name);
                }
            }
            else if (sub.equals("remove")) {
                boolean removed = main.removeCommand(name);
                msgExists(removed, sender, "Command removed: " + name);
            }
            else if (sub.equals("enable")) {
                Optional<EmotyCommand> cmd = main.useCommand(name);
                if (cmd.isPresent() && !cmd.get().canEnable()) {
                    new TextBuilder("Command must define both messages to enable.", style -> style.color(ChatColor.YELLOW)).sendTo(sender);
                    String editCmd = "/emoty edit " + name;
                    new TextBuilder("Use: ", style -> style.color(ChatColor.YELLOW))
                            .append(editCmd, style -> style.color(ChatColor.GREEN)
                                                           .click(Action.SUGGEST_COMMAND, editCmd)
                                                           .hover("Click to insert"))
                            .sendTo(sender);
                    return true;
                }
                boolean updated = main.updateCommand(name, emotyCommand -> emotyCommand.setEnabled(true));
                msgExists(updated, sender, "Command enabled: " + name);
            }
            else if (sub.equals("disable")) {
                boolean updated = main.updateCommand(name, emotyCommand -> emotyCommand.setEnabled(false));
                msgExists(updated, sender, "Command disabled: " + name);
            }
            else if (sub.equals("edit")) {
                EmotyCommand emotyCommand = main.getCommands().get(name);
                if (emotyCommand == null) {
                    sender.sendMessage("Command doesn't exist.");
                    return true;
                }
                TextBuilder line1 = new TextBuilder()
                        .append("Editing: ", style -> style.color(ChatColor.GREEN))
                        .append(name, style -> style.color(ChatColor.YELLOW));
                TextBuilder line2 = new TextBuilder()
                        .append("Click label to edit line:", style -> style.color(ChatColor.GRAY));
                String self = emotyCommand.getData("self");
                String selfColor = EmotyCommand.colorsAt(self, "player");
                String selfView = EmotyCommand.replaceFix(self, "player", "&nyou&r" + selfColor);
                selfColor = EmotyCommand.colorsAt(self, "who");
                selfView = EmotyCommand.replace(selfView, "who", "&nwho&r" + selfColor);
                TextBuilder selfLine = new TextBuilder()
                        .append("self: ", style -> style.color(ChatColor.LIGHT_PURPLE)
                                                        .click(Action.SUGGEST_COMMAND, "/emoty edit " + name + " self " + self)
                                                        .hover("Click to edit"))
                        .append(EmotyCommand.encode(selfView));
                String other = emotyCommand.getData("other");
                String otherColor = EmotyCommand.colorsAt(other, "player");
                String otherView = EmotyCommand.replaceFix(other, "player", "&nplayer&r" + otherColor);
                otherColor = EmotyCommand.colorsAt(other, "who");
                otherView = EmotyCommand.replaceFix(otherView, "who", "&nwho&r" + otherColor);
                TextBuilder otherLine = new TextBuilder()
                        .append("other: ", style -> style.color(ChatColor.LIGHT_PURPLE)
                                                         .click(Action.SUGGEST_COMMAND, "/emoty edit " + name + " other " + other)
                                                         .hover("Click to edit"))
                        .append(EmotyCommand.encode(otherView));
                line1.sendTo(sender);
                line2.sendTo(sender);
                selfLine.sendTo(sender);
                otherLine.sendTo(sender);
            }
            else {
                sender.sendMessage("Unknown subcommand.");
            }
        }
        else if (args.length > 3) {
            String sub = args[0].toLowerCase();
            String name = args[1].toLowerCase();
            String option = args[2].toLowerCase();
            String update = Stream.of(args).skip(3).collect(Collectors.joining(" "));
            if (sub.equals("edit")) {
                if (!options.contains(option)) {
                    sender.sendMessage("Invalid command option.");
                    return true;
                }
                if (option.equals("permission")) {
                    boolean enable = Boolean.parseBoolean(update);
                    boolean updated = main.updateCommand(name, emotyCommand -> emotyCommand.setRequirePermission(enable));
                    msgExists(updated, sender, "Command updated: " + name);
                    return true;
                }
                if (!update.contains("{{player}}") || !update.contains("{{who}}")) {
                    sender.sendMessage("Message must contain both {{player}} and {{who}} variables.");
                    return true;
                }
                boolean updated = main.updateCommand(name, emotyCommand -> {
                    emotyCommand.setData(option, update);
                });
                msgExists(updated, sender, "Command updated: " + name);
                if (sender instanceof Player) ((Player) sender).performCommand("emoty edit " + name);
            }
            else sender.sendMessage("Unknown syntax.");
        }
        else sendUsage(sender);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>(5);
            StringUtil.copyPartialMatches(args[args.length - 1], subs, out);
            return out;
        }
        else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("list") || sub.equals("reload")) return Collections.emptyList();
            List<String> out = new ArrayList<>();
            StringUtil.copyPartialMatches(args[args.length - 1], main.getCommands().keySet(), out);
            return out;
        }
        else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (!sub.equals("edit")) return Collections.emptyList();
            List<String> out = new ArrayList<>(2);
            StringUtil.copyPartialMatches(args[args.length - 1], options, out);
            return out;
        }
        return Collections.emptyList();
    }
    
}
