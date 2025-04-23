import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditorPersona extends JDialog {
    private Persona persona;
    private boolean isSaved = false;
    private boolean readOnly = false;

    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtIndirizzo;
    private JTextField txtTelefono;
    private JTextField txtEta;

    public EditorPersona(JFrame parent, Persona persona, boolean readOnly) {
        super(parent, readOnly ? "Visualizza Persona" : (persona == null ? "Nuova Persona" : "Modifica Persona"), true);
        this.persona = persona;
        this.readOnly = readOnly;

        initComponents();

        if (persona != null) {
            fillFields();
        }

        if (readOnly) {
            setFieldsReadOnly();
        }

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Nome:"));
        txtNome = new JTextField(20);
        panel.add(txtNome);

        panel.add(new JLabel("Cognome:"));
        txtCognome = new JTextField(20);
        panel.add(txtCognome);

        panel.add(new JLabel("Indirizzo:"));
        txtIndirizzo = new JTextField(20);
        panel.add(txtIndirizzo);

        panel.add(new JLabel("Telefono:"));
        txtTelefono = new JTextField(20);
        panel.add(txtTelefono);

        panel.add(new JLabel("Età:"));
        txtEta = new JTextField(20);
        panel.add(txtEta);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (readOnly) {
            JButton btnChiudi = new JButton("Chiudi");
            btnChiudi.addActionListener(e -> dispose());
            buttonPanel.add(btnChiudi);
        } else {
            JButton btnSalva = new JButton("Salva");
            JButton btnAnnulla = new JButton("Annulla");

            btnSalva.addActionListener(e -> salva());
            btnAnnulla.addActionListener(e -> dispose());

            buttonPanel.add(btnSalva);
            buttonPanel.add(btnAnnulla);
        }

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillFields() {
        txtNome.setText(persona.getNome());
        txtCognome.setText(persona.getCognome());
        txtIndirizzo.setText(persona.getIndirizzo());
        txtTelefono.setText(persona.getTelefono());
        txtEta.setText(String.valueOf(persona.getEta()));
    }

    private void setFieldsReadOnly() {
        txtNome.setEditable(false);
        txtCognome.setEditable(false);
        txtIndirizzo.setEditable(false);
        txtTelefono.setEditable(false);
        txtEta.setEditable(false);

        Color bgColor = new Color(240, 240, 240);
        txtNome.setBackground(bgColor);
        txtCognome.setBackground(bgColor);
        txtIndirizzo.setBackground(bgColor);
        txtTelefono.setBackground(bgColor);
        txtEta.setBackground(bgColor);
    }

    private void salva() {
        try {
            int eta = Integer.parseInt(txtEta.getText());

            if (persona == null) {
                persona = new Persona();
            }

            persona.setNome(txtNome.getText());
            persona.setCognome(txtCognome.getText());
            persona.setIndirizzo(txtIndirizzo.getText());
            persona.setTelefono(txtTelefono.getText());
            persona.setEta(eta);

            isSaved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "L'età deve essere un numero intero",
                "Errore",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public Persona getPersona() {
        return persona;
    }

    public boolean isSaved() {
        return isSaved;
    }
}
