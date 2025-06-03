package com.timetablebuilder.ui;

import com.timetablebuilder.model.Timetable; // Need Timetable for the model
import com.timetablebuilder.model.TimetableEntry;
import com.timetablebuilder.service.PersistenceService;
import com.timetablebuilder.service.TimetableGenerator;
import com.timetablebuilder.service.TimetableService;
import com.timetablebuilder.ui.model.GeneratedTimetableModel;
import com.timetablebuilder.ui.renderer.TimetableCellRenderer; // Import the renderer

import javax.swing.*;
import javax.swing.table.TableColumn; // Needed for column manipulation
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Panel for the Automatic Timetable Generation Tab
public class AutoTimetablePanel extends JPanel {

    private JButton btnGenerate;
    private JButton btnApplyToMaster;
    private JProgressBar progressBar;
    private JLabel lblStatus;

    // Constraint Components (Placeholders)
    private JCheckBox chkEnforceCapacity;
    private JCheckBox chkMinimizeGaps;
    private JList<String> listInstructorPrefs; // Placeholder
    private DefaultListModel<String> listModelInstructorPrefs;

    // Timetable Display Components
    private JTable timetableDisplayTable;
    private GeneratedTimetableModel timetableModel;

    // Services
    private final PersistenceService persistenceService;
    private final TimetableService timetableService;
    private final TimetableGenerator timetableGenerator;
    
    // Generated timetable
    private Timetable generatedTimetable;

    // TODO: Add components to display the generated timetable (e.g., JTable, custom panel)
    // TODO: Add options/constraints inputs before generation

    public AutoTimetablePanel(PersistenceService persistenceService, TimetableService timetableService) {
        setLayout(new BorderLayout(10, 10)); // Gaps between components
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Store services
        this.persistenceService = persistenceService;
        this.timetableService = timetableService;
        this.timetableGenerator = new TimetableGenerator(persistenceService, timetableService);

        // --- Top Panel: Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGenerate = new JButton("Generate Timetable");
        btnApplyToMaster = new JButton("Apply to Master Timetable");
        btnApplyToMaster.setEnabled(false); // Disabled until generation completes
        progressBar = new JProgressBar();
        progressBar.setVisible(false); // Initially hidden
        progressBar.setStringPainted(true); // Show percentage
        lblStatus = new JLabel("Ready.");

        controlPanel.add(btnGenerate);
        controlPanel.add(btnApplyToMaster);
        controlPanel.add(progressBar);
        controlPanel.add(lblStatus);

        add(controlPanel, BorderLayout.NORTH);

        // --- West Panel: Constraints ---
        JPanel constraintsPanel = createConstraintsPanel();
        add(constraintsPanel, BorderLayout.WEST);

        // --- Center Panel: Timetable Display Area ---
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Generated Timetable"));

        // Initialize Table and Model
        timetableModel = new GeneratedTimetableModel();
        timetableDisplayTable = new JTable(timetableModel);

        // Basic Table Setup
        timetableDisplayTable.setRowHeight(60); // Increased height for renderer
        timetableDisplayTable.getTableHeader().setReorderingAllowed(false);
        timetableDisplayTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scroll

        // Apply the custom renderer to day columns
        TimetableCellRenderer cellRenderer = new TimetableCellRenderer();
        for (int i = 1; i < timetableDisplayTable.getColumnCount(); i++) { // Skip first column (Hour)
            TableColumn dayColumn = timetableDisplayTable.getColumnModel().getColumn(i);
            dayColumn.setCellRenderer(cellRenderer);
            dayColumn.setPreferredWidth(120); // Set a reasonable width for day columns
        }
        // Set width for the first column (Hour)
        TableColumn timeColumn = timetableDisplayTable.getColumnModel().getColumn(0);
        timeColumn.setPreferredWidth(60);

        // Add table in scroll pane
        JScrollPane scrollPane = new JScrollPane(timetableDisplayTable);
        displayPanel.add(scrollPane, BorderLayout.CENTER);

        add(displayPanel, BorderLayout.CENTER);

        // --- Action Listeners ---
        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGeneration();
            }
        });
        
        btnApplyToMaster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyToMasterTimetable();
            }
        });
    }

    private JPanel createConstraintsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Stack components vertically
        panel.setBorder(BorderFactory.createTitledBorder("Constraints"));

        chkEnforceCapacity = new JCheckBox("Strictly Enforce Room Capacity");
        chkEnforceCapacity.setSelected(true);
        chkMinimizeGaps = new JCheckBox("Minimize Instructor Gaps");
        chkMinimizeGaps.setSelected(true);

        // Placeholder for instructor preferences
        JLabel lblPrefs = new JLabel("Instructor Preferences:");
        listModelInstructorPrefs = new DefaultListModel<>();
        // Add some dummy preferences for demo purposes
        listModelInstructorPrefs.addElement("Prof. Jane: No early classes");
        listModelInstructorPrefs.addElement("Dr. Smith: No Friday classes");
        
        listInstructorPrefs = new JList<>(listModelInstructorPrefs);
        JScrollPane prefsScrollPane = new JScrollPane(listInstructorPrefs);
        prefsScrollPane.setPreferredSize(new Dimension(200, 100));

        // Add components with some spacing
        panel.add(chkEnforceCapacity);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        panel.add(chkMinimizeGaps);
        panel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer
        panel.add(lblPrefs);
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        panel.add(prefsScrollPane);

        // Add glue at the bottom to push components to the top
        panel.add(Box.createVerticalGlue());

        // Set a preferred width for the panel
        panel.setPreferredSize(new Dimension(250, panel.getPreferredSize().height));

        return panel;
    }

    private void startGeneration() {
        // Read constraints from UI
        boolean enforceCapacity = chkEnforceCapacity.isSelected();
        boolean minimizeGaps = chkMinimizeGaps.isSelected();
        
        // Pass options to generator
        timetableGenerator.setOptions(enforceCapacity, minimizeGaps);
        System.out.println("Starting generation with constraints: Enforce Capacity=" + enforceCapacity + ", Minimize Gaps=" + minimizeGaps);

        // Update UI state
        btnGenerate.setEnabled(false);
        btnApplyToMaster.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        lblStatus.setText("Preparing for generation...");

        // Create and execute the SwingWorker
        TimetableGeneratorWorker worker = new TimetableGeneratorWorker();
        worker.execute();
    }
    
    private void applyToMasterTimetable() {
        if (generatedTimetable == null || generatedTimetable.getEntries().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No timetable has been generated or the generated timetable is empty.",
                "Cannot Apply", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirm with user
        int response = JOptionPane.showConfirmDialog(this,
            "This will replace the current master timetable with the generated one.\n" +
            "Any existing entries will be lost.\nAre you sure you want to continue?",
            "Confirm Apply", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (response != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Apply the generated timetable to the master timetable
        Timetable masterTimetable = timetableService.getMasterTimetable();
        
        // Clear existing entries
        List<TimetableEntry> currentEntries = new ArrayList<>(masterTimetable.getEntries());
        for (TimetableEntry entry : currentEntries) {
            timetableService.removeEntryFromMaster(entry);
        }
        
        // Add generated entries
        int entriesAdded = 0;
        for (TimetableEntry entry : generatedTimetable.getEntries()) {
            timetableService.addEntryToMaster(entry);
            entriesAdded++;
        }
        
        // Update UI and notify user
        lblStatus.setText("Applied " + entriesAdded + " entries to master timetable!");
        JOptionPane.showMessageDialog(this,
            entriesAdded + " entries have been successfully applied to the master timetable.",
            "Apply Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- SwingWorker for Background Generation ---
    private class TimetableGeneratorWorker extends SwingWorker<Timetable, String> {

        @Override
        protected Timetable doInBackground() throws Exception {
            return timetableGenerator.generateTimetable(
                // Progress callback
                percentComplete -> {
                    setProgress(percentComplete);
                    progressBar.setValue(percentComplete);
                },
                // Status callback
                status -> publish(status)
            );
        }

        @Override
        protected void process(List<String> chunks) {
            // Update status label with the latest message
            if (!chunks.isEmpty()) {
                lblStatus.setText(chunks.get(chunks.size() - 1));
            }
        }

        @Override
        protected void done() {
            try {
                generatedTimetable = get();
                if (generatedTimetable != null) {
                    int entries = generatedTimetable.getEntries().size();
                    if (entries > 0) {
                        lblStatus.setText("Timetable generated successfully with " + entries + " entries!");
                        timetableModel.setTimetable(generatedTimetable);
                        btnApplyToMaster.setEnabled(true);
                    } else {
                        lblStatus.setText("Generation completed but no entries could be scheduled.");
                    }
                } else {
                    lblStatus.setText("Generation failed to produce a timetable.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                lblStatus.setText("Generation interrupted.");
            } catch (ExecutionException e) {
                lblStatus.setText("Error during generation: " + e.getCause().getMessage());
                e.printStackTrace();
            } finally {
                // Re-enable button and reset progress bar
                btnGenerate.setEnabled(true);
                progressBar.setValue(0);
                progressBar.setVisible(false);
            }
        }
    }
} 