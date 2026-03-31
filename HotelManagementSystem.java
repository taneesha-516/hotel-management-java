import java.util.*;
import java.time.LocalDate;

class HotelManagementSystem {
    public ArrayList<Room> rooms;
    public HashMap<String, Customer> customers;
    public HashMap<String, Booking> bookings;
    private HashMap<String, String> roomToCustomer;

    public HotelManagementSystem() {
        this.rooms = new ArrayList<>();
        this.customers = new HashMap<>();
        this.bookings = new HashMap<>();
        this.roomToCustomer = new HashMap<>();
    }

    public boolean addRoom(Room room) {
        for (Room r : rooms) {
            if (r.getRoomNumber().equals(room.getRoomNumber())) {
                return false;
            }
        }
        rooms.add(room);
        return true;
    }

    public boolean removeRoom(String roomNumber) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber().equals(roomNumber)) {
                if (!rooms.get(i).isAvailable()) {
                    return false; // Can't delete booked room
                }
                rooms.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean addCustomer(Customer customer) {
        if (customers.containsKey(customer.getCustomerId())) {
            return false;
        }
        customers.put(customer.getCustomerId(), customer);
        return true;
    }

    public boolean removeCustomer(String customerId) {
        // Check if customer has active bookings
        for (Booking booking : bookings.values()) {
            if (booking.getCustomerId().equals(customerId) && !booking.completed) {
                return false;
            }
        }
        return customers.remove(customerId) != null;
    }

    public boolean customerExists(String customerId) {
        return customers.containsKey(customerId);
    }

    public boolean bookRoom(String customerId, String roomNumber, int days) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                if (room.isAvailable()) {
                    room.setAvailable(false);
                    roomToCustomer.put(roomNumber, customerId);
                    
                    String bookingId = "BOOK" + System.currentTimeMillis();
                    LocalDate checkIn = LocalDate.now();
                    LocalDate checkOut = checkIn.plusDays(days);
                    double totalCost = room.getPricePerDay() * days;
                    
                    Booking booking = new Booking(bookingId, customerId, roomNumber, checkIn, checkOut, totalCost);
                    bookings.put(bookingId, booking);
                    
                    return true;
                }
            }
        }
        return false;
    }

    public double checkoutRoom(String roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber) && !room.isAvailable()) {
                room.setAvailable(true);
                roomToCustomer.remove(roomNumber);
                
                for (Booking booking : bookings.values()) {
                    if (booking.getRoomNumber().equals(roomNumber) && !booking.completed) {
                        booking.completed = true;
                        return booking.getTotalCost();
                    }
                }
            }
        }
        return -1;
    }

    public ArrayList<Room> getAvailableRooms() {
        ArrayList<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isAvailable()) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public void displayAllRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms available!");
            return;
        }
        System.out.println("\n========== ALL ROOMS ==========");
        for (Room room : rooms) {
            System.out.println(room);
        }
    }

    public void displayAvailableRooms() {
        ArrayList<Room> availableRooms = getAvailableRooms();
        
        if (availableRooms.isEmpty()) {
            System.out.println("\nNo available rooms at the moment!");
            return;
        }
        
        System.out.println("\n========== AVAILABLE ROOMS ==========");
        availableRooms.sort((r1, r2) -> Double.compare(r1.getPricePerDay(), r2.getPricePerDay()));
        for (Room room : availableRooms) {
            System.out.println(room);
        }
    }

    public void displayAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers registered!");
            return;
        }
        System.out.println("\n========== ALL CUSTOMERS ==========");
        for (Customer customer : customers.values()) {
            System.out.println(customer);
        }
    }

    public void displayAllBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found!");
            return;
        }
        System.out.println("\n========== ALL BOOKINGS ==========");
        for (Booking booking : bookings.values()) {
            System.out.println(booking);
        }
    }
}
