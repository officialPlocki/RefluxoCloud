package me.refluxo.cloud.manager;

import me.refluxo.cloud.service.IGroup;
import me.refluxo.cloud.service.ITemplate;
import me.refluxo.cloud.service.ServiceType;
import me.refluxo.cloud.util.ConfigurationUtil;
import me.refluxo.cloud.util.files.FileBuilder;

import java.util.ArrayList;
import java.util.List;

public class GroupManager {

    public static final List<IGroup> groups = new ArrayList<>();

    public void registerGroup(IGroup group) {
        groups.add(group);
    }

    public IGroup createGroup(String groupName, ServiceType type, List<ITemplate> templates, int ram, int minInstances, int maxInstances, boolean staticGroup) {
        return new IGroup() {
            @Override
            public List<ITemplate> getTemplates() {
                return templates;
            }

            @Override
            public int getRam() {
                return ram;
            }

            @Override
            public int minInstances() {
                return minInstances;
            }

            @Override
            public String getGroupName() {
                return groupName;
            }

            @Override
            public int maxInstances() {
                return maxInstances;
            }

            @Override
            public boolean isStatic() {
                return staticGroup;
            }

            @Override
            public ServiceType getServiceType() {
                return type;
            }
        };
    }

    public List<IGroup> getGroups() {
        return groups;
    }

    public IGroup getGroup(String name) {
        for(IGroup group : getGroups()) {
            if(name.equals(group.getGroupName())) {
                return group;
            }
        }
        return null;
    }

    public static void load() {
        FileBuilder fb = new FileBuilder("groups.yml");
        groups.addAll(new ConfigurationUtil().getGroupList(fb));
    }

    public static void unload() {
        FileBuilder fb = new FileBuilder("groups.yml");
        new ConfigurationUtil().saveGroupList(groups, fb);
    }

}
