import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ToDoListApp extends JFrame {
    private DefaultListModel<String> tasksListModel;
    private JList<String> tasksList;

    public ToDoListApp() {
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setLocationRelativeTo(null);

        tasksListModel = new DefaultListModel<>();
        tasksList = new JList<>(tasksListModel);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newTask = JOptionPane.showInputDialog(null, "Enter a new task:");
                if (newTask != null && !newTask.isEmpty()) {
                    tasksListModel.addElement(newTask);
                }
            }
        });

        JButton removeButton = new JButton("Remove Task");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tasksList.getSelectedIndex();
                if (selectedIndex != -1) {
                    tasksListModel.remove(selectedIndex);
                }
            }
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1));
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);

        Container contentPane = getContentPane();
        contentPane.add(new JScrollPane(tasksList), BorderLayout.CENTER);
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        loadTasks(); // Load tasks from file on startup

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks(); // Save tasks to file before closing
            }
        });
    }

    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
            String task;
            while ((task = reader.readLine()) != null) {
                tasksListModel.addElement(task);
            }
        } catch (IOException e) {
            // Handle file read error
            e.printStackTrace();
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (int i = 0; i < tasksListModel.size(); i++) {
                writer.write(tasksListModel.getElementAt(i) + "\n");
            }
        } catch (IOException e) {
            // Handle file write error
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToDoListApp().setVisible(true);
            }
        });
    }
}
