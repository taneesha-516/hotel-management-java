class Room {
    private String roomNumber;
    private String roomType;
    private double pricePerDay;
    private boolean available;

    public Room(String roomNumber, String roomType, double pricePerDay) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.available = true;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("Room #%s | Type: %s | Price: Rs. %.2f/day | Status: %s",
                roomNumber, roomType, pricePerDay, available ? "Available" : "Booked");
    }
}
