package de.vapecloud.driver.commands;

/*
 * Projectname: VapeCloud
 * Created AT: 21.12.2021/15:06
 * Created by Robin B. (RauchigesEtwas)
 */

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.console.logger.enums.MessageType;
import lombok.SneakyThrows;

import java.util.HashSet;
import java.util.Set;

public class ICommandHandler {

    private final Set<ICommand> commands;

    public ICommandHandler() {
        this.commands = new HashSet<>();
    }


    public Set<ICommand> getCommands() {
        return commands;
    }

    public void registerCommand(ICommand command){
        commands.add(command);
    }

    public void unregisterCommand(ICommand command){
        commands.remove(command);
    }

    @SneakyThrows
    public void executeCommand(String line, ICommandSender sender){
        VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().resetPromptLine("", "", 0);
        VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().setPrompt("");

        ICommand command = getCommand(line.split(" ")[0]);
        String[] args = dropFirstString(line.split(" "));
        if(command != null){
            if(VapeDriver.getInstance().getConsolHandler().isAlive()){
                command.execute(command, sender, args);
                VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().resetPromptLine("", "", 0);
                VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().setPrompt("");

            }
        }else {
            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, true, "The command can't be found. Please type \"help\" for help.");
        }

    }

    public String[] dropFirstString(String[] input){
        String[] string = new String[input.length - 1];
        System.arraycopy(input, 1, string, 0, input.length - 1);
        return string;
    }

    public ICommand getCommand(String name){
        for (ICommand command : this.commands){
            if(command.getName().equalsIgnoreCase(name)){
                return command;
            }
            if (command.getAliases().contains(name)){
                return command;
            }
        }
        return null;
    }
}
