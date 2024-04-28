package GestorPorts;

import javax.swing.*;
import java.util.List;
import java.awt.GridLayout;

public class RuleModifier {
    private List<Object> selectedRule;
    private JPanel modifyPanel;
    private JTextField nomField, portField, appField, usuariField, grupField, ipField, interficieField;
    private JComboBox<String> protocolField, accioField, sentitField;

    public RuleModifier(List<Object> selectedRule) {
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
}