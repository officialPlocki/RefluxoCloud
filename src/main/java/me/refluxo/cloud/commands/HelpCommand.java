package me.refluxo.cloud.commands;

/*
 * Projectname: VapeCloud
 * Created AT: 27.12.2021/16:04
 * Created by Robin B. (RauchigesEtwas)
 */

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;
import de.vapecloud.driver.console.logger.enums.MessageType;

public class HelpCommand extends ICommand {

    public HelpCommand() {
        super("help", "Help overview", "?");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {

        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION,true, "The following Commands are registered:");
        VapeDriver.getInstance().getConsolHandler().getCommandHandler().getCommands().forEach(cmd -> {
            String aliases = "";

            if(cmd.getAliases().isEmpty()){

            }else if (cmd.getAliases().size() == 1){
                aliases = cmd.getAliases().get(0);
            }else {
                for (int i = 0; i !=   cmd.getAliases().size(); i++){
                    if(i == 0){
                        aliases =   cmd.getAliases().get(i);
                    }else{
                        aliases = aliases + ", " +   cmd.getAliases().get(i);
                    }
                }
            }

            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION,true, "   -> §e" + cmd.getName() + " §7- Aliases: §6[" +aliases+"] §7~ §f" + cmd.getDescription());
        });
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "");
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "Threads§7: §e"+ Runtime.getRuntime().availableProcessors());
    }
}
