import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MotorPHEmployeeApp {

    private final List<String[]> employeeData = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MotorPHEmployeeApp().showLoginScreen());
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 200);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if ("admin".equals(username) && "1234".equals(password)) {
                loginFrame.dispose();
                showMainWindow();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    private void showMainWindow() {
        mainFrame = new JFrame("MotorPH Employee Management");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 500);
        mainFrame.setLocationRelativeTo(null);

        String[] columnNames = {
            "Employee No", "Last Name", "First Name", "SSS", "PhilHealth",
            "TIN", "Pag-IBIG", "Status", "Position", "Salary"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable employeeTable = new JTable(tableModel);
        employeeTable.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(employeeTable);

        JButton addButton = new JButton("Add Employee");
        addButton.addActionListener(e -> openAddEmployeeDialog());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.EAST);
        mainFrame.setVisible(true);
    }

    private void openAddEmployeeDialog() {
        JTextField empNoField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField sssField = new JTextField();
        JTextField philHealthField = new JTextField();
        JTextField tinField = new JTextField();
        JTextField pagIbigField = new JTextField();
        JTextField statusField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField salaryField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Employee No:")); panel.add(empNoField);
        panel.add(new JLabel("Last Name:")); panel.add(lastNameField);
        panel.add(new JLabel("First Name:")); panel.add(firstNameField);
        panel.add(new JLabel("SSS No:")); panel.add(sssField);
        panel.add(new JLabel("PhilHealth No:")); panel.add(philHealthField);
        panel.add(new JLabel("TIN:")); panel.add(tinField);
        panel.add(new JLabel("Pag-IBIG No:")); panel.add(pagIbigField);
        panel.add(new JLabel("Status:")); panel.add(statusField);
        panel.add(new JLabel("Position:")); panel.add(positionField);
        panel.add(new JLabel("Salary:")); panel.add(salaryField);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String empNo = empNoField.getText().trim();
            if (empNo.isEmpty() || !empNo.matches("\\d{5,6}")) {
                JOptionPane.showMessageDialog(mainFrame, "Employee Number must only be 5 to 6 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] employee = {
                empNo,
                lastNameField.getText().trim(),
                firstNameField.getText().trim(),
                sssField.getText().trim(),
                philHealthField.getText().trim(),
                tinField.getText().trim(),
                pagIbigField.getText().trim(),
                statusField.getText().trim(),
                positionField.getText().trim(),
                salaryField.getText().trim()
            };

            employeeData.add(employee);
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (String[] row : employeeData) {
            tableModel.addRow(row);
        }
    }
} 
