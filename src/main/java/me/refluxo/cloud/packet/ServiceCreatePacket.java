package me.refluxo.cloud.packet;

import me.refluxo.cloud.RefluxoCloud;
import me.refluxo.cloud.manager.GroupManager;
import me.refluxo.cloud.manager.ServiceManager;
import me.refluxo.cloud.manager.TemplateManager;
import me.refluxo.cloud.service.IGroup;
import me.refluxo.cloud.service.IService;
import me.refluxo.cloud.service.ITemplate;
import me.refluxo.networker.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class ServiceCreatePacket implements Packet {

    private final IGroup group;
    private final ITemplate template;

    public ServiceCreatePacket(IGroup group, ITemplate template) {
        this.group = group;
        this.template = template;
    }

    @Override
    public void read(ObjectInputStream objectInputStream) throws IOException {
        IGroup group = new GroupManager().getGroup(objectInputStream.readUTF());
        ITemplate template = new TemplateManager().getTemplate(objectInputStream.readUTF());
        List<IService> services = new ArrayList<>();
        for (IService value : ServiceManager.runningServices.values()) {
            if(value.getInstanceGroup().equals(group)) {
                services.add(value);
            }
        }
        String uuid = UUID.randomUUID().toString();
        int taskid = services.size()+1;
        int port = new Random().nextInt(65534);
        IService service = new IService() {
            @Override
            public String getInstanceUUID() {
                return uuid;
            }

            @Override
            public int getTaskID() {
                return taskid;
            }

            @Override
            public IGroup getInstanceGroup() {
                return group;
            }

            @Override
            public ITemplate getTemplate() {
                return template;
            }

            @Override
            public int getPort() {
                return port;
            }
        };
        new ServiceManager().registerService(service);
    }

    @Override
    public void write(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeUTF(group.getGroupName());
        objectOutputStream.writeUTF(template.getTemplateName());
    }
}
