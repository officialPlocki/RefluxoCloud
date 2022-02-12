package me.refluxo.cloud.commands;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;
import de.vapecloud.driver.console.logger.Logger;
import de.vapecloud.driver.console.logger.enums.MessageType;
import me.refluxo.cloud.manager.ServiceManager;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCommand extends ICommand {

    public ServiceCommand() {
        super("service", "Service management", "ser");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {
        if(args.length == 0) {
            sendHelp();
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("list")) {
                AtomicInteger services = new AtomicInteger();
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "» Running Services «");
                ServiceManager.runningServices.values().forEach(service -> {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "\nService Name: " + service.getInstanceGroup() + "@" + service.getTaskID() + "\nService UUID: " + service.getInstanceUUID() + "\nTemplate Name: " + service.getTemplate().getTemplateName() + "\nTemplate Location: " + service.getTemplate().getTemplateLocation() + "\nGroup: " + service.getInstanceGroup().getGroupName() + "\nGroup Type: " + service.getInstanceGroup().getServiceType().name() + "\n\n");
                    services.getAndAdd(1);
                });
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "» Running CustomServices «");
                ServiceManager.runningCustomServices.values().forEach(service -> {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "\nService Name: " + service.getInstanceUUID() + "\nService UUID: " + service.getInstanceUUID() + "\nTemplate Name: " + service.getTemplate().getTemplateName() + "\nTemplate Location: " + service.getTemplate().getTemplateLocation() + "\n\n");
                    services.getAndAdd(1);
                });
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "Totally " + services.get() + " are running.");
            } else {
                sendHelp();
            }
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("stop")) {
                String string = args[1];
                if(string.contains("-")) {
                    new ServiceManager().unregisterService(new ServiceManager().getServiceByUUID(string));
                } else if(string.contains("@")) {
                    new ServiceManager().unregisterService(new ServiceManager().getServiceByName(string));
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "Stopping Service...");
                } else {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "The Service argument doesn't contain a @ or -, so it's not an UUID or Service Name.");
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
        logger.sendMessage(MessageType.INFORMATION, true, "» Service Help «");
        logger.sendMessage(MessageType.INFORMATION, true, "");
        logger.sendMessage(MessageType.INFORMATION, true, "» service stop <NAME / UUID> - Stop a service");
        logger.sendMessage(MessageType.INFORMATION, true, "» service list - Show all online services");
    }

}
