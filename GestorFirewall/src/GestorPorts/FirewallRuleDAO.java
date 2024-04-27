package GestorPorts;

import java.sql.*;

public class FirewallRuleDAO {
    private static FirewallRuleDAO instance;
    private Connection connection;

    private FirewallRuleDAO() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestorfirewall", "root", "root");
    }

    public static FirewallRuleDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new FirewallRuleDAO();
        }
        return instance;
    }

    public void addRule(FirewallRule rule) {
        String sql = "INSERT INTO reglas_firewall (nombre, puerto, protocolo, aplicacion, usuario, grupo, direccion_ip, accion, interfaz_red, direccion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        String sql = "DELETE FROM reglas_firewall WHERE nombre = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ruleName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // other methods to update and retrieve rules
}