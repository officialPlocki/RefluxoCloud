package me.refluxo.cloud.commands;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;
import de.vapecloud.driver.console.logger.Logger;
import de.vapecloud.driver.console.logger.enums.MessageType;
import me.refluxo.cloud.manager.TemplateManager;

import java.util.concurrent.atomic.AtomicInteger;

public class TemplateCommand extends ICommand {

    public TemplateCommand() {
        super("template", "Template management", "templates", "t");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {
        if(args.length == 0) {
            sendHelp();
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("list")) {
                AtomicInteger templates = new AtomicInteger();
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "» Templates «");
                new TemplateManager().getTemplates().forEach(template -> {
                    VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "\nTemplate Name: " + template.getTemplateName() + "\nTemplate Location: " + template.getTemplateLocation() + "\n\n");
                    templates.getAndAdd(1);
                });
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "Totally " + templates.get() + " templates are existing.");
            } else {
                sendHelp();
            }
        } else if(args.length == 4) {
            if(args[0].equalsIgnoreCase("create")) {
                String name = args[1];
                String parent = args[2];
                String sub = args[3];
                new TemplateManager().registerTemplate(new TemplateManager().createTemplate(name, parent, sub));
                VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "The Template is now registered under the name \"" + name + "\". Please restart the Cloud to use it.");
            } else {
                sendHelp();
            }
        } else {
            sendHelp();
        }
    }

    private void sendHelp() {
        Logger logger = VapeDriver.getInstance().getConsolHandler().getLogger();
        logger.sendMessage(MessageType.INFORMATION, true, "» Template Help «");
        logger.sendMessage(MessageType.INFORMATION, true, "");
        logger.sendMessage(MessageType.INFORMATION, true, "» template create <NAME> <PARENTFOLDER> <SUBFOLDER> - Create a Template");
        logger.sendMessage(MessageType.INFORMATION, true, "» template list - List all Templates");
        logger.sendMessage(MessageType.INFORMATION, true, "");
        logger.sendMessage(MessageType.INFORMATION, true, "SUBFOLDER = PARENTFOLDER -> NAME -> SUBFOLDER");
    }

}
