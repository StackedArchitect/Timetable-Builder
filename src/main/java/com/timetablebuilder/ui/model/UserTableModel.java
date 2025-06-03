package com.timetablebuilder.ui.model;

import com.timetablebuilder.model.User;
import com.timetablebuilder.model.UserRole;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class UserTableModel extends AbstractTableModel {

    // Don't display password hash in the table!
    private final String[] columnNames = {"Username", "Role", "Associated ID"};
    private List<User> users;

    public UserTableModel() {
        this.users = new ArrayList<>();
    }

    public UserTableModel(List<User> users) {
        this.users = new ArrayList<>(users); // Use a copy
    }

    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        fireTableDataChanged();
    }

    public User getUserAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < users.size()) {
            return users.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return String.class;   // Username
            case 1: return UserRole.class; // Role (Enum)
            case 2: return String.class;   // Associated ID
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        switch (columnIndex) {
            case 0: return user.getUsername();
            case 1: return user.getRole();
            case 2: return user.getUserID();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Edited via dialog
    }
} 