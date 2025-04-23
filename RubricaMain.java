import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class RubricaMain extends JFrame {
    private Vector<Persona> persone;
    private Vector<Persona> filteredPersone;
    private JTable tabella;
    private DefaultTableModel model;
    private JTextField searchField;
    private JLabel statusLabel;
    private JPopupMenu popupMenu;

    public RubricaMain() {
        super("Rubrica Telefonica");

        persone = FileManager.caricaPersone();
        filteredPersone = new Vector<>(persone);

        initUI();

        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel searchLabel = new JLabel("Cerca: ");
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterContacts(searchField.getText());
            }
        });
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        String[] columns = {"Nome", "Cognome", "Telefono"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        updateTable();

        tabella = new JTable(model);
        tabella.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        tabella.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabella);

        createContextMenu();

        JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
        JButton btnNuovo = new JButton("Nuovo");

        btnNuovo.addActionListener(e -> nuovaPersona());

        statusLabel = new JLabel("Contatti totali: " + persone.size());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        buttonPanel.add(statusLabel, BorderLayout.WEST);
        buttonPanel.add(btnNuovo, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        tabella.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateStatusBar();
            }
        });
    }

    private void createContextMenu() {
        popupMenu = new JPopupMenu();

        JMenuItem menuItemView = new JMenuItem("Visualizza");
        JMenuItem menuItemEdit = new JMenuItem("Modifica");
        JMenuItem menuItemDelete = new JMenuItem("Elimina");

        menuItemView.addActionListener(e -> visualizzaPersona());
        menuItemEdit.addActionListener(e -> modificaPersona());
        menuItemDelete.addActionListener(e -> eliminaPersona());

        popupMenu.add(menuItemView);
        popupMenu.add(menuItemEdit);
        popupMenu.addSeparator();
        popupMenu.add(menuItemDelete);

        tabella.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int r = tabella.rowAtPoint(e.getPoint());
                if (r >= 0 && r < tabella.getRowCount()) {
                    tabella.setRowSelectionInterval(r, r);
                } else {
                    tabella.clearSelection();
                }

                if (tabella.getSelectedRow() != -1) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    visualizzaPersona();
                }
            }
        });
    }

    private void filterContacts(String query) {
        query = query.toLowerCase();
        filteredPersone.clear();

        if (query.isEmpty()) {
            filteredPersone.addAll(persone);
        } else {
            for (Persona p : persone) {
                if (p.getNome().toLowerCase().contains(query) ||
                    p.getCognome().toLowerCase().contains(query) ||
                    p.getTelefono().toLowerCase().contains(query)) {
                    filteredPersone.add(p);
                }
            }
        }

        updateTable();
        updateStatusBar();
    }

    private void updateTable() {
        model.setRowCount(0);

        for (Persona p : filteredPersone) {
            model.addRow(new Object[] {p.getNome(), p.getCognome(), p.getTelefono()});
        }
    }

    private void updateStatusBar() {
        int selectedRow = tabella.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = tabella.convertRowIndexToModel(selectedRow);
            Persona selected = filteredPersone.get(modelRow);
            statusLabel.setText("Contatti totali: " + persone.size() + " | Selezionato: " +
                               selected.getNome() + " " + selected.getCognome());
        } else {
            statusLabel.setText("Contatti totali: " + persone.size() + " | Visualizzati: " + filteredPersone.size());
        }
    }

    private void visualizzaPersona() {
        int selectedRow = tabella.getSelectedRow();

        if (selectedRow != -1) {
            int modelRow = tabella.convertRowIndexToModel(selectedRow);
            Persona personaSelezionata = filteredPersone.get(modelRow);

            EditorPersona editor = new EditorPersona(this, personaSelezionata, true);
            editor.setVisible(true);
        }
    }

    private void nuovaPersona() {
        EditorPersona editor = new EditorPersona(this, null, false);
        editor.setVisible(true);

        if (editor.isSaved()) {
            Persona nuovaPersona = editor.getPersona();
            persone.add(nuovaPersona);
            filterContacts(searchField.getText());

            FileManager.salvaPersone(persone);
        }
    }

    private void modificaPersona() {
        int selectedRow = tabella.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Per modificare è necessario prima selezionare una persona",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabella.convertRowIndexToModel(selectedRow);
        Persona personaSelezionata = filteredPersone.get(modelRow);

        int originalIndex = persone.indexOf(personaSelezionata);
        if (originalIndex == -1) {
            for (int i = 0; i < persone.size(); i++) {
                Persona p = persone.get(i);
                if (p.getNome().equals(personaSelezionata.getNome()) &&
                    p.getCognome().equals(personaSelezionata.getCognome()) &&
                    p.getTelefono().equals(personaSelezionata.getTelefono())) {
                    originalIndex = i;
                    break;
                }
            }
        }

        EditorPersona editor = new EditorPersona(this, personaSelezionata, false);
        editor.setVisible(true);

        if (editor.isSaved() && originalIndex != -1) {
            persone.setElementAt(editor.getPersona(), originalIndex);

            filterContacts(searchField.getText());

            FileManager.salvaPersone(persone);
        }
    }

    private void eliminaPersona() {
        int selectedRow = tabella.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Per eliminare è necessario prima selezionare una persona",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabella.convertRowIndexToModel(selectedRow);
        Persona personaSelezionata = filteredPersone.get(modelRow);
        String messaggio = "Eliminare la persona " + personaSelezionata.getNome() + " " +
                           personaSelezionata.getCognome() + "?";

        int response = JOptionPane.showConfirmDialog(this,
            messaggio,
            "Conferma eliminazione",
            JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            FileManager.deletePersona(personaSelezionata);

            persone.remove(personaSelezionata);

            filterContacts(searchField.getText());

            FileManager.salvaPersone(persone);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            RubricaMain app = new RubricaMain();
            app.setVisible(true);
        });
    }
}
