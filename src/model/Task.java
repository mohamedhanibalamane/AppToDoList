package model;

public class Task {
    private String title;
    private String paragraph;
    private String state;
    private String startDate;
    private String startTime;
    private String dueDate;
    private String dueTime;
    private String type;
    private String priority;

    public Task(String title, String paragraph, String state, String startDate, String startTime, 
               String dueDate, String dueTime, String type, String priority) {
        this.title = title;
        this.paragraph = paragraph;
        this.state = state;
        this.startDate = startDate;
        this.startTime = startTime;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.type = type;
        this.priority = priority;
    }

    // Getters et setters pour tous les champs
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getParagraph() { return paragraph; }
    public void setParagraph(String paragraph) { this.paragraph = paragraph; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    
    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}