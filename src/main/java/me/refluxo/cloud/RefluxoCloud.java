package me.refluxo.cloud;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.console.ConsolHandler;
import de.vapecloud.driver.console.logger.enums.MessageType;
import me.refluxo.cloud.commands.*;
import me.refluxo.cloud.manager.GroupManager;
import me.refluxo.cloud.manager.ServiceManager;
import me.refluxo.cloud.manager.TemplateManager;
import me.refluxo.cloud.packet.*;
import me.refluxo.cloud.util.files.FileBuilder;
import me.refluxo.cloud.util.files.YamlConfiguration;
import me.refluxo.cloud.manager.MySQLManager;
import me.refluxo.cloud.util.mysql.MySQLService;
import me.refluxo.networker.PacketServer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

public class RefluxoCloud {

    public static String host = "localhost";
    public static String sessionUUID;
    public static PacketServer packetServer;

    public static void main(String[] args) {
        sessionUUID = UUID.randomUUID().toString();
        FileBuilder fb = new FileBuilder("config.yml");
        YamlConfiguration yml = fb.getYaml();
        if(!fb.getFile().exists()) {
            yml.set("host.ip", "localhost");
            yml.set("mysql.host", "127.0.0.1");
            yml.set("mysql.port", 3306);
            yml.set("mysql.database", "db");
            yml.set("mysql.user", "user");
            yml.set("mysql.password", "passwd");
            fb.save();
        }
        host = yml.getString("host.ip");
        //packet server init
        packetServer = new PacketServer();
        packetServer.start(sessionUUID, "Cloud", host, 6);
        packetServer.getPacketHandler().registerPacket(StopServicePacket.class);
        packetServer.getPacketHandler().registerPacket(ServiceDeletePacket.class);
        packetServer.getPacketHandler().registerPacket(CustomServiceDeletePacket.class);
        packetServer.getPacketHandler().registerPacket(CustomServiceCreatePacket.class);
        packetServer.getPacketHandler().registerPacket(ServiceCreatePacket.class);
        //console init
        new VapeDriver();
        if (VapeDriver.getInstance().getConsolHandler() == null){
            VapeDriver.getInstance().setConsolHandler(new ConsolHandler());
            VapeDriver.getInstance().getConsolHandler().createHandel("CONSOLE");
        }
        System.out.println("lol");
        VapeDriver.getInstance().getConsolHandler().clearScreen();
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, false, "§b\n" +
                "   ___      _____               _______             __\n" +
                "  / _ \\___ / _/ /_ ____ _____  / ___/ /__  __ _____/ /\n" +
                " / , _/ -_) _/ / // /\\ \\ / _ \\/ /__/ / _ \\/ // / _  / \n" +
                "/_/|_|\\__/_//_/\\_,_//_\\_\\\\___/\\___/_/\\___/\\_,_/\\_,_/  \n" +
                "                                                      ");
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "\nConsole has been loaded.");
        //register commands
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new ClearConsoleCommand());
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new GroupCommand());
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new HelpCommand());
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new ServiceCommand());
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new StopCommand());
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().registerCommand(new TemplateCommand());
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Console commands have been registered.");
        //configuration
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Loading Configurations...");
        GroupManager.load();
        TemplateManager.load();
        generateProperties();
        //mysql
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Configurations have been loaded.");
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Trying to connect to MySQL...");
        try {
            MySQLService.connect(yml.getString("mysql.host"), yml.getInt("mysql.port"), yml.getString("mysql.database"), yml.getString("mysql.user"), yml.getString("mysql.password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Tried to connect to MySQL.");
        if(!MySQLService.isConnected()) {
            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Can't connect to MySQL, stopping cloud...");
            RefluxoCloud.stop();
            Runtime.getRuntime().exit(1);
        }
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Connected to MySQL.");
        MySQLManager.init();
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Setting up MySQL...");
        //hooks
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Loading hooks...");
        ServiceManager.loadServiceScheduler();
        Runtime.getRuntime().addShutdownHook(new Thread(RefluxoCloud::stop));
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "Hooks loaded, clearing console...");
        VapeDriver.getInstance().getConsolHandler().clearScreen();
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, false, "§b\n" +
                "   ___      _____               _______             __\n" +
                "  / _ \\___ / _/ /_ ____ _____  / ___/ /__  __ _____/ /\n" +
                " / , _/ -_) _/ / // /\\ \\ / _ \\/ /__/ / _ \\/ // / _  / \n" +
                "/_/|_|\\__/_//_/\\_,_//_\\_\\\\___/\\___/_/\\___/\\_,_/\\_,_/  \n" +
                "                                                      ");
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "\n§bRefluxo§fCloud§a loaded. Have fun §c♥ §f| §aMade with love by §cplocki§a.");
    }

    private static void generateProperties() {
        new Thread(() -> {
            FileBuilder fb = new FileBuilder("default/server.properties");
            if(!fb.getFile().exists()) {
                try {
                    fb.getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(fb.getFile(), true));
                writer.write("online-mode=false\nallow-nether=false\nmax-players=200\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            FileBuilder fb = new FileBuilder("default/bukkit.yml");
            YamlConfiguration yml = fb.getYaml();
            if(!fb.getFile().exists()) {
                try {
                    fb.getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            yml.set("settings.connection-throttle", -1);
            yml.set("settings.allow-end", false);
            fb.save();
        }).start();
        new Thread(() -> {
            FileBuilder fb = new FileBuilder("default/spigot.yml");
            YamlConfiguration yml = fb.getYaml();
            if(!fb.getFile().exists()) {
                try {
                    fb.getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            yml.set("settings.bungeecord", true);
            fb.save();
        }).start();
    }

    public static void stop() {
        MySQLService.disconnect();
        GroupManager.unload();
        TemplateManager.unload();
        ServiceManager.runningServices.values().forEach(service -> {
            new ServiceManager().unregisterService(service);
        });
        ServiceManager.runningCustomServices.values().forEach(service -> {
            new ServiceManager().unregisterCustomService(service);
        });
        packetServer.stop();
    }


}
