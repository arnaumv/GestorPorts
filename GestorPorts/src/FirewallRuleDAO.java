import java.sql.*;

public class FirewallRuleDAO {
    private Connection connection;

    public FirewallRuleDAO() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestorports", "username", "password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRule(FirewallRule rule) {
        String sql = "INSERT INTO firewall_rules (name, port, protocol, application, user, group, ip_address, action, network_interface, direction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rule.getName());
            statement.setInt(2, rule.getPort());
            statement.setString(3, rule.getProtocol());
            statement.setString(4, rule.getApplication());
            statement.setString(5, rule.getUser());
            statement.setString(6, rule.getGroup());
            statement.setString(7, rule.getIpAddress());
            statement.setString(8, rule.getAction());
            statement.setString(9, rule.getNetworkInterface());
            statement.setString(10, rule.getDirection());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRule(String ruleName) {
        String sql = "DELETE FROM firewall_rules WHERE name = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ruleName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // other methods to update and retrieve rules
}