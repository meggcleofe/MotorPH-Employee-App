# MotorPH Employee Management System - Changelog 

This changelog summarizes all key updates and improvements made to the MotorPH Employee App across different versions. Each entry describes what was added or changed in a way that is understandable to non-technical stakeholders.

### What’s New
- A user-friendly interface has been added to replace the old console-based system.
- Users can now log in securely using a username and password(admin as username and password is 1234).
- A new “Add Employee” button allows HR personnel to input new employee records.
- Input validation has been introduced to ensure employee numbers follow the correct format (5–6 digits only).
- Data is shown in a clean, organized table format for easy review.
- Employee and attendance data now load automatically from CSV files.
- Implemented automatic attendance tracking, with worked hours and late deductions computed per day.
- Added a Monthly Payroll Breakdown feature that summarizes the weekly hours worked, late deductions, gross pay per week, SSS, PhilHealth, Pag-IBIG, and Withholding Tax. 
- Net Pay and Allowances
- Built-in BIR-compliant tax computation based on progressive income tax brackets.
- Total Rice, Phone, and Clothing Allowances now included in monthly payroll computations.
- Improved time validation: Handles invalid login/logout entries and prevents negative hours.
- Error messages and logs provide feedback for missing or malformed data in attendance and salary computation.

### Why It Matters
- This makes the system easier to use, even for non-technical staff.
- Reduces input errors by validating fields before saving.
- Provides a foundation for more advanced features in future updates. 
- Automates payroll processes and reduces manual HR work.
- Ensures accurate and fair salary computation using real attendance and government deductions.
- Helps prevent tax miscalculations and supports legal compliance.
- Provides detailed transparency in salary breakdowns, useful for both HR and employees.
- Makes the system scalable for future features like leave management, benefits, or digital payslip export.



