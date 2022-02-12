package me.refluxo.cloud.util;

import me.refluxo.cloud.service.IGroup;
import me.refluxo.cloud.service.IService;
import me.refluxo.cloud.service.ITemplate;
import me.refluxo.cloud.service.ServiceType;
import me.refluxo.cloud.util.files.FileBuilder;
import me.refluxo.cloud.util.files.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationUtil {

    public void saveTemplateList(List<ITemplate> list, FileBuilder builder) {
        YamlConfiguration yml = builder.getYaml();
        if(yml.isSet("templates")) {
            yml.set("templates", new ArrayList<String>());
            builder.save();
        }
        List<String> templates = new ArrayList<>(yml.getStringList("templates"));
        list.forEach(o -> {
            if(!templates.contains(o.getTemplateName())) {
                templates.add(o.getTemplateName());
            }
        });
        yml.set("templates", templates);
        builder.save();
        for (ITemplate template : list) {
            if(!yml.isSet("list." + template.getTemplateName())) {
                yml.set("list." + template.getTemplateName() + ".location", template.getTemplateLocation());
                yml.set("list." + template.getTemplateName() + ".name", template.getTemplateName());
                builder.save();
            }
        }
        builder.save();
    }

    public List<ITemplate> getTemplateList(FileBuilder builder) {
        YamlConfiguration yml = builder.getYaml();
        if(!yml.isSet("templates")) {
            yml.set("templates", new ArrayList<String>());
            builder.save();
        }
        List<ITemplate> templates = new ArrayList<>();
        List<String> names = yml.getStringList("templates");
        for (String string : names) {
            templates.add(new ITemplate() {
                @Override
                public String getTemplateLocation() {
                    return yml.getString("list." + string + ".location");
                }

                @Override
                public String getTemplateName() {
                    return yml.getString("list." + string + ".name");
                }
            });
        }
        return templates;
    }

    public void saveGroupList(List<IGroup> list, FileBuilder builder) {
        YamlConfiguration yml = builder.getYaml();
        if(!yml.isSet("groups")) {
            yml.set("groups", new ArrayList<String>());
            builder.save();
        }
        List<String> groups = new ArrayList<>(yml.getStringList("groups"));
        list.forEach(o -> {
            if(!groups.contains(o.getGroupName())) {
                groups.add(o.getGroupName());
            }
        });
        yml.set("groups", groups);
        builder.save();
        for (IGroup group : list) {
            if(!yml.isSet("list." + group.getGroupName())) {
                List<String> templates = new ArrayList<>();
                group.getTemplates().forEach(template -> templates.add(template.getTemplateName()));
                yml.set("list." + group.getGroupName() + ".name", group.getGroupName());
                yml.set("list." + group.getGroupName() + ".minInstances", group.minInstances());
                yml.set("list." + group.getGroupName() + ".maxInstances", group.maxInstances());
                yml.set("list." + group.getGroupName() + ".ram", group.getRam());
                yml.set("list." + group.getGroupName() + ".type", group.getServiceType().name());
                yml.set("list." + group.getGroupName() + ".templates", templates);
                yml.set("list." + group.getGroupName() + ".static", group.isStatic());
                builder.save();
            }
        }
        builder.save();
    }

    public List<IGroup> getGroupList(FileBuilder builder) {
        YamlConfiguration yml = builder.getYaml();
        if(!yml.isSet("groups")) {
            yml.set("groups", new ArrayList<String>());
            builder.save();
        }
        List<IGroup> groups = new ArrayList<>();
        List<String> names = yml.getStringList("groups");
        for (String string : names) {
            List<ITemplate> templates = new ArrayList<>();
            List<ITemplate> cTemplateList = getTemplateList(new FileBuilder("templates.yml"));
            cTemplateList.forEach(template -> {
                if(yml.getStringList("list." + string + ".templates").contains(template.getTemplateName())) {
                    templates.add(template);
                }
            });
            groups.add(new IGroup() {
                @Override
                public List<ITemplate> getTemplates() {
                    return templates;
                }

                @Override
                public int getRam() {
                    return yml.getInt("list." + string + ".ram");
                }

                @Override
                public int minInstances() {
                    return yml.getInt("list." + string + ".minInstances");
                }

                @Override
                public String getGroupName() {
                    return yml.getString("list." + string + ".name");
                }

                @Override
                public int maxInstances() {
                    return yml.getInt("list." + string + ".maxInstances");
                }

                @Override
                public boolean isStatic() {
                    return yml.getBoolean("list." + string + ".static");
                }

                @Override
                public ServiceType getServiceType() {
                    return ServiceType.valueOf(yml.getString("list." + string + ".type"));
                }
            });
        }
        return groups;
    }

}
