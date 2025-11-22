package com.example.myapplication;

public class RecurringExpense {
    private int id;
    private String category;
    private String description;
    private double amount;
    private String startDate;
    private String endDate;
    private String lastGeneratedDate;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLastGeneratedDate() {
        return lastGeneratedDate;
    }

    public void setLastGeneratedDate(String lastGeneratedDate) {
        this.lastGeneratedDate = lastGeneratedDate;
    }
}
