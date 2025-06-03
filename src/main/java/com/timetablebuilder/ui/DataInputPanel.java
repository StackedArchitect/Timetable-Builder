package com.timetablebuilder.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import com.timetablebuilder.model.Classroom;
import com.timetablebuilder.model.Course;
import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.model.User;
import com.timetablebuilder.service.PersistenceService;
import com.timetablebuilder.ui.dialogs.ClassroomDialog;
import com.timetablebuilder.ui.dialogs.CourseDialog;
import com.timetablebuilder.ui.dialogs.InstructorDialog;
import com.timetablebuilder.ui.dialogs.SectionDialog;
import com.timetablebuilder.ui.dialogs.StudentDialog;
import com.timetablebuilder.ui.dialogs.UserDialog;
import com.timetablebuilder.ui.model.ClassroomTableModel;
import com.timetablebuilder.ui.model.CourseTableModel;
import com.timetablebuilder.ui.model.InstructorTableModel;
import com.timetablebuilder.ui.model.SectionTableModel;
import com.timetablebuilder.ui.model.StudentTableModel;
import com.timetablebuilder.ui.model.UserTableModel;

// Panel for Admin Data Input Tab
public class DataInputPanel extends JPanel {

    private JTabbedPane dataTabs;
    private PersistenceService persistenceService;
    private JButton btnSaveAll;

    // Classroom Tab Components
    private JTable classroomTable;
    private ClassroomTableModel classroomTableModel;
    private JButton btnAddClassroom, btnEditClassroom, btnDeleteClassroom;

    // Instructor Tab Components
    private JTable instructorTable;
    private InstructorTableModel instructorTableModel;
    private JButton btnAddInstructor, btnEditInstructor, btnDeleteInstructor;

    // Course Tab Components
    private JTable courseTable;
    private CourseTableModel courseTableModel;
    private JButton btnAddCourse, btnEditCourse, btnDeleteCourse;

    // Section Tab Components
    private JTable sectionTable;
    private SectionTableModel sectionTableModel;
    private JButton btnAddSection, btnEditSection, btnDeleteSection;

    // Student Tab Components
    private JTable studentTable;
    private StudentTableModel studentTableModel;
    private JButton btnAddStudent, btnEditStudent, btnDeleteStudent;

    // User Tab Components
    private JTable userTable;
    private UserTableModel userTableModel;
    private JButton btnAddUser, btnEditUser, btnDeleteUser;

    public DataInputPanel(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        setLayout(new BorderLayout(5, 5));

        dataTabs = new JTabbedPane();
        dataTabs.addTab("Classrooms", createClassroomPanel());
        dataTabs.addTab("Instructors", createInstructorPanel());
        dataTabs.addTab("Courses", createCoursePanel());
        dataTabs.addTab("Sections", createSectionPanel());
        dataTabs.addTab("Students", createStudentPanel());
        dataTabs.addTab("Users", createUserPanel());

        add(dataTabs, BorderLayout.CENTER);

        // Save Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSaveAll = new JButton("Save All Changes");
        btnSaveAll.addActionListener(e -> {
            System.out.println("Save All button clicked.");
            persistenceService.saveAllData();
            JOptionPane.showMessageDialog(this, "All data saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        bottomPanel.add(btnSaveAll);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load initial data into tables from PersistenceService
        refreshAllTables(); 
    }

    private void refreshAllTables() {
        refreshClassroomTable();
        refreshInstructorTable();
        refreshCourseTable();
        refreshSectionTable();
        refreshStudentTable();
        refreshUserTable();
    }

    // --- Panel Creation Methods ---
    private JPanel createClassroomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Add gaps

        // Table setup
        classroomTableModel = new ClassroomTableModel();
        classroomTable = new JTable(classroomTableModel);
        classroomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classroomTable.setAutoCreateRowSorter(true); // Enable sorting
        
        // --- Add Boolean Renderer --- 
        classroomTable.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                         boolean isSelected, boolean hasFocus, 
                                                         int row, int column) {
                // Get the default component (label)
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Boolean) {
                    setText(((Boolean) value) ? "Yes" : "No");
                    setHorizontalAlignment(JLabel.CENTER); // Center align Yes/No
                } else {
                    setText(""); // Handle null or unexpected type
                }
                return c;
            }
        });
        // ---------------------------
        
        panel.add(new JScrollPane(classroomTable), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAddClassroom = new JButton("Add Classroom");
        btnEditClassroom = new JButton("Edit Selected");
        btnDeleteClassroom = new JButton("Delete Selected");

        btnAddClassroom.addActionListener(e -> openClassroomDialog(null));
        btnEditClassroom.addActionListener(e -> {
            int selectedRow = classroomTable.getSelectedRow();
            if (selectedRow != -1) {
                 // Need to convert view row index to model row index if sorting/filtering is enabled
                 int modelRow = classroomTable.convertRowIndexToModel(selectedRow);
                 Classroom classroomToEdit = classroomTableModel.getClassroomAt(modelRow);
                 if (classroomToEdit != null) {
                      openClassroomDialog(classroomToEdit);
                 }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a classroom to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteClassroom.addActionListener(e -> deleteSelectedClassroom());

        buttonPanel.add(btnAddClassroom);
        buttonPanel.add(btnEditClassroom);
        buttonPanel.add(btnDeleteClassroom);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding

        return panel;
    }

    private JPanel createInstructorPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        instructorTableModel = new InstructorTableModel();
        instructorTable = new JTable(instructorTableModel);
        instructorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instructorTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(instructorTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAddInstructor = new JButton("Add Instructor");
        btnEditInstructor = new JButton("Edit Selected");
        btnDeleteInstructor = new JButton("Delete Selected");
        btnAddInstructor.addActionListener(e -> openInstructorDialog(null));
        btnEditInstructor.addActionListener(e -> {
            int selectedRow = instructorTable.getSelectedRow();
            if (selectedRow != -1) {
                 int modelRow = instructorTable.convertRowIndexToModel(selectedRow);
                 Instructor instructorToEdit = instructorTableModel.getInstructorAt(modelRow);
                 if (instructorToEdit != null) openInstructorDialog(instructorToEdit);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an instructor to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteInstructor.addActionListener(e -> deleteSelectedInstructor());
        buttonPanel.add(btnAddInstructor);
        buttonPanel.add(btnEditInstructor);
        buttonPanel.add(btnDeleteInstructor);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        courseTableModel = new CourseTableModel();
        courseTable = new JTable(courseTableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAddCourse = new JButton("Add Course");
        btnEditCourse = new JButton("Edit Selected");
        btnDeleteCourse = new JButton("Delete Selected");
        btnAddCourse.addActionListener(e -> openCourseDialog(null));
        btnEditCourse.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow != -1) {
                 int modelRow = courseTable.convertRowIndexToModel(selectedRow);
                 Course courseToEdit = courseTableModel.getCourseAt(modelRow);
                 if (courseToEdit != null) openCourseDialog(courseToEdit);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteCourse.addActionListener(e -> deleteSelectedCourse());
        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnEditCourse);
        buttonPanel.add(btnDeleteCourse);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        sectionTableModel = new SectionTableModel();
        sectionTable = new JTable(sectionTableModel);
        setupTableDefaults(sectionTable);
        panel.add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        btnAddSection = new JButton("Add");
        btnEditSection = new JButton("Edit");
        btnDeleteSection = new JButton("Delete");

        btnAddSection.addActionListener(e -> openSectionDialog(null));
        btnEditSection.addActionListener(e -> editSelectedSection());
        btnDeleteSection.addActionListener(e -> deleteSelectedSection());

        panel.add(createButtonPanel(btnAddSection, btnEditSection, btnDeleteSection), BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        studentTableModel = new StudentTableModel();
        studentTable = new JTable(studentTableModel);
        setupTableDefaults(studentTable);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        btnAddStudent = new JButton("Add");
        btnEditStudent = new JButton("Edit");
        btnDeleteStudent = new JButton("Delete");

        btnAddStudent.addActionListener(e -> openStudentDialog(null));
        btnEditStudent.addActionListener(e -> editSelectedStudent());
        btnDeleteStudent.addActionListener(e -> deleteSelectedStudent());

        panel.add(createButtonPanel(btnAddStudent, btnEditStudent, btnDeleteStudent), BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        userTableModel = new UserTableModel();
        userTable = new JTable(userTableModel);
        setupTableDefaults(userTable);
        // Add renderer for UserRole enum if desired
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        btnAddUser = new JButton("Add");
        btnEditUser = new JButton("Edit/Reset Pwd");
        btnDeleteUser = new JButton("Delete");

        btnAddUser.addActionListener(e -> openUserDialog(null));
        btnEditUser.addActionListener(e -> editSelectedUser());
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());

        panel.add(createButtonPanel(btnAddUser, btnEditUser, btnDeleteUser), BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private void setupTableDefaults(JTable table) {
        // Implementation of setupTableDefaults method
    }

    private JPanel createButtonPanel(JButton add, JButton edit, JButton delete) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align buttons to the right
        if (add != null) {
            buttonPanel.add(add);
        }
        if (edit != null) {
            buttonPanel.add(edit);
        }
        if (delete != null) {
            buttonPanel.add(delete);
        }
        return buttonPanel;
    }

    // --- Refresh Table Methods ---
    private void refreshClassroomTable() {
        System.out.println("Refreshing classroom table from PersistenceService...");
        List<Classroom> currentClassrooms = persistenceService.getClassrooms();
        classroomTableModel.setClassrooms(currentClassrooms);
        System.out.println("Classroom table refreshed with " + currentClassrooms.size() + " entries.");
    }

    private void refreshInstructorTable() {
        System.out.println("Refreshing instructor table from PersistenceService...");
        List<Instructor> currentInstructors = persistenceService.getInstructors();
        instructorTableModel.setInstructors(currentInstructors);
        System.out.println("Instructor table refreshed with " + currentInstructors.size() + " entries.");
    }

    private void refreshCourseTable() {
        System.out.println("Refreshing course table from PersistenceService...");
        List<Course> currentCourses = persistenceService.getCourses();
        courseTableModel.setCourses(currentCourses);
        System.out.println("Course table refreshed with " + currentCourses.size() + " entries.");
    }

    private void refreshSectionTable() {
        System.out.println("Refreshing section table from PersistenceService...");
        List<Section> currentSections = persistenceService.getSections();
        sectionTableModel.setSections(currentSections);
        System.out.println("Section table refreshed with " + currentSections.size() + " entries.");
    }

    private void refreshStudentTable() {
        System.out.println("Refreshing student table from PersistenceService...");
        List<Student> currentStudents = persistenceService.getStudents();
        studentTableModel.setStudents(currentStudents);
        System.out.println("Student table refreshed with " + currentStudents.size() + " entries.");
    }

    private void refreshUserTable() {
        System.out.println("Refreshing user table from PersistenceService...");
        List<User> currentUsers = persistenceService.getUsers();
        userTableModel.setUsers(currentUsers);
        System.out.println("User table refreshed with " + currentUsers.size() + " entries.");
    }

    // --- Edit Action Methods ---
    private void editSelectedClassroom() {
        // Implementation of editSelectedClassroom method
    }

    private void editSelectedInstructor() {
        // Implementation of editSelectedInstructor method
    }

    private void editSelectedCourse() {
        // Implementation of editSelectedCourse method
    }

    private void editSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = sectionTable.convertRowIndexToModel(selectedRow);
            Section sectionToEdit = sectionTableModel.getSectionAt(modelRow);
            if (sectionToEdit != null) openSectionDialog(sectionToEdit);
        } else {
            showSelectionError("section");
        }
    }

    private void editSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            Student studentToEdit = studentTableModel.getStudentAt(modelRow);
            if (studentToEdit != null) openStudentDialog(studentToEdit);
        } else {
            showSelectionError("student");
        }
    }

    private void editSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = userTable.convertRowIndexToModel(selectedRow);
            User userToEdit = userTableModel.getUserAt(modelRow);
            // Don't allow editing the built-in admin? Maybe?
            if (userToEdit != null) openUserDialog(userToEdit);
        } else {
            showSelectionError("user");
        }
    }

    // --- Dialog Launch Methods ---
    private void openClassroomDialog(Classroom classroomToEdit) {
        Frame owner = getOwnerFrame();
        ClassroomDialog dialog = new ClassroomDialog(owner, classroomToEdit);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Classroom savedClassroom = dialog.getClassroom();
            if (classroomToEdit == null) {
                persistenceService.addClassroom(savedClassroom);
                System.out.println("Called persistenceService.addClassroom for: " + savedClassroom.getClassroomID());
            } else {
                persistenceService.updateClassroom(savedClassroom);
                 System.out.println("Called persistenceService.updateClassroom for: " + savedClassroom.getClassroomID());
            }
            refreshClassroomTable();
        } else {
             System.out.println("Classroom Dialog Closed - Cancelled");
        }
    }

    private void openInstructorDialog(Instructor instructorToEdit) {
        Frame owner = getOwnerFrame();
        InstructorDialog dialog = new InstructorDialog(owner, instructorToEdit);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Instructor savedInstructor = dialog.getInstructor();
            if (instructorToEdit == null) {
                persistenceService.addInstructor(savedInstructor);
                 System.out.println("Called persistenceService.addInstructor for: " + savedInstructor.getInstructorID());
            } else {
                persistenceService.updateInstructor(savedInstructor);
                 System.out.println("Called persistenceService.updateInstructor for: " + savedInstructor.getInstructorID());
            }
            refreshInstructorTable();
        } else {
             System.out.println("Instructor Dialog Closed - Cancelled");
        }
    }

    private void openCourseDialog(Course courseToEdit) {
        Frame owner = getOwnerFrame();
        // Pass the current list of courses from the service for conflict selection
        CourseDialog dialog = new CourseDialog(owner, courseToEdit, persistenceService.getCourses()); 
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Course savedCourse = dialog.getCourse();
            if (courseToEdit == null) {
                 persistenceService.addCourse(savedCourse);
                 System.out.println("Called persistenceService.addCourse for: " + savedCourse.getCourseCode());
            } else {
                persistenceService.updateCourse(savedCourse);
                 System.out.println("Called persistenceService.updateCourse for: " + savedCourse.getCourseCode());
            }
            refreshCourseTable();
        } else {
             System.out.println("Course Dialog Closed - Cancelled");
        }
    }

    private void openSectionDialog(Section sectionToEdit) {
        Frame owner = getOwnerFrame();
        // Pass current lists from service to the dialog
        SectionDialog dialog = new SectionDialog(owner, sectionToEdit, 
                                             persistenceService.getCourses(), 
                                             persistenceService.getInstructors());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Section savedSection = dialog.getSection();
            if (sectionToEdit == null) {
                persistenceService.addSection(savedSection);
                 System.out.println("Called persistenceService.addSection for: " + savedSection.getSectionID());
            } else {
                persistenceService.updateSection(savedSection);
                System.out.println("Called persistenceService.updateSection for: " + savedSection.getSectionID());
            }
            refreshSectionTable();
        } else {
            System.out.println("Section Dialog Closed - Cancelled");
        }
    }

    private void openStudentDialog(Student studentToEdit) {
        Frame owner = getOwnerFrame();
        StudentDialog dialog = new StudentDialog(owner, studentToEdit);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Student savedStudent = dialog.getStudent();
            if (studentToEdit == null) {
                persistenceService.addStudent(savedStudent);
                System.out.println("Called persistenceService.addStudent for: " + savedStudent.getStudentID());
            } else {
                persistenceService.updateStudent(savedStudent);
                System.out.println("Called persistenceService.updateStudent for: " + savedStudent.getStudentID());
            }
            refreshStudentTable();
        } else {
            System.out.println("Student Dialog Closed - Cancelled");
        }
    }

    private void openUserDialog(User userToEdit) {
        Frame owner = getOwnerFrame();
        // Pass instructor, student, and user lists from PersistenceService
        UserDialog dialog = new UserDialog(owner, userToEdit, 
                                       persistenceService.getInstructors(), 
                                       persistenceService.getStudents(),
                                       persistenceService.getUsers());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            User savedUser = dialog.getUser();
            if (userToEdit == null) { // Add
                 persistenceService.addUser(savedUser);
                 System.out.println("Called persistenceService.addUser for: " + savedUser.getUsername());
            } else { // Edit
                persistenceService.updateUser(savedUser);
                System.out.println("Called persistenceService.updateUser for: " + savedUser.getUsername());
            }
            refreshUserTable();
        } else {
            System.out.println("User Dialog Closed - Cancelled");
        }
    }

    // --- Delete Action Methods (with checks) ---
    private void deleteSelectedClassroom() {
        int selectedRow = classroomTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = classroomTable.convertRowIndexToModel(selectedRow);
            Classroom classroomToDelete = classroomTableModel.getClassroomAt(modelRow);
            if (classroomToDelete != null) {
                // Check if classroom is used in the timetable
                boolean isUsed = persistenceService.getMasterTimetable().getEntries().stream()
                                 .anyMatch(entry -> entry.getClassroom() != null && 
                                                  entry.getClassroom().equals(classroomToDelete));

                if (isUsed) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete classroom '" + classroomToDelete.getClassroomID() + "' because it is currently assigned in the timetable.",
                            "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent deletion
                }

                if (showDeleteConfirmation("classroom", classroomToDelete.getClassroomID())) {
                    System.out.println("Deleting classroom: " + classroomToDelete.getClassroomID());
                    persistenceService.deleteClassroom(classroomToDelete.getClassroomID());
                    System.out.println("Called persistenceService.deleteClassroom.");
                    refreshClassroomTable();
                    // Refresh timetable panel if it depends on classroom list? (Not directly, but good practice)
                }
            } else {
                 System.err.println("Error: Could not find classroom object for selected row.");
            }
        } else {
            showSelectionError("classroom");
        }
    }

    private void deleteSelectedInstructor() {
        int selectedRow = instructorTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = instructorTable.convertRowIndexToModel(selectedRow);
            Instructor instructorToDelete = instructorTableModel.getInstructorAt(modelRow);
            if (instructorToDelete != null) {
                 // Check if instructor is assigned to any section
                 boolean isAssigned = persistenceService.getSections().stream()
                                  .anyMatch(section -> section.getInstructor() != null && 
                                                   section.getInstructor().equals(instructorToDelete));

                if (isAssigned) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete instructor '" + instructorToDelete.getName() + "' because they are assigned to one or more sections.",
                            "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent deletion
                }

                if (showDeleteConfirmation("instructor", instructorToDelete.getName())) {
                     System.out.println("Deleting instructor: " + instructorToDelete.getInstructorID());
                    persistenceService.deleteInstructor(instructorToDelete.getInstructorID());
                     System.out.println("Called persistenceService.deleteInstructor.");
                    refreshInstructorTable();
                     // Refresh section table/panel if it depends on instructor list?
                }
            }
        } else {
            showSelectionError("instructor");
        }
    }

    private void deleteSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = courseTable.convertRowIndexToModel(selectedRow);
            Course courseToDelete = courseTableModel.getCourseAt(modelRow);
            if (courseToDelete != null) {
                 // Check if course has sections
                 boolean hasSections = persistenceService.getSections().stream()
                                  .anyMatch(section -> section.getParentCourse() != null && 
                                                   section.getParentCourse().equals(courseToDelete));

                if (hasSections) {
                     JOptionPane.showMessageDialog(this,
                            "Cannot delete course '" + courseToDelete.getCourseName() + "' because it has one or more sections defined.",
                            "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent deletion
                }
                 
                if (showDeleteConfirmation("course", courseToDelete.getCourseName())) {
                     System.out.println("Deleting course: " + courseToDelete.getCourseCode());
                    persistenceService.deleteCourse(courseToDelete.getCourseCode());
                     System.out.println("Called persistenceService.deleteCourse.");
                    refreshCourseTable();
                     // Refresh section table/panel if it depends on course list?
                }
            }
        } else {
            showSelectionError("course");
        }
    }

    private void deleteSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = sectionTable.convertRowIndexToModel(selectedRow);
            Section sectionToDelete = sectionTableModel.getSectionAt(modelRow);
            if (sectionToDelete != null) {
                // Check if section is used in the timetable
                boolean isScheduled = persistenceService.getMasterTimetable().getEntries().stream()
                                 .anyMatch(entry -> entry.getSection() != null && 
                                                  entry.getSection().equals(sectionToDelete));

                if (isScheduled) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete section '" + sectionToDelete.getSectionID() + "' because it is currently scheduled in the timetable.",
                            "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent deletion
                }

                if (showDeleteConfirmation("section", sectionToDelete.getSectionID())) {
                    System.out.println("Deleting section: " + sectionToDelete.getSectionID());
                    persistenceService.deleteSection(sectionToDelete.getSectionID());
                    System.out.println("Called persistenceService.deleteSection.");
                    refreshSectionTable();
                    // Refresh timetable panel as it uses section list
                    // TODO: Find a better way to notify other panels? Event bus?
                }
            }
        } else {
            showSelectionError("section");
        }
    }

    // Delete Student: Currently no direct dependencies prevent deletion,
    // but associated User might be affected. Section enrollment is handled elsewhere.
    private void deleteSelectedStudent() { 
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = studentTable.convertRowIndexToModel(selectedRow);
            Student studentToDelete = studentTableModel.getStudentAt(modelRow);
            if (studentToDelete != null) {
                // Check if a User is associated with this student ID
                boolean isUserAssociated = persistenceService.getUsers().stream()
                                .anyMatch(user -> user.getRole() == com.timetablebuilder.model.UserRole.STUDENT &&
                                                 studentToDelete.getStudentID().equals(user.getUserID()));

                if (isUserAssociated) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete student '" + studentToDelete.getName() + "' because a User account is associated with this Student ID.\n" + 
                            "Please delete the associated User account first.",
                            "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                    return; // Prevent deletion
                }
                
                if (showDeleteConfirmation("student", studentToDelete.getName())) {
                    System.out.println("Deleting student: " + studentToDelete.getStudentID());
                    persistenceService.deleteStudent(studentToDelete.getStudentID());
                    System.out.println("Called persistenceService.deleteStudent.");
                    refreshStudentTable();
                    // Also refresh User table in case the check was somehow bypassed or for consistency?
                    // refreshUserTable(); 
                }
            }
        } else {
            showSelectionError("student");
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = userTable.convertRowIndexToModel(selectedRow);
            User itemToDelete = userTableModel.getUserAt(modelRow);
            if (itemToDelete != null && !"admin".equals(itemToDelete.getUsername())) { 
                if (showDeleteConfirmation("user", itemToDelete.getUsername())) {
                     System.out.println("Deleting user: " + itemToDelete.getUsername());
                     persistenceService.deleteUser(itemToDelete.getUsername());
                     System.out.println("Called persistenceService.deleteUser.");
                     refreshUserTable();
                }
            } else if (itemToDelete != null && "admin".equals(itemToDelete.getUsername())) {
                JOptionPane.showMessageDialog(this, "Cannot delete the default admin user.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            } 
        } else {
            showSelectionError("user");
        }
    }
    
    // --- UI Helper Methods ---
    private Frame getOwnerFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof Frame) {
            return (Frame) window;
        }
        return null; // Should ideally find the top-level frame
    }

    private void showSelectionError(String itemType) {
         JOptionPane.showMessageDialog(this,
                    "Please select a " + itemType + " to perform this action.", 
                    "Selection Error", 
                    JOptionPane.WARNING_MESSAGE);
    }

    private boolean showDeleteConfirmation(String itemType, String itemName) {
         int confirmation = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete " + itemType + " '" + itemName + "'?", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        return confirmation == JOptionPane.YES_OPTION;
    }
}

// --- REMOVED INNER DIALOG CLASSES (ClassroomDialog, InstructorDialog, CourseDialog) --- 