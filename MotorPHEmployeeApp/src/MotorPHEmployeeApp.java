import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MotorPHEmployeeApp {

    private final List<String[]> employeeData = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JFrame mainFrame;
    private JTable employeeTable;
    
    
    private final Map<String, List<AttendanceRecord>> attendanceMap = new HashMap<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");


    private static class AttendanceRecord {
        String date;
        LocalDate parsedDate;
        double hoursWorked;
        double deductedHours;

        AttendanceRecord(String date, double hoursWorked, double deductedHours) {
            this.date = date;
            this.hoursWorked = hoursWorked;
            this.deductedHours = deductedHours;
            this.parsedDate = parseDate(date);
        }
        
        private LocalDate parseDate(String dateStr) {
    try {
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        
        if (date.getYear() < 100) {
            if (date.getYear() < 50) {
                date = date.withYear(date.getYear() + 2000);
            } else {
                date = date.withYear(date.getYear() + 1900);
            }
        }
        System.out.println("Parsed date string '" + dateStr + "' to LocalDate: " + date);
        return date;
    } catch (DateTimeParseException e) {
        System.out.println(" Failed to parse date: " + dateStr);
        return null;
    }
}

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MotorPHEmployeeApp().showLoginScreen());
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 200);
        loginFrame.setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));

        JTextField usernameInputField = new JTextField();
        JPasswordField passwordInputField = new JPasswordField();
        JButton loginSubmitButton = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameInputField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordInputField);
        loginPanel.add(new JLabel());
        loginPanel.add(loginSubmitButton);

        loginSubmitButton.addActionListener(e -> {
            String enteredUsername = usernameInputField.getText();
            String enteredPassword = new String(passwordInputField.getPassword()); 

            if ("admin".equals(enteredUsername) && "1234".equals(enteredPassword)) {
                loginFrame.dispose();
                showMainWindow();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);
    }

    private void showMainWindow() {
        mainFrame = new JFrame("MotorPH Employee Management");
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            int response = JOptionPane.showConfirmDialog(mainFrame, 
                "Are you sure you want to exit?", "Confirm Exit", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    });


        mainFrame.setSize(1100, 500);
        mainFrame.setLocationRelativeTo(null);

        String[] employeeTableHeaders = {
            "Employee No", "Last Name", "First Name", "SSS", "PhilHealth",
            "TIN", "Pag-IBIG", "Status", "Position", "Salary",
            "Rice Allowance", "Phone Allowance", "Clothing Allowance", "Semi-Monthly Gross", "Hourly Rate"
        };
        tableModel = new DefaultTableModel(employeeTableHeaders, 0);
        employeeTable = new JTable(tableModel);
        employeeTable.setEnabled(true);

        JScrollPane tableScrollPane = new JScrollPane(employeeTable);

        JButton addNewEmployeeButton = new JButton("Add Employee");
        addNewEmployeeButton.addActionListener(e -> openAddEmployeeDialog());

        JButton updateEmployeeButton = new JButton("Update Employee");
        updateEmployeeButton.addActionListener(e -> openUpdateEmployeeDialog());

        JButton deleteButton = new JButton("Delete Employee");
        deleteButton.addActionListener(e -> openDeleteEmployeeDialog());

        JButton viewEmployeeDetailsButton = new JButton("View Employee");
        viewEmployeeDetailsButton.addActionListener(e -> openEmployeeDetailDialog());

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new BoxLayout(actionButtonPanel, BoxLayout.Y_AXIS));
        actionButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionButtonPanel.add(addNewEmployeeButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionButtonPanel.add(updateEmployeeButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionButtonPanel.add(viewEmployeeDetailsButton);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(tableScrollPane, BorderLayout.CENTER);
        mainFrame.add(actionButtonPanel, BorderLayout.EAST);
        mainFrame.setVisible(true);

        
        loadEmployeeDataFromCSV("C:\\Users\\Michiko\\Desktop\\MotorPHEmployeeApp\\MotorPHEmployeeApp\\src\\employee_data.csv");
        loadAttendanceDataFromCSV("C:\\Users\\Michiko\\Desktop\\MotorPHEmployeeApp\\MotorPHEmployeeApp\\src\\attendance_record.csv");
    }

    private void loadAttendanceDataFromCSV(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean firstLine = true;
        int recordCount = 0;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] tokens = line.split(",");
            if (tokens.length < 6) continue;

            String employeeId = tokens[0].trim();
            String date = tokens[3].trim();
            String login = tokens[tokens.length - 2].trim();
            String logout = tokens[tokens.length - 1].trim();

            try {
                LocalTime loginTime = LocalTime.parse(login, timeFormatter);
                LocalTime logoutTime = LocalTime.parse(logout, timeFormatter);

                
                if (logoutTime.isBefore(loginTime)) {
                    System.out.println("Skipping invalid login/logout: " + login + " - " + logout + " for employee " + employeeId);
                    continue;
                }

                double totalHours = Duration.between(loginTime, logoutTime).toMinutes() / 60.0;

                
                LocalTime gracePeriod = LocalTime.of(8, 10);
                double deductedHours = 0.0;
                if (loginTime.isAfter(gracePeriod)) {
                    long minutesLate = Duration.between(gracePeriod, loginTime).toMinutes();
                    deductedHours = minutesLate / 60.0;
                }

                double adjustedHours = Math.max(totalHours - deductedHours, 0.0);

                AttendanceRecord record = new AttendanceRecord(date, adjustedHours, deductedHours);
                attendanceMap.computeIfAbsent(employeeId, k -> new ArrayList<>()).add(record);
                recordCount++;

                System.out.println("Loaded attendance: Employee " + employeeId +
                    ", Date: " + date +
                    ", Login: " + login +
                    ", Logout: " + logout +
                    ", Raw Hours: " + totalHours +
                    ", Deducted: " + deductedHours +
                    ", Final Hours: " + adjustedHours);

            } catch (DateTimeParseException e) {
                System.out.println("Skipping invalid time entry: " + login + " - " + logout + " for employee " + employeeId);
            }
        }

        
        System.out.println("Total attendance records loaded: " + recordCount);
        System.out.println("Employees with attendance data: " + attendanceMap.keySet());

    } catch (IOException e) {
        JOptionPane.showMessageDialog(mainFrame, "Error loading attendance data from CSV.", "Warning", JOptionPane.WARNING_MESSAGE);
    }
}

  private void openAddEmployeeDialog() {
   
    JTextField employeeNumberInput = new JTextField();
    JTextField lastNameInput = new JTextField();
    JTextField firstNameInput = new JTextField();
    JTextField birthdayInput = new JTextField();
    JTextField addressInput = new JTextField();
    JTextField phoneInput = new JTextField();
    JTextField sssNumberInput = new JTextField();
    JTextField philHealthNumberInput = new JTextField();
    JTextField tinNumberInput = new JTextField();
    JTextField pagIbigNumberInput = new JTextField();
    JTextField employmentStatusInput = new JTextField();
    JTextField jobPositionInput = new JTextField();
    JTextField supervisorInput = new JTextField();
    JTextField salaryInput = new JTextField();
    
    
    JTextField riceInput = new JTextField("1500");
    JTextField phoneAllowanceInput = new JTextField("1000");
    JTextField clothingInput = new JTextField("1000");

    JPanel inputFormPanel = new JPanel(new GridLayout(0, 2));
    inputFormPanel.add(new JLabel("Employee No:")); inputFormPanel.add(employeeNumberInput);
    inputFormPanel.add(new JLabel("Last Name:")); inputFormPanel.add(lastNameInput);
    inputFormPanel.add(new JLabel("First Name:")); inputFormPanel.add(firstNameInput);
    inputFormPanel.add(new JLabel("Birthday (M/D/YY):")); inputFormPanel.add(birthdayInput);
    inputFormPanel.add(new JLabel("Address:")); inputFormPanel.add(addressInput);
    inputFormPanel.add(new JLabel("Phone Number:")); inputFormPanel.add(phoneInput);
    inputFormPanel.add(new JLabel("SSS No:")); inputFormPanel.add(sssNumberInput);
    inputFormPanel.add(new JLabel("PhilHealth No:")); inputFormPanel.add(philHealthNumberInput);
    inputFormPanel.add(new JLabel("TIN:")); inputFormPanel.add(tinNumberInput);
    inputFormPanel.add(new JLabel("Pag-IBIG No:")); inputFormPanel.add(pagIbigNumberInput);
    inputFormPanel.add(new JLabel("Status:")); inputFormPanel.add(employmentStatusInput);
    inputFormPanel.add(new JLabel("Position:")); inputFormPanel.add(jobPositionInput);
    inputFormPanel.add(new JLabel("Immediate Supervisor:")); inputFormPanel.add(supervisorInput);
    inputFormPanel.add(new JLabel("Basic Salary:")); inputFormPanel.add(salaryInput);
    inputFormPanel.add(new JLabel("Rice Subsidy:")); inputFormPanel.add(riceInput);
    inputFormPanel.add(new JLabel("Phone Allowance:")); inputFormPanel.add(phoneAllowanceInput);
    inputFormPanel.add(new JLabel("Clothing Allowance:")); inputFormPanel.add(clothingInput);

    int result = JOptionPane.showConfirmDialog(mainFrame, inputFormPanel, "Add Employee", 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        
        String empNo = employeeNumberInput.getText().trim();
        String salaryText = salaryInput.getText().trim();

        if (empNo.isEmpty() || !empNo.matches("\\d{5,6}")) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Employee Number must be 5 to 6 digits.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (salaryText.isEmpty() || !salaryText.matches("\\d+(\\.\\d{1,2})?")) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Salary must be a valid number.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        double basicSalary = Double.parseDouble(salaryText);
        double gross = basicSalary / 2;
        double hourly = basicSalary / 21 / 8; 

        
        String[] employee = {
            empNo,                                     // Employee ID
            lastNameInput.getText().trim(),            // Last Name
            firstNameInput.getText().trim(),           // First Name
            birthdayInput.getText().trim(),            // Birthday
            addressInput.getText().trim(),             // Address
            phoneInput.getText().trim(),               // Phone Number
            sssNumberInput.getText().trim(),          // SSS
            philHealthNumberInput.getText().trim(),    // Philhealth
            tinNumberInput.getText().trim(),           // TIN
            pagIbigNumberInput.getText().trim(),       // Pag-ibig
            employmentStatusInput.getText().trim(),    // Status
            jobPositionInput.getText().trim(),         // Position
            supervisorInput.getText().trim(),          // Immediate Supervisor
            salaryText,                                // Basic Salary
            riceInput.getText().trim(),                // Rice Subsidy
            phoneAllowanceInput.getText().trim(),      // Phone Allowance
            clothingInput.getText().trim(),            // Clothing Allowance
            String.format("%.2f", gross),              // Gross Semi Monthly Rate
            String.format("%.2f", hourly)             // Hourly Rate
        };

       
        saveEmployeeToCSV(employee, "C:\\Users\\Michiko\\Desktop\\MotorPHEmployeeApp\\MotorPHEmployeeApp\\src\\employee_data.csv");
        
       
        employeeData.add(employee);
        refreshTable();
    


           
        }
    }

    private void openUpdateEmployeeDialog() {
    int selectedRow = employeeTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(mainFrame, 
            "Please select an employee to update", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String[] employee = employeeData.get(selectedRow);
    String empId = employee[0];

    JPanel panel = new JPanel(new GridLayout(0, 2));
    JTextField firstNameField = new JTextField(employee[2]);
    JTextField lastNameField = new JTextField(employee[1]);
    JTextField salaryField = new JTextField(employee[9]);

    panel.add(new JLabel("First Name:"));
    panel.add(firstNameField);
    panel.add(new JLabel("Last Name:"));
    panel.add(lastNameField);
    panel.add(new JLabel("Salary:"));
    panel.add(salaryField);

    int result = JOptionPane.showConfirmDialog(
        mainFrame, 
        panel, 
        "Update Employee " + empId,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        Map<String, String> updates = new HashMap<>();
        updates.put("firstName", firstNameField.getText());
        updates.put("lastName", lastNameField.getText());
        
       
        try {
            Double.parseDouble(salaryField.getText());
            updates.put("basicSalary", salaryField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Invalid salary format", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateEmployee(
            "C:\\Users\\Michiko\\Desktop\\MotorPHEmployeeApp\\MotorPHEmployeeApp\\src\\employee_data.csv",
            empId, 
            updates
        );
    }
}

    private void openDeleteEmployeeDialog() {
    String empIdToDelete = JOptionPane.showInputDialog(null, "Enter Employee ID to delete:");

    if (empIdToDelete != null && !empIdToDelete.trim().isEmpty()) {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete employee ID: " + empIdToDelete + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            deleteEmployee("C:\\Users\\Michiko\\Desktop\\MotorPHEmployeeApp\\MotorPHEmployeeApp\\src\\employee_data.csv", empIdToDelete); 
            refreshTable(); 
        }
    }
}
   
    private void openEmployeeDetailDialog() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Please select an employee to view.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] employee = employeeData.get(selectedRow);
        String empId = employee[0];
        double basicSalary = Double.parseDouble(employee[9]);
        double hourlyRate = Double.parseDouble(employee[14]);

        JPanel employeeInfoPanel = new JPanel(new GridLayout(0, 1));
        employeeInfoPanel.add(new JLabel("Employee No: " + empId));
        employeeInfoPanel.add(new JLabel("Name: " + employee[2] + " " + employee[1]));
        employeeInfoPanel.add(new JLabel("Status: " + employee[7]));
        employeeInfoPanel.add(new JLabel("Position: " + employee[8]));
        employeeInfoPanel.add(new JLabel("Salary: " + employee[9]));

        JButton computeSalaryButton = new JButton("Compute Salary");
        computeSalaryButton.addActionListener(e -> showSalaryOptions(empId, basicSalary, hourlyRate));

        JPanel container = new JPanel(new BorderLayout());
        container.add(employeeInfoPanel, BorderLayout.CENTER);
        container.add(computeSalaryButton, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(mainFrame, container, "Employee Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSalaryOptions(String empId, double basicSalary, double hourlyRate) {
        String[] options = {"Basic Salary Computation", "Monthly Payroll Breakdown"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
            "Select computation type:",
            "Salary Computation",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            showBasicSalaryComputation(empId, basicSalary);
        } else if (choice == 1) {
            showMonthlyPayrollOptions(empId, basicSalary, hourlyRate);
        }
    }

    private void showBasicSalaryComputation(String empId, double basicSalary) {
      
        String[] employeeInfo = employeeData.stream()
            .filter(e -> e[0].equals(empId))
            .findFirst()
            .orElse(null);
        
        if (employeeInfo == null) {
            JOptionPane.showMessageDialog(mainFrame, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double rice = Double.parseDouble(employeeInfo[10]);
        double phone = Double.parseDouble(employeeInfo[11]);
        double clothing = Double.parseDouble(employeeInfo[12]);
        double totalAllowance = rice + phone + clothing;

        double pagibig = 100;
        double philhealth = basicSalary * 0.03;
        double sss = calculateSSS(basicSalary);
        double withholdingTax = calculateWithholdingTax(basicSalary);

        double netPay = basicSalary - (sss + pagibig + philhealth + withholdingTax) + totalAllowance;

        JOptionPane.showMessageDialog(mainFrame,
            "Net Pay Computation:\n"
                + "Basic Salary: ₱" + String.format("%,.2f", basicSalary) + "\n"
                + "SSS: ₱" + String.format("%,.2f", sss) + "\n"
                + "Pag-IBIG: ₱" + String.format("%,.2f", pagibig) + "\n"
                + "PhilHealth: ₱" + String.format("%,.2f", philhealth) + "\n"
                + "Withholding Tax: ₱" + String.format("%,.2f", withholdingTax) + "\n"
                + "Allowances: ₱" + String.format("%,.2f", totalAllowance) + "\n"
                + "\nFinal Net Pay: ₱" + String.format("%,.2f", netPay),
            "Computed Salary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMonthlyPayrollOptions(String empId, double basicSalary, double hourlyRate) {
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        
        String selectedMonth = (String) JOptionPane.showInputDialog(mainFrame,
            "Select month for payroll computation:",
            "Monthly Payroll",
            JOptionPane.QUESTION_MESSAGE,
            null,
            months,
            months[0]);

        if (selectedMonth != null) {
            computeMonthlyPayroll(empId, basicSalary, hourlyRate, selectedMonth);
        }
    }

    private void computeMonthlyPayroll(String empId, double basicSalary, double hourlyRate, String monthName) {
        System.out.println("Computing monthly payroll for employee: " + empId + ", Month: " + monthName);
        
        int year = 2024;

    
    Month month;
    try {
        month = Month.valueOf(monthName.toUpperCase());
    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(mainFrame, "Invalid month selected.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
        
        
        if (!attendanceMap.containsKey(empId)) {
            JOptionPane.showMessageDialog(mainFrame, 
                "No attendance data found for employee " + empId + ".\nPlease check if the employee ID matches the attendance records.", 
                "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LocalDate firstMonday = getFirstMondayOfMonth(year, month);
        System.out.println("First Monday of " + monthName + " " + year + ": " + firstMonday);

        StringBuilder report = new StringBuilder();
        report.append("Payroll for ").append(monthName).append(" ").append(year).append("\n\n");
        report.append("Employee: ").append(empId).append("\n");
        report.append("Hourly Rate: ₱").append(String.format("%.2f", hourlyRate)).append("\n\n");

        double totalNet = 0.0;
        double totalHours = 0.0;
        double totalDeductions = 0.0;

        
        String[] employeeInfo = employeeData.stream()
            .filter(e -> e[0].equals(empId))
            .findFirst()
            .orElse(null);

        if (employeeInfo == null) {
            JOptionPane.showMessageDialog(mainFrame, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int weekOffset = 0; weekOffset < 4; weekOffset++) {
            LocalDate startDate = firstMonday.plusWeeks(weekOffset);
            LocalDate endDate = startDate.plusDays(4); // Monday to Friday

            Map<String, Double> weeklySummary = computeWeeklySummary(empId, startDate, endDate);
            double weeklyHours = weeklySummary.get("totalHours");
            double weeklyDeductions = weeklySummary.get("totalDeductions");
            double weeklyGross = weeklyHours * hourlyRate;
            double weeklyDeductionAmount = weeklyDeductions * hourlyRate;

            report.append("Week ").append(weekOffset + 1).append(" (");
                  report.append(startDate.getDayOfMonth()).append(" - ");
                  report.append(endDate.getDayOfMonth()).append("):\n");
            report.append("  Hours Worked: ").append(String.format("%.2f", weeklyHours)).append("\n");
            report.append("  Late Hours Deducted: ").append(String.format("%.2f", weeklyDeductions)).append("\n");
            report.append("  Gross Pay: ").append(String.format("₱%,.2f", weeklyGross)).append("\n");
            report.append("  Late Deduction: ").append(String.format("₱%,.2f", weeklyDeductionAmount)).append("\n");

            
            if (weekOffset == 3) {
                double sss = calculateSSS(basicSalary);
                double philhealth = basicSalary * 0.03;
                double pagibig = 100;
                
                double taxableIncome = weeklyGross - sss - philhealth - pagibig;
                taxableIncome = Math.max(taxableIncome, 0); // prevent negative
                double tax = calculateWithholdingTax(taxableIncome);

                double totalDeduction = weeklyDeductionAmount + sss + philhealth + pagibig + tax;
                double weeklyNet = weeklyGross - totalDeduction;
                totalNet += weeklyNet;
                
                report.append("  SSS: ").append(String.format("₱%,.2f", sss)).append("\n");
                report.append("  PhilHealth: ").append(String.format("₱%,.2f", philhealth)).append("\n");
                report.append("  Pag-IBIG: ").append(String.format("₱%,.2f", pagibig)).append("\n");
                report.append("  Withholding Tax: ").append(String.format("₱%,.2f", tax)).append("\n");
                report.append("  Net Pay: ").append(String.format("₱%,.2f", weeklyNet)).append("\n\n");
            } else {
                double weeklyNet = weeklyGross - weeklyDeductionAmount;
                totalNet += weeklyNet;
                report.append("  Net Pay: ").append(String.format("₱%,.2f", weeklyNet)).append("\n\n");
            }
            
            totalHours += weeklyHours;
            totalDeductions += weeklyDeductions;
        }

        
        double rice = Double.parseDouble(employeeInfo[10]);
        double phone = Double.parseDouble(employeeInfo[11]);
        double clothing = Double.parseDouble(employeeInfo[12]);
        double totalAllowance = rice + phone + clothing;
        
        double finalNet = totalNet + totalAllowance;
        
        report.append("Monthly Summary:\n");
        report.append("  Total Hours Worked: ").append(String.format("%.2f", totalHours)).append("\n");
        report.append("  Total Late Hours: ").append(String.format("%.2f", totalDeductions)).append("\n");
        report.append("  Total Allowances: ").append(String.format("₱%,.2f", totalAllowance)).append("\n");
        report.append("  Final Net Pay: ").append(String.format("₱%,.2f", finalNet)).append("\n");

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(mainFrame, scrollPane, "Monthly Payroll Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private LocalDate getFirstMondayOfMonth(int year, Month month) {
        LocalDate date = LocalDate.of(year, month, 1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private Map<String, Double> computeWeeklySummary(String empId, LocalDate weekStart, LocalDate weekEnd) {
        double hours = 0;
        double deductions = 0;
        
        System.out.println("Computing weekly summary for " + empId + " from " + weekStart + " to " + weekEnd);
        
        if (attendanceMap.containsKey(empId)) {
            List<AttendanceRecord> records = attendanceMap.get(empId);
            System.out.println("Found " + records.size() + " attendance records for employee " + empId);
            
            for (AttendanceRecord record : records) {
                if (record.parsedDate != null) {
                    System.out.println("  Checking record: " + record.parsedDate + 
                                     " (hours: " + record.hoursWorked + ")");
                    
                    if (!record.parsedDate.isBefore(weekStart) && !record.parsedDate.isAfter(weekEnd)) {
                        System.out.println("    Date matches - adding to weekly total");
                        hours += record.hoursWorked;
                        deductions += record.deductedHours;
                    } else {
                        System.out.println("    Date outside range");
                    }
                } else {
                    System.out.println("  Skipping record with null parsed date: " + record.date);
                }
            }
        } else {
            System.out.println("No attendance records found for employee: " + empId);
        }
        
        System.out.println("Weekly summary: Hours=" + hours + ", Deductions=" + deductions);
        
        Map<String, Double> result = new HashMap<>();
        result.put("totalHours", hours);
        result.put("totalDeductions", deductions);
        return result;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (String[] row : employeeData) {
            tableModel.addRow(row);
        }
    }

    private void saveEmployeeToCSV(String[] employee, String csvFilePath) {
    try {
        
        Path path = Paths.get(csvFilePath);
        Files.createDirectories(path.getParent());
        
        
        boolean fileExists = Files.exists(path);
        boolean needsHeader = !fileExists || Files.size(path) == 0;
        
        try (BufferedWriter writer = Files.newBufferedWriter(path, 
            StandardCharsets.UTF_8, 
            StandardOpenOption.CREATE, 
            StandardOpenOption.APPEND)) {
            
            
            if (needsHeader) {
                String[] headers = {
                    "Employee No", "Last Name", "First Name", "SSS", "PhilHealth",
                    "TIN", "Pag-IBIG", "Status", "Position", "Salary",
                    "Rice Allowance", "Phone Allowance", "Clothing Allowance", 
                    "Semi-Monthly Gross", "Hourly Rate"
                };
                writer.write(String.join(",", headers));
                writer.newLine();
            }
            
            
            writer.write(String.join(",", employee));
            writer.newLine();
            
            JOptionPane.showMessageDialog(mainFrame, 
                "Employee saved successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(mainFrame, 
            "Error saving employee:\n" + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void loadEmployeeDataFromCSV(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (columns.length >= 19) {
                    try {
                        double basicSalary = Double.parseDouble(columns[13]);
                        double rice = Double.parseDouble(columns[14]);
                        double phone = Double.parseDouble(columns[15]);
                        double clothing = Double.parseDouble(columns[16]);
                        double gross = Double.parseDouble(columns[17]);
                        double hourly = Double.parseDouble(columns[18]);

                        String[] employee = new String[] {
                            columns[0], columns[1], columns[2], columns[6], columns[7],
                            columns[8], columns[9], columns[10], columns[11], columns[13],
                            String.valueOf(rice), String.valueOf(phone), String.valueOf(clothing),
                            String.format("%.2f", gross), String.format("%.2f", hourly)
                        };

                        employeeData.add(employee);
                        System.out.println("Loaded employee: " + columns[0] + " - " + columns[2] + " " + columns[1]);
                    } catch (NumberFormatException e) {
                        
                        System.err.println("Skipping row due to number format error: " + line);
                    }
                }
            }
            refreshTable();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error loading employee data from CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee(String csvFilePath, String empIdToUpdate, Map<String, String> updatedFields) {
    try {

        Path filePath = Paths.get(csvFilePath);  
        List<String[]> updatedEmployeeData = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    updatedEmployeeData.add(line.split(","));
                    continue;
                }

                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                
                if (columns.length >= 19 && columns[0].equals(empIdToUpdate)) {
                    found = true;
                    
                    
                    for (Map.Entry<String, String> entry : updatedFields.entrySet()) {
                        switch (entry.getKey()) {
                            case "firstName":
                                columns[1] = entry.getValue();
                                break;
                            case "lastName":
                                columns[2] = entry.getValue();
                                break;
                            case "basicSalary":
                                columns[13] = entry.getValue();
                                break;
                            
                        }
                    }
                    
                    
                    if (updatedFields.containsKey("basicSalary")) {
                        double basicSalary = Double.parseDouble(columns[13]);
                        columns[17] = String.format("%.2f", basicSalary * 1.5); // Example gross calculation
                        columns[18] = String.format("%.2f", basicSalary / 160); // Example hourly rate
                    }
                }
                
                updatedEmployeeData.add(columns);
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(mainFrame, "Employee ID not found: " + empIdToUpdate, 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

      
    try (BufferedWriter writer = new BufferedWriter(
    new OutputStreamWriter(
        new FileOutputStream(csvFilePath), 
        StandardCharsets.UTF_8))) {
    
    for (String[] employee : updatedEmployeeData) {
        writer.write(String.join(",", employee));
        writer.newLine();
    }
    
    
    try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.WRITE)) {
        channel.force(true);
        System.out.println("File changes synced to disk: " + filePath);
    }
}

        
        loadEmployeeDataFromCSV(csvFilePath);
        JOptionPane.showMessageDialog(mainFrame, "Employee updated successfully!", 
                                    "Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (IOException e) {
        JOptionPane.showMessageDialog(mainFrame, "Error updating employee!: " + e.getMessage(), 
                                    "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(mainFrame, "Invalid number format in salary!", 
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }

}

    private void deleteEmployee(String csvFilePath, String empIdToDelete) {
    List<String> updatedLines = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty() && !line.startsWith(empIdToDelete + ",")) {
                updatedLines.add(line);
            }
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error reading employee file!: " + e.getMessage());
        return;
    }

    try (PrintWriter writer = new PrintWriter(csvFilePath)) {
        for (String updatedLine : updatedLines) {
            writer.println(updatedLine);
        }
        JOptionPane.showMessageDialog(null, "Employee deleted successfully!");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Error writing to employee file!: " + e.getMessage());
    }
}

    private static double calculateSSS(double salary) {
        double[][] sssTable = {
            {0, 3249, 135}, {3250, 3749, 157.5}, {3750, 4249, 180}, {4250, 4749, 202.5},
            {4750, 5249, 225}, {5250, 5749, 247.5}, {5750, 6249, 270}, {6250, 6749, 292.5},
            {6750, 7249, 315}, {7250, 7749, 337.5}, {7750, 8249, 360}, {8250, 8749, 382.5},
            {8750, 9249, 405}, {9250, 9749, 427.5}, {9750, 10249, 450}, {10250, 10749, 472.5},
            {10750, 11249, 495}, {11250, 11749, 517.5}, {11750, 12249, 540}, {12250, 12749, 562.5},
            {12750, 13249, 585}, {13250, 13749, 607.5}, {13750, 14249, 630}, {14250, 14749, 652.5},
            {14750, 15249, 675}, {15250, 15749, 697.5}, {15750, 16249, 720}, {16250, 16749, 742.5},
            {16750, 17249, 765}, {17250, 17749, 787.5}, {17750, 18249, 810}, {18250, 18749, 832.5},
            {18750, 19249, 855}, {19250, 19749, 877.5}, {19750, 20249, 900}, {20250, 20749, 922.5},
            {20750, 21249, 945}, {21250, 21749, 967.5}, {21750, 22249, 990}, {22250, 22749, 1012.5},
            {22750, 23249, 1035}, {23250, 23749, 1057.5}, {23750, 24249, 1080}, {24250, 24749, 1102.5},
            {24750, Double.MAX_VALUE, 1125}
        };

        for (double[] bracket : sssTable) {
            if (salary >= bracket[0] && salary <= bracket[1]) {
                return bracket[2];
            }
        }
        return 0;
    }

    private static double calculateWithholdingTax(double basicSalary) {
        if (basicSalary <= 20833) return 0;
        if (basicSalary <= 33333) return (basicSalary - 20833) * 0.20;
        if (basicSalary <= 66667) return 2500 + (basicSalary - 33333) * 0.25;
        if (basicSalary <= 166667) return 10833.33 + (basicSalary - 66667) * 0.30;
        if (basicSalary <= 666667) return 40833.33 + (basicSalary - 166667) * 0.32;
        return 200833.33 + (basicSalary - 666667) * 0.35;
    }
}