package de.vapecloud.driver;


import de.vapecloud.driver.console.ConsolHandler;



/*
 * Projectname: VapeCloud
 * Created AT: 21.12.2021/15:06
 * Created by Robin B. (RauchigesEtwas)
 */

public class VapeDriver {


    private static VapeDriver instance;
    private  ConsolHandler consolHandler;

    public VapeDriver() {
        instance = this;
    }

    public static VapeDriver getInstance() {
        return instance;
    }

    public void setConsolHandler(ConsolHandler consolHandler) {
        this.consolHandler = consolHandler;
    }

    public ConsolHandler getConsolHandler() {
        return consolHandler;
    }

}
