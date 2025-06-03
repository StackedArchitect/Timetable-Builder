# Timetable Builder Application

A comprehensive Java Swing application for educational institutions to manage, schedule, and view timetables for courses.

## System Requirements

- Java Development Kit (JDK) 17 or newer
- Maven 3.6.0 or newer
- Windows, macOS, or Linux operating system with GUI support

## Project Architecture

### File Structure

```
TT/
├── pom.xml
├── Readme.md
├── data/
│   ├── classrooms.json
│   ├── courses.json
│   ├── instructors.json
│   ├── sections.json
│   ├── students.json
│   ├── timetable.json
│   └── users.json
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── timetablebuilder/
│                   ├── Main.java
│                   ├── model/
│                   │   ├── Classroom.java
│                   │   ├── ComponentType.java
│                   │   ├── Course.java
│                   │   ├── Instructor.java
│                   │   ├── Section.java
│                   │   ├── Student.java
│                   │   ├── TimeSlot.java
│                   │   └── ...
│                   ├── service/
│                   │   ├── AuthService.java
│                   │   ├── DataRepository.java
│                   │   ├── PersistenceService.java
│                   │   ├── TimetableGenerator.java
│                   │   └── TimetableService.java
│                   ├── solver/
│                   │   └── TimetableSolver.java
│                   ├── ui/
│                   │   ├── AutoTimetablePanel.java
│                   │   ├── CourseBrowserPanel.java
│                   │   ├── DataInputPanel.java
│                   │   ├── LoginDialog.java
│                   │   ├── MainFrame.java
│                   │   ├── ManualTimetablePanel.java
│                   │   ├── StudentTimetablePanel.java
│                   │   ├── TeacherTimetablePanel.java
│                   │   └── ...
│                   └── util/
│                       └── PasswordUtils.java
└── target/
    ├── TimetableBuilder-1.0-SNAPSHOT.jar
    └── classes/
        └── com/
            └── timetablebuilder/
                └── ...
```

### System Design Overview

- **UI Layer (`ui/`)**: Java Swing panels, dialogs, and frames for all user roles (Admin, Teacher, Student). Handles user input and displays data.
- **Model Layer (`model/`)**: Java classes representing core entities (Classroom, Course, Instructor, Section, Student, TimeSlot, etc.).
- **Service Layer (`service/`)**: Contains business logic, authentication, data management, and timetable generation. Key classes:
  - `AuthService`: Handles user authentication and session management.
  - `PersistenceService`: Loads and saves data to JSON files.
  - `TimetableService` & `TimetableGenerator`: Manage and generate timetables.
- **Solver Layer (`solver/`)**: Implements algorithms for automatic timetable generation and conflict resolution.
- **Utility Layer (`util/`)**: Helper classes for tasks like password hashing.

**Data Flow:**

1. User interacts with the UI (e.g., logs in, manages data, views timetables).
2. UI calls service layer methods to perform actions.
3. Service layer updates models and persists changes using `PersistenceService`.
4. Data is loaded from and saved to JSON files in the `data/` directory.

## Data Architecture

- All persistent data is stored as JSON files in the `data/` directory.
- Each file corresponds to a specific entity or relationship:
  - `classrooms.json`: Classroom details (id, name, capacity, type, etc.)
  - `courses.json`: Course information (code, name, credits, etc.)
  - `instructors.json`: Instructor profiles and assignments.
  - `sections.json`: Course sections, including schedule, assigned instructor, and classroom.
  - `students.json`: Student records, enrollments, and credentials.
  - `users.json`: User login credentials and roles (admin, teacher, student).
  - `timetable.json`: The master timetable, mapping sections to time slots and classrooms.

**Persistence Mechanism:**

- On startup, the application loads all JSON files into memory.
- Any changes made via the UI (add/edit/delete) are immediately saved back to the corresponding JSON file.
- The `PersistenceService` abstracts file I/O, ensuring data consistency and integrity.

**Example Data Structure (section of `sections.json`):**

```json
[
  {
    "id": "SEC101",
    "courseId": "CSE101",
    "instructorId": "INST01",
    "classroomId": "CR01",
    "timeSlot": {
      "day": "Monday",
      "startTime": "09:00",
      "endTime": "10:00"
    },
    "enrolledStudents": ["STU01", "STU02"]
  }
]
```

**System Diagram:**

```
[User]
   │
   ▼
[UI Layer] ──calls──> [Service Layer] ──manages──> [Model Layer]
   │                                   │
   │                                   ▼
   └──────────reads/writes────────> [PersistenceService] ──JSON──> [data/]
```

## Setup Instructions

### 1. Install Java Development Kit (JDK)

If you don't have JDK installed:

1. Download JDK from [Oracle's website](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK.
2. Install the JDK by following the installation wizard.
3. Verify installation by opening a terminal or command prompt and running:

   java -version

### 2. Install Maven

If you don't have Maven installed:

1. Download Maven from [Apache Maven's website](https://maven.apache.org/download.cgi).
2. Extract the archive to a location of your choice.
3. Add Maven's bin directory to your system's PATH variable.
4. Verify installation by running:

   mvn -version

### 3. Download the zip

download and extract the ZIP file from the drive.

### 4. Build the Project

Navigate to the project root directory and run:

mvn clean install

This will download all dependencies specified in the pom.xml file and build the project.

### 5. Run the Application

There are several ways to run the application:

#### Using terminal

Just run the Main.java
using VScode or any other IDE as java application.

#### Using the JAR file

If the project is built with Maven, run the compiled JAR file:

java -jar target/TimetableBuilder-1.0-SNAPSHOT.jar

#### Using an IDE

1. Open the project in your preferred IDE.
2. Ensure that all Maven dependencies are properly imported
3. Find the Main class (com.timetablebuilder.Main)
4. Right-click on the class and select "Run" or "Run As > Java Application"

#### Direct Java command (if all dependencies are in classpath)

If you have all the required dependencies (including Jackson libraries) in your classpath:

1. Compile the project:

   javac -cp "/path/to/dependencies/_" -d target/classes src/main/java/com/timetablebuilder/\*\*/_.java

2. Run the compiled class:

   java -cp "target/classes:/path/to/dependencies/\*" com.timetablebuilder.Main

For Windows users (Command Prompt):
cmd
javac -cp "path\to\dependencies\*" -d target\classes src\main\java\com\timetablebuilder\*\*\*.java
java -cp "target\classes;path\to\dependencies\*" com.timetablebuilder.Main

## Using the Application

The application supports three user roles, each with different permissions and views:

### Admin Role

_Login Credentials:_

- Username: admin1
- Password: admin123

_Capabilities:_

1. _Data Management_

   - Manage classrooms, instructors, courses, sections, and students
   - Add, edit, or remove any data entity
   - View all data in tabular format

2. _Manual Timetable Management_

   - View the master timetable
   - Schedule sections by selecting from unscheduled sections
   - Assign classrooms and time slots
   - Remove scheduled sections

3. _Automatic Timetable Generation_
   - Generate timetables automatically based on constraints
   - Preview generated timetables before applying
   - Apply generated schedules to the master timetable

_Instructions for Common Tasks:_

- _Adding a Classroom_: Go to Data Management tab → Select Classroom panel → Click "Add" → Fill in details → Submit
- _Scheduling a Section_: Go to Manual Timetable tab → Select a section from the unscheduled list → Select a classroom → Click on a time slot in the grid → Click "Assign"
- _Generating Timetable_: Go to Auto Timetable tab → Set parameters → Click "Generate" → Review → Apply to master

### Teacher Role

_Login Credentials:_

- Username: ajith (or a specific instructor ID)
- Password: teacher123

_Capabilities:_

1. _View Personal Timetable_

   - See all sections assigned to the logged-in instructor
   - View by day and time slot

2. _View Master Timetable_
   - Read-only view of all scheduled sections

_Instructions for Common Tasks:_

- _Viewing Your Schedule_: After login, the personal timetable is displayed automatically
- _Finding a Specific Class_: Use the search functionality (if available) or locate the cell in the timetable grid

### Student Role

_Login Credentials:_

- Username: vardhan (or a specific student ID)
- Password: student123

_Capabilities:_

1. _View Personal Timetable_

   - See all sections the student is enrolled in
   - View by day and time slot

2. _Course Registration_
   - Browse available courses and sections
   - Register for courses
   - Drop enrolled courses

_Instructions for Common Tasks:_

- _Enrolling in a Course_: Go to Course Registration tab → Browse available sections → Select a section → Click "Enroll"
- _Dropping a Course_: Go to Course Registration tab → View enrolled sections → Select a section → Click "Drop"
- _Viewing Your Schedule_: Go to My Timetable tab to see your personal schedule

## Application Workflow

1. _Admin Setup_:

   - Add classrooms, instructors, courses, sections, and students
   - Schedule sections in the timetable
   - Save all changes

2. _Instructor Usage_:

   - Log in to view assigned teaching schedule
   - Plan accordingly

3. _Student Registration_:
   - Log in to view available courses
   - Register for courses
   - View personal timetable

## Data Persistence

All data is saved in JSON format in the data directory:

- classrooms.json
- instructors.json
- courses.json
- sections.json
- students.json
- users.json
- timetable.json

The application automatically loads data from these files on startup and saves changes when operations are performed.

## Troubleshooting

- _Application won't start_: Ensure Java is installed and properly configured
- _Missing dependencies_: Run mvn clean install to download required libraries
- _Data not loading_: Check if the data directory exists and contains valid JSON files