package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirewallGUI {
    private FirewallManager manager;
    private JTable table;
    private JButton modifyButton;
    private JButton deleteButton;
    private JButton newRuleButton;
    private JButton historyButton;
    private DefaultTableModel tableModel;
    private JList<String> historyList;
    private JFrame frame;

    public FirewallGUI() {
        this.manager = new FirewallManager();
        this.tableModel = new DefaultTableModel(new Object[] { "Nom", "Port", "Protocol", "App", "Usuari", "Grup",
                "IP", "Accio", "Interficie", "Sentit" }, 0);
        this.table = new JTable(tableModel);
        this.modifyButton = new JButton("Modificar");
        this.deleteButton = new JButton("Esborrar");
        this.newRuleButton = new JButton("Nova Regla");
        this.historyButton = new JButton("Historial");
        this.historyList = new JList<>();
        this.frame = new JFrame("Configurar Regles del Firewall");

        modifyButton.setEnabled(false);
        deleteButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean isRowSelected = table.getSelectedRow() != -1;
            modifyButton.setEnabled(isRowSelected);
            deleteButton.setEnabled(isRowSelected);
        });

        newRuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RuleDialog dialog = new RuleDialog(frame);
                dialog.setVisible(true);
                if (dialog.isRuleSaved()) {
                    addRuleToTable(dialog.getRule());
                }
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    List<Object> selectedRule = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        selectedRule.add(tableModel.getValueAt(selectedRow, i));
                    }

                    // Store the original name of the rule
                    String originalName = (String) selectedRule.get(0);
                    System.out.println("Original rule name: " + originalName);

                    RuleModifier ruleModifier = new RuleModifier(manager, selectedRule);
                    JPanel modifyPanel = ruleModifier.getModifyPanel();

                    // Set the preferred size of the panel
                    modifyPanel.setPreferredSize(new Dimension(400, 400));

                    // Declare the JFrame here
                    JFrame modifyFrame = new JFrame("Modificar Regla");

                    // Create a new panel for the buttons with a FlowLayout
                    JPanel buttonPanel = new JPanel(new FlowLayout());

                    JButton saveButton = new JButton("Guardar");
                    saveButton.addActionListener(ruleModifier.getSaveButtonActionListener(modifyFrame, originalName,
                            tableModel, selectedRow));
                    buttonPanel.add(saveButton);

                    // Add a cancel button that closes the frame
                    JButton cancelButton = new JButton("Cancelar");
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            modifyFrame.dispose();
                        }
                    });
                    buttonPanel.add(cancelButton);

                    // Add some space above the buttons
                    modifyPanel.add(Box.createVerticalStrut(20));

                    // Add the button panel to the modify panel
                    modifyPanel.add(buttonPanel);

                    // Add a border to the panel to create space between the buttons and the fields
                    // above
                    modifyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    modifyFrame.setContentPane(modifyPanel);
                    modifyFrame.pack();
                    modifyFrame.setVisible(true);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Implement rule deletion logic
                    // Add entry to history
                }
            }
        });

        // Load rules from database
        List<FirewallRule> rules = manager.getAllRules();
        for (FirewallRule rule : rules) {
            addRuleToTable(rule);
        }
    }

    private void addRuleToTable(FirewallRule rule) {
        tableModel.addRow(new Object[] {
                rule.getName(),
                rule.getPort(),
                rule.getProtocol(),
                rule.getApplication(),
                rule.getUser(),
                rule.getGroup(),
                rule.getIpAddress(),
                rule.getAction(),
                rule.getNetworkInterface(),
                rule.getDirection()
        });
    }

    public void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel rulesLabel = new JLabel("  Regles: ");
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(rulesLabel, BorderLayout.WEST);
        topPanel.add(historyButton, BorderLayout.EAST);

        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLeftPanel.add(newRuleButton);

        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.add(modifyButton);
        bottomRightPanel.add(deleteButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
        bottomPanel.add(bottomRightPanel, BorderLayout.EAST);

        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}