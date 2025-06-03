package com.timetablebuilder.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.model.User;
import com.timetablebuilder.model.UserRole;
import com.timetablebuilder.service.PersistenceService;
import com.timetablebuilder.service.TimetableService;

public class MainFrame extends JFrame {

    private User loggedInUser;
    private PersistenceService persistenceService;
    private TimetableService timetableService;

    private JTabbedPane tabbedPane;

    public MainFrame(User loggedInUser) {
        super("Timetable Builder");
        this.loggedInUser = loggedInUser;

        this.persistenceService = new PersistenceService();
        this.timetableService = new TimetableService(this.persistenceService);

        // Basic setup
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1024, 768); // Increased size
        setLocationRelativeTo(null); // Center the window

        // Add Window Listener to save data on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("MainFrame: Window closing event triggered.");
                // Optionally ask for confirmation
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Do you want to save all changes before exiting?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("MainFrame: Saving data before exit...");
                    persistenceService.saveAllData();
                    System.out.println("MainFrame: Data saved. Exiting.");
                    dispose(); // Close the window
                    System.exit(0); // Terminate the application
                } else if (confirm == JOptionPane.NO_OPTION) {
                    System.out.println("MainFrame: Exiting without saving.");
                    dispose();
                    System.exit(0);
                }
                // If CANCEL_OPTION, do nothing, the window remains open.
            }
        });

        // Initialize UI based on role
        initializeUI(loggedInUser.getRole());

        System.out.println("MainFrame initialized for user: " + loggedInUser.getUsername() + ", Role: " + loggedInUser.getRole());
    }

    private void initializeUI(UserRole role) {
        // Remove previous components if any (e.g., from placeholder)
        getContentPane().removeAll();
        setLayout(new BorderLayout()); // Set layout for the frame

        // --- Create Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // --- Create Tabbed Pane ---
        tabbedPane = new JTabbedPane();

        // --- Role-Specific UI Elements ---
        switch (role) {
            case ADMIN:
                setTitle("Timetable Builder - Admin Portal");

                // Admin File Menu Items (Removed Load/Save as persistence is automatic)
                // JMenuItem loadTimetableMenuItem = new JMenuItem("Load Master Timetable");
                // JMenuItem saveTimetableMenuItem = new JMenuItem("Save Master Timetable");
                // // TODO: Add ActionListeners to menu items
                // fileMenu.add(loadTimetableMenuItem);
                // fileMenu.add(saveTimetableMenuItem);

                // Admin Generate Menu
                JMenu generateMenu = new JMenu("Generate");
                JMenuItem autoSuggestMenuItem = new JMenuItem("Auto-Suggest Timetable");
                // TODO: Add ActionListener for auto-suggest
                generateMenu.add(autoSuggestMenuItem);
                menuBar.add(generateMenu);

                // Admin Tabs
                tabbedPane.addTab("Data Input", new DataInputPanel(this.persistenceService));
                tabbedPane.addTab("Manual Timetable", new ManualTimetablePanel(this.timetableService, this.persistenceService));
                tabbedPane.addTab("Auto Suggest", new AutoTimetablePanel(this.persistenceService, this.timetableService));
                break;

            case TEACHER:
                 setTitle("Timetable Builder - Teacher Portal");

                 // Teacher File Menu Items
                 JMenuItem viewTeacherTimetableMenuItem = new JMenuItem("View My Timetable");
                 // TODO: Add ActionListener for view timetable
                 fileMenu.add(viewTeacherTimetableMenuItem);

                 // Teacher Tabs
                 // Lookup instructor using PersistenceService
                 Instructor currentInstructor = findInstructorById(loggedInUser.getUserID()); // Use updated method
                 if (currentInstructor != null) {
                      // Pass TimetableService to TeacherTimetablePanel
                      tabbedPane.addTab("My Timetable", new TeacherTimetablePanel(currentInstructor, this.timetableService));
                 } else {
                      // Handle error: Instructor not found
                      System.err.println("Error: Could not find Instructor with ID: " + loggedInUser.getUserID());
                      tabbedPane.addTab("Error", new JLabel("Could not load teacher data. Instructor ID mismatch?"));
                 }
                 break;

            case STUDENT:
                 setTitle("Timetable Builder - Student Portal");

                 // Student File Menu Items
                 JMenuItem viewStudentTimetableMenuItem = new JMenuItem("View My Timetable");
                 // TODO: Add ActionListener for view timetable
                 fileMenu.add(viewStudentTimetableMenuItem);

                 // Student Tabs
                 // Lookup student using PersistenceService
                 Student currentStudent = findStudentById(loggedInUser.getUserID()); // Use updated method
                 if (currentStudent != null) {
                     // Pass services needed by student panels
                     tabbedPane.addTab("Course Browser", new CourseBrowserPanel(currentStudent, this.persistenceService, this.timetableService));
                     tabbedPane.addTab("My Schedule", new StudentTimetablePanel(currentStudent, this.timetableService));
                 } else {
                     // Handle error: Student not found
                     System.err.println("Error: Could not find Student with ID: " + loggedInUser.getUserID());
                     tabbedPane.addTab("Error", new JLabel("Could not load student data. Student ID mismatch?"));
                 }
                 break;
        }

        // Common File Menu Item: Exit (Handled by WindowListener)
        // fileMenu.addSeparator(); // Removed as Load/Save were removed
        // JMenuItem exitMenuItem = new JMenuItem("Exit");
        // menuBar.add(fileMenu); // fileMenu is added below
        // setJMenuBar(menuBar);

        // Add File menu if it has items OR if Generate menu exists (for Admin)
        if (fileMenu.getItemCount() > 0 || menuBar.getMenuCount() > 1) { // Check if File menu needed
            if (fileMenu.getItemCount() > 0 && menuBar.getMenu(0) != fileMenu) { // Add separator if needed
                fileMenu.addSeparator();
            }
             if (menuBar.getMenu(0) != fileMenu) { // Ensure File menu is added if not already
                 menuBar.add(fileMenu); 
             }
        }
        
        // Always set the menu bar
        setJMenuBar(menuBar);

        // Add the tabbed pane to the frame
        add(tabbedPane, BorderLayout.CENTER);

        // Revalidate and repaint to ensure changes are shown
        revalidate();
        repaint();
    }

    // --- Updated methods to use PersistenceService ---
    private Instructor findInstructorById(String instructorId) {
        if (instructorId == null || persistenceService == null) {
            return null;
        }
        System.out.println("MainFrame: Searching for instructor with ID: " + instructorId);
        return persistenceService.getInstructors().stream()
                .filter(instructor -> instructorId.equals(instructor.getInstructorID()))
                .findFirst()
                .orElse(null); // Return null if not found
    }

    private Student findStudentById(String studentId) {
        if (studentId == null || persistenceService == null) {
            return null;
        }
        System.out.println("MainFrame: Searching for student with ID: " + studentId);
        return persistenceService.getStudents().stream()
                .filter(student -> studentId.equals(student.getStudentID()))
                .findFirst()
                .orElse(null); // Return null if not found
    }
    // -------------------------------------------------------
} 