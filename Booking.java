import java.time.LocalDate;

class Booking {
    private String bookingId;
    private String customerId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalCost;
    public boolean completed;

    public Booking(String bookingId, String customerId, String roomNumber, 
                   LocalDate checkInDate, LocalDate checkOutDate, double totalCost) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalCost = totalCost;
        this.completed = false;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    @Override
    public String toString() {
        return String.format("Booking #%s | Customer: %s | Room: %s | Check-in: %s | Check-out: %s | Cost: Rs. %.2f",
                bookingId, customerId, roomNumber, checkInDate, checkOutDate, totalCost);
    }
}
