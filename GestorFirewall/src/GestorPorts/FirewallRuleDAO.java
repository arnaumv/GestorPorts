package GestorPorts;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FirewallRuleDAO {
    // Instancia singleton de la clase DAO
    private static FirewallRuleDAO instance;
    // Conexión a la base de datos

    private Connection connection;

    // Constructor privado para el patrón singleton, establece la conexión a la base
    // de datos

    private FirewallRuleDAO() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestorfirewall", "user777", "");
    }

    // Método para obtener la instancia singleton, si no existe la crea
    public static FirewallRuleDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new FirewallRuleDAO();
        }
        return instance;
    }
    // Método para añadir una nueva regla a la base de datos

    public void addRule(FirewallRule rule) throws SQLException {
        if (getRule(rule.getName()) != null) {
            throw new SQLException("Una regla con el mismo nombre ya existe.");
        }
        // Consultas SQL para insertar la regla en la tabla de reglas y en la tabla de
        // historial

        String sql = "INSERT INTO reglas_firewall (nombre, puerto, protocolo, aplicacion, usuario, grupo, direccion_ip, accion, interfaz_red, direccion, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlHistory = "INSERT INTO reglas_historial (nombre, puerto, protocolo, aplicacion, usuario, grupo, direccion_ip, accion, interfaz_red, direccion, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating rule failed, no rows affected.");
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(sqlHistory, Statement.RETURN_GENERATED_KEYS)) {
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
            statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating rule in history failed, no rows affected.");
            }
        }
    }

    // Método para obtener todas las reglas de la base de datos

    public List<FirewallRule> getAllRules() {
        List<FirewallRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM reglas_firewall";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                FirewallRule rule = new FirewallRule(
                        rs.getString("nombre"),
                        rs.getInt("puerto"),
                        rs.getString("protocolo"),
                        rs.getString("aplicacion"),
                        rs.getString("usuario"),
                        rs.getString("grupo"),
                        rs.getString("direccion_ip"),
                        rs.getString("accion"),
                        rs.getString("interfaz_red"),
                        rs.getString("direccion"));
                rules.add(rule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rules;
    }

    // Método para obtener una regla por su nombre de la base de datos

    public FirewallRule getRule(String name) {
        FirewallRule rule = null;
        String sql = "SELECT * FROM reglas_firewall WHERE nombre = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                rule = new FirewallRule(
                        rs.getString("nombre"),
                        rs.getInt("puerto"),
                        rs.getString("protocolo"),
                        rs.getString("aplicacion"),
                        rs.getString("usuario"),
                        rs.getString("grupo"),
                        rs.getString("direccion_ip"),
                        rs.getString("accion"),
                        rs.getString("interfaz_red"),
                        rs.getString("direccion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rule;
    }

    // Método para comprobar si una regla ya existe en la base de datos
    public boolean ruleExists(FirewallRule rule) {
        String sql = "SELECT * FROM reglas_firewall WHERE nombre = ? AND puerto = ? AND protocolo = ? AND aplicacion = ? AND usuario = ? AND grupo = ? AND direccion_ip = ? AND accion = ? AND interfaz_red = ? AND direccion = ?";

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

            ResultSet rs = statement.executeQuery();
            return rs.next(); // Si hay un resultado, la regla existe
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Si ocurre una excepción, asumimos que la regla no existe
    }

    // Método para actualizar una regla en la base de datos

    public void updateRule(String originalName, FirewallRule rule) {
        String sql = "UPDATE reglas_firewall SET nombre = ?, puerto = ?, protocolo = ?, aplicacion = ?, usuario = ?, grupo = ?, direccion_ip = ?, accion = ?, interfaz_red = ?, direccion = ? WHERE nombre = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rule.getName());
            statement.setInt(2, rule.getPort());
            statement.setString(3, rule.getProtocol());
            statement.setString(4, rule.getApplication());
            statement.setString(5, rule.getUser());
            statement.setString(6, rule.getGroup());
            statement.setString(7, rule.getIpAddress());
            statement.setString(8, rule.getAction());
            statement.setString(10, rule.getDirection());
            statement.setString(11, originalName);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insertar la regla actualizada en la tabla "reglas_historial"
        sql = "INSERT INTO reglas_historial (nombre, puerto, protocolo, aplicacion, usuario, grupo, direccion_ip, accion, interfaz_red, direccion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

    // Método para eliminar una regla de la base de datos

    public void deleteRule(String ruleName) {
        String deleteSql = "delete from reglas_firewall where nombre = ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setString(1, ruleName);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String updateSql = "update reglas_historial set fecha_borrada = current_timestamp where nombre = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, ruleName);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<FirewallRule> getHistoryRules() {
        List<FirewallRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM reglas_historial";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                FirewallRule rule = new FirewallRule(
                        rs.getString("nombre"),
                        rs.getInt("puerto"),
                        rs.getString("protocolo"),
                        rs.getString("aplicacion"),
                        rs.getString("usuario"),
                        rs.getString("grupo"),
                        rs.getString("direccion_ip"),
                        rs.getString("accion"),
                        rs.getString("interfaz_red"),
                        rs.getString("direccion"));
                rules.add(rule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return rules;
    }

    // other methods to update and retrieve rules
}