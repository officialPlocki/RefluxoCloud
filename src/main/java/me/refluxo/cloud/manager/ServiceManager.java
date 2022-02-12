package me.refluxo.cloud.manager;

import de.vapecloud.driver.VapeDriver;
import de.vapecloud.driver.console.logger.enums.MessageType;
import me.refluxo.cloud.RefluxoCloud;
import me.refluxo.cloud.packet.StopServicePacket;
import me.refluxo.cloud.service.*;
import me.refluxo.cloud.util.files.FileBuilder;
import me.refluxo.cloud.util.files.YamlConfiguration;
import me.refluxo.cloud.util.mysql.MySQLService;
import me.refluxo.networker.instance.ConnectionManager;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceManager {

    public static final HashMap<String, ITemplate> serviceTemplates = new HashMap<>();
    public static final HashMap<String, IService> runningServices = new HashMap<>();
    public static final HashMap<String, ICustomService> runningCustomServices = new HashMap<>();

    public void registerCustomService(ICustomService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§aRegistered Service " + service.getInstanceUUID());
        new MySQLManager().registerCustomService(service);
        ITemplate cache = new TemplateManager().createTemplate(service.getTemplate().getTemplateName(), "tmp", service.getOwnerUUID());
        new TemplateManager().registerTemplate(cache);
        serviceTemplates.put(service.getInstanceUUID(), cache);
        try {
            copyDirectory(new File(service.getTemplate().getTemplateLocation()), new File(cache.getTemplateLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(ITemplate templates : service.getTemplates()) {
            try {
                copyDirectory(new File(templates.getTemplateLocation()), new File(cache.getTemplateLocation()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        startCustomService(service);
        runningCustomServices.put(service.getInstanceUUID(), service);
    }

    public void registerService(IService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§aRegistered Service " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
        new MySQLManager().registerService(service);
        TemplateManager tm = new TemplateManager();
        ITemplate cache = tm.createTemplate(service.getTemplate().getTemplateName(), "tmp", service.getTemplate().getTemplateName()+ "@" + service.getTaskID());
        tm.registerTemplate(cache);
        serviceTemplates.put(service.getInstanceUUID(), cache);
        try {
            copyDirectory(new File(service.getTemplate().getTemplateLocation()), new File(cache.getTemplateLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        service.getInstanceGroup().getTemplates().forEach(template -> {
            try {
                copyDirectory(new File(template.getTemplateLocation()), new File(cache.getTemplateLocation()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        startService(service);
        runningServices.put(service.getInstanceUUID(), service);
    }

    public void unregisterCustomService(ICustomService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§cUnregistered Service " + service.getInstanceUUID());
        stopCustomService(service);
        ITemplate cache = serviceTemplates.get(service.getInstanceUUID());
        RefluxoCloud.packetServer.sendPacket(new StopServicePacket(), service.getInstanceUUID());
        new Thread(() -> {
            try {
                Thread.sleep(25000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new MySQLManager().unregisterCustomService(service);
            if(service.isStatic()) {
                try {
                    copyDirectory(new File(cache.getTemplateLocation()), new File(service.getTemplate().getTemplateLocation()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File[] allContents = new File(cache.getTemplateLocation()).listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    file.delete();
                }
            }
            new File(cache.getTemplateLocation()).delete();
            serviceTemplates.remove(service.getInstanceUUID());
            runningCustomServices.remove(service.getInstanceUUID());
        }).start();
    }

    public void unregisterService(IService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§cUnregistered Service " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
        stopService(service);
        RefluxoCloud.packetServer.sendPacket(new StopServicePacket(), service.getInstanceGroup().getGroupName() + "@" + service.getTaskID());
        ITemplate cache = serviceTemplates.get(service.getInstanceUUID());
        new Thread(() -> {
            try {
                Thread.sleep(25000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new MySQLManager().unregisterService(service);
            if(service.getInstanceGroup().isStatic()) {
                try {
                    copyDirectory(new File(cache.getTemplateLocation()), new File(service.getTemplate().getTemplateLocation()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File[] allContents = new File(cache.getTemplateLocation()).listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    file.delete();
                }
            }
            new File(cache.getTemplateLocation()).delete();
            serviceTemplates.remove(service.getInstanceUUID());
            runningServices.remove(service.getInstanceUUID());
        }).start();
    }

    private void startService(IService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§eLoading Service " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
        ITemplate cache = serviceTemplates.get(service.getInstanceUUID());
        File file = new File(cache.getTemplateLocation() + "/server.jar");
        if(!file.exists()) {
            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§eDownloading Server File for " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
            if(service.getInstanceGroup().getServiceType().equals(ServiceType.SERVER)) {
                try (BufferedInputStream in = new BufferedInputStream(new URL("https://www.gardling.com/jenkins/job/JettPack/279/artifact/target/jettpack-paperclip-b279.jar").openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else if(service.getInstanceGroup().getServiceType().equals(ServiceType.PROXY)) {
                try (BufferedInputStream in = new BufferedInputStream(new URL("https://serverjars.com/api/fetchJar/waterfall/1.18").openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        try {
            FileUtils.copyFile(new File("CloudAPI.jar"), new File(cache.getTemplateLocation()+"/plugins/CloudAPI.jar"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(service.getInstanceGroup().getServiceType().equals(ServiceType.SERVER)) {
            try {
                copyDirectory(new File("default/"), new File(cache.getTemplateLocation()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String[] cmdarray = {
                        "screen", "-dmS", service.getInstanceUUID(), "java", "-Xmx" + service.getInstanceGroup().getRam() + "M", "-Xms" + service.getInstanceGroup().getRam() + "M", "-Dcom.mojang.eula.agree=true", "-jar", "server.jar", "--nogui", "--port", "" + service.getPort()
                };
                Runtime.getRuntime().exec(cmdarray, null, new File(cache.getTemplateLocation()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(service.getInstanceGroup().getServiceType().equals(ServiceType.PROXY)) {
            try {
                String[] cmdarray = {
                        "screen", "-dmS", service.getInstanceUUID(), "java", "-Xmx" + service.getInstanceGroup().getRam() + "M", "-Xms" + service.getInstanceGroup().getRam() + "M", "-jar", "server.jar"
                };
                Runtime.getRuntime().exec(cmdarray, null, new File(cache.getTemplateLocation()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileUtils.copyFile(new File("config.yml"), new File(cache.getTemplateLocation()+"/config/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileBuilder config = new FileBuilder(cache.getTemplateLocation() + "/config/config.yml");
        YamlConfiguration yml = config.getYaml();
        if(!config.getFile().exists()) {
            try {
                config.getFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yml.set("info.uuid", service.getInstanceUUID());
        yml.set("info.name", service.getInstanceGroup().getGroupName() + "@" + service.getTaskID());
        yml.set("info.host", RefluxoCloud.host);
        yml.set("info.sessionUUID", RefluxoCloud.sessionUUID);
        yml.set("info.type", service.getInstanceGroup().getServiceType().name());
        config.save();
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§aStarting Service " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
    }

    private void startCustomService(ICustomService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§eLoading Service " + service.getInstanceUUID());
        ITemplate cache = serviceTemplates.get(service.getInstanceUUID());
        File file = new File(cache.getTemplateLocation() + "/server.jar");
        if(!file.exists()) {
            VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§eDownloading Server File for " + service.getInstanceUUID());
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://www.gardling.com/jenkins/job/JettPack/279/artifact/target/jettpack-paperclip-b279.jar").openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        try {
            FileUtils.copyFile(new File("CloudAPI.jar"), new File(cache.getTemplateLocation()+"/plugins/CloudAPI.jar"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            copyDirectory(new File("default/"), new File(cache.getTemplateLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String[] cmdarray = {
                    "screen", "-dmS", service.getInstanceUUID(), "java", "-Xmx" + service.getRam() + "M", "-Xms" + service.getRam() + "M", "-Dcom.mojang.eula.agree=true", "-jar", "server.jar", "--nogui", "--port", "" + service.getPort()
            };
            Runtime.getRuntime().exec(cmdarray, null, new File(cache.getTemplateLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.copyFile(new File("config.yml"), new File(cache.getTemplateLocation()+"/config/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileBuilder config = new FileBuilder(cache.getTemplateLocation() + "/config/config.yml");
        YamlConfiguration yml = config.getYaml();
        if(!config.getFile().exists()) {
            try {
                config.getFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yml.set("info.uuid", service.getInstanceUUID());
        yml.set("info.name", service.getInstanceUUID());
        yml.set("info.host", RefluxoCloud.host);
        yml.set("info.sessionUUID", RefluxoCloud.sessionUUID);
        yml.set("info.type", ServiceType.SERVER.name());
        config.save();
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§aStarting Service " + service.getInstanceUUID());
    }

    private void stopCustomService(ICustomService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§cStopping Service " + service.getInstanceUUID());
    }

    private void stopService(IService service) {
        VapeDriver.getInstance().getConsolHandler().getLogger().sendMessage(MessageType.DEBUG, false, "§cStopping Service " + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + " (" + service.getInstanceUUID() + ")");
    }

    public static void loadServiceScheduler() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(Integer.MAX_VALUE);
        MySQLService mysql = new MySQLService();
        service.scheduleWithFixedDelay(() -> {
            ServiceManager manager = new ServiceManager();
            TemplateManager templateManager = new TemplateManager();
            new GroupManager().getGroups().forEach(group -> {
                List<IService> services = new ArrayList<>();
                for (IService value : runningServices.values()) {
                    if(value.getInstanceGroup().equals(group)) {
                        services.add(value);
                    }
                }
                if(services.size() < group.minInstances()) {
                    ITemplate template = templateManager.createTemplate(group.getGroupName(), "local", "default");
                    String id = UUID.randomUUID().toString();
                    int port = new Random().nextInt(65534);
                    manager.registerService(new IService() {

                        @Override
                        public String getInstanceUUID() {
                            return id;
                        }

                        @Override
                        public int getTaskID() {
                            return services.size()+1;
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
                    });
                }
            });
            ResultSet rs = mysql.getResult("SELECT * FROM serviceStatus;");
            try {
                while(rs.next()) {
                    String uuid = rs.getString("serviceUUID");
                    boolean online = rs.getBoolean("isOnline");
                    if(!online) {
                        if(new ServiceManager().getServiceByUUID(uuid) != null) {
                            new ServiceManager().unregisterService(new ServiceManager().getServiceByUUID(uuid));
                        } else if(new ServiceManager().getCustomServiceByUUID(uuid) != null) {
                            new ServiceManager().unregisterCustomService(new ServiceManager().getCustomServiceByUUID(uuid));
                        }
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public IService getServiceByName(String name) {
        AtomicReference<IService> s = new AtomicReference<>(null);
        runningServices.forEach((uuid, service) -> {
            if(service.getInstanceGroup().getGroupName().equals(name.split("@")[0])) {
                s.set(service);
            }
        });
        return s.get();
    }

    public ICustomService getCustomServiceByName(String name) {
        AtomicReference<ICustomService> s = new AtomicReference<>(null);
        runningCustomServices.forEach((uuid, service) -> {
            if(service.getInstanceUUID().equals(name)) {
                s.set(service);
            }
        });
        return s.get();
    }

    public IService getServiceByUUID(String uuid) {
        return runningServices.getOrDefault(uuid, null);
    }

    public ICustomService getCustomServiceByUUID(String uuid) {
        return runningCustomServices.getOrDefault(uuid, null);
    }

    private void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i< Objects.requireNonNull(children).length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

}
