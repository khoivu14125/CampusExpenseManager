package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu đại diện cho ngân sách của một danh mục trong một tháng cụ thể.
 */
public class CategoryBudget {
    private int id;                 // ID của bản ghi ngân sách
    private String monthYear;       // Tháng và năm áp dụng (Định dạng: yyyy-MM)
    private String category;        // Tên danh mục
    private double budgetedAmount;  // Số tiền ngân sách đã đặt ra

    public CategoryBudget() {}

    public CategoryBudget(String monthYear, String category, double budgetedAmount) {
        this.monthYear = monthYear;
        this.category = category;
        this.budgetedAmount = budgetedAmount;
    }

    // Getters và Setters
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
