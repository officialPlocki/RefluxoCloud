package me.refluxo.cloud.commands;

import de.vapecloud.driver.commands.ICommand;
import de.vapecloud.driver.commands.ICommandSender;

public class StopCommand extends ICommand {
    public StopCommand() {
        super("stop", "Stops the Cloud");
    }

    @Override
    public void execute(ICommand command, ICommandSender sender, String[] args) {
        Runtime.getRuntime().exit(1);
    }
}
