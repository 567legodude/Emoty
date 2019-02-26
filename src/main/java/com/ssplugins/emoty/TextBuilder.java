package com.ssplugins.emoty;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextBuilder {
    
    private List<BaseComponent> components = new ArrayList<>();
	
	public TextBuilder() {}
	
	public TextBuilder(String text) {
		this(text, null);
	}
	
	public TextBuilder(String text, Styler styler) {
		append(text, styler);
	}
	
	public TextBuilder append(String text) {
		return append(text, null);
	}
	
	public TextBuilder append(String text, Styler styler) {
		TextStyle style = new TextStyle(text);
		if (styler != null) styler.style(style);
        components.addAll(Arrays.asList(style.toComponents()));
		return this;
	}
	
	public TextBuilder sendTo(CommandSender sender) {
		sender.spigot().sendMessage(components.toArray(new BaseComponent[0]));
		return this;
	}
	
	public interface Styler {
		
		void style(TextStyle style);
		
	}
	
	public static class TextStyle {
		
		private ComponentBuilder builder;
		
		TextStyle(String text) {
			builder = new ComponentBuilder(text);
		}
		
		private net.md_5.bungee.api.ChatColor convert(ChatColor color) {
			return net.md_5.bungee.api.ChatColor.getByChar(color.getChar());
		}
		
		public TextStyle color(ChatColor color) {
			builder.color(convert(color));
			return this;
		}
		
		public TextStyle bold() {
			builder.bold(true);
			return this;
		}
		
		public TextStyle italic() {
			builder.italic(true);
			return this;
		}
		
		public TextStyle underline() {
			builder.underlined(true);
			return this;
		}
		
		public TextStyle strike() {
			builder.strikethrough(true);
			return this;
		}
		
		public TextStyle magic() {
			builder.obfuscated(true);
			return this;
		}
		
		public TextStyle hover(String text) {
			return hover(new ComponentBuilder(text).create());
		}
		
		public TextStyle hover(BaseComponent... components) {
			builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, components));
			return this;
		}
		
		public TextStyle click(ClickEvent.Action action, String data) {
			builder.event(new ClickEvent(action, data));
			return this;
		}
		
		public BaseComponent[] toComponents() {
			return builder.create();
		}
		
	}
	
}