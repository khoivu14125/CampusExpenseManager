package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu đại diện cho một khoản chi phí (Expense).
 */
public class Expense {
    private int id;             // ID của khoản chi (khóa chính trong DB)
    private double amount;      // Số tiền chi
    private String description; // Mô tả hoặc ghi chú
    private String category;    // Danh mục chi tiêu (Ví dụ: Ăn uống, Đi lại...)
    private String date;        // Ngày chi tiêu (Định dạng: yyyy-MM-dd)

    // Getters và setters
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
