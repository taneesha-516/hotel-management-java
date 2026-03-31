import com.sun.net.httpserver.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.*;

public class HotelServer {
    private static HotelManagementSystem hotel = new HotelManagementSystem();
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "./";

    public static void main(String[] args) throws Exception {
        initializeDefaultData();
        startServer();
    }

    private static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(PORT), 0);
        
        // Context handlers
        server.createContext("/", HotelServer::handleFile);
        server.createContext("/api/rooms", HotelServer::handleRooms);
        server.createContext("/api/customers", HotelServer::handleCustomers);
        server.createContext("/api/bookings", HotelServer::handleBookings);
        server.createContext("/api/dashboard", HotelServer::handleDashboard);

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        
        System.out.println("Hotel Management System Server started on http://localhost:" + PORT);
        System.out.println("Open http://localhost:" + PORT + " in your browser");
    }

    // File handler for serving static files
    private static void handleFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) {
            path = "index.html";
        } else {
            path = path.substring(1);
        }

        try {
            File file = new File(WEB_ROOT + path);
            if (file.exists() && !file.isDirectory()) {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", getContentType(path));
                exchange.sendResponseHeaders(200, fileBytes.length);
                exchange.getResponseBody().write(fileBytes);
                exchange.close();
            } else {
                sendResponse(exchange, 404, "File not found");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    // Room API handler
    private static void handleRooms(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        
        try {
            if ("GET".equals(method)) {
                handleGetRooms(exchange);
            } else if ("POST".equals(method)) {
                handleAddRoom(exchange);
            } else if ("DELETE".equals(method)) {
                handleDeleteRoom(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, e.getMessage());
        }
    }

    private static void handleGetRooms(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        StringBuilder roomsJson = new StringBuilder();
        roomsJson.append("[");

        List<Room> roomList;
        if (query != null && query.contains("available=true")) {
            roomList = hotel.getAvailableRooms();
        } else {
            roomList = hotel.rooms;
        }

        for (int i = 0; i < roomList.size(); i++) {
            roomsJson.append(roomToJSON(roomList.get(i)));
            if (i < roomList.size() - 1) {
                roomsJson.append(",");
            }
        }
        roomsJson.append("]");

        String response = "{\"success\": true, \"data\": " + roomsJson.toString() + "}";
        sendJSONResponse(exchange, response);
    }

    private static void handleAddRoom(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        
        try {
            String roomNumber = extractJsonString(body, "roomNumber");
            String roomType = extractJsonString(body, "roomType");
            double pricePerDay = extractJsonDouble(body, "pricePerDay");

            Room room = new Room(roomNumber, roomType, pricePerDay);
            boolean added = hotel.addRoom(room);

            if (added) {
                String response = "{\"success\": true, \"message\": \"Room added successfully\"}";
                sendJSONResponse(exchange, response);
            } else {
                String response = "{\"success\": false, \"message\": \"Room already exists\"}";
                sendJSONResponse(exchange, response, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private static void handleDeleteRoom(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String roomNumber = null;
        if (query != null) {
            roomNumber = URLDecoder.decode(query.split("=")[1], "UTF-8");
        }

        if (roomNumber != null && hotel.removeRoom(roomNumber)) {
            String response = "{\"success\": true, \"message\": \"Room deleted successfully\"}";
            sendJSONResponse(exchange, response);
        } else {
            String response = "{\"success\": false, \"message\": \"Room not found or cannot be deleted\"}";
            sendJSONResponse(exchange, response, 400);
        }
    }

    // Customer API handler
    private static void handleCustomers(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equals(method)) {
                handleGetCustomers(exchange);
            } else if ("POST".equals(method)) {
                handleAddCustomer(exchange);
            } else if ("DELETE".equals(method)) {
                handleDeleteCustomer(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, e.getMessage());
        }
    }

    private static void handleGetCustomers(HttpExchange exchange) throws IOException {
        StringBuilder customersJson = new StringBuilder();
        customersJson.append("[");

        List<Customer> customerList = new ArrayList<>(hotel.customers.values());
        for (int i = 0; i < customerList.size(); i++) {
            customersJson.append(customerToJSON(customerList.get(i)));
            if (i < customerList.size() - 1) {
                customersJson.append(",");
            }
        }
        customersJson.append("]");

        String response = "{\"success\": true, \"data\": " + customersJson.toString() + "}";
        sendJSONResponse(exchange, response);
    }

    private static void handleAddCustomer(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        
        try {
            String customerId = extractJsonString(body, "customerId");
            String name = extractJsonString(body, "name");
            String contactNumber = extractJsonString(body, "contactNumber");

            Customer customer = new Customer(customerId, name, contactNumber);
            boolean added = hotel.addCustomer(customer);

            if (added) {
                String response = "{\"success\": true, \"message\": \"Customer added successfully\"}";
                sendJSONResponse(exchange, response);
            } else {
                String response = "{\"success\": false, \"message\": \"Customer already exists\"}";
                sendJSONResponse(exchange, response, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private static void handleDeleteCustomer(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String customerId = null;
        if (query != null) {
            customerId = URLDecoder.decode(query.split("=")[1], "UTF-8");
        }

        if (customerId != null && hotel.removeCustomer(customerId)) {
            String response = "{\"success\": true, \"message\": \"Customer removed successfully\"}";
            sendJSONResponse(exchange, response);
        } else {
            String response = "{\"success\": false, \"message\": \"Customer not found\"}";
            sendJSONResponse(exchange, response, 400);
        }
    }

    // Booking API handler
    private static void handleBookings(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equals(method)) {
                handleGetBookings(exchange);
            } else if ("POST".equals(method)) {
                handleAddBooking(exchange);
            } else if ("PUT".equals(method)) {
                handleCheckoutBooking(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, e.getMessage());
        }
    }

    private static void handleGetBookings(HttpExchange exchange) throws IOException {
        StringBuilder bookingsJson = new StringBuilder();
        bookingsJson.append("[");

        List<Booking> bookingList = new ArrayList<>(hotel.bookings.values());
        for (int i = 0; i < bookingList.size(); i++) {
            bookingsJson.append(bookingToJSON(bookingList.get(i)));
            if (i < bookingList.size() - 1) {
                bookingsJson.append(",");
            }
        }
        bookingsJson.append("]");

        String response = "{\"success\": true, \"data\": " + bookingsJson.toString() + "}";
        sendJSONResponse(exchange, response);
    }

    private static void handleAddBooking(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        
        try {
            String customerId = extractJsonString(body, "customerId");
            String roomNumber = extractJsonString(body, "roomNumber");
            int days = extractJsonInt(body, "days");

            if (!hotel.customerExists(customerId)) {
                String response = "{\"success\": false, \"message\": \"Customer not found\"}";
                sendJSONResponse(exchange, response, 400);
                return;
            }

            if (hotel.bookRoom(customerId, roomNumber, days)) {
                String response = "{\"success\": true, \"message\": \"Room booked successfully\"}";
                sendJSONResponse(exchange, response);
            } else {
                String response = "{\"success\": false, \"message\": \"Room booking failed\"}";
                sendJSONResponse(exchange, response, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private static void handleCheckoutBooking(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        
        try {
            String roomNumber = extractJsonString(body, "roomNumber");

            double amount = hotel.checkoutRoom(roomNumber);

            if (amount > 0) {
                String response = "{\"success\": true, \"message\": \"Checkout successful\", \"amount\": " + amount + "}";
                sendJSONResponse(exchange, response);
            } else {
                String response = "{\"success\": false, \"message\": \"Room not found or not booked\"}";
                sendJSONResponse(exchange, response, 400);
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    // Dashboard API handler
    private static void handleDashboard(HttpExchange exchange) throws IOException {
        long activeBookings = hotel.bookings.values().stream()
                .filter(b -> !b.completed).count();

        String stats = "{" +
                "\"totalRooms\": " + hotel.rooms.size() + "," +
                "\"availableRooms\": " + hotel.getAvailableRooms().size() + "," +
                "\"totalCustomers\": " + hotel.customers.size() + "," +
                "\"activeBookings\": " + activeBookings +
                "}";

        String response = "{\"success\": true, \"data\": " + stats + "}";
        sendJSONResponse(exchange, response);
    }

    // Helper methods
    private static void sendJSONResponse(HttpExchange exchange, String json) throws IOException {
        sendJSONResponse(exchange, json, 200);
    }

    private static void sendJSONResponse(HttpExchange exchange, String json, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
        exchange.close();
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"success\": false, \"message\": \"" + escapeJson(message) + "\"}";
        sendJSONResponse(exchange, json, statusCode);
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static double extractJsonDouble(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([\\d.]+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return 0.0;
    }

    private static int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    private static String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".json")) return "application/json";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg")) return "image/jpeg";
        return "text/plain";
    }

    private static String roomToJSON(Room room) {
        return "{" +
                "\"roomNumber\": \"" + escapeJson(room.getRoomNumber()) + "\"," +
                "\"roomType\": \"" + escapeJson(room.getRoomType()) + "\"," +
                "\"pricePerDay\": " + room.getPricePerDay() + "," +
                "\"available\": " + room.isAvailable() +
                "}";
    }

    private static String customerToJSON(Customer customer) {
        return "{" +
                "\"customerId\": \"" + escapeJson(customer.getCustomerId()) + "\"," +
                "\"name\": \"" + escapeJson(customer.getName()) + "\"," +
                "\"contactNumber\": \"" + escapeJson(customer.getContactNumber()) + "\"" +
                "}";
    }

    private static String bookingToJSON(Booking booking) {
        return "{" +
                "\"bookingId\": \"" + escapeJson(booking.getBookingId()) + "\"," +
                "\"customerId\": \"" + escapeJson(booking.getCustomerId()) + "\"," +
                "\"roomNumber\": \"" + escapeJson(booking.getRoomNumber()) + "\"," +
                "\"checkInDate\": \"" + booking.getCheckInDate().toString() + "\"," +
                "\"checkOutDate\": \"" + booking.getCheckOutDate().toString() + "\"," +
                "\"totalCost\": " + booking.getTotalCost() + "," +
                "\"completed\": " + booking.completed +
                "}";
    }

    private static void initializeDefaultData() {
        HotelManagementSystem tempHotel = new HotelManagementSystem();
        hotel.addRoom(new Room("101", "Single", 2000));
        hotel.addRoom(new Room("102", "Double", 3000));
        hotel.addRoom(new Room("103", "Deluxe", 4000));
        hotel.addRoom(new Room("104", "Suite", 5000));
        hotel.addRoom(new Room("105", "Single", 2000));
    }
}