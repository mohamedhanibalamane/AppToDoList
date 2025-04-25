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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }
}