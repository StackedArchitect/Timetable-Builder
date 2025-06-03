package com.timetablebuilder.ui;

import java.awt.BorderLayout; // Import all models
import java.awt.Dimension; // Added
import java.awt.FlowLayout;
import java.time.DayOfWeek; // Import list transfer handler
import java.util.ArrayList; // Import grid transfer handler
import java.util.List;
import java.util.Objects; // Needed for renderer setup
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory; // Added import
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel; // Needed for efficient lookup
import javax.swing.JList; // Needed for stream operations
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import com.timetablebuilder.model.Classroom;
import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.TimeSlot;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;
import com.timetablebuilder.service.PersistenceService;
import com.timetablebuilder.service.TimetableService;
import com.timetablebuilder.ui.model.ManualTimetableModel;
import com.timetablebuilder.ui.renderer.SectionListRenderer;
import com.timetablebuilder.ui.renderer.TimetableCellRenderer;

// Panel for Admin Manual Timetable Editing Tab
public class ManualTimetablePanel extends JPanel {

    // private JComboBox<Section> sectionComboBox; // Removed
    private JComboBox<Classroom> classroomComboBox;
    private JButton assignButton;
    private JButton removeButton;
    private JTable timetableGrid;
    private ManualTimetableModel manualTimetableModel; // Use new model

    // Components for unscheduled list
    private JList<Section> unscheduledList;
    private DefaultListModel<Section> unscheduledListModel;

    private TimetableService timetableService; // Service reference
    private PersistenceService persistenceService; // Added reference

    // --- Removed Dummy Data Fields --- 
    // private List<Section> availableSections = new ArrayList<>();
    // private List<Classroom> availableClassrooms = new ArrayList<>();
    // private Instructor dummyInstructor1 = new Instructor("T001", "Prof. Xavier");
    // private Instructor dummyInstructor2 = new Instructor("T002", "Dr. Strange");
    // private Course dummyCourse1 = new Course("CSF213", "OOP");
    // private Course dummyCourse2 = new Course("CSF214", "DSA");
    // ----------------------------------------------------------------

    // Constructor now accepts PersistenceService
    public ManualTimetablePanel(TimetableService timetableService, PersistenceService persistenceService) {
        this.timetableService = timetableService;
        this.persistenceService = persistenceService; // Store service
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // --- Removed Dummy Data Initialization call --- 
        // initializeDummyData(); 
        // ---------------------------

        // --- Top Panel: Selection Controls ---
        JPanel selectionPanel = createSelectionPanel();
        add(selectionPanel, BorderLayout.NORTH);

        // --- Center Panel: Timetable Grid ---
        setupTimetableGrid();
        add(new JScrollPane(timetableGrid), BorderLayout.CENTER);

        // --- West Panel: Unscheduled Sections List ---
        JPanel listPanel = createUnscheduledListPanel();
        add(listPanel, BorderLayout.WEST);

        // --- Initial Data Population ---
        populateComboBoxes(); // Now uses PersistenceService
        refreshTimetableGrid(); // Load initial timetable entries from service
        // refreshUnscheduledList(); // This is called within refreshTimetableGrid
    }

    // Extracted setup for top panel
    private JPanel createSelectionPanel() {
         JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // Removed Section ComboBox and Label
        // selectionPanel.add(new JLabel("Section:"));
        // sectionComboBox = new JComboBox<>();
        // selectionPanel.add(sectionComboBox);

        selectionPanel.add(new JLabel("Classroom:"));
        classroomComboBox = new JComboBox<>();
        selectionPanel.add(classroomComboBox);

        assignButton = new JButton("Assign Selected Section to Slot"); // Updated button text
        removeButton = new JButton("Remove from Selected Slot");
        JButton refreshButton = new JButton("Refresh Data");

        assignButton.addActionListener(e -> assignSelected());
        removeButton.addActionListener(e -> removeSelected());
        refreshButton.addActionListener(e -> refreshData());

        selectionPanel.add(assignButton);
        selectionPanel.add(removeButton);
        selectionPanel.add(refreshButton);
        return selectionPanel;
    }

    // Setup for West panel
    private JPanel createUnscheduledListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Unscheduled Sections"));

        unscheduledListModel = new DefaultListModel<>();
        unscheduledList = new JList<>(unscheduledListModel);
        unscheduledList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Set the custom renderer
        unscheduledList.setCellRenderer(new SectionListRenderer()); 

        // Removed DnD setup
        // unscheduledList.setDragEnabled(true);
        // unscheduledList.setTransferHandler(new UnscheduledListTransferHandler());

        JScrollPane scrollPane = new JScrollPane(unscheduledList);
        scrollPane.setPreferredSize(new Dimension(250, 0)); // Increased width slightly for renderer
        listPanel.add(scrollPane, BorderLayout.CENTER);

        return listPanel;
    }

    private void setupTimetableGrid() {
        // Use the new ManualTimetableModel
        manualTimetableModel = new ManualTimetableModel();
        timetableGrid = new JTable(manualTimetableModel);

        timetableGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableGrid.setCellSelectionEnabled(true);
        timetableGrid.setRowHeight(60); // Adjusted row height for renderer
        timetableGrid.getTableHeader().setReorderingAllowed(false);
        timetableGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scroll

        // Apply the custom renderer to day columns
        TimetableCellRenderer cellRenderer = new TimetableCellRenderer();
        for (int i = 1; i < timetableGrid.getColumnCount(); i++) { // Skip first column (Hour)
            TableColumn dayColumn = timetableGrid.getColumnModel().getColumn(i);
            dayColumn.setCellRenderer(cellRenderer);
            dayColumn.setPreferredWidth(120);
        }
        // Set width for the first column (Hour)
        TableColumn timeColumn = timetableGrid.getColumnModel().getColumn(0);
        timeColumn.setPreferredWidth(60);

        // Removed DnD setup
        // timetableGrid.setTransferHandler(new TimetableGridTransferHandler(this, timetableService));
        // Optional: Visual feedback on drop 
        // timetableGrid.setDropMode(DropMode.USE_SELECTION); // Or DropMode.ON
    }

    // Updated to use PersistenceService
    private void populateComboBoxes() {
        System.out.println("Populating combo boxes from PersistenceService...");
        classroomComboBox.removeAllItems();
        // Fetch classrooms from the service
        List<Classroom> classrooms = persistenceService.getClassrooms();
        if (classrooms != null) {
             classrooms.forEach(classroomComboBox::addItem);
             System.out.println("Classroom combo box populated with " + classrooms.size() + " entries.");
        } else {
             System.err.println("Warning: Could not populate classroom combo box, PersistenceService returned null list.");
        }
    }

    private void refreshTimetableGrid() {
        System.out.println("ManualTimetablePanel: Refreshing Grid from TimetableService");
        Timetable currentTimetable = timetableService.getMasterTimetable();
        manualTimetableModel.loadTimetable(currentTimetable);
        refreshUnscheduledList(currentTimetable);
        System.out.println("Grid refreshed using ManualTimetableModel.");
    }

    // Updated to use PersistenceService
    private void refreshUnscheduledList(Timetable currentTimetable) {
         System.out.println("Refreshing unscheduled sections list using PersistenceService...");
         // Get ALL sections from PersistenceService
         List<Section> allSections = persistenceService.getSections(); 
         if (allSections == null) {
              System.err.println("Warning: Cannot refresh unscheduled list, PersistenceService returned null sections list.");
              allSections = new ArrayList<>(); // Use empty list to avoid NPE
         }
         if (currentTimetable == null) {
              System.err.println("Warning: Cannot refresh unscheduled list, TimetableService returned null timetable.");
               unscheduledListModel.clear();
              return;
         }
         
         Set<Section> scheduledSections = currentTimetable.getEntries().stream()
                                                        .map(TimetableEntry::getSection)
                                                        .filter(Objects::nonNull) // Avoid NPE if entry has null section
                                                        .collect(Collectors.toSet());
        unscheduledListModel.clear();
        for (Section section : allSections) {
            // Ensure the section itself is not null
            if (section != null && !scheduledSections.contains(section)) {
                unscheduledListModel.addElement(section);
            }
        }
         System.out.println("Unscheduled list updated with " + unscheduledListModel.getSize() + " sections.");
    }

    private void assignSelected() {
        int selectedRow = timetableGrid.getSelectedRow();
        int selectedColumn = timetableGrid.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a time slot cell in the grid.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get section from the JList instead of ComboBox
        Section selectedSection = unscheduledList.getSelectedValue();
        if (selectedSection == null) {
             JOptionPane.showMessageDialog(this, "Please select a Section from the list on the left.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Classroom selectedClassroom = (Classroom) classroomComboBox.getSelectedItem();
        if (selectedClassroom == null) {
            JOptionPane.showMessageDialog(this, "Please select a Classroom.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Determine the DayOfWeek and Hour from the grid selection 
        // (Used for conflict messages, not for the entry's actual timeslot)
        int selectedHour = manualTimetableModel.getDisplayStartHour() + selectedRow;
        DayOfWeek selectedDay = manualTimetableModel.getDays().get(selectedColumn - 1);
        String selectedCellTimeInfo = String.format("%s H%d", selectedDay, selectedHour); // For messages
        
        // **Important:** The actual TimeSlot comes from the Section object itself!
        TimeSlot sectionTimeSlot = selectedSection.getTimeSlot();
        if (sectionTimeSlot == null) {
             JOptionPane.showMessageDialog(this, "Selected Section has no TimeSlot defined!", "Data Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        System.out.println("Attempting to assign: " + selectedSection.getSectionID() + 
                           " (Time: " + sectionTimeSlot + ") to Classroom: " + 
                           selectedClassroom.getClassroomID() + " at grid slot: " + selectedCellTimeInfo);

        // Create the potential entry using the section's actual time slot
        TimetableEntry potentialEntry = new TimetableEntry(selectedSection, selectedClassroom, sectionTimeSlot);

        // Check for conflicts
        List<String> conflicts = timetableService.checkMasterConflicts(potentialEntry);

        if (conflicts.isEmpty()) {
            // No conflicts, add to the master timetable via the service
            timetableService.addEntryToMaster(potentialEntry);
            System.out.println("Assignment successful.");
            refreshTimetableGrid(); // Refresh grid and unscheduled list
        } else {
            // Show conflicts
            String conflictMessage = "Could not assign section due to conflicts:\n" + String.join("\n", conflicts);
            JOptionPane.showMessageDialog(this, conflictMessage, "Assignment Conflict", JOptionPane.WARNING_MESSAGE);
            System.out.println("Assignment failed due to conflicts.");
        }
    }

    private void removeSelected() {
        int selectedRow = timetableGrid.getSelectedRow();
        int selectedColumn = timetableGrid.getSelectedColumn();

        if (selectedRow == -1 || selectedColumn <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a timetable cell containing an entry to remove.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the entry directly from the model using the grid coordinates
        int selectedHour = manualTimetableModel.getDisplayStartHour() + selectedRow;
        DayOfWeek selectedDay = manualTimetableModel.getDays().get(selectedColumn - 1);
        TimetableEntry entryToRemove = manualTimetableModel.getEntryAt(selectedDay, selectedHour);
        
        if (entryToRemove != null) {
            // Confirm removal (optional)
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to remove section '" + entryToRemove.getSection().getSectionID() + "' from this slot?", 
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Remove the entry using the TimetableService
                timetableService.removeEntryFromMaster(entryToRemove);
                System.out.println("Removal successful for entry at " + selectedDay + " H" + selectedHour);
                refreshTimetableGrid(); // Refresh grid and unscheduled list
            }
        } else {
            JOptionPane.showMessageDialog(this, "The selected cell does not contain a scheduled entry.", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to refresh all data from services
    private void refreshData() {
        System.out.println("ManualTimetablePanel: Refreshing all data...");
        // Reload from persistence service first
        persistenceService.loadAllData();
        // Refresh UI components
        populateComboBoxes();
        refreshTimetableGrid();
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!", 
                                     "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
    }
} 