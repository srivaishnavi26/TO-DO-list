import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;

class Task implements Comparable<Task> {
    String description;
    boolean isCompleted;
    Date dueDate;

    Task(String description, Date dueDate) {
        this.description = description;
        this.isCompleted = false;
        this.dueDate = dueDate;
    }

    @Override
    public int compareTo(Task other) {
        if (this.dueDate == null && other.dueDate == null) {
            return 0;
        } else if (this.dueDate == null) {
            return 1;
        } else if (other.dueDate == null) {
            return -1;
        }
        return this.dueDate.compareTo(other.dueDate);
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return (isCompleted ? "[✓] " : "[ ] ") + description + 
               (dueDate != null ? " (Due: " + dateFormat.format(dueDate) + ")" : "");
    }
}

public class ToDoListApp extends JFrame {
    private DefaultListModel<Task> tasksListModel;
    private JList<Task> tasksList;
    private PriorityQueue<Task> taskQueue;
    private HashMap<String, Task> taskMap;

    public ToDoListApp() {
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        tasksListModel = new DefaultListModel<>();
        tasksList = new JList<>(tasksListModel);
        taskQueue = new PriorityQueue<>();
        taskMap = new HashMap<>();

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newTaskDescription = JOptionPane.showInputDialog(null, "Enter a new task:");
                if (newTaskDescription != null && !newTaskDescription.trim().isEmpty()) {
                    String dateInput = JOptionPane.showInputDialog(null, "Enter due date (yyyy-MM-dd):");
                    Date dueDate = null;
                    if (dateInput != null && !dateInput.trim().isEmpty()) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            dueDate = dateFormat.parse(dateInput);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid date format. Task added without a due date.");
                        }
                    }
                    Task newTask = new Task(newTaskDescription.trim(), dueDate);
                    tasksListModel.addElement(newTask);
                    taskQueue.add(newTask);
                    taskMap.put(newTaskDescription.trim(), newTask);
                    JOptionPane.showMessageDialog(null, "Task added successfully!");
                }
            }
        });

        JButton removeButton = new JButton("Remove Task");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tasksList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Task taskToRemove = tasksListModel.get(selectedIndex);
                    tasksListModel.remove(selectedIndex);
                    taskQueue.remove(taskToRemove);
                    taskMap.remove(taskToRemove.description);
                    JOptionPane.showMessageDialog(null, "Task removed successfully!");
                }
            }
        });

        JButton completeButton = new JButton("Complete Task");
        completeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tasksList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Task task = tasksListModel.get(selectedIndex);
                    task.isCompleted = !task.isCompleted; // Toggle completion
                    tasksListModel.set(selectedIndex, task); // Refresh the display
                }
            }
        });

        JButton clearButton = new JButton("Clear All Tasks");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tasksListModel.clear();
                taskQueue.clear();
                taskMap.clear();
                JOptionPane.showMessageDialog(null, "All tasks cleared!");
            }
        });

        JButton saveButton = new JButton("Save Tasks");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTasks();
                JOptionPane.showMessageDialog(null, "Tasks saved to file.");
            }
        });

        JButton sortButton = new JButton("Sort by Due Date");
        sortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortTasksByDueDate();
            }
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1));
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(completeButton);
        buttonsPanel.add(clearButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(sortButton);

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
        File file = new File("tasks.txt");
        if (!file.exists()) {
            return; // Exit if the file does not exist
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String taskLine;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while ((taskLine = reader.readLine()) != null) {
                String[] parts = taskLine.split(" \\(Due: |\\)");
                boolean completed = parts[0].startsWith("[✓]");
                String description = completed ? parts[0].substring(4) : parts[0].substring(4);
                Date dueDate = null;
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    dueDate = dateFormat.parse(parts[1]);
                }
                Task task = new Task(description.trim(), dueDate);
                task.isCompleted = completed;
                tasksListModel.addElement(task);
                taskQueue.add(task);
                taskMap.put(description.trim(), task);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (int i = 0; i < tasksListModel.size(); i++) {
                Task task = tasksListModel.getElementAt(i);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dueDateStr = (task.dueDate != null) ? " (Due: " + dateFormat.format(task.dueDate) + ")" : "";
                writer.write((task.isCompleted ? "[✓] " : "[ ] ") + task.description + dueDateStr + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortTasksByDueDate() {
        java.util.List<Task> taskList = Collections.list(tasksListModel.elements());
        taskList.sort(Task::compareTo);
        tasksListModel.clear();
        for (Task task : taskList) {
            tasksListModel.addElement(task);
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
