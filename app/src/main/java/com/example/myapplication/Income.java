package com.example.myapplication;

/**
 * Lớp mô hình dữ liệu đại diện cho một khoản thu nhập (Income).
 */
public class Income {
    private int id;             // ID của khoản thu (khóa chính trong DB)
    private double amount;      // Số tiền thu nhập
    private String description; // Mô tả hoặc nguồn thu
    private String date;        // Ngày thu nhập (Định dạng: yyyy-MM-dd)

    // Constructors
    public Income() {}

    public Income(int id, double amount, String description, String date) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    // Getters và Setters
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
