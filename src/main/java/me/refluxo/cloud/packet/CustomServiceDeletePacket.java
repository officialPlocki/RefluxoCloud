package me.refluxo.cloud.packet;

import me.refluxo.cloud.manager.ServiceManager;
import me.refluxo.networker.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CustomServiceDeletePacket implements Packet {

    private final String serviceName;

    public CustomServiceDeletePacket(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void read(ObjectInputStream objectInputStream) throws IOException {
        new ServiceManager().unregisterCustomService(new ServiceManager().getCustomServiceByName(objectInputStream.readUTF()));
    }

    @Override
    public void write(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeUTF(serviceName);
    }
}

