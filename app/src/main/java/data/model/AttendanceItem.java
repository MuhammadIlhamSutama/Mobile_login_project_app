package data.model;

public class AttendanceItem {
    private String date;
    private String checkIn;
    private String checkOut;
    private String status;

    public AttendanceItem(String date, String checkIn, String checkOut, String status) {
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public String getStatus() {
        return status;
    }
}
