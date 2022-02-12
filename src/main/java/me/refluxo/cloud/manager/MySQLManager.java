package me.refluxo.cloud.manager;

import me.refluxo.cloud.RefluxoCloud;
import me.refluxo.cloud.service.ICustomService;
import me.refluxo.cloud.service.IService;
import me.refluxo.cloud.service.ServiceType;
import me.refluxo.cloud.util.mysql.MySQLService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class MySQLManager {

    private static MySQLService mysql;

    public void registerService(IService service) {
        if(!contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            mysql.executeUpdate("INSERT INTO cloudInfo(serviceUUID,players) VALUES ('" + service.getInstanceUUID() + "',0);");
            mysql.executeUpdate("INSERT INTO cloudServices(serviceUUID,serviceName) VALUES ('" + service.getInstanceUUID() + "','" + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + "');");
            mysql.executeUpdate("INSERT INTO serviceStatus(serviceUUID,isOnline) VALUES ('" + service.getInstanceUUID() + "',true);");
            if(service.getInstanceGroup().getServiceType().equals(ServiceType.SERVER)) {
                mysql.executeUpdate("INSERT INTO bungeeList(serviceName,serviceIP,servicePort) VALUES ('" + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + "','" + RefluxoCloud.host + "'," + service.getPort() + ");");
            }
        }
    }

    public void registerCustomService(ICustomService service) {
        if(!contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            mysql.executeUpdate("INSERT INTO cloudInfo(serviceUUID,players) VALUES ('" + service.getInstanceUUID() + "',0);");
            mysql.executeUpdate("INSERT INTO customCloudServices(serviceUUID,serviceName,serviceOwner) VALUES ('" + service.getInstanceUUID() + "','" + service.getInstanceUUID() + "','" + service.getOwnerUUID() +"');");
            mysql.executeUpdate("INSERT INTO bungeeList(serviceName,serviceIP,servicePort) VALUES ('" + service.getInstanceUUID() + "','" + RefluxoCloud.host + "'," + service.getPort() + ");");
            mysql.executeUpdate("INSERT INTO serviceStatus(serviceUUID,isOnline) VALUES ('" + service.getInstanceUUID() + "',true);");
        }
    }

    public void unregisterCustomService(ICustomService service) {
        if(contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            mysql.executeUpdate("DELETE FROM cloudInfo WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            mysql.executeUpdate("DELETE FROM customCloudServices WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            mysql.executeUpdate("DELETE FROM bungeeList WHERE serviceName = '" + service.getInstanceUUID() + "';");
            mysql.executeUpdate("DELETE FROM serviceStatus WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
        }
    }

    public boolean isOnline(ICustomService service) {
        if(contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            ResultSet rs = mysql.getResult("SELECT * FROM serviceStatus WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            try {
                if(rs.next()) {
                    return rs.getBoolean("isOnline");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean isOnline(IService service) {
        if(contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            ResultSet rs = mysql.getResult("SELECT * FROM serviceStatus WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            try {
                if(rs.next()) {
                    return rs.getBoolean("isOnline");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void unregisterService(IService service) {
        if(contains("cloudInfo", "serviceUUID", service.getInstanceUUID())) {
            mysql.executeUpdate("DELETE FROM cloudInfo WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            mysql.executeUpdate("DELETE FROM cloudServices WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            mysql.executeUpdate("DELETE FROM serviceStatus WHERE serviceUUID = '" + service.getInstanceUUID() + "';");
            if(service.getInstanceGroup().getServiceType().equals(ServiceType.SERVER)) {
                mysql.executeUpdate("DELETE FROM bungeeList WHERE serviceName = '" + service.getInstanceGroup().getGroupName() + "@" + service.getTaskID() + "';");
            }
        }
    }

    private boolean contains(String table, String string, String value) {
        ResultSet rs = mysql.getResult("SELECT * FROM " + table + " WHERE " + string + " = '" + value + "';");
        try {
            if(rs.next()) {
                return rs.getString(string) != null || !Objects.equals(rs.getString(string), "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void init() {
        mysql = new MySQLService();
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS cloudServices(serviceUUID TEXT, serviceName TEXT);");
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS customCloudServices(serviceUUID TEXT, serviceName TEXT, serviceOwner TEXT);");
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS cloudInfo(serviceUUID TEXT, players INT);");
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS bungeeList(serviceName TEXT, serviceIP TEXT, servicePort INT)");
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS serviceStatus(serviceUUID TEXT, isOnline BOOLEAN);");
        mysql.executeUpdate("CREATE TABLE IF NOT EXISTS packetPipes(serviceUUID TEXT, pipeUUID TEXT)");
    }

}
