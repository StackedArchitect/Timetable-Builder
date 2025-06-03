package com.timetablebuilder.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablebuilder.model.*; // Import all models

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities; // Added import
import java.time.DayOfWeek; // Added import for TimeSlot

public class PersistenceService {

    private static final String DATA_DIR = "data"; // Directory to store data files
    private static final String CLASSROOMS_FILE = "classrooms.json";
    private static final String INSTRUCTORS_FILE = "instructors.json";
    private static final String COURSES_FILE = "courses.json";
    private static final String SECTIONS_FILE = "sections.json";
    private static final String STUDENTS_FILE = "students.json";
    private static final String USERS_FILE = "users.json";
    private static final String TIMETABLE_FILE = "timetable.json"; // Added

    private final ObjectMapper objectMapper; // Jackson object mapper

    // In-memory lists to hold the loaded data
    private List<Classroom> classrooms;
    private List<Instructor> instructors;
    private List<Course> courses;
    private List<Section> sections;
    private List<Student> students;
    private List<User> users;
    private Timetable masterTimetable; // Added

    public PersistenceService() {
        this.objectMapper = new ObjectMapper();
        // Configure for pretty printing JSON output
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // TODO: Configure for Java Time API if models use it (e.g., LocalDate)
        // objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Load data on initialization
        loadAllData();
    }

    // --- Public Getters for Data Lists ---
    // Return unmodifiable lists to prevent direct external modification
    public List<Classroom> getClassrooms() {
        return Collections.unmodifiableList(classrooms);
    }

    public List<Instructor> getInstructors() {
        return Collections.unmodifiableList(instructors);
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
    
    // Getter for Timetable
    public Timetable getMasterTimetable() {
        return masterTimetable; // Return the actual instance (or a copy if immutability needed)
    }
    
    // --- Load Method ---
    public void loadAllData() {
        System.out.println("PersistenceService: Loading all data...");
        File dataDir = new File(DATA_DIR);
        System.out.println("Attempting to use data directory: " + dataDir.getAbsolutePath()); // Log absolute path

        if (!dataDir.exists()) {
            System.out.println("Data directory does not exist, creating: " + dataDir.getAbsolutePath());
            boolean created = dataDir.mkdirs();
            System.out.println("Directory creation status: " + created);
        } else {
             System.out.println("Data directory exists.");
        }

        // Load core data first
        classrooms = loadData(new File(dataDir, CLASSROOMS_FILE), new TypeReference<List<Classroom>>() {});
        instructors = loadData(new File(dataDir, INSTRUCTORS_FILE), new TypeReference<List<Instructor>>() {});
        courses = loadData(new File(dataDir, COURSES_FILE), new TypeReference<List<Course>>() {});
        sections = loadData(new File(dataDir, SECTIONS_FILE), new TypeReference<List<Section>>() {});
        students = loadData(new File(dataDir, STUDENTS_FILE), new TypeReference<List<Student>>() {});
        users = loadData(new File(dataDir, USERS_FILE), new TypeReference<List<User>>() {});

        // Load Timetable
        masterTimetable = loadSingleObjectData(new File(dataDir, TIMETABLE_FILE), new TypeReference<Timetable>() {});
        if (masterTimetable == null) {
            System.out.println("Timetable file not found or empty/invalid, initializing new Timetable.");
            masterTimetable = new Timetable();
        }

        // Link related objects after loading everything
        linkLoadedData();

        System.out.println("PersistenceService: Data loading complete.");
    }

    // Generic helper method to load a list from a JSON file
    private <T> List<T> loadData(File file, TypeReference<List<T>> typeReference) {
        System.out.println("--- Loading List: " + file.getName() + " ---");
        System.out.println("Attempting to load from absolute path: " + file.getAbsolutePath());
        List<T> loadedList = new ArrayList<>();
        if (file.exists()) {
            System.out.println("File exists.");
            if (!file.canRead()) {
                System.err.println("Error: Cannot read file! Check permissions: " + file.getAbsolutePath());
                return loadedList;
            }
            if (file.length() == 0) {
                 System.out.println("File is empty, initializing empty list.");
                 return loadedList;
            }
            
            try {
                // Log raw content first
                String rawJson = "Error reading raw content";
                try {
                     rawJson = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                     System.out.println("Raw JSON content for " + file.getName() + ":\n>>>\n" + rawJson + "\n<<<");
                } catch (IOException readEx) {
                     System.err.println("Failed to read raw JSON content: " + readEx.getMessage());
                     // Continue anyway, let Jackson try
                }

                System.out.println("Reading file content via Jackson...");
                if (!rawJson.equals("Error reading raw content")) {
                    loadedList = objectMapper.readValue(rawJson, typeReference);
                } else {
                    loadedList = objectMapper.readValue(file, typeReference);
                }
                System.out.println("Successfully parsed JSON. Loaded items: " + (loadedList != null ? loadedList.size() : "null list - error?"));
            } catch (IOException e) {
                System.err.println("FATAL Jackson Error loading list data from " + file.getAbsolutePath() + ". Exception type: " + e.getClass().getName());
                e.printStackTrace(); // Keep stack trace for debugging
                // Show user-friendly error dialog
                showErrorDialog("Error Loading Data", 
                                "Failed to load data from file: " + file.getName() + ".\n" + 
                                "The application might not function correctly.\n" + 
                                "Error details: " + e.getMessage());
                loadedList = new ArrayList<>(); // Return empty list on error
            }
        } else {
            System.out.println("File does not exist, initializing empty list: " + file.getAbsolutePath());
        }
        System.out.println("--- Finished Loading List: " + file.getName() + " ---");
        return loadedList;
    }

    // Generic helper method to load a single object from a JSON file
    private <T> T loadSingleObjectData(File file, TypeReference<T> typeReference) {
        System.out.println("--- Loading Object: " + file.getName() + " ---");
        System.out.println("Attempting to load from absolute path: " + file.getAbsolutePath());
         T loadedObject = null;
        if (file.exists()) {
             System.out.println("File exists.");
             if (!file.canRead()) {
                 System.err.println("Error: Cannot read file! Check permissions: " + file.getAbsolutePath());
                 return null;
             }
            if (file.length() == 0) {
                 System.out.println("File is empty, returning null object.");
                 return null;
            }
            
            try {
                 // Log raw content first
                 String rawJson = "Error reading raw content";
                 try {
                      rawJson = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                      System.out.println("Raw JSON content for " + file.getName() + ":\n>>>\n" + rawJson + "\n<<<");
                 } catch (IOException readEx) {
                      System.err.println("Failed to read raw JSON content: " + readEx.getMessage());
                      // Continue anyway, let Jackson try
                 }
                 
                 System.out.println("Reading file content via Jackson...");
                 if (!rawJson.equals("Error reading raw content")) {
                     loadedObject = objectMapper.readValue(rawJson, typeReference);
                 } else {
                     loadedObject = objectMapper.readValue(file, typeReference);
                 }
                System.out.println("Successfully parsed JSON. Loaded object is " + (loadedObject != null ? "not null" : "null"));
            } catch (IOException e) {
                System.err.println("FATAL Jackson Error loading object data from " + file.getAbsolutePath() + ". Exception type: " + e.getClass().getName());
                e.printStackTrace(); // Keep stack trace
                // Show user-friendly error dialog
                showErrorDialog("Error Loading Data", 
                                "Failed to load data from file: " + file.getName() + ".\n" + 
                                "Application defaults will be used if possible.\n" + 
                                "Error details: " + e.getMessage());
                loadedObject = null; // Return null on error
            }
        } else {
            System.out.println("File does not exist, returning null object: " + file.getAbsolutePath());
        }
        System.out.println("--- Finished Loading Object: " + file.getName() + " ---");
        return loadedObject;
    }

    // --- Save Method ---
    public synchronized void saveAllData() { // Synchronized to prevent concurrent writes
        System.out.println("PersistenceService: Saving all data...");
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        saveData(new File(dataDir, CLASSROOMS_FILE), classrooms);
        saveData(new File(dataDir, INSTRUCTORS_FILE), instructors);
        saveData(new File(dataDir, COURSES_FILE), courses);
        saveData(new File(dataDir, SECTIONS_FILE), sections);
        saveData(new File(dataDir, STUDENTS_FILE), students);
        saveData(new File(dataDir, USERS_FILE), users);
        saveSingleObjectData(new File(dataDir, TIMETABLE_FILE), masterTimetable); // Save timetable

        System.out.println("PersistenceService: Data saving complete.");
    }

    // Generic helper method to save a list to a JSON file
    private <T> void saveData(File file, List<T> dataList) {
        try {
            System.out.println("Saving data to: " + file.getPath());
            
            // Special handling for Students to ensure proper ID-based serialization
            if (file.getName().equals(STUDENTS_FILE) && !dataList.isEmpty() && dataList.get(0) instanceof Student) {
                List<Student> students = (List<Student>) dataList;
                // Create a simplified structure manually
                StringBuilder json = new StringBuilder("[\n");
                
                boolean first = true;
                for (Student student : students) {
                    if (!first) {
                        json.append(",\n");
                    }
                    first = false;
                    
                    // Manual JSON for each student using only IDs for sections
                    json.append("  {\n");
                    json.append("    \"studentID\" : \"").append(student.getStudentID()).append("\",\n");
                    json.append("    \"name\" : \"").append(student.getName()).append("\",\n");
                    
                    // Section IDs as an array of strings
                    json.append("    \"enrolledSections\" : [");
                    
                    boolean firstSection = true;
                    for (String sectionId : student.getEnrolledSectionIds()) {
                        if (!firstSection) {
                            json.append(", ");
                        }
                        firstSection = false;
                        json.append("\"").append(sectionId).append("\"");
                    }
                    
                    json.append("]\n  }");
                }
                
                json.append("\n]");
                
                // Write the manually built JSON
                java.nio.file.Files.writeString(file.toPath(), json.toString(), 
                                             java.nio.charset.StandardCharsets.UTF_8);
                
                System.out.println("Students saved with manual JSON formatting (ID-based references).");
                return; // Skip default serialization
            }
            
            // Default serialization for other types
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataList);
        } catch (IOException e) {
            System.err.println("Error saving data to " + file.getPath() + ": " + e.getMessage());
            System.err.println("Error saving single object data to " + file.getPath() + ": " + e.getMessage());
        }
    }

    // Generic helper method to save a single object to a JSON file
    private <T> void saveSingleObjectData(File file, T dataObject) {
        if (dataObject == null) {
            System.err.println("Warning: Attempted to save null object to " + file.getPath());
            return;
        }
        try {
            System.out.println("Saving single object data to: " + file.getPath());
            
            // Special handling for Timetable to ensure proper ID-based serialization
            if (dataObject instanceof Timetable && file.getName().equals(TIMETABLE_FILE)) {
                Timetable timetable = (Timetable) dataObject;
                // Create a simplified structure manually
                StringBuilder json = new StringBuilder("{\n  \"entries\" : [ ");
                
                boolean first = true;
                for (TimetableEntry entry : timetable.getEntries()) {
                    if (!first) {
                        json.append(", ");
                    }
                    first = false;
                    
                    // Manual JSON for each entry using only IDs for section/classroom
                    json.append("{\n");
                    json.append("    \"section\" : \"").append(entry.getSectionId()).append("\",\n");
                    json.append("    \"classroom\" : \"").append(entry.getClassroomId()).append("\",\n");
                    
                    // TimeSlot is a value object - include full details
                    TimeSlot ts = entry.getTimeSlot();
                    json.append("    \"timeSlot\" : {\n");
                    json.append("      \"dayOfWeek\" : \"").append(ts.getDayOfWeek()).append("\",\n");
                    json.append("      \"startHour\" : ").append(ts.getStartHour()).append(",\n");
                    json.append("      \"startMinute\" : ").append(ts.getStartMinute()).append(",\n");
                    json.append("      \"durationMinutes\" : ").append(ts.getDurationMinutes()).append("\n");
                    json.append("    }\n");
                    
                    json.append("  }");
                }
                
                json.append(" ]\n}");
                
                // Write the manually built JSON
                java.nio.file.Files.writeString(file.toPath(), json.toString(), 
                                               java.nio.charset.StandardCharsets.UTF_8);
                
                System.out.println("Timetable saved with manual JSON formatting (ID-based references).");
                return; // Skip default serialization for Timetable
            }
            
            // Default serialization for other object types
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataObject);
        } catch (IOException e) {
            System.err.println("Error saving single object data to " + file.getPath() + ": " + e.getMessage());
        }
    }

    // --- Data Linking ---
    private void linkLoadedData() {
        System.out.println("PersistenceService: Linking loaded data objects...");
        
        if (classrooms == null || instructors == null || courses == null || sections == null || students == null || users == null) {
             System.err.println("Error: Cannot link data because one or more core data lists are null.");
             return;
        }

        // Create maps for quick lookup by ID (still useful)
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCourseCode, c -> c, (c1, c2) -> c1));
        Map<String, Instructor> instructorMap = instructors.stream()
                .collect(Collectors.toMap(Instructor::getInstructorID, i -> i, (i1, i2) -> i1));
        Map<String, Classroom> classroomMap = classrooms.stream()
                .collect(Collectors.toMap(Classroom::getClassroomID, cr -> cr, (cr1, cr2) -> cr1));
        Map<String, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getStudentID, s -> s, (s1, s2) -> s1));
        Map<String, Section> sectionMap = sections.stream()
                .collect(Collectors.toMap(Section::getSectionID, s -> s, (s1, s2) -> s1));

        // Link Sections (ParentCourse, Instructor)
        System.out.println("Linking sections (ParentCourse, Instructor)...");
        
        for (Section section : sections) {
            linkSectionReferences(section, courseMap, instructorMap);
            
            // Use the new method to link enrolled students from IDs to objects
            section.linkEnrolledStudents(studentMap);
        }
        System.out.println("Section linking finished.");
        
        // Link Students (Verify enrolledSections)
        System.out.println("Linking student section enrollments...");
        
        for (Student student : students) {
            // Use the new method to link enrolled sections from IDs to objects
            student.linkEnrolledSections(sectionMap);
        }
        System.out.println("Student enrollment linking finished.");

        // Link Timetable Entries (Section, Classroom) - Manual linking still likely needed
        // as TimetableEntry constructor doesn't inherently link based on ID alone.
        if (masterTimetable != null && masterTimetable.getEntries() != null) {
             System.out.println("Linking timetable entries using IDs...");
             // IMPORTANT: We link the entries WITHIN the masterTimetable loaded by Jackson
             // (which now holds entries with IDs but null object references)
             // No need to reconstruct the whole list like before.

             int linkedCount = 0;
             int failedCount = 0;

             // Iterate through the entries loaded by Jackson (which have IDs now)
             for (TimetableEntry loadedEntry : masterTimetable.getEntries()) {
                 if (loadedEntry == null) {
                     System.err.println("Warning: Found null entry in loaded timetable list.");
                     failedCount++;
                     continue;
                 }
                 
                 // Get IDs from the loaded entry
                 String sectionId = loadedEntry.getSectionId();
                 String classroomId = loadedEntry.getClassroomId();
                 // TimeSlot is a value object, should be loaded correctly by Jackson
                 TimeSlot timeSlot = loadedEntry.getTimeSlot();

                 if (sectionId == null || classroomId == null || timeSlot == null) {
                      System.err.println("Warning: Timetable entry is missing required IDs or TimeSlot after load. Entry: SectionID=" + sectionId + ", ClassroomID=" + classroomId);
                      failedCount++;
                      continue;
                 }

                 // Find the actual objects using the maps
                 Section mappedSection = sectionMap.get(sectionId);
                 Classroom mappedClassroom = classroomMap.get(classroomId);

                 // If objects found, link them into the entry
                 if (mappedSection != null && mappedClassroom != null) {
                     loadedEntry.linkSection(mappedSection);
                     loadedEntry.linkClassroom(mappedClassroom);
                     linkedCount++;
                     // Now the loadedEntry within masterTimetable has its references set
                 } else {
                     failedCount++;
                     if (mappedSection == null) {
                          System.err.println("Warning: Failed to link timetable entry. Could not find Section with ID: " + sectionId);
                     }
                     if (mappedClassroom == null) {
                          System.err.println("Warning: Failed to link timetable entry. Could not find Classroom with ID: " + classroomId);
                     }
                     // Optionally: remove the entry from masterTimetable if linking fails?
                     // masterTimetable.removeEntry(loadedEntry); // Careful with concurrent modification
                 }
             }
             System.out.println("Timetable entry linking finished. Linked: " + linkedCount + ", Failed/Skipped: " + failedCount);
             
             // If there were failures, it might be wise to remove those entries now
             if (failedCount > 0) {
                 System.out.println("Removing " + failedCount + " timetable entries that failed to link...");
                 try {
                     // Create a new list without failed entries instead of modifying the existing list
                     List<TimetableEntry> validEntries = new ArrayList<>();
                     for (TimetableEntry entry : masterTimetable.getEntries()) {
                         if (entry.getSection() != null && entry.getClassroom() != null) {
                             validEntries.add(entry);
                         }
                     }
                     
                     // Create a new timetable with only the valid entries
                     Timetable newTimetable = new Timetable();
                     for (TimetableEntry entry : validEntries) {
                         newTimetable.addEntry(entry);
                     }
                     
                     // Replace the masterTimetable reference
                     this.masterTimetable = newTimetable;
                     
                     System.out.println("Successfully recreated timetable with " + validEntries.size() + " valid entries");
                 } catch (Exception e) {
                     System.err.println("Error while removing failed timetable entries: " + e.getMessage());
                     e.printStackTrace();
                 }
             }
             
        } else {
             System.out.println("No timetable or entries to link.");
        }
        
        System.out.println("PersistenceService: Data linking finished.");
    }
    
    // Renamed helper for clarity
    private void linkSectionReferences(Section section, Map<String, Course> courseMap, Map<String, Instructor> instructorMap) {
        if (section == null) return;

        // Link Parent Course using the stored ID
        String courseCode = section.getParentCourseCode();
        if (courseCode != null) {
            Course mappedCourse = courseMap.get(courseCode);
            if (mappedCourse != null) {
                section.linkParentCourse(mappedCourse); // Use the new setter
            } else {
                 System.err.println("Warning: Section " + section.getSectionID() + " refers to unknown Parent Course ID: " + courseCode);
            }
        } else {
             System.err.println("Warning: Section " + section.getSectionID() + " has null Parent Course Code after load.");
        }

        // Link Instructor using the stored ID
        String instructorId = section.getInstructorId();
        if (instructorId != null) {
            Instructor mappedInstructor = instructorMap.get(instructorId);
            if (mappedInstructor != null) {
                section.linkInstructor(mappedInstructor); // Use the new setter
            } else {
                 System.err.println("Warning: Section " + section.getSectionID() + " refers to unknown Instructor ID: " + instructorId);
                 // Keep instructor null in the Section object
            }
        } else {
             System.err.println("Warning: Section " + section.getSectionID() + " has null Instructor ID after load.");
        }
        
        // Link enrolled students (if needed - assuming Student uses ID reference correctly)
        // ... (existing student linking logic, if any, remains the same) ...
    }

    // --- Modification Methods --- 
    // Operate on in-memory lists. Call saveAllData() separately to persist.
    
    // Classroom Methods (already present)
    public void addClassroom(Classroom classroom) {
        if (classroom != null && !classrooms.contains(classroom)) {
            classrooms.add(classroom); // Add to the internal list
            System.out.println("Added classroom to PersistenceService list: " + classroom.getClassroomID());
            // Consider immediate save or rely on periodic/shutdown save
            // saveAllData(); 
        }
    }

    public void updateClassroom(Classroom classroom) {
        if (classroom == null) return;
        // Find existing and replace
        for (int i = 0; i < classrooms.size(); i++) {
            if (classrooms.get(i).getClassroomID().equals(classroom.getClassroomID())) {
                classrooms.set(i, classroom); // Replace in the internal list
                System.out.println("Updated classroom in PersistenceService list: " + classroom.getClassroomID());
                // saveAllData();
                return;
            }
        }
         // If not found, maybe add it?
         // addClassroom(classroom);
    }

    public void deleteClassroom(String classroomId) {
        boolean removed = classrooms.removeIf(c -> c.getClassroomID().equals(classroomId));
        if (removed) {
            System.out.println("Deleted classroom from PersistenceService list: " + classroomId);
            // saveAllData();
        }
    }

    // Instructor Methods
    public void addInstructor(Instructor instructor) {
        if (instructor != null && instructors.stream().noneMatch(i -> i.getInstructorID().equals(instructor.getInstructorID()))) {
            instructors.add(instructor);
            System.out.println("Added instructor: " + instructor.getInstructorID());
        }
    }
    public void updateInstructor(Instructor instructor) {
        if (instructor == null) return;
        for (int i = 0; i < instructors.size(); i++) {
            if (instructors.get(i).getInstructorID().equals(instructor.getInstructorID())) {
                instructors.set(i, instructor);
                System.out.println("Updated instructor: " + instructor.getInstructorID());
                return;
            }
        }
    }
    public void deleteInstructor(String instructorId) {
        boolean removed = instructors.removeIf(i -> i.getInstructorID().equals(instructorId));
        if (removed) {
            System.out.println("Deleted instructor: " + instructorId);
            // TODO: Check if instructor is assigned to sections and handle/warn?
        }
    }

    // Course Methods
    public void addCourse(Course course) {
        if (course != null && courses.stream().noneMatch(c -> c.getCourseCode().equals(course.getCourseCode()))) {
            courses.add(course);
            System.out.println("Added course: " + course.getCourseCode());
        }
    }
    public void updateCourse(Course course) {
        if (course == null) return;
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseCode().equals(course.getCourseCode())) {
                courses.set(i, course);
                System.out.println("Updated course: " + course.getCourseCode());
                return;
            }
        }
    }
    public void deleteCourse(String courseCode) {
        boolean removed = courses.removeIf(c -> c.getCourseCode().equals(courseCode));
        if (removed) {
            System.out.println("Deleted course: " + courseCode);
            // TODO: Check if course has sections and handle/warn?
        }
    }

    // Section Methods
    public void addSection(Section section) {
        if (section != null && sections.stream().noneMatch(s -> s.getSectionID().equals(section.getSectionID()))) {
            // Link references before adding
            linkSectionReferences(section, 
                courses.stream().collect(Collectors.toMap(Course::getCourseCode, c -> c)),
                instructors.stream().collect(Collectors.toMap(Instructor::getInstructorID, i -> i))
            );
            sections.add(section);
            System.out.println("Added section: " + section.getSectionID());
        }
    }
    public void updateSection(Section section) {
        if (section == null) return;
        // Link references before updating
        linkSectionReferences(section, 
                courses.stream().collect(Collectors.toMap(Course::getCourseCode, c -> c)),
                instructors.stream().collect(Collectors.toMap(Instructor::getInstructorID, i -> i))
            ); 
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getSectionID().equals(section.getSectionID())) {
                sections.set(i, section);
                System.out.println("Updated section: " + section.getSectionID());
                return;
            }
        }
    }
    public void deleteSection(String sectionId) {
        boolean removed = sections.removeIf(s -> s.getSectionID().equals(sectionId));
        if (removed) {
            System.out.println("Deleted section: " + sectionId);
        }
    }

    // Student Methods
    public void addStudent(Student student) {
        if (student != null && students.stream().noneMatch(s -> s.getStudentID().equals(student.getStudentID()))) {
            students.add(student);
            System.out.println("Added student: " + student.getStudentID());
        }
    }
    public void updateStudent(Student student) {
        if (student == null) return;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentID().equals(student.getStudentID())) {
                students.set(i, student);
                System.out.println("Updated student: " + student.getStudentID());
                return;
            }
        }
    }
    public void deleteStudent(String studentId) {
        // Find the student object first to remove from sections
        Student studentToRemove = students.stream()
            .filter(s -> s.getStudentID().equals(studentId))
            .findFirst()
            .orElse(null);
            
        if (studentToRemove == null) {
            System.err.println("Attempted to delete non-existent student ID: " + studentId);
            return; // Student not found
        }
        
        boolean removed = students.removeIf(s -> s.getStudentID().equals(studentId));
        if (removed) {
            System.out.println("Deleted student: " + studentId);
            
            // Remove student from enrollment in all sections
            System.out.println("Removing deleted student " + studentId + " from section enrollments...");
            int sectionsUpdated = 0;
            if (sections != null) {
                for (Section section : sections) {
                    if (section.removeStudent(studentToRemove)) {
                         sectionsUpdated++;
                    }
                }
            }
            System.out.println("Removed student from " + sectionsUpdated + " sections.");
            
            // TODO: Consider saving data immediately after such a cascading change?
            // saveAllData(); 
        }
    }

    // User Methods
    public void addUser(User user) {
        if (user != null && users.stream().noneMatch(u -> u.getUsername().equals(user.getUsername()))) {
            users.add(user);
            System.out.println("Added user: " + user.getUsername());
        }
    }
    public void updateUser(User user) {
        if (user == null) return;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(user.getUsername())) {
                users.set(i, user); // Replace user (handles password update etc.)
                System.out.println("Updated user: " + user.getUsername());
                return;
            }
        }
    }
    public void deleteUser(String username) {
        // Prevent deleting default admin?
        if ("admin".equals(username)) {
            System.err.println("Cannot delete default admin user.");
            return;
        }
        boolean removed = users.removeIf(u -> u.getUsername().equals(username));
        if (removed) {
            System.out.println("Deleted user: " + username);
        }
    }

    // Helper method to show error dialog (avoids repeating code)
    private void showErrorDialog(String title, String message) {
        // Ensure dialog is shown on the Event Dispatch Thread if called from non-UI thread
        // Although current load happens before UI, this is good practice.
        if (SwingUtilities.isEventDispatchThread()) {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        } else {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
            );
        }
    }

}