import java.util.*;

public class main {
    private static HotelManagementSystem hotel = new HotelManagementSystem();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeDefaultData();
        displayMainMenu();
    }

    private static void displayMainMenu() {
        while (true) {
            System.out.println("\n========== HOTEL MANAGEMENT SYSTEM ==========");
            System.out.println("1. Room Management");
            System.out.println("2. Customer Management");
            System.out.println("3. Booking Management");
            System.out.println("4. Display Available Rooms");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        roomManagementMenu();
                        break;
                    case 2:
                        customerManagementMenu();
                        break;
                    case 3:
                        bookingManagementMenu();
                        break;
                    case 4:
                        hotel.displayAvailableRooms();
                        break;
                    case 5:
                        System.out.println("Thank you for using Hotel Management System!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void roomManagementMenu() {
        while (true) {
            System.out.println("\n========== ROOM MANAGEMENT ==========");
            System.out.println("1. Add New Room");
            System.out.println("2. View All Rooms");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        addRoom();
                        break;
                    case 2:
                        hotel.displayAllRooms();
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void addRoom() {
        try {
            System.out.print("Enter Room Number: ");
            String roomNumber = scanner.nextLine();
            
            System.out.println("Room Type: ");
            System.out.println("1. Single");
            System.out.println("2. Double");
            System.out.println("3. Deluxe");
            System.out.println("4. Suite");
            System.out.print("Select room type: ");
            int typeChoice = Integer.parseInt(scanner.nextLine());
            String roomType = getRoomType(typeChoice);
            
            System.out.print("Enter Price per Day: ");
            double price = Double.parseDouble(scanner.nextLine());
            
            Room room = new Room(roomNumber, roomType, price);
            if (hotel.addRoom(room)) {
                System.out.println("Room added successfully!");
            } else {
                System.out.println("Room already exists!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid data.");
        }
    }

    private static String getRoomType(int choice) {
        return switch (choice) {
            case 1 -> "Single";
            case 2 -> "Double";
            case 3 -> "Deluxe";
            case 4 -> "Suite";
            default -> "Single";
        };
    }

    private static void customerManagementMenu() {
        while (true) {
            System.out.println("\n========== CUSTOMER MANAGEMENT ==========");
            System.out.println("1. Add New Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Remove Customer");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        addCustomer();
                        break;
                    case 2:
                        hotel.displayAllCustomers();
                        break;
                    case 3:
                        removeCustomer();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void addCustomer() {
        try {
            System.out.print("Enter Customer ID: ");
            String customerId = scanner.nextLine();
            
            System.out.print("Enter Customer Name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter Contact Number: ");
            String contactNumber = scanner.nextLine();
            
            Customer customer = new Customer(customerId, name, contactNumber);
            if (hotel.addCustomer(customer)) {
                System.out.println("Customer added successfully!");
            } else {
                System.out.println("Customer already exists!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please try again.");
        }
    }

    private static void removeCustomer() {
        try {
            System.out.print("Enter Customer ID to remove: ");
            String customerId = scanner.nextLine();
            
            if (hotel.removeCustomer(customerId)) {
                System.out.println("Customer removed successfully!");
            } else {
                System.out.println("Customer not found!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please try again.");
        }
    }

    private static void bookingManagementMenu() {
        while (true) {
            System.out.println("\n========== BOOKING MANAGEMENT ==========");
            System.out.println("1. Book a Room");
            System.out.println("2. Checkout Room");
            System.out.println("3. View All Bookings");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        bookRoom();
                        break;
                    case 2:
                        checkoutRoom();
                        break;
                    case 3:
                        hotel.displayAllBookings();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void bookRoom() {
        try {
            System.out.print("Enter Customer ID: ");
            String customerId = scanner.nextLine();
            
            if (!hotel.customerExists(customerId)) {
                System.out.println("Customer not found!");
                return;
            }
            
            hotel.displayAvailableRooms();
            System.out.print("Enter Room Number to book: ");
            String roomNumber = scanner.nextLine();
            
            System.out.print("Enter Number of Days: ");
            int days = Integer.parseInt(scanner.nextLine());
            
            if (hotel.bookRoom(customerId, roomNumber, days)) {
                System.out.println("Room booked successfully!");
            } else {
                System.out.println("Room booking failed! Please check if the room is available.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid data.");
        }
    }

    private static void checkoutRoom() {
        try {
            System.out.print("Enter Room Number to checkout: ");
            String roomNumber = scanner.nextLine();
            
            double amount = hotel.checkoutRoom(roomNumber);
            if (amount > 0) {
                System.out.println("Checkout successful!");
                System.out.println("Amount to be paid: Rs. " + amount);
            } else {
                System.out.println("Room not found or not booked!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please try again.");
        }
    }

    private static void initializeDefaultData() {
        // Add some default rooms
        hotel.addRoom(new Room("101", "Single", 2000));
        hotel.addRoom(new Room("102", "Double", 3000));
        hotel.addRoom(new Room("103", "Deluxe", 4000));
        hotel.addRoom(new Room("104", "Suite", 5000));
        hotel.addRoom(new Room("105", "Single", 2000));
    }
}
