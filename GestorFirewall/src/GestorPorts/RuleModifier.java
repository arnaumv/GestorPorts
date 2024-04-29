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

        protocolField = new JComboBox<>(new String[] { "TCP", "UDP", "ICMP", "IP" });
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

                setInteractiveElementsEnabled(false);

                // Create a SwingWorker to perform the long-running task
                SwingWorker<FirewallRule, Void> worker = new SwingWorker<FirewallRule, Void>() {
                    private FirewallRule modifiedRule;

                    @Override
                    protected FirewallRule doInBackground() throws Exception {
                        // Create a new FirewallRule with the modified values
                        modifiedRule = new FirewallRule(
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

                        return modifiedRule;
                    }

                    @Override
                    protected void done() {
                        try {
                            // Validate the rule before updating
                            String validationError = isRuleValid(modifiedRule);
                            if (validationError != null) {
                                JOptionPane.showMessageDialog(null,
                                        validationError,
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                setInteractiveElementsEnabled(true);
                                return;
                            }

                            // Check for duplicate rule
                            if (isDuplicateRule(modifiedRule, originalName)) {
                                JOptionPane.showMessageDialog(null, "Nom de la regla duplicat", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                setInteractiveElementsEnabled(true);
                                return;
                            }

                            // Check if a rule with the new name already exists
                            if (!originalName.equals(modifiedRule.getName())
                                    && manager.getRule(modifiedRule.getName()) != null) {
                                JOptionPane.showMessageDialog(null, "A rule with this name already exists", "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                setInteractiveElementsEnabled(true);
                                return;
                            }

                            // Update the rule in the firewall manager
                            try {
                                // Add a delay of 1.5 seconds
                                Thread.sleep(1500);

                                manager.updateRule(originalName, modifiedRule);
                                System.out.println("Rule updated successfully");
                                // Close the frame after successful update
                                modifyFrame.dispose();
                            } catch (IllegalArgumentException ex) {
                                // Show error message if the update failed
                                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                setInteractiveElementsEnabled(true);
                            } catch (RuntimeException ex) {
                                // Show error message if a runtime exception occurred
                                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                setInteractiveElementsEnabled(true);
                            } catch (Exception ex) {
                                // Handle the exception appropriately for your application
                                System.out.println("Error updating rule: " + ex.getMessage());
                                setInteractiveElementsEnabled(true);
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
                        } catch (NumberFormatException ex) {
                            // Show custom error message
                            JOptionPane.showMessageDialog(null, "El port ha de ser un numero correcte", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            setInteractiveElementsEnabled(true);
                        }
                    }
                };

                // Start the SwingWorker
                worker.execute();
            }
        };
    }

    public void setInteractiveElementsEnabled(boolean enabled) {
        nomField.setEnabled(enabled);
        portField.setEnabled(enabled);
        protocolField.setEnabled(enabled);
        appField.setEnabled(enabled);
        usuariField.setEnabled(enabled);
        grupField.setEnabled(enabled);
        ipField.setEnabled(enabled);
        accioField.setEnabled(enabled);
        interficieField.setEnabled(enabled);
        sentitField.setEnabled(enabled);

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

    private String isRuleValid(FirewallRule rule) {
        // Check if the rule is null
        if (rule == null) {
            return "El port no es correcte.";
        }

        // Check if the rule name is empty
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return "El nom de la regla no pot estar buit.";
        }

        // Check if the port number is valid
        int port = rule.getPort();
        if (port < 1 || port > 65535) {
            return "El port ha de ser un número entre 1 i 65535.";
        }

        // Check if the IP address is valid, if it is not empty
        if (rule.getIpAddress() != null && !rule.getIpAddress().trim().isEmpty() && !isValidIP(rule.getIpAddress())) {
            return "La direcció IP no és vàlida.";
        }

        // Add more validation logic as needed...

        return null;
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

    private boolean isDuplicateRule(FirewallRule modifiedRule, String originalName) {
        // Get the existing rule with the same name
        FirewallRule existingRule = manager.getRule(modifiedRule.getName());

        // If there is no existing rule with the same name, it's not a duplicate
        if (existingRule == null) {
            return false;
        }

        // If the existing rule is the same rule that is being modified, it's not a
        // duplicate
        if (existingRule.getName().equals(originalName)) {
            return false;
        }

        // If the existing rule has the same name but all other fields are different,
        // it's not a duplicate
        if (existingRule.getPort() != modifiedRule.getPort() ||
                !existingRule.getProtocol().equals(modifiedRule.getProtocol()) ||
                !existingRule.getApplication().equals(modifiedRule.getApplication()) ||
                !existingRule.getUser().equals(modifiedRule.getUser()) ||
                !existingRule.getGroup().equals(modifiedRule.getGroup()) ||
                !existingRule.getIpAddress().equals(modifiedRule.getIpAddress()) ||
                !existingRule.getAction().equals(modifiedRule.getAction()) ||
                !existingRule.getNetworkInterface().equals(modifiedRule.getNetworkInterface()) ||
                !existingRule.getDirection().equals(modifiedRule.getDirection())) {
            return false;
        }

        // Otherwise, it's a duplicate
        return true;
    }

}