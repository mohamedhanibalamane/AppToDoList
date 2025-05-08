package model;

public class Task {
    private String description;
    private String state;
    private String dueDate;
    private String dueTime;

    public Task(String description, String state, String dueDate, String dueTime) {
        this.description = description;
        this.state = state;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
    }

}