// API Client for backend communication
const API = {
    BASE_URL: 'http://localhost:8080/api',

    async request(endpoint, method = 'GET', data = null) {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (data) {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(this.BASE_URL + endpoint, options);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            showAlert('Network error: ' + error.message, 'error');
            return { success: false, message: 'Network error' };
        }
    },

    rooms() { return this.request('/rooms'); },
    addRoom(room) { return this.request('/rooms', 'POST', room); },
    deleteRoom(roomNumber) { return this.request(`/rooms?id=${roomNumber}`, 'DELETE'); },
    getAvailableRooms() { return this.request('/rooms?available=true'); },

    customers() { return this.request('/customers'); },
    addCustomer(customer) { return this.request('/customers', 'POST', customer); },
    removeCustomer(customerId) { return this.request(`/customers?id=${customerId}`, 'DELETE'); },

    bookings() { return this.request('/bookings'); },
    addBooking(booking) { return this.request('/bookings', 'POST', booking); },
    checkoutRoom(roomNumber) { return this.request('/bookings', 'PUT', { roomNumber }); },

    dashboard() { return this.request('/dashboard'); }
};

// Local data cache
const DB = {
    rooms: [],
    customers: [],
    bookings: [],

    async init() {
        await this.refresh();
    },

    async refresh() {
        const [roomsRes, customersRes, bookingsRes] = await Promise.all([
            API.rooms(),
            API.customers(),
            API.bookings()
        ]);

        if (roomsRes.success) this.rooms = roomsRes.data || [];
        if (customersRes.success) this.customers = customersRes.data || [];
        if (bookingsRes.success) this.bookings = bookingsRes.data || [];
    },

    getAvailableRooms() {
        return this.rooms.filter(r => r.available);
    },

    getCustomerBookings(customerId) {
        return this.bookings.filter(b => b.customerId === customerId);
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
    // Setup event listeners first
    setupEventListeners();
    
    // Load data
    await DB.init();
    
    // Update UI
    await updateDashboard();
    await updateRoomsDropdown();
    await renderRooms();
    await renderCustomers();
    await renderBookings();
    
    // Set up auto-refresh every 3 seconds
    setInterval(async () => {
        await DB.refresh();
        await updateDashboard();
        await renderRooms();
        await renderCustomers();
        await renderBookings();
        await updateRoomsDropdown();
    }, 3000);
});

// Page Navigation
function switchPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
        p.style.display = 'none';
    });
    
    // Remove active from all menu items
    document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));

    // Show selected page
    const page = document.getElementById(pageName);
    if (page) {
        page.classList.add('active');
        page.style.display = 'block';
    }
    
    // Mark menu item as active
    const menuItem = document.querySelector(`[data-page="${pageName}"]`);
    if (menuItem) {
        menuItem.classList.add('active');
    }
}

// Setup event listeners
function setupEventListeners() {
    // Menu navigation
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', function() {
            const pageName = this.getAttribute('data-page');
            switchPage(pageName);
        });
    });

    // Room Form
    document.getElementById('roomForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const roomNumber = document.getElementById('roomNumber').value.trim();
        const roomType = document.getElementById('roomType').value;
        const pricePerDay = parseFloat(document.getElementById('roomPrice').value);

        if (!roomNumber || !roomType || !pricePerDay) {
            showAlert('Please fill all fields', 'error');
            return;
        }

        const room = { roomNumber, roomType, pricePerDay, available: true };
        const result = await API.addRoom(room);
        
        if (result.success) {
            showAlert('Room added successfully!', 'success');
            document.getElementById('roomForm').reset();
            await DB.refresh();
            await renderRooms();
            await updateDashboard();
            await updateRoomsDropdown();
        } else {
            showAlert(result.message || 'Room already exists!', 'error');
        }
    });

    // Customer Form
    document.getElementById('customerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const customerId = document.getElementById('customerId').value.trim();
        const customerName = document.getElementById('customerName').value.trim();
        const customerContact = document.getElementById('customerContact').value.trim();

        if (!customerId || !customerName || !customerContact) {
            showAlert('Please fill all fields', 'error');
            return;
        }

        const customer = { customerId, name: customerName, contactNumber: customerContact };
        const result = await API.addCustomer(customer);
        
        if (result.success) {
            showAlert('Customer registered successfully!', 'success');
            document.getElementById('customerForm').reset();
            await DB.refresh();
            await renderCustomers();
            await updateDashboard();
        } else {
            showAlert(result.message || 'Customer ID already exists!', 'error');
        }
    });

    // Booking Form
    document.getElementById('bookingForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const customerId = document.getElementById('bookingCustomerId').value.trim();
        const roomNumber = document.getElementById('bookingRoom').value;
        const days = parseInt(document.getElementById('bookingDays').value);

        if (!customerId || !roomNumber || !days) {
            showAlert('Please fill all fields', 'error');
            return;
        }

        const booking = { customerId, roomNumber, days };
        const result = await API.addBooking(booking);
        
        if (result.success) {
            showAlert('Room booked successfully!', 'success');
            document.getElementById('bookingForm').reset();
            await DB.refresh();
            await renderBookings();
            await renderRooms();
            await updateDashboard();
            await updateRoomsDropdown();
        } else {
            showAlert(result.message || 'Room booking failed!', 'error');
        }
    });

    // Filter buttons for rooms
    document.querySelectorAll('[data-filter]').forEach(btn => {
        btn.addEventListener('click', function() {
            if (this.parentElement.id === 'roomTableBody') return;
            
            const filter = this.getAttribute('data-filter');
            this.parentElement.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            const section = this.closest('.table-section');
            if (section && section.previousElementSibling?.querySelector('form')) {
                // This is in rooms or bookings section
                if (document.activeElement?.id === 'bookingRoom') {
                    filterBookings(filter);
                } else {
                    filterRooms(filter);
                }
            }
        });
    });

    // Customer search
    document.getElementById('customerSearch').addEventListener('input', (e) => {
        const search = e.target.value.toLowerCase();
        document.querySelectorAll('#customerTableBody tr').forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(search) ? '' : 'none';
        });
    });

    // Modal close button
    document.querySelector('.close').addEventListener('click', closeModal);
    window.addEventListener('click', (e) => {
        const modal = document.getElementById('modal');
        if (e.target === modal) closeModal();
    });
}

// Render Functions
async function renderRooms() {
    const tbody = document.getElementById('roomTableBody');
    tbody.innerHTML = '';

    if (DB.rooms.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">No rooms added yet</td></tr>';
        return;
    }

    DB.rooms.forEach(room => {
        const row = document.createElement('tr');
        const statusClass = room.available ? 'status-available' : 'status-booked';
        const statusText = room.available ? 'Available' : 'Booked';

        row.innerHTML = `
            <td><strong>${room.roomNumber}</strong></td>
            <td>${room.roomType}</td>
            <td>Rs. ${room.pricePerDay}</td>
            <td><span class="status-badge ${statusClass}">${statusText}</span></td>
            <td>
                <div class="action-icons">
                    <button class="icon-btn" onclick="deleteRoom('${room.roomNumber}')" title="Delete Room">🗑️</button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function renderCustomers() {
    const tbody = document.getElementById('customerTableBody');
    tbody.innerHTML = '';

    if (DB.customers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">No customers registered yet</td></tr>';
        return;
    }

    DB.customers.forEach(customer => {
        const bookings = DB.getCustomerBookings(customer.customerId).length;
        const row = document.createElement('tr');

        row.innerHTML = `
            <td><strong>${customer.customerId}</strong></td>
            <td>${customer.name}</td>
            <td>${customer.contactNumber}</td>
            <td>${bookings}</td>
            <td>
                <div class="action-icons">
                    <button class="icon-btn" onclick="deleteCustomer('${customer.customerId}')" title="Remove Customer">🗑️</button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function renderBookings() {
    const tbody = document.getElementById('bookingTableBody');
    tbody.innerHTML = '';

    if (DB.bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="no-data">No bookings yet</td></tr>';
        return;
    }

    DB.bookings.forEach(booking => {
        const row = document.createElement('tr');
        const statusClass = booking.completed ? 'status-completed' : 'status-active';
        const statusText = booking.completed ? 'Completed' : 'Active';

        row.innerHTML = `
            <td><strong>${booking.bookingId}</strong></td>
            <td>${booking.customerId}</td>
            <td>${booking.roomNumber}</td>
            <td>${booking.checkInDate}</td>
            <td>${booking.checkOutDate}</td>
            <td>Rs. ${booking.totalCost}</td>
            <td><span class="status-badge ${statusClass}">${statusText}</span></td>
            <td>
                <div class="action-icons">
                    ${!booking.completed ? `<button class="icon-btn" onclick="checkoutBooking('${booking.roomNumber}')" title="Checkout">✓</button>` : ''}
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function updateDashboard() {
    const result = await API.dashboard();
    if (result.success && result.data) {
        document.getElementById('totalRooms').textContent = result.data.totalRooms;
        document.getElementById('availableRooms').textContent = result.data.availableRooms;
        document.getElementById('totalCustomers').textContent = result.data.totalCustomers;
        document.getElementById('activeBookings').textContent = result.data.activeBookings;
    }
}

async function updateRoomsDropdown() {
    const select = document.getElementById('bookingRoom');
    const availableRooms = DB.getAvailableRooms();

    let html = '<option value="">Select Room</option>';
    availableRooms.forEach(room => {
        html += `<option value="${room.roomNumber}">Room ${room.roomNumber} (${room.roomType}) - Rs. ${room.pricePerDay}/day</option>`;
    });

    select.innerHTML = html;
}

// Filter Functions
function filterRooms(filter) {
    document.querySelectorAll('#roomTableBody tr').forEach(row => {
        const status = row.querySelector('.status-badge');
        if (!status) return;

        let show = true;
        if (filter === 'available') show = status.textContent === 'Available';
        else if (filter === 'booked') show = status.textContent === 'Booked';

        row.style.display = show ? '' : 'none';
    });
}

function filterBookings(filter) {
    document.querySelectorAll('#bookingTableBody tr').forEach(row => {
        const status = row.querySelector('.status-badge');
        if (!status) return;

        let show = true;
        if (filter === 'active') show = status.textContent === 'Active';
        else if (filter === 'completed') show = status.textContent === 'Completed';

        row.style.display = show ? '' : 'none';
    });
}

// Action Functions
function deleteRoom(roomNumber) {
    showModal(
        'Delete Room',
        `Are you sure you want to delete Room ${roomNumber}?`,
        async () => {
            const result = await API.deleteRoom(roomNumber);
            if (result.success) {
                showAlert('Room deleted successfully!', 'success');
                await DB.refresh();
                await renderRooms();
                await updateDashboard();
                await updateRoomsDropdown();
                closeModal();
            } else {
                showAlert(result.message || 'Cannot delete room!', 'error');
            }
        }
    );
}

function deleteCustomer(customerId) {
    showModal(
        'Remove Customer',
        `Are you sure you want to remove Customer ${customerId}?`,
        async () => {
            const result = await API.removeCustomer(customerId);
            if (result.success) {
                showAlert('Customer removed successfully!', 'success');
                await DB.refresh();
                await renderCustomers();
                await updateDashboard();
                closeModal();
            } else {
                showAlert(result.message || 'Cannot remove customer!', 'error');
            }
        }
    );
}

function checkoutBooking(roomNumber) {
    showModal(
        'Checkout Room',
        `Checkout room ${roomNumber}? The customer will be charged accordingly.`,
        async () => {
            const result = await API.checkoutRoom(roomNumber);
            if (result.success) {
                showAlert(`Checkout successful! Amount to be paid: Rs. ${result.amount}`, 'success');
                await DB.refresh();
                await renderBookings();
                await renderRooms();
                await updateDashboard();
                await updateRoomsDropdown();
                closeModal();
            } else {
                showAlert(result.message || 'Checkout failed!', 'error');
            }
        }
    );
}

// Modal Functions
function showModal(title, message, onConfirm) {
    const modal = document.getElementById('modal');
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalMessage').textContent = message;

    document.getElementById('modalConfirm').onclick = onConfirm;
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('modal').style.display = 'none';
}

// Alert Function
function showAlert(message, type = 'success') {
    const alert = document.getElementById('alert');
    alert.textContent = message;
    alert.className = `alert show ${type}`;

    setTimeout(() => {
        alert.classList.remove('show');
    }, 3000);
}

// Filter button event listeners (delegated)
document.addEventListener('click', function(e) {
    if (!e.target.classList.contains('filter-btn')) return;

    const filter = e.target.getAttribute('data-filter');
    if (!filter) return;

    // Find which section this filter belongs to
    const section = e.target.closest('.table-section');
    if (!section) return;

    const table = section.querySelector('table');
    if (!table) return;

    // Remove active class from all filters in this section
    section.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    e.target.classList.add('active');

    // Determine which table and apply filter
    if (table.id === 'roomsTable') {
        filterRooms(filter);
    } else if (table.id === 'bookingsTable') {
        filterBookings(filter);
    }
});
