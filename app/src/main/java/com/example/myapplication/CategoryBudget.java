package com.example.myapplication;

public class CategoryBudget {
    private int id;
    private String monthYear;
    private String category;
    private double budgetedAmount;

    public CategoryBudget() {}

    public CategoryBudget(String monthYear, String category, double budgetedAmount) {
        this.monthYear = monthYear;
        this.category = category;
        this.budgetedAmount = budgetedAmount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBudgetedAmount() {
        return budgetedAmount;
    }

    public void setBudgetedAmount(double budgetedAmount) {
        this.budgetedAmount = budgetedAmount;
    }
}
