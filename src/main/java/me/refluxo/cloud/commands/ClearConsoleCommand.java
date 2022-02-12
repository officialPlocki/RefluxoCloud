package me.refluxo.cloud.commands;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;

public class ClearConsoleCommand extends ICommand {

    public ClearConsoleCommand() {
        super("clear", "Clear the console screen", "c", "clearc", "cconsole", "clearconsole");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {
        VapeDriver.getInstance().getConsolHandler().clearScreen();
    }

}
