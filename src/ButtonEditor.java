import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;

public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private boolean isPushed;
    private AppointmentPanel panel;
    private JTable table;

    public ButtonEditor(JCheckBox checkBox, AppointmentPanel panel) {
        super(checkBox);
        this.panel = panel;
        button = new JButton("Actions");
        button.setOpaque(true);

        button.addActionListener(e -> {
            fireEditingStopped();

            int row = table.getSelectedRow();
            int appointmentId = (int) table.getModel().getValueAt(row, 4);

            Object[] options = {"Cancel", "Reschedule"};
            int choice = JOptionPane.showOptionDialog(button,
                    "Choose an action for this appointment:",
                    "Appointment Action",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                panel.handleAction("cancel", appointmentId);
            } else if (choice == JOptionPane.NO_OPTION) {
                panel.handleAction("reschedule", appointmentId);
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table;
        return button;
    }

    public Object getCellEditorValue() {
        return "Actions";
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
