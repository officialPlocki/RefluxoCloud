package me.refluxo.cloud.util.files;

public class YamlConfigurationOptions extends FileConfigurationOptions {

    public YamlConfigurationOptions(YamlConfiguration configuration) {
        super(configuration);
    }

    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public void copyDefaults(boolean value) {
        super.copyDefaults(value);
    }

    @Override
    public void pathSeparator(char value) {
        super.pathSeparator(value);
    }

    @Override
    public void header(String value) {
        super.header(value);
    }

    @Override
    public void copyHeader(boolean value) {
        super.copyHeader(value);
    }

    public int indent() {
        return 2;
    }

}