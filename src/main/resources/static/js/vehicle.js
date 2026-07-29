/**
 * Vehicle Management JavaScript
 * Connected to Spring Boot Backend via api.js
 */

// Global State for Pagination and Sorting
let currentPage = 0;
const pageSize = 10;
let currentSort = 'id,desc';
let currentVehicleId = null;
let searchTimeout;

document.addEventListener('DOMContentLoaded', () => {
    // Set Current Date
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    // Initial API Load
    fetchVehicles(0);
});

// Custom Toast Message Function
function showMessage(msg, isError = false) {
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.backgroundColor = isError ? '#EF4444' : '#10B981';
    toast.style.color = '#FFF';
    toast.style.padding = '12px 24px';
    toast.style.borderRadius = '8px';
    toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    toast.style.zIndex = '9999';
    toast.style.transition = 'opacity 0.3s ease';
    toast.style.whiteSpace = 'pre-line';
    toast.textContent = msg;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function showLoading() {
    const tableBody = document.getElementById('vehiclesTableBody');
    tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 30px; color: #64748B; font-weight: 500;">Loading vehicles...</td></tr>`;
}

// Fetch Vehicles from Backend
async function fetchVehicles(page = 0) {
    showLoading();
    currentPage = page;
    
    const searchInput = document.getElementById('searchInput').value;
    const statusFilter = document.getElementById('statusFilter').value;
    
    const params = {
        page: currentPage,
        size: pageSize,
        sort: currentSort
    };
    
    // Choose endpoint based on whether we are searching/filtering or just loading all
    let endpoint = '/vehicles';
    
    if (searchInput || statusFilter) {
        endpoint = '/vehicles/search';
        if (searchInput) params.search = searchInput;
        if (statusFilter) params.status = statusFilter;
    }

    try {
        let responseData = await Api.get(endpoint, params);
        
        // Handle difference between basic List response and Page response
        // Depending on how /api/vehicles is implemented, it might return a List or Page
        // Assuming we wrapped both to return Pageable or we normalize it here:
        if (Array.isArray(responseData)) {
            // Mock pagination structure if endpoint returns a raw array
            responseData = {
                content: responseData,
                totalElements: responseData.length,
                number: 0,
                size: responseData.length,
                totalPages: 1,
                first: true,
                last: true,
                numberOfElements: responseData.length
            };
        } else if (!responseData.content && responseData.data) {
            // Some generic wraps
            responseData = responseData.data;
        }

        renderTable(responseData);
    } catch (error) {
        document.getElementById('vehiclesTableBody').innerHTML = 
            `<tr><td colspan="8" style="text-align: center; padding: 30px; color: #EF4444; font-weight: 500;">Failed to load vehicles.<br>${error.message}</td></tr>`;
        showMessage(error.message, true);
    }
}

// Render Table Rows
function renderTable(pageData) {
    const tableBody = document.getElementById('vehiclesTableBody');
    tableBody.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 30px; color: #64748B;">No vehicles found matching your criteria.</td></tr>`;
        renderPagination(pageData);
        return;
    }

    pageData.content.forEach(vehicle => {
        let statusClass = '';
        let statusLabel = '';
        if (vehicle.status === 'AVAILABLE') { statusClass = 'status-available'; statusLabel = 'Available'; }
        else if (vehicle.status === 'IN_SERVICE') { statusClass = 'status-inservice'; statusLabel = 'In Service'; }
        else if (vehicle.status === 'MAINTENANCE') { statusClass = 'status-maintenance'; statusLabel = 'Maintenance'; }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${vehicle.vehicleNumber}</strong></td>
            <td>${vehicle.brand}</td>
            <td>${vehicle.model}</td>
            <td>${vehicle.manufacturingYear}</td>
            <td>${formatFuelType(vehicle.fuelType)}</td>
            <td>${vehicle.currentOdometer.toLocaleString()} km</td>
            <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
            <td>
                <button class="btn-action" onclick="openEditModal(${vehicle.id})">Edit</button>
                <button class="btn-danger" onclick="deleteVehicle(${vehicle.id})">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
    
    renderPagination(pageData);
}

function formatFuelType(fuel) {
    if (!fuel) return '-';
    return fuel.charAt(0) + fuel.slice(1).toLowerCase();
}

// Render Pagination Controls
function renderPagination(pageData) {
    const paginationDiv = document.querySelector('.pagination');
    
    if (!pageData || pageData.totalElements === 0) {
        paginationDiv.innerHTML = `<span class="page-info">Showing 0 entries</span><div class="page-buttons"></div>`;
        return;
    }

    const start = pageData.number * pageData.size + 1;
    const end = Math.min(start + pageData.numberOfElements - 1, pageData.totalElements);
    
    let html = `<span class="page-info">Showing ${start} to ${end} of ${pageData.totalElements} entries</span>
                <div class="page-buttons">
                    <button class="btn-page" ${pageData.first ? 'disabled' : ''} onclick="fetchVehicles(${pageData.number - 1})">Previous</button>`;
    
    // Generate page numbers
    for (let i = 0; i < (pageData.totalPages || 1); i++) {
        html += `<button class="btn-page ${i === pageData.number ? 'active' : ''}" onclick="fetchVehicles(${i})">${i + 1}</button>`;
    }
    
    html += `<button class="btn-page" ${pageData.last ? 'disabled' : ''} onclick="fetchVehicles(${pageData.number + 1})">Next</button>
             </div>`;
             
    paginationDiv.innerHTML = html;
}

// Search and Filter logic (Debounced)
function searchVehicles() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchVehicles(0); // Reset to page 0 on new search
    }, 400);
}

function filterVehicles() {
    fetchVehicles(0); // Reset to page 0 on new filter
}

// Modal Logic
const modal = document.getElementById('vehicleModal');
const form = document.getElementById('vehicleForm');

function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Add Vehicle';
    form.reset();
    document.getElementById('vehicleId').value = '';
    currentVehicleId = null;
    modal.classList.add('active');
}

async function openEditModal(id) {
    try {
        const vehicle = await Api.get(`/vehicles/${id}`);
        
        document.getElementById('modalTitle').textContent = 'Edit Vehicle';
        document.getElementById('vehicleId').value = vehicle.id;
        currentVehicleId = vehicle.id;
        
        document.getElementById('vehicleNumber').value = vehicle.vehicleNumber;
        document.getElementById('brand').value = vehicle.brand;
        document.getElementById('model').value = vehicle.model;
        document.getElementById('manufacturingYear').value = vehicle.manufacturingYear;
        document.getElementById('fuelType').value = vehicle.fuelType;
        document.getElementById('odometer').value = vehicle.currentOdometer;
        document.getElementById('status').value = vehicle.status;

        modal.classList.add('active');
    } catch (error) {
        showMessage('Error fetching vehicle details: \n' + error.message, true);
    }
}

function closeModal() {
    modal.classList.remove('active');
}

// Form Submission (Add/Edit)
async function handleFormSubmit(event) {
    event.preventDefault();

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Saving...';
    submitBtn.disabled = true;

    const vehicleData = {
        vehicleNumber: document.getElementById('vehicleNumber').value,
        brand: document.getElementById('brand').value,
        model: document.getElementById('model').value,
        manufacturingYear: parseInt(document.getElementById('manufacturingYear').value),
        fuelType: document.getElementById('fuelType').value,
        currentOdometer: parseInt(document.getElementById('odometer').value),
        status: document.getElementById('status').value
    };

    try {
        if (currentVehicleId) {
            // Update Existing
            await Api.put(`/vehicles/${currentVehicleId}`, vehicleData);
            showMessage('Vehicle updated successfully!');
        } else {
            // Create New
            await Api.post('/vehicles', vehicleData);
            showMessage('Vehicle created successfully!');
        }
        
        closeModal();
        fetchVehicles(currentPage); // Reload current page
    } catch (error) {
        showMessage('Error saving vehicle: \n' + error.message, true);
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

// Delete Logic
async function deleteVehicle(id) {
    if (confirm("Are you sure you want to delete this vehicle? This action cannot be undone.")) {
        try {
            await Api.delete(`/vehicles/${id}`);
            showMessage('Vehicle deleted successfully!');
            fetchVehicles(0); 
        } catch (error) {
            showMessage('Error deleting vehicle: \n' + error.message, true);
        }
    }
}
