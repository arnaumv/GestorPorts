package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Arrays;
import java.util.List;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class RuleModifier {
    private FirewallManager manager; // Asume que FirewallManager es la clase que maneja las reglas del firewall
    private FirewallRuleDAO dao; // Asume que FirewallRuleDAO es la clase que maneja la persistencia de las
                                 // reglas

    private List<Object> selectedRule;
    private JPanel modifyPanel;
    private JTextField nomField, portField, appField, usuariField, grupField, ipField, interficieField;
    private JComboBox<String> protocolField, accioField, sentitField;

    public RuleModifier(FirewallManager manager, List<Object> selectedRule) {
        this.manager = manager;
        try {
            this.dao = FirewallRuleDAO.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
            // Maneja esta excepción de la manera que prefieras
        }
        this.selectedRule = selectedRule;
        this.modifyPanel = new JPanel(new GridLayout(0, 2));
    }

    public JPanel getModifyPanel() {
        nomField = new JTextField((String) selectedRule.get(0));
        modifyPanel.add(new JLabel("Nom:"));
        modifyPanel.add(nomField);

        portField = new JTextField(String.valueOf(selectedRule.get(1)));
        modifyPanel.add(new JLabel("Port:"));
        modifyPanel.add(portField);

        protocolField = new JComboBox<>(new String[] { "TCP", "UDP" });
        protocolField.setSelectedItem((String) selectedRule.get(2));
        modifyPanel.add(new JLabel("Protocol:"));
        modifyPanel.add(protocolField);

        appField = new JTextField((String) selectedRule.get(3));
        modifyPanel.add(new JLabel("App:"));
        modifyPanel.add(appField);

        usuariField = new JTextField((String) selectedRule.get(4));
        modifyPanel.add(new JLabel("Usuari:"));
        modifyPanel.add(usuariField);

        grupField = new JTextField((String) selectedRule.get(5));
        modifyPanel.add(new JLabel("Grup:"));
        modifyPanel.add(grupField);

        ipField = new JTextField((String) selectedRule.get(6));
        modifyPanel.add(new JLabel("IP:"));
        modifyPanel.add(ipField);

        accioField = new JComboBox<>(new String[] { "Permetre", "Denegar" });
        accioField.setSelectedItem((String) selectedRule.get(7));
        modifyPanel.add(new JLabel("Acció:"));
        modifyPanel.add(accioField);

        interficieField = new JTextField((String) selectedRule.get(8));
        modifyPanel.add(new JLabel("Interfície:"));
        modifyPanel.add(interficieField);

        sentitField = new JComboBox<>(new String[] { "IN", "OUT" });
        sentitField.setSelectedItem((String) selectedRule.get(9));
        modifyPanel.add(new JLabel("Sentit:"));
        modifyPanel.add(sentitField);

        return modifyPanel;
    }

    public ActionListener getSaveButtonActionListener(JFrame modifyFrame, String originalName,
            DefaultTableModel tableModel, int selectedRow) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create a new FirewallRule with the modified values
                FirewallRule modifiedRule = new FirewallRule(
                        getNomFieldText(),
                        Integer.parseInt(getPortFieldText()),
                        getProtocolFieldText(),
                        getAppFieldText(),
                        getUsuariFieldText(),
                        getGrupFieldText(),
                        getIpFieldText(),
                        getAccioFieldText(),
                        getInterficieFieldText(),
                        getSentitFieldText());

                // Validate the rule before updating
                if (!isRuleValid(modifiedRule)) {
                    JOptionPane.showMessageDialog(null, "Regla incorrecte. Name, port, and IP cannot be empty.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                // Check for duplicate rule
                if (isDuplicateRule(modifiedRule)) {
                    JOptionPane.showMessageDialog(null, "Nom de la regla duplicat", "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                // Check if a rule with the new name already exists
                if (!originalName.equals(modifiedRule.getName()) && manager.getRule(modifiedRule.getName()) != null) {
                    JOptionPane.showMessageDialog(null, "A rule with this name already exists", "Error",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                // Update the rule in the firewall manager
                try {
                    manager.updateRule(originalName, modifiedRule);
                    System.out.println("Rule updated successfully");
                    // Close the frame after successful update
                    modifyFrame.dispose();
                } catch (IllegalArgumentException ex) {
                    // Show error message if the update failed
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    // Handle the exception appropriately for your application
                    System.out.println("Error updating rule: " + ex.getMessage());
                }

                // Update the table model
                List<Object> modifiedRuleList = Arrays.asList(modifiedRule.getName(),
                        modifiedRule.getPort(),
                        modifiedRule.getProtocol(), modifiedRule.getApplication(), modifiedRule.getUser(),
                        modifiedRule.getGroup(), modifiedRule.getIpAddress(), modifiedRule.getAction(),
                        modifiedRule.getNetworkInterface(), modifiedRule.getDirection());

                for (int i = 0; i < 10; i++) {
                    tableModel.setValueAt(modifiedRuleList.get(i), selectedRow, i);
                }
            }
        };
    }

    public String getNomFieldText() {
        return nomField.getText();
    }

    public String getPortFieldText() {
        return portField.getText();
    }

    public String getProtocolFieldText() {
        return (String) protocolField.getSelectedItem();
    }

    public String getAppFieldText() {
        return appField.getText();
    }

    public String getUsuariFieldText() {
        return usuariField.getText();
    }

    public String getGrupFieldText() {
        return grupField.getText();
    }

    public String getIpFieldText() {
        return ipField.getText();
    }

    public String getAccioFieldText() {
        return (String) accioField.getSelectedItem();
    }

    public String getInterficieFieldText() {
        return interficieField.getText();
    }

    public String getSentitFieldText() {
        return (String) sentitField.getSelectedItem();
    }

    private boolean isRuleValid(FirewallRule rule) {
        // Check if the rule name is empty
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return false;
        }

        // Check if the port number is valid
        if (rule.getPort() < 1 || rule.getPort() > 65535) {
            return false;
        }

        // Check if the IP address is empty
        if (rule.getIpAddress() == null || rule.getIpAddress().trim().isEmpty()) {
            return false;
        }

        // Check if the IP address is valid
        if (!isValidIP(rule.getIpAddress())) {
            return false;
        }

        // Add more validation logic as needed...

        return true;
    }

    private boolean isValidIP(String ip) {
        // Check if the IP address is a range
        if (ip.contains("-")) {
            String[] parts = ip.split("-");
            if (parts.length != 2) {
                return false;
            }

            return isValidSingleIP(parts[0].trim()) && isValidSingleIP(parts[1].trim());
        }

        // If it's not a range, check if it's a valid single IP address
        return isValidSingleIP(ip);
    }

    private boolean isValidSingleIP(String ip) {
        // Use a regular expression (regex) to check if the IP address is valid
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }

    private boolean isDuplicateRule(FirewallRule rule) {
        return dao.ruleExists(rule);
    }

}