package me.refluxo.cloud.service;

import java.util.List;

public interface IGroup {

    List<ITemplate> getTemplates();

    int getRam();

    int minInstances();

    String getGroupName();

    int maxInstances();

    boolean isStatic();

    ServiceType getServiceType();

}
