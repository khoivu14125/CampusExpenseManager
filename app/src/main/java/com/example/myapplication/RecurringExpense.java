package com.example.myapplication;

public class RecurringExpense {
    private int id;
    private double amount;
    private String description;
    private String category;
    private String startDate;
    private String endDate;
    private String lastGeneratedDate; // To track when the expense was last added

    public RecurringExpense() {
    }

    public RecurringExpense(double amount, String description, String category, String startDate, String endDate, String lastGeneratedDate) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lastGeneratedDate = lastGeneratedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
