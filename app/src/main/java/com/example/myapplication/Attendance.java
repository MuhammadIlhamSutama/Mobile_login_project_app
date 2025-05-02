package com.example.myapplication;

public class Attendance {
    public int id;
    public String date;
    public String checkIn;
    public String checkOut;
    public String status;

    // Konstruktor dengan parameter date
    public Attendance(int id, String date, String checkIn, String checkOut, String status) {
        this.id = id;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }
}
