package GestorPorts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RuleDialog extends JDialog {
    private JTextField nomField;
    private JTextField portField;
    private JComboBox<String> protocolField;
    private JTextField appField;
    private JTextField usuariField;
    private JTextField grupField;
    private JTextField ipField;
    private JComboBox<String> accioField;
    private JTextField interficieField;
    private JComboBox<String> sentitField;
    private JButton saveButton;
    private JButton cancelButton;

    public RuleDialog(Frame owner) {
        super(owner, "Nova Regla", true);

        nomField = new JTextField();
        portField = new JTextField();
        protocolField = new JComboBox<>(new String[] { "TCP", "UDP" });
        appField = new JTextField();
        usuariField = new JTextField();
        grupField = new JTextField();
        ipField = new JTextField();
        accioField = new JComboBox<>(new String[] { "Permetre", "Denegar" });
        interficieField = new JTextField();
        sentitField = new JComboBox<>(new String[] { "IN", "OUT" });
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a new FirewallRule from the input fields
                FirewallRule rule = new FirewallRule(
                        nomField.getText(),
                        Integer.parseInt(portField.getText()),
                        (String) protocolField.getSelectedItem(),
                        appField.getText(),
                        usuariField.getText(),
                        grupField.getText(),
                        ipField.getText(),
                        (String) accioField.getSelectedItem(),
                        interficieField.getText(),
                        (String) sentitField.getSelectedItem());

                // Validate the rule and check for duplicates
                // ...

                // Add the rule to the firewall
                FirewallManager manager = new FirewallManager();
                manager.addRule(rule);

                // Close the dialog
                RuleDialog.this.dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close dialog without saving
                RuleDialog.this.dispose();
            }
        });

        setLayout(new GridLayout(11, 2));
        add(new JLabel("Nom: "));
        add(nomField);
        add(new JLabel("Port: "));
        add(portField);
        add(new JLabel("Protocol: "));
        add(protocolField);
        add(new JLabel("App: "));
        add(appField);
        add(new JLabel("Usuari: "));
        add(usuariField);
        add(new JLabel("Grup: "));
        add(grupField);
        add(new JLabel("IP: "));
        add(ipField);
        add(new JLabel("Accio: "));
        add(accioField);
        add(new JLabel("Interficie: "));
        add(interficieField);
        add(new JLabel("Sentit: "));
        add(sentitField);
        add(saveButton);
        add(cancelButton);

        pack();
    }
}