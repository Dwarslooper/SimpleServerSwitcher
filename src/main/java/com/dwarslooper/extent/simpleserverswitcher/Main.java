package com.dwarslooper.extent.simpleserverswitcher;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class Main extends Plugin {

    private static Main instance;

    private Configuration config;
    private final List<ProvidedServer> servers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

        PluginManager pm = getProxy().getPluginManager();

        pm.registerCommand(this, new ReloadCommand());

        instance = this;

        reload(pm);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void reload(PluginManager pm) {
        try {
            servers.forEach(providedServer -> {
                providedServer.getCommands().forEach(pm::unregisterCommand);
            });
            servers.clear();

            File configFile = new File(getInstance().getDataFolder(), "config.yml");

            if(!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                try(InputStream stream = getResourceAsStream("config.yml"); OutputStream fileOutputStream = new FileOutputStream(configFile)) {
                    fileOutputStream.write(stream.readAllBytes());
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            Configuration serversConfig = config.getSection("servers");

            serversConfig.getKeys().forEach(s -> {
                Configuration server = serversConfig.getSection(s);
                servers.add(new ProvidedServer.Parser(server).buildOrThrow(s));
            });

            servers.forEach(providedServer -> {
                providedServer.getCommands().forEach(command -> pm.registerCommand(this, command));
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Configuration getConfig() {
        return config;
    }

    public static Main getInstance() {
        return instance;
    }

    private static class ReloadCommand extends Command {

        public ReloadCommand() {
            super("ssreload", "simpleserverswitcher.reload");
        }

        @Override
        public void execute(CommandSender commandSender, String[] strings) {
            getInstance().reload(getInstance().getProxy().getPluginManager());
            commandSender.sendMessage(new ComponentBuilder("§aReloaded (%s servers listed)".formatted(getInstance().servers.size())).create());
        }
    }

}
