package me.refluxo.cloud.manager;

import me.refluxo.cloud.service.ITemplate;
import me.refluxo.cloud.util.ConfigurationUtil;
import me.refluxo.cloud.util.files.FileBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TemplateManager {

    private static final List<ITemplate> templates = new ArrayList<>();

    public void registerTemplate(ITemplate template) {
        templates.add(template);
        new File(template.getTemplateLocation()).mkdirs();
    }

    public ITemplate createTemplate(String templateName, String parentFolder, String subFolder) {
        return new ITemplate() {
            @Override
            public String getTemplateLocation() {
                return parentFolder + "/" + templateName + "/" + subFolder;
            }

            @Override
            public String getTemplateName() {
                return templateName;
            }
        };
    }

    public List<ITemplate> getTemplates() {
        return templates;
    }

    public static void load() {
        FileBuilder fb = new FileBuilder("templates.yml");
        templates.addAll(new ConfigurationUtil().getTemplateList(fb));
    }

    public static void unload() {
        FileBuilder fb = new FileBuilder("templates.yml");
        new ConfigurationUtil().saveTemplateList(templates, fb);
    }

    public ITemplate getTemplate(String name) {
        for(ITemplate te : getTemplates()) {
            if(name.equals(te.getTemplateName())) {
                return te;
            }
        }
        return null;
    }

}
