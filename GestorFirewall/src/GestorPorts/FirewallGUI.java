package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        this.tableModel = new DefaultTableModel(new Object[] { "Nom", "Port", "Protocol", "App", "Usuari", "Grup", "IP",
                "Accio", "Interficie", "Sentit" }, 0);
        this.table = new JTable(tableModel);
        this.modifyButton = new JButton("Modificar");
        this.deleteButton = new JButton("Esborrar");
        this.newRuleButton = new JButton("Nova Regla");
        this.historyButton = new JButton("Historial");
        this.historyList = new JList<>();
        this.frame = new JFrame("Firewall Manager");

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
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    RuleDialog dialog = new RuleDialog(frame);
                    // Populate dialog with current rule data
                    dialog.setVisible(true);
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