package com.timetablebuilder.service;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.timetablebuilder.model.Classroom;
import com.timetablebuilder.model.ComponentType;
import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.TimeSlot;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;

/**
 * Service responsible for generating timetables automatically
 * based on constraints and available resources.
 */
public class TimetableGenerator {

    private final PersistenceService persistenceService;
    private final TimetableService timetableService;
    
    // Configuration options
    private boolean enforceCapacity = true;
    private boolean minimizeGaps = true;
    
    // Progress tracking
    private int totalSections = 0;
    private int scheduledSections = 0;
    
    public TimetableGenerator(PersistenceService persistenceService, TimetableService timetableService) {
        this.persistenceService = persistenceService;
        this.timetableService = timetableService;
    }
    
    /**
     * Set configuration options for the generator
     */
    public void setOptions(boolean enforceCapacity, boolean minimizeGaps) {
        this.enforceCapacity = enforceCapacity;
        this.minimizeGaps = minimizeGaps;
    }
    
    /**
     * Main method to generate a timetable
     * @param progressCallback A callback to receive progress updates (0-100)
     * @param statusCallback A callback to receive status text updates
     * @return A new timetable with scheduled entries
     */
    public Timetable generateTimetable(ProgressCallback progressCallback, StatusCallback statusCallback) {
        // Create a new timetable
        Timetable generatedTimetable = new Timetable();
        
        // Fetch required data
        statusCallback.updateStatus("Loading data...");
        List<Section> sections = new ArrayList<>(persistenceService.getSections());
        List<Classroom> classrooms = new ArrayList<>(persistenceService.getClassrooms());
        
        // Skip if no data
        if (sections.isEmpty() || classrooms.isEmpty()) {
            statusCallback.updateStatus("No sections or classrooms available to schedule");
            return generatedTimetable;
        }
        
        // Sort sections by priority (Labs before Lectures, then larger sections first)
        statusCallback.updateStatus("Prioritizing sections...");
        Collections.sort(sections, (a, b) -> {
            // Lab sections first
            if (a.getType() == ComponentType.LAB && b.getType() != ComponentType.LAB) {
                return -1;
            }
            if (a.getType() != ComponentType.LAB && b.getType() == ComponentType.LAB) {
                return 1;
            }
            
            // Then by capacity (descending)
            return Integer.compare(b.getSectionCapacity(), a.getSectionCapacity());
        });
        
        // Sort classrooms by capacity (descending)
        Collections.sort(classrooms, (a, b) -> Integer.compare(b.getCapacity(), a.getCapacity()));
        
        // Initialize tracking
        totalSections = sections.size();
        scheduledSections = 0;
        progressCallback.updateProgress(0);
        
        // Generate all possible time slots
        List<TimeSlot> possibleTimeSlots = generatePossibleTimeSlots();
        
        // For each section, try to find a valid classroom and time slot
        for (Section section : sections) {
            statusCallback.updateStatus("Scheduling section: " + section.getSectionID());
            
            // Get suitable classrooms for this section
            List<Classroom> suitableClassrooms = getSuitableClassrooms(section, classrooms);
            if (suitableClassrooms.isEmpty()) {
                statusCallback.updateStatus("Warning: No suitable classroom for section " + section.getSectionID());
                continue;
            }
            
            // Shuffle time slots (random starting point increases diversity)
            Collections.shuffle(possibleTimeSlots);
            
            boolean scheduled = false;
            
            // Try each classroom and time slot until we find a valid combination
            for (Classroom classroom : suitableClassrooms) {
                if (scheduled) break;
                
                for (TimeSlot timeSlot : possibleTimeSlots) {
                    // Create potential entry
                    TimetableEntry entry = new TimetableEntry(section, classroom, timeSlot);
                    
                    // Check for conflicts
                    List<String> conflicts = timetableService.checkMasterConflicts(entry);
                    
                    if (conflicts.isEmpty()) {
                        // No conflicts, add to generated timetable
                        generatedTimetable.addEntry(entry);
                        scheduled = true;
                        scheduledSections++;
                        break;
                    }
                }
            }
            
            // Update progress
            int progressPercent = (scheduledSections * 100) / totalSections;
            progressCallback.updateProgress(progressPercent);
        }
        
        statusCallback.updateStatus(String.format("Completed: %d/%d sections scheduled (%.1f%%)", 
                                    scheduledSections, totalSections, 
                                    (double)scheduledSections/totalSections*100));
        
        return generatedTimetable;
    }
    
    /**
     * Get classrooms suitable for a section based on requirements
     */
    private List<Classroom> getSuitableClassrooms(Section section, List<Classroom> allClassrooms) {
        List<Classroom> suitable = new ArrayList<>();
        
        for (Classroom classroom : allClassrooms) {
            // Skip classrooms that don't meet capacity if enforcing
            if (enforceCapacity && classroom.getCapacity() < section.getSectionCapacity()) {
                continue;
            }
            
            // Check for lab requirement
            if (section.getType() == ComponentType.LAB && !classroom.isLab()) {
                continue;
            }
            
            // Check for AV requirement
            if (section.requiresAV() && !classroom.hasAudioVideo()) {
                continue;
            }
            
            // Check for computers requirement
            if (section.getRequiredComputers() > 0 && 
                classroom.getNumComputers() < section.getRequiredComputers()) {
                continue;
            }
            
            suitable.add(classroom);
        }
        
        return suitable;
    }
    
    /**
     * Generate all possible time slots for scheduling
     */
    private List<TimeSlot> generatePossibleTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        
        // For each day Monday-Friday
        for (DayOfWeek day : Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            
            // For each hour 8am-5pm
            for (int hour = 8; hour <= 17; hour++) {
                // Create 1-hour slot
                slots.add(new TimeSlot(day, hour, 0, 60));
                
                // Also create 90-minute slot if not too late in the day
                if (hour <= 16) {
                    slots.add(new TimeSlot(day, hour, 0, 90));
                }
                
                // Create 2-hour slot if not too late in the day
                if (hour <= 15) {
                    slots.add(new TimeSlot(day, hour, 0, 120));
                }
            }
        }
        
        return slots;
    }
    
    /**
     * Callback interface for progress updates
     */
    public interface ProgressCallback {
        void updateProgress(int percentComplete);
    }
    
    /**
     * Callback interface for status text updates
     */
    public interface StatusCallback {
        void updateStatus(String status);
    }
} 