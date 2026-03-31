# Hotel Management System - Integrated Backend & Frontend

This is a complete Hotel Management System with a Java backend server and a modern HTML/CSS/JavaScript web frontend.

## Project Structure

```
├── HotelServer.java          # Main HTTP Server entry point
├── Room.java                 # Room class
├── Customer.java             # Customer class
├── Booking.java              # Booking class
├── HotelManagementSystem.java # Core management system
├── main.java                 # Console-based UI (original)
├── index.html                # Web frontend
├── styles.css                # Styling
├── script.js                 # Frontend logic
└── README.md                 # This file
```

## Prerequisites

- Java 11 or higher
- Modern web browser (Chrome, Firefox, Safari, Edge)
- No other dependencies required (uses built-in Java HttpServer)

## Compilation

Compile all Java files:

```bash
javac *.java
```

Or compile only the server files:

```bash
javac HotelServer.java Room.java Customer.java Booking.java HotelManagementSystem.java
```

## Running the System

### Option 1: Web Frontend (Recommended)

Start the backend server:

```bash
java HotelServer
```

The server will start on `http://localhost:8080`

Then open your browser and navigate to:
```
http://localhost:8080
```

The frontend will automatically connect to the backend server and manage all data through REST API calls.

### Option 2: Console-Based UI

Run the console application:

```bash
java main
```

This will display the original text-based menu interface.

## Features

### Backend Server (HotelServer.java)
- **REST API Endpoints:**
  - `/api/rooms` - GET all rooms, POST to add room, DELETE to remove room
  - `/api/customers` - GET all customers, POST to add customer, DELETE to remove customer
  - `/api/bookings` - GET all bookings, POST to add booking, PUT to checkout
  - `/api/dashboard` - GET statistics

- **Static File Serving:**
  - Serves HTML, CSS, and JavaScript files
  - Automatically serves index.html as default page

### Frontend Interface
1. **Dashboard** - View statistics (total rooms, available rooms, customers, active bookings)
2. **Room Management** - Add rooms, view all rooms, delete rooms, filter by availability
3. **Customer Management** - Register customers, view all customers, manage records
4. **Booking Management** - Book rooms, checkout, view all bookings

### Data Management
- Data is persisted in memory on the server
- All operations are validated
- Prevents booking of already booked rooms
- Prevents deletion of booked rooms
- Prevents removal of customers with active bookings

## API Response Format

All API responses are in JSON format:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {...}
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description"
}
```

## Room Types

- Single - Rs. 2000/day
- Double - Rs. 3000/day
- Deluxe - Rs. 4000/day
- Suite - Rs. 5000/day

## Default Rooms

The system comes preloaded with 5 default rooms:
- Room 101 (Single)
- Room 102 (Double)
- Room 103 (Deluxe)
- Room 104 (Suite)
- Room 105 (Single)

## Browser Compatibility

- Chrome/Chromium 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Troubleshooting

**Port 8080 is already in use:**
- The server uses port 8080 by default
- If this port is in use, modify the PORT constant in HotelServer.java

**CORS Errors:**
- The server includes CORS headers for all responses
- If you see CORS errors, ensure you're accessing via `http://localhost:8080`

**Data not persisting:**
- The current server keeps data in memory
- Data will be lost when the server is restarted
- For persistence, implement database integration

## Future Enhancements

1. Integrate with a database (MySQL, PostgreSQL)
2. User authentication and authorization
3. Email notifications for bookings
4. Payment gateway integration
5. Advanced reporting and analytics
6. Multi-hotel support
7. Mobile app for customers

## License

This is a student project for OOSD coursework.

## Support

For issues or questions, please refer to the code comments or the implementation details in the source files.
