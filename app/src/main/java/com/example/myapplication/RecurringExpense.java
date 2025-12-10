package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu đại diện cho một khoản chi phí định kỳ (lặp lại hàng tháng).
 */
public class RecurringExpense {
    private int id;                 // ID của chi phí định kỳ
    private String category;        // Danh mục chi tiêu
    private String description;     // Mô tả
    private double amount;          // Số tiền chi định kỳ
    private String startDate;       // Ngày bắt đầu hiệu lực (yyyy-MM-dd)
    private String endDate;         // Ngày kết thúc hiệu lực (yyyy-MM-dd)
    private String lastGeneratedDate; // Ngày cuối cùng chi phí này được tự động tạo ra

    // Getters và setters
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
