package com.timetablebuilder.ui.renderer;

import com.timetablebuilder.model.ComponentType;
import com.timetablebuilder.model.TimetableEntry;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

// Custom renderer for displaying TimetableEntry objects in a JTable cell
public class TimetableCellRenderer extends JTextArea implements TableCellRenderer {

    // Define some colors for different section types (optional)
    private static final Color LECTURE_COLOR = new Color(200, 220, 255); // Light blue
    private static final Color LAB_COLOR = new Color(255, 220, 200);     // Light orange
    private static final Color TUTORIAL_COLOR = new Color(210, 255, 210); // Light green
    private static final Color DEFAULT_COLOR = UIManager.getColor("Table.background");
    private static final Color CONFLICT_COLOR = Color.PINK; // Example for conflict highlighting (if needed later)

    public TimetableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true); // Make sure background color is painted
        setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); // Match table border style
        setFont(UIManager.getFont("Table.font"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row,
                                               int column) {

        if (value instanceof TimetableEntry) {
            TimetableEntry entry = (TimetableEntry) value;
            // Format the text - Use HTML for simple multi-line
            setText(String.format("<html>%s (%s)<br>%s<br>%s</html>",
                    entry.getSection().getParentCourse().getCourseCode(),
                    entry.getSection().getType(),       // Use getType() for Section Type (L, P, T)
                    entry.getSection().getInstructor().getInstructorID(),
                    entry.getClassroom().getClassroomID()
            ));

            // Set background based on section type
            ComponentType type = entry.getSection().getType(); // Use getType()
            if (type == ComponentType.LECTURE) {
                setBackground(LECTURE_COLOR);
            } else if (type == ComponentType.LAB) {
                setBackground(LAB_COLOR);
            } else if (type == ComponentType.TUTORIAL) {
                setBackground(TUTORIAL_COLOR);
            } else {
                setBackground(DEFAULT_COLOR);
            }
            setForeground(UIManager.getColor("Table.foreground")); // Default text color

            // TODO: Add tooltip for more details?
            // setToolTipText("Section: " + entry.getSection().getSectionID() + ", Instructor: " + entry.getSection().getInstructor().getName());

        } else {
            // Handle empty cells or non-TimetableEntry values
            setText("");
            setBackground(DEFAULT_COLOR);
            setForeground(UIManager.getColor("Table.foreground"));
            // setToolTipText(null);
        }

        // Handle selection highlighting
        if (isSelected) {
            setBackground(UIManager.getColor("Table.selectionBackground"));
            setForeground(UIManager.getColor("Table.selectionForeground"));
        }

        return this;
    }
} 