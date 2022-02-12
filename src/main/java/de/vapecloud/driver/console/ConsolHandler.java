package de.vapecloud.driver.console;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.commands.ICommandHandler;
import de.vapecloud.driver.commands.ICommandSender;
import de.vapecloud.driver.console.logger.Logger;
import de.vapecloud.driver.console.logger.enums.MessageType;
import lombok.SneakyThrows;

import java.io.IOException;

/*
 * Projectname: VapeCloud
 * Created AT: 21.12.2021/15:06
 * Created by Robin B. (RauchigesEtwas)
 */

public class ConsolHandler extends Thread {

    private ICommandHandler commandHandler;
    private Logger logger;
    private String sender;

    public ConsolHandler() {}


    @SneakyThrows
    @Override
    public void run() {
        while (!isInterrupted() && isAlive()){
                if(this.commandHandler != null) {
                    String line;
                    String coloredPromp = getLogger().colorString("§bRefluxo§fCloud §7» §7");
                    while ((line = this.getLogger().getConsoleReader().readLine(coloredPromp)) != null) {
                        if (!line.trim().isEmpty()) {
                            this.getCommandHandler().executeCommand(line, new ICommandSender("console", sender, null, null));
                        } else {
                            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.INFORMATION, false, "The command can't be found. Please type \"help\" for help.");
                            VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().setPrompt("");
                            VapeDriver.getInstance().getConsolHandler().getLogger().getConsoleReader().resetPromptLine("", "", 0);

                        }
                    }
                }
        }
    }


    /**
     * Clear screen.
     */
    public void clearScreen(){
        try {
            this.getLogger().getConsoleReader().clearScreen();
        } catch (IOException exception) {
            this.getLogger().sendMessage(MessageType.ERROR,false, exception.getMessage());
        }
    }


    public void createHandel(String sender){
        this.initLogger();
        this.initCommandSystem();
        this.setDaemon(true);
        this.sender = sender;
        this.start();

    }

    public Logger getLogger() {
        return logger;
    }


    public ICommandHandler getCommandHandler() {
        return commandHandler;
    }


    private void initLogger(){
        this.logger = new Logger();
    }

    private void initCommandSystem(){
        this.commandHandler = new ICommandHandler();
    }
}
