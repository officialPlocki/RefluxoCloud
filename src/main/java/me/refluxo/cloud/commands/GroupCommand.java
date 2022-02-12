package me.refluxo.cloud.commands;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;
import de.vapecloud.driver.console.logger.Logger;
import de.vapecloud.driver.console.logger.enums.MessageType;
import me.refluxo.cloud.RefluxoCloud;
import me.refluxo.cloud.service.ITemplate;
import me.refluxo.cloud.service.ServiceType;
import me.refluxo.cloud.manager.GroupManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupCommand extends ICommand {

    public GroupCommand() {
        super("group", "Group management", "gr", "gp");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {
        if(args.length == 0) {
            sendHelp();
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("list")) {
                AtomicInteger groups = new AtomicInteger();
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "» Groups «");
                GroupManager.groups.forEach(group -> {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "\nGroup Name: " + group.getGroupName() + "\nGroup Ram (per Service): " + group.getRam() + "\nMin. Services: " + group.minInstances() + "\nMax. Services: " + group.maxInstances() + "\nGroup Type: " + group.getServiceType().name() + "\nStatic Group: " + group.isStatic() + "\nTemplates: " + group.getTemplates().toString() + "\n\n");
                    groups.getAndAdd(1);
                });
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "Totally " + groups.get() + " groups are existing.");
            } else {
                sendHelp();
            }
        } else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("create")) {
                String type = args[2].toUpperCase();
                if(type.equals("SERVER") || type.equals("PROXY")) {
                    new GroupManager().registerGroup(new GroupManager().createGroup(args[1], ServiceType.valueOf(type), List.of(new ITemplate() {
                        @Override
                        public String getTemplateLocation() {
                            return "exampleFolder/exampleTemplate/exampleDefault";
                        }

                        @Override
                        public String getTemplateName() {
                            return "example (PLEASE REPLACE!)";
                        }
                    }), 512, 0, 10, false));
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.WARNING, true, "The Group was created, the Cloud will be stopped in 15s. Please configure the Group in the groups.yml.");
                    new Thread(() -> {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        RefluxoCloud.stop();
                        Runtime.getRuntime().exit(1);
                    }).start();
                } else {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.ERROR, true, "Please choose as type PROXY or SERVER.");
                }
            } else {
                sendHelp();
            }
        } else {
            sendHelp();
        }
    }

    private void sendHelp() {
        Logger logger = VapeDriver.getInstance().getConsolHandler().getLogger();
        logger.sendMessage(MessageType.INFORMATION, true, "» Group Help «");
        logger.sendMessage(MessageType.INFORMATION, true, "");
        logger.sendMessage(MessageType.INFORMATION, true, "» group create <NAME> <TYPE(SERVER,PROXY)> - Create a group");
        logger.sendMessage(MessageType.INFORMATION, true, "» group list - List all groups");
    }

}
