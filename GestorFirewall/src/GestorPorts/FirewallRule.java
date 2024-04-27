package GestorPorts;

public class FirewallRule {
    private String name;
    private int port;
    private String protocol;
    private String application;
    private String user;
    private String group;
    private String ipAddress;
    private String action;
    private String networkInterface;
    private String direction;

    // Constructor
    public FirewallRule(String name, int port, String protocol, String application, String user, String group,
            String ipAddress, String action, String networkInterface, String direction) {
        this.name = name;
        this.port = port;
        this.protocol = protocol;
        this.application = application;
        this.user = user;
        this.group = group;
        this.ipAddress = ipAddress;
        this.action = action;
        this.networkInterface = networkInterface;
        this.direction = direction;
    }
    // Getters and setters for each field

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}