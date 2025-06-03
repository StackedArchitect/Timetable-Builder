package com.timetablebuilder.service;

import java.util.ArrayList; // Import all models
import java.util.HashSet;
import java.util.List; // Added import
import java.util.Optional; // Added import
import java.util.Set;
import java.util.stream.Collectors; // Added import

import com.timetablebuilder.model.Classroom;
import com.timetablebuilder.model.ComponentType;
import com.timetablebuilder.model.Course;
import com.timetablebuilder.model.Instructor;
import com.timetablebuilder.model.Section;
import com.timetablebuilder.model.Student;
import com.timetablebuilder.model.TimeSlot;
import com.timetablebuilder.model.Timetable;
import com.timetablebuilder.model.TimetableEntry;

// Provides access to the timetable and checks for conflicts.
public class TimetableService {

    private Timetable masterTimetable;
    private PersistenceService persistenceService;

    public TimetableService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        
        // Get the timetable instance loaded (and linked) by PersistenceService
        this.masterTimetable = persistenceService.getMasterTimetable(); 

        // Check if PersistenceService returned a null timetable (e.g., initial run)
        if (this.masterTimetable == null) {
            System.out.println("TimetableService: PersistenceService provided null timetable. Creating new empty timetable.");
            this.masterTimetable = new Timetable(); 
            // Optionally, save this new empty timetable immediately?
            // this.persistenceService.saveAllData(); 
        } else {
             System.out.println("TimetableService: Initialized with timetable from PersistenceService containing " + this.masterTimetable.getEntries().size() + " entries.");
        }
    }

    public Timetable getMasterTimetable() {
        return masterTimetable;
    }

    public void addEntryToMaster(TimetableEntry entry) {
        if (masterTimetable == null) return; // Safety check
        masterTimetable.addEntry(entry);
        System.out.println("TimetableService: Added entry, triggering saveAllData.");
        persistenceService.saveAllData(); // Save all data after modification
    }

    public void removeEntryFromMaster(TimetableEntry entry) {
         if (masterTimetable == null) return; // Safety check
        masterTimetable.removeEntry(entry);
         System.out.println("TimetableService: Removed entry, triggering saveAllData.");
         persistenceService.saveAllData(); // Save all data after modification
    }

    /**
     * Checks a potential new entry against the master timetable for conflicts.
     * Implements constraints C1-C6 from the plan.
     *
     * @param potentialEntry The TimetableEntry to check.
     * @return A list of conflict description strings. Empty if no conflicts.
     */
    public List<String> checkMasterConflicts(TimetableEntry potentialEntry) {
        List<String> conflicts = new ArrayList<>();
        if (potentialEntry == null || potentialEntry.getTimeSlot() == null ||
            potentialEntry.getClassroom() == null || potentialEntry.getSection() == null ||
            potentialEntry.getSection().getInstructor() == null || potentialEntry.getSection().getParentCourse() == null)
        {
            conflicts.add("Invalid Timetable Entry data.");
            return conflicts; // Basic validation
        }

        TimeSlot newSlot = potentialEntry.getTimeSlot();
        Classroom newClassroom = potentialEntry.getClassroom();
        Instructor newInstructor = potentialEntry.getSection().getInstructor();
        Course newCourse = potentialEntry.getSection().getParentCourse();
        Section newSection = potentialEntry.getSection();

        // [C4] Resource Mismatch
        if (newSection.requiresAV() && !newClassroom.hasAudioVideo()) {
            conflicts.add("[C4] Resource Conflict: Section requires AV, Classroom '" + newClassroom.getClassroomID() + "' lacks it.");
        }
        if (newSection.getRequiredComputers() > 0 && newClassroom.getNumComputers() < newSection.getRequiredComputers()) {
             conflicts.add("[C4] Resource Conflict: Section requires " + newSection.getRequiredComputers() + " computers, Classroom '" + newClassroom.getClassroomID() + "' has " + newClassroom.getNumComputers() + ".");
        }
        if (newSection.getType() == ComponentType.LAB && !newClassroom.isLab()) {
             conflicts.add("[C4] Resource Conflict: Lab Section requires a Lab Classroom, '" + newClassroom.getClassroomID() + "' is not a lab.");
        }

        // [C5] Classroom Capacity
        if (newSection.getSectionCapacity() > newClassroom.getCapacity()) {
             conflicts.add("[C5] Capacity Conflict: Section capacity (" + newSection.getSectionCapacity() + ") exceeds Classroom '" + newClassroom.getClassroomID() + "' capacity (" + newClassroom.getCapacity() + ").");
        }


        for (TimetableEntry existingEntry : masterTimetable.getEntries()) {
            // Ensure existing entry data is not null before checks
            if (existingEntry == null || existingEntry.getTimeSlot() == null ||
                existingEntry.getClassroom() == null || existingEntry.getSection() == null ||
                existingEntry.getSection().getInstructor() == null || existingEntry.getSection().getParentCourse() == null)
            {
                 System.err.println("Warning: Skipping conflict check due to invalid data in existing timetable entry: " + existingEntry);
                 continue; // Skip this entry
            }

            // Check for conflicts only if the timeslots overlap
            if (existingEntry.getTimeSlot().overlaps(newSlot)) {
                // Conflict checks for entries overlapping in time

                // [C1] Classroom Overlap
                if (existingEntry.getClassroom().equals(newClassroom)) {
                    conflicts.add("[C1] Classroom Conflict: Classroom '" + newClassroom.getClassroomID() + "' is already booked by Section '" + existingEntry.getSection().getSectionID() + "' at " + newSlot + ".");
                }

                // [C2] Instructor Overlap
                if (existingEntry.getSection().getInstructor().equals(newInstructor)) {
                    conflicts.add("[C2] Instructor Conflict: Instructor '" + newInstructor.getName() + "' ('" + newInstructor.getInstructorID() + "') is already teaching Section '" + existingEntry.getSection().getSectionID() + "' at " + newSlot + ".");
                }

                // [C3] Course Conflict
                Course existingCourse = existingEntry.getSection().getParentCourse();
                if (newCourse.getConflictingCourses().contains(existingCourse.getCourseCode())) {
                    conflicts.add("[C3] Course Conflict: Course '" + newCourse.getCourseCode() + "' conflicts with scheduled Course '" + existingCourse.getCourseCode() + "' at " + newSlot + ".");
                }
                // Check reverse conflict as well
                if (existingCourse.getConflictingCourses().contains(newCourse.getCourseCode())) {
                     conflicts.add("[C3] Course Conflict: Scheduled Course '" + existingCourse.getCourseCode() + "' conflicts with Course '" + newCourse.getCourseCode() + "' at " + newSlot + ".");
                }
            }

            /* // Commented out C6 check as dayDifference method was removed from TimeSlot
            // [C6] BITS Policy - Lecture/Lab Gap
            if (existingEntry.getSection().getParentCourse().equals(newCourse)) {
                ComponentType existingType = existingEntry.getSection().getType();
                ComponentType newType = newSection.getType();

                boolean isLectureLabOrTutPair = (existingType == ComponentType.LECTURE && (newType == ComponentType.LAB || newType == ComponentType.TUTORIAL)) ||
                                              (newType == ComponentType.LECTURE && (existingType == ComponentType.LAB || existingType == ComponentType.TUTORIAL));

                if (isLectureLabOrTutPair) {
                    int dayDiff = newSlot.dayDifference(existingEntry.getTimeSlot());
                    if (dayDiff <= 1) {
                        conflicts.add("[C6] Lecture/Lab/Tut Gap Policy: Section '" + newSection.getSectionID() + "' (" + newType + ") is scheduled too close (Day Diff: "+ dayDiff +") to Section '" + existingEntry.getSection().getSectionID() + "' (" + existingType + ") of the same course at " + newSlot + " / " + existingEntry.getTimeSlot() + ".");
                    }
                }
            }
            */
        }

        return conflicts;
    }

    // --- Helper to find the scheduled entry for a specific section ---
    public Optional<TimetableEntry> findScheduledEntryForSection(Section section) {
        if (masterTimetable == null || section == null) {
            return Optional.empty();
        }
        return masterTimetable.getEntries().stream()
                .filter(entry -> section.equals(entry.getSection()))
                .findFirst();
    }

    // --- Student-Specific Conflict Check --- 
    /**
     * Checks if enrolling a student in a given section would cause conflicts
     * based on the student's current schedule and course conflicts.
     * 
     * @param student The student enrolling.
     * @param sectionToEnroll The section to potentially enroll in.
     * @return A list of conflict description strings. Empty if no conflicts.
     */
    public List<String> checkStudentEnrollmentConflicts(Student student, Section sectionToEnroll) {
        List<String> conflicts = new ArrayList<>();
        System.out.println("--- Starting Enrollment Conflict Check ---");
        System.out.println("Student: " + ((student != null) ? student.getStudentID() : "NULL"));
        System.out.println("Section to Enroll: " + ((sectionToEnroll != null) ? sectionToEnroll.getSectionID() : "NULL"));
        
        if (student == null || sectionToEnroll == null || masterTimetable == null) {
             conflicts.add("Invalid input for conflict check.");
             System.out.println("Check failed: Invalid input.");
             System.out.println("--- Finished Enrollment Conflict Check --- Finding " + conflicts.size() + " conflicts.");
             return conflicts;
        }
        
        System.out.println("Student currently enrolled in " + student.getEnrolledSections().size() + " sections (according to Student object).");

        // 1. Check if already enrolled
        if (student.getEnrolledSections().contains(sectionToEnroll)) {
            conflicts.add("Already enrolled in Section '" + sectionToEnroll.getSectionID() + "'.");
            // Return early as other checks are irrelevant if already enrolled
            return conflicts; 
        }
        
        // 2. Check Section Capacity
        if (sectionToEnroll.getCurrentEnrollment() >= sectionToEnroll.getSectionCapacity()) {
             conflicts.add("Section '" + sectionToEnroll.getSectionID() + "' is full (Capacity: " + 
                           sectionToEnroll.getSectionCapacity() + ").");
        }

        // 3. Find the potential timeslot for the section to enroll in
        Optional<TimetableEntry> entryToEnrollOpt = findScheduledEntryForSection(sectionToEnroll);
        if (!entryToEnrollOpt.isPresent()) {
            // This check assumes the section MUST be scheduled to be enrollable.
            // If sections can be enrolled in *before* scheduling, this check needs adjustment.
            conflicts.add("Section '" + sectionToEnroll.getSectionID() + "' is not currently scheduled in the master timetable.");
            return conflicts; // Cannot check time conflicts if not scheduled
        }
        TimetableEntry entryToEnroll = entryToEnrollOpt.get();
        TimeSlot slotToEnroll = entryToEnroll.getTimeSlot();
        Course courseToEnroll = sectionToEnroll.getParentCourse(); // Assume not null if scheduled

        // 4. Check against the student's *current* schedule for time/course conflicts
        System.out.println("Checking conflicts against schedule for student: " + student.getStudentID());
        List<TimetableEntry> studentCurrentSchedule = getStudentScheduledEntries(student);
        System.out.println("Found " + studentCurrentSchedule.size() + " currently scheduled entries for student.");
        
        for (TimetableEntry existingEntry : studentCurrentSchedule) {
            System.out.println("  Comparing with enrolled section: " + existingEntry.getSection().getSectionID());
            TimeSlot existingSlot = existingEntry.getTimeSlot();
            Course existingCourse = existingEntry.getSection().getParentCourse(); // Assume not null
            
            boolean timeOverlap = existingSlot.overlaps(slotToEnroll);
            System.out.println("    Time Overlap Check (" + slotToEnroll + " vs " + existingSlot + "): " + timeOverlap);
            
            if (timeOverlap) {
                // Time Conflict
                conflicts.add("Time Conflict: Section '" + sectionToEnroll.getSectionID() + "' at " + slotToEnroll +
                              " clashes with your enrolled Section '" + existingEntry.getSection().getSectionID() + "' at " + existingSlot + ".");

                // Course Conflict Check (only if time overlaps)
                boolean conflictCheck1 = courseToEnroll.getConflictingCourses().contains(existingCourse.getCourseCode());
                System.out.println("    Course Conflict Check 1 (" + courseToEnroll.getCourseCode() + " conflicts with " + existingCourse.getCourseCode() + "): " + conflictCheck1);
                if (conflictCheck1) {
                     conflicts.add("Course Conflict: Course '" + courseToEnroll.getCourseCode() + 
                                   "' conflicts with your scheduled Course '" + existingCourse.getCourseCode() + "' at " + slotToEnroll + ".");
                }
                
                boolean conflictCheck2 = existingCourse.getConflictingCourses().contains(courseToEnroll.getCourseCode());
                System.out.println("    Course Conflict Check 2 (" + existingCourse.getCourseCode() + " conflicts with " + courseToEnroll.getCourseCode() + "): " + conflictCheck2);
                if (conflictCheck2) {
                     conflicts.add("Course Conflict: Your scheduled Course '" + existingCourse.getCourseCode() + 
                                   "' conflicts with Course '" + courseToEnroll.getCourseCode() + "' at " + slotToEnroll + ".");
                }
            }
        }
        
        System.out.println("Student enrollment conflict check finished.");
        System.out.println("--- Finished Enrollment Conflict Check --- Finding " + conflicts.size() + " conflicts.");
        return conflicts;
    }
    
    // Helper to get scheduled entries for a student
    private List<TimetableEntry> getStudentScheduledEntries(Student student) {
         if (student == null || masterTimetable == null) {
            return new ArrayList<>();
        }
        Set<Section> enrolledSections = new HashSet<>(student.getEnrolledSections());
        return masterTimetable.getEntries().stream()
                .filter(entry -> entry.getSection() != null && enrolledSections.contains(entry.getSection()))
                .collect(Collectors.toList());
    }

    // Add methods later for student-specific checks if needed here,
    // or keep them closer to the student interaction point (e.g., CourseBrowserPanel logic).
} 