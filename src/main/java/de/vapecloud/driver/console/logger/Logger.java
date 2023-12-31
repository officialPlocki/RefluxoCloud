package de.vapecloud.driver.console.logger;

import de.vapecloud.driver.console.logger.enums.Color;
import de.vapecloud.driver.console.logger.enums.MessageType;
import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.*;
import java.text.SimpleDateFormat;

/*
 * Projectname: VapeCloud
 * Created AT: 21.12.2021/15:06
 * Created by Robin B. (RauchigesEtwas)
 */

public class Logger {

    public ConsoleReader consoleReader;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "HH:mm:ss" );
    private final File file;



    /**
     * Instantiates a new Logger provider.
     */
    public Logger() {
        file = new File("latest.log");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.consoleReader = new ConsoleReader(System.in, System.out);
        } catch (IOException ignored) { }
        this.consoleReader.setExpandEvents(false);
    }

    /**
     * Info.
     *
     * @param message the message
     */
    public void sendMessage(MessageType messageType, boolean usecommand, String message ) {
        switch (messageType){
            case INFORMATION:
                printLine(usecommand,"INFO", message, null);
                break;
            case EMPTY:
                printLine(usecommand,null, message, null);
                break;
            case ERROR:
                printLine(usecommand,"§cERROR", message, null);
                break;
            case NETWORK:
                printLine(usecommand,"NETWORK", message, null);
                break;
            case SETUP:
                printLine(usecommand,"SETUP", message, null);
                break;
            case WARNING:
                printLine(usecommand,"§eWARN", message,null);
                break;
            case MODULE:
                printLine(usecommand,"MODULE", message,null);
                break;
            case DEBUG:
                printLine(usecommand,"DEBUG", message,null);
                break;
            case NETWORKERROR:
                printLine(usecommand,"§cNETWORK", message, null);
                break;
            case SUCCESS:
                printLine(usecommand,"§aSUCCESS", message, null);
                break;
        }
    }




    private void printLine(Boolean usedcommand, String prefix, String message, String print) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            writer.write(message + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String inline = "";

            if(!usedcommand){
                inline = consoleReader.getCursorBuffer().toString();
                consoleReader.setPrompt("");
                consoleReader.resetPromptLine("", "", 0);
            }
            if(prefix == null){
                try {
                    consoleReader.println(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + colorString(message + Color.RESET.getAnsiCode()));
                    consoleReader.drawLine();
                    consoleReader.flush();

                } catch (IOException exception) {

                }
            }else{
                try {
                    consoleReader.println(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + colorString("§7[§f" + simpleDateFormat.format(System.currentTimeMillis()) +"§7] §b"+ prefix + "§7: §r" + Color.RESET.getAnsiCode() + message + Color.RESET.getAnsiCode()));

                    consoleReader.drawLine();
                    consoleReader.flush();
                } catch (IOException exception) {

                }
            }
            if(!usedcommand){

                if(print != null){
                    String coloredPromp = colorString("§bRefluxo§fCloud §7» §7");
                    consoleReader.setPrompt(colorString(coloredPromp));
                    consoleReader.resetPromptLine(colorString(coloredPromp), print, print.length());
                }else{
                    String coloredPromp = colorString("§bRefluxo§fCloud §7» §7");
                    consoleReader.setPrompt(colorString(coloredPromp));
                    consoleReader.resetPromptLine(colorString(coloredPromp), inline, inline.length());
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    /**
     * Read line string.
     *
     * @return the string
     */
    public String readLine() {
        try {
            return this.consoleReader.readLine();
        } catch (IOException ex) {
            return "null";
        }
    }



    /**
     * Gets console reader.
     *
     * @return the console reader
     */
    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }


    /**
     * Color string string.
     *
     * @param text the text
     * @return the string
     */
    public String colorString(String text) {

        for (Color consoleColour : Color.values()) {
            text = text.replace('§' + "" + consoleColour.getIndex(), consoleColour.getAnsiCode());
        }

        return text;
    }
}