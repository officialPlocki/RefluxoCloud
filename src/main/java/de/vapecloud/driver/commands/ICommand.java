package de.vapecloud.driver.commands;

/*
 * Projectname: VapeCloud
 * Created AT: 21.12.2021/15:06
 * Created by Robin B. (RauchigesEtwas)
 */

import java.util.ArrayList;
import java.util.Collections;

public abstract class ICommand {


    private final String name;
    private final String[] aliases;
    private final String description;

    public ICommand(String commandname, String description, String... aliases) {
        this.name = commandname;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void execute(ICommand command, ICommandSender sender, String[] args);

    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public ArrayList<String> getAliases() {
        ArrayList<String> resuls = new ArrayList<>();
        Collections.addAll(resuls, aliases);
        return resuls;
    }

}
