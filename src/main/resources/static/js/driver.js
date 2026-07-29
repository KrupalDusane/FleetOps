/**
 * Driver Management JavaScript
 */

let currentPage = 0;
const pageSize = 10;
let currentSort = 'id,desc';
let currentDriverId = null;
let searchTimeout;

document.addEventListener('DOMContentLoaded', () => {
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    fetchDrivers(0);
});

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
    const tableBody = document.getElementById('driversTableBody');
    tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 30px; color: #64748B; font-weight: 500;">Loading drivers...</td></tr>`;
}

async function fetchDrivers(page = 0) {
    showLoading();
    currentPage = page;
    
    const searchInput = document.getElementById('searchInput').value;
    const statusFilter = document.getElementById('statusFilter').value;
    
    const params = { page: currentPage, size: pageSize, sort: currentSort };
    let endpoint = '/drivers';
    
    if (searchInput || statusFilter) {
        endpoint = '/drivers/search';
        if (searchInput) params.search = searchInput;
        if (statusFilter) params.status = statusFilter;
    }

    try {
        let responseData = await Api.get(endpoint, params);
        if (Array.isArray(responseData)) {
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
            responseData = responseData.data;
        }
        renderTable(responseData);
    } catch (error) {
        document.getElementById('driversTableBody').innerHTML = 
            `<tr><td colspan="6" style="text-align: center; padding: 30px; color: #EF4444; font-weight: 500;">Failed to load drivers.<br>${error.message}</td></tr>`;
        showMessage(error.message, true);
    }
}

function renderTable(pageData) {
    const tableBody = document.getElementById('driversTableBody');
    tableBody.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 30px; color: #64748B;">No drivers found matching your criteria.</td></tr>`;
        renderPagination(pageData);
        return;
    }

    pageData.content.forEach(driver => {
        const statusClass = driver.status === 'ON_DUTY' ? 'status-onduty' : 'status-offduty';
        const statusLabel = driver.status === 'ON_DUTY' ? 'On Duty' : 'Off Duty';
        
        let vehicleHtml = `<span class="vehicle-unassigned">Unassigned</span>`;
        if (driver.currentVehicle) {
            vehicleHtml = `<span class="vehicle-assigned">${driver.currentVehicle.vehicleNumber} (${driver.currentVehicle.brand})</span>`;
        }

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${driver.name}</strong></td>
            <td>${driver.licenseNumber}</td>
            <td>${driver.phone}</td>
            <td>${vehicleHtml}</td>
            <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
            <td>
                <button class="btn-action" onclick="openEditModal(${driver.id})">Edit</button>
                <button class="btn-danger" onclick="deleteDriver(${driver.id})">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
    
    renderPagination(pageData);
}

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
                    <button class="btn-page" ${pageData.first ? 'disabled' : ''} onclick="fetchDrivers(${pageData.number - 1})">Previous</button>`;
    
    for (let i = 0; i < (pageData.totalPages || 1); i++) {
        html += `<button class="btn-page ${i === pageData.number ? 'active' : ''}" onclick="fetchDrivers(${i})">${i + 1}</button>`;
    }
    
    html += `<button class="btn-page" ${pageData.last ? 'disabled' : ''} onclick="fetchDrivers(${pageData.number + 1})">Next</button>
             </div>`;
             
    paginationDiv.innerHTML = html;
}

function searchDrivers() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchDrivers(0);
    }, 400);
}

function filterDrivers() {
    fetchDrivers(0);
}

/* Modal Logic */
const modal = document.getElementById('driverModal');
const form = document.getElementById('driverForm');

async function populateVehicleDropdown(selectedVehicleId = null) {
    const select = document.getElementById('currentVehicle');
    select.innerHTML = '<option value="">Loading vehicles...</option>';
    
    try {
        const pageData = await Api.get('/vehicles', { size: 100 }); // Load enough vehicles for dropdown
        const vehicles = Array.isArray(pageData) ? pageData : (pageData.content || []);
        
        select.innerHTML = '<option value="">No Vehicle Assigned</option>';
        vehicles.forEach(v => {
            // Usually we only want to show AVAILABLE vehicles in a real app, 
            // but we'll show all here for management purposes.
            const option = document.createElement('option');
            option.value = v.id;
            option.textContent = `${v.vehicleNumber} (${v.brand} - ${v.status})`;
            if (selectedVehicleId && v.id === selectedVehicleId) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    } catch (err) {
        select.innerHTML = '<option value="">Failed to load vehicles</option>';
    }
}

async function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Add Driver';
    form.reset();
    document.getElementById('driverId').value = '';
    currentDriverId = null;
    await populateVehicleDropdown();
    modal.classList.add('active');
}

async function openEditModal(id) {
    try {
        const driver = await Api.get(`/drivers/${id}`);
        
        document.getElementById('modalTitle').textContent = 'Edit Driver';
        document.getElementById('driverId').value = driver.id;
        currentDriverId = driver.id;
        
        document.getElementById('name').value = driver.name;
        document.getElementById('licenseNumber').value = driver.licenseNumber;
        document.getElementById('phone').value = driver.phone;
        document.getElementById('status').value = driver.status;
        
        const assignedVehicleId = driver.currentVehicle ? driver.currentVehicle.id : null;
        await populateVehicleDropdown(assignedVehicleId);

        modal.classList.add('active');
    } catch (error) {
        showMessage('Error fetching driver details: \n' + error.message, true);
    }
}

function closeModal() {
    modal.classList.remove('active');
}

async function handleFormSubmit(event) {
    event.preventDefault();

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Saving...';
    submitBtn.disabled = true;

    const vehicleId = document.getElementById('currentVehicle').value;

    const driverData = {
        name: document.getElementById('name').value,
        licenseNumber: document.getElementById('licenseNumber').value,
        phone: document.getElementById('phone').value,
        status: document.getElementById('status').value,
        currentVehicle: vehicleId ? { id: parseInt(vehicleId) } : null
    };

    try {
        if (currentDriverId) {
            await Api.put(`/drivers/${currentDriverId}`, driverData);
            showMessage('Driver updated successfully!');
        } else {
            await Api.post('/drivers', driverData);
            showMessage('Driver created successfully!');
        }
        
        closeModal();
        fetchDrivers(currentPage);
    } catch (error) {
        showMessage('Error saving driver: \n' + error.message, true);
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

async function deleteDriver(id) {
    if (confirm("Are you sure you want to delete this driver? This action cannot be undone.")) {
        try {
            await Api.delete(`/drivers/${id}`);
            showMessage('Driver deleted successfully!');
            fetchDrivers(0); 
        } catch (error) {
            showMessage('Error deleting driver: \n' + error.message, true);
        }
    }
}
