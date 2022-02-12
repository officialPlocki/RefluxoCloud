package me.refluxo.cloud.packet;

import me.refluxo.cloud.manager.ServiceManager;
import me.refluxo.cloud.manager.TemplateManager;
import me.refluxo.cloud.service.ICustomService;
import me.refluxo.cloud.service.ITemplate;
import me.refluxo.networker.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class CustomServiceCreatePacket implements Packet {

    private final boolean isStatic;
    private final List<ITemplate> templates;
    private final ITemplate template;
    private final int ram;
    private final String owner;

    public CustomServiceCreatePacket(boolean isStatic, List<ITemplate> templates, ITemplate template, int ram, String ownerUUID) {
        this.isStatic = isStatic;
        this.templates = templates;
        this.template = template;
        this.ram = ram;
        this.owner = ownerUUID;
    }

    @Override
    public void read(ObjectInputStream objectInputStream) throws IOException {
        boolean isStatic = objectInputStream.readBoolean();
        List<String> templates = new ArrayList<>(Arrays.asList(objectInputStream.readUTF().split(",")));
        List<ITemplate> t = new ArrayList<>();
        for(String s : templates) {
            t.add(new TemplateManager().getTemplate(s));
        }
        ITemplate template = new TemplateManager().getTemplate(objectInputStream.readUTF());
        int ram = objectInputStream.readInt();
        String owner = objectInputStream.readUTF();
        String uuid = UUID.randomUUID().toString();
        int port = new Random().nextInt(65534);
        ICustomService service = new ICustomService() {
            @Override
            public boolean isStatic() {
                return isStatic;
            }

            @Override
            public String getInstanceUUID() {
                return uuid;
            }

            @Override
            public List<ITemplate> getTemplates() {
                return t;
            }

            @Override
            public ITemplate getTemplate() {
                return template;
            }

            @Override
            public int getRam() {
                return ram;
            }

            @Override
            public String getOwnerUUID() {
                return owner;
            }

            @Override
            public int getPort() {
                return port;
            }
        };
        new ServiceManager().registerCustomService(service);
    }

    @Override
    public void write(ObjectOutputStream objectOutputStream) throws IOException {
            objectOutputStream.writeBoolean(isStatic);
            String ts;
            List<String> tn = new ArrayList<>();
            templates.forEach(t -> tn.add(t.getTemplateName()));
            ts = String.join(",", tn);
            objectOutputStream.writeUTF(ts);
            objectOutputStream.writeUTF(template.getTemplateName());
            objectOutputStream.writeInt(ram);
            objectOutputStream.writeUTF(owner);
    }
}
