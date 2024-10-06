package com.dwarslooper.extent.simpleserverswitcher;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ProvidedServer {

    private final String id;
    private final String permission;
    private final boolean restricted;
    private final List<Command> commands = new ArrayList<>();

    public ProvidedServer(String id, List<String> commands, @Nullable String permission, boolean restricted) {
        this.id = id;
        this.permission = permission;
        this.restricted = restricted;
        commands.forEach(cmd -> {
            this.commands.add(new Command(cmd));
        });
    }

    public String getId() {
        return id;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public List<String> getCommandNames() {
        return commands.stream().map(net.md_5.bungee.api.plugin.Command::getName).toList();
    }

    public String getPermission() {
        return permission;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public class Command extends net.md_5.bungee.api.plugin.Command {

        public Command(String name) {
            super(name, ProvidedServer.this.permission);
            setPermissionMessage("§cYou are lacking the permission node §f%s §crequired to connect to this server.".formatted(getPermission()));
        }

        @Override
        public void execute(CommandSender commandSender, String[] args) {
            accept(commandSender);
        }

        public void accept(CommandSender commandSender) {
            if(commandSender instanceof ProxiedPlayer player) {
                ServerInfo info = Main.getInstance().getProxy().getServerInfo(getId());
                if(info == null) commandSender.sendMessage(new ComponentBuilder("§cThe target server is not registered on the proxy. Please report this to an admin.").create());
                else player.connect(info);
            } else {
                commandSender.sendMessage(new ComponentBuilder("§cThis command can only be ran by players").create());
            }
        }

    }

    public static class Parser {

        private final Configuration configuration;

        public Parser(Configuration configuration) {
            this.configuration = configuration;
        }

        public ProvidedServer buildOrThrow(String id) {
            boolean restricted = configuration.getBoolean("restricted");
            return new ProvidedServer(id, configuration.getStringList("commands"), restricted ? configuration.getString("permission") : null, restricted);
        }
    }

}
