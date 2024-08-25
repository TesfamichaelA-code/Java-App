package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class NoteApp extends JFrame {
    private int userId;
    private JPanel notesPanel;
    private JTextField searchField;

    public NoteApp(int userId) {
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Note Application");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Top Panel with New Note Button, Search Field, and User Icon
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(60, 63, 65));

        // New Note Button
        JButton newNoteButton = new JButton("+");
        newNoteButton.setFont(new Font("San-serif", Font.BOLD, 24));
        newNoteButton.setBackground(new Color(60, 63, 65));
        newNoteButton.setForeground(Color.WHITE);
        newNoteButton.setFocusPainted(false);
        newNoteButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        newNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewNoteDialog();
            }
        });
        topPanel.add(newNoteButton, BorderLayout.WEST);

        // Search Field
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createTitledBorder("Search Notes"));
        topPanel.add(searchField, BorderLayout.CENTER);

        // Search Button
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(60, 63, 65));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                searchNotes(query);
            }
        });
        topPanel.add(searchButton, BorderLayout.EAST);

        // User Panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(new Color(60, 63, 65));

        JLabel userIcon = new JLabel(new ImageIcon("user_icon.png"));
        JLabel usernameLabel = new JLabel("USER");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
        	     PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
        	    ps.setInt(1, userId);
        	    ResultSet rs = ps.executeQuery();

        	    if (rs.next()) {
        	        String username = rs.getString("username");
        	        usernameLabel.setText(username);
        	    }
        	} catch (SQLException ex) {
        	    ex.printStackTrace();
        	    JOptionPane.showMessageDialog(null, "Error retrieving username.");
        	}
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        usernameLabel.setForeground(Color.WHITE);

        userPanel.add(userIcon);
        userPanel.add(usernameLabel);
        userPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to log out?", "Logout",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        topPanel.add(userPanel, BorderLayout.NORTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel to display notes
        notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(notesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        loadNotes();
        setVisible(true);
    }

    private void openNewNoteDialog() {
        JDialog newNoteDialog = new JDialog(NoteApp.this, "New Note", true);
        newNoteDialog.setSize(400, 300);
        newNoteDialog.setLocationRelativeTo(NoteApp.this);
        newNoteDialog.setLayout(new BorderLayout());

        JTextField titleField = new JTextField();
        titleField.setBorder(BorderFactory.createTitledBorder("Title"));
        newNoteDialog.add(titleField, BorderLayout.NORTH);

        JTextArea noteArea = new JTextArea();
        noteArea.setBorder(BorderFactory.createTitledBorder("Note Content"));
        newNoteDialog.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Note");
        saveButton.setBackground(new Color(60, 63, 65));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String content = noteArea.getText();
                
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO notes (user_id, title, content) VALUES (?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, title);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Note saved successfully.");
                    loadNotes();
                    newNoteDialog.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error saving note.");
                }
            }
        });
        newNoteDialog.add(saveButton, BorderLayout.SOUTH);

        newNoteDialog.setVisible(true);
    }

    private void loadNotes() {
        notesPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
             PreparedStatement ps = conn.prepareStatement("SELECT id, title, content FROM notes WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int noteId = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");

                JPanel notePanel = createNotePanel(noteId, title, content);
                notesPanel.add(notePanel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading notes.");
        }
        notesPanel.revalidate();
        notesPanel.repaint();
    }

    private void searchNotes(String query) {
        notesPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
             PreparedStatement ps = conn.prepareStatement("SELECT id, title, content FROM notes WHERE user_id = ? AND title LIKE ?")) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int noteId = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");

                JPanel notePanel = createNotePanel(noteId, title, content);
                notesPanel.add(notePanel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error searching notes.");
        }
        notesPanel.revalidate();
        notesPanel.repaint();
    }

    private JPanel createNotePanel(int noteId, String title, String content) {
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65), 1));
        notePanel.setBackground(Color.WHITE);
        notePanel.setPreferredSize(new Dimension(450, 150));
        notePanel.setMaximumSize(new Dimension(450, 150));
        notePanel.setMinimumSize(new Dimension(450, 150));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        notePanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setForeground(Color.black);
        contentArea.setFont(new Font("san-serif", Font.PLAIN,14));
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        notePanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton editButton = new JButton("Edit");
        editButton.setBackground(new Color(60, 63, 65));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                openEditNoteDialog(noteId, title, content);
            }
        });
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(60, 63, 65));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this note?", "Delete Note",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    deleteNote(noteId);
                }
            }
        });
        buttonPanel.add(deleteButton);

        notePanel.add(buttonPanel, BorderLayout.SOUTH);

        return notePanel;
    }

    private void openEditNoteDialog(int noteId, String title, String content) {
        JDialog editNoteDialog = new JDialog(NoteApp.this, "Edit Note", true);
        editNoteDialog.setSize(400, 300);
        editNoteDialog.setLocationRelativeTo(NoteApp.this);
        editNoteDialog.setLayout(new BorderLayout());

        JTextField titleField = new JTextField(title);
        titleField.setBorder(BorderFactory.createTitledBorder("Title"));
        editNoteDialog.add(titleField, BorderLayout.NORTH);

        JTextArea noteArea = new JTextArea(content);
        noteArea.setBorder(BorderFactory.createTitledBorder("Note Content"));
        editNoteDialog.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(60, 63, 65));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newTitle = titleField.getText();
                String newContent = noteArea.getText();

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
                     PreparedStatement ps = conn.prepareStatement("UPDATE notes SET title = ?, content = ? WHERE id = ? AND user_id = ?")) {
                    ps.setString(1, newTitle);
                    ps.setString(2, newContent);
                    ps.setInt(3, noteId);
                    ps.setInt(4, userId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Note updated successfully.");
                    loadNotes();
                    editNoteDialog.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error updating note.");
                }
            }
        });
        editNoteDialog.add(saveButton, BorderLayout.SOUTH);

        editNoteDialog.setVisible(true);
    }

    private void deleteNote(int noteId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/OOPPROJECT", "root", "Medhanealem.27.t");
             PreparedStatement ps = conn.prepareStatement("DELETE FROM notes WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, noteId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Note deleted successfully.");
            loadNotes();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting note.");
        }
    }

    public static void main(String[] args) {
        // Replace with actual user ID for testing
        int userId = 1;
        new NoteApp(userId);
    }
}