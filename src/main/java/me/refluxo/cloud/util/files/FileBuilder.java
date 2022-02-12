package me.refluxo.cloud.util.files;

import java.io.File;
import java.io.IOException;

public class FileBuilder {

    private final File file;
    private final YamlConfiguration yml;

    public FileBuilder(String path) {
        file = new File(path);
        yml = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration getYaml() {
        return yml;
    }

    public File getFile() {
        return file;
    }

    public void save() {
        try {
            yml.save(file);
        } catch (IOException ignored) {
        }
    }

}
