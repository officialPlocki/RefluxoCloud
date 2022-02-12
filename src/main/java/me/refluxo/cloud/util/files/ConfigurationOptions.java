package me.refluxo.cloud.util.files;

public class ConfigurationOptions {
    private char pathSeparator = '.';
    private boolean copyDefaults = false;
    private final Configuration configuration;

    protected ConfigurationOptions(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration configuration() {
        return configuration;
    }

    public char pathSeparator() {
        return pathSeparator;
    }

    public void pathSeparator(char value) {
        this.pathSeparator = value;
    }

    public boolean copyDefaults() {
        return copyDefaults;
    }

    public void copyDefaults(boolean value) {
        this.copyDefaults = value;
    }
}