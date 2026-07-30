/**
 * Maintenance Management JavaScript
 */

let currentPage = 0;
const pageSize = 10;
let currentSort = 'serviceDate,desc';
let currentMaintenanceId = null;
let searchTimeout;

document.addEventListener('DOMContentLoaded', async () => {
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    await loadVehicleFilters();
    fetchMaintenance(0);
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
    const tableBody = document.getElementById('maintenanceTableBody');
    tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #64748B; font-weight: 500;">Loading maintenance records...</td></tr>`;
}

async function loadVehicleFilters() {
    try {
        const pageData = await Api.get('/vehicles', { size: 500 });
        const vehicles = Array.isArray(pageData) ? pageData : (pageData.content || []);
        
        const filterSelect = document.getElementById('vehicleFilter');
        vehicles.forEach(v => {
            const option = document.createElement('option');
            option.value = v.id;
            option.textContent = `${v.vehicleNumber} (${v.brand})`;
            filterSelect.appendChild(option);
        });
    } catch (e) {
        console.error("Failed to load vehicles for filter dropdown");
    }
}

async function fetchMaintenance(page = 0) {
    showLoading();
    currentPage = page;
    
    const searchInput = document.getElementById('searchInput').value;
    const vehicleFilter = document.getElementById('vehicleFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;
    
    const params = { page: currentPage, size: pageSize, sort: currentSort };
    let endpoint = '/maintenance';
    
    if (searchInput || vehicleFilter || statusFilter) {
        endpoint = '/maintenance/search';
        if (searchInput) params.garage = searchInput;
        if (vehicleFilter) params.vehicleId = vehicleFilter;
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
        document.getElementById('maintenanceTableBody').innerHTML = 
            `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #EF4444; font-weight: 500;">Failed to load maintenance records.<br>${error.message}</td></tr>`;
        showMessage(error.message, true);
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount || 0);
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function renderTable(pageData) {
    const tableBody = document.getElementById('maintenanceTableBody');
    tableBody.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #64748B;">No maintenance records found.</td></tr>`;
        renderPagination(pageData);
        return;
    }

    pageData.content.forEach(log => {
        const vehicleInfo = log.vehicle ? `${log.vehicle.vehicleNumber}` : 'Unknown';
        
        let statusClass = '';
        let statusLabel = '';
        if (log.status === 'SCHEDULED') { statusClass = 'status-scheduled'; statusLabel = 'Scheduled'; }
        else if (log.status === 'IN_PROGRESS') { statusClass = 'status-inprogress'; statusLabel = 'In Progress'; }
        else if (log.status === 'COMPLETED') { statusClass = 'status-completed'; statusLabel = 'Completed'; }
        else if (log.status === 'CANCELLED') { statusClass = 'status-cancelled'; statusLabel = 'Cancelled'; }
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${vehicleInfo}</strong></td>
            <td>${log.garage}</td>
            <td>${formatDate(log.serviceDate)}</td>
            <td>${formatDate(log.nextServiceDate)}</td>
            <td class="money-text">${formatCurrency(log.cost)}</td>
            <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
            <td>
                <button class="btn-action" onclick="openEditModal(${log.id})">Edit</button>
                <button class="btn-danger" onclick="deleteMaintenance(${log.id})">Delete</button>
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
                    <button class="btn-page" ${pageData.first ? 'disabled' : ''} onclick="fetchMaintenance(${pageData.number - 1})">Previous</button>`;
    
    for (let i = 0; i < (pageData.totalPages || 1); i++) {
        html += `<button class="btn-page ${i === pageData.number ? 'active' : ''}" onclick="fetchMaintenance(${i})">${i + 1}</button>`;
    }
    
    html += `<button class="btn-page" ${pageData.last ? 'disabled' : ''} onclick="fetchMaintenance(${pageData.number + 1})">Next</button>
             </div>`;
             
    paginationDiv.innerHTML = html;
}

function searchMaintenance() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchMaintenance(0);
    }, 400);
}

function filterMaintenance() {
    fetchMaintenance(0);
}

function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('vehicleFilter').value = '';
    document.getElementById('statusFilter').value = '';
    fetchMaintenance(0);
}

/* Modal Logic */
const modal = document.getElementById('maintenanceModal');
const form = document.getElementById('maintenanceForm');

async function populateVehicleSelect(selectedId = null) {
    const select = document.getElementById('vehicleId');
    select.innerHTML = '<option value="">Loading vehicles...</option>';
    
    try {
        const pageData = await Api.get('/vehicles', { size: 500 });
        const vehicles = Array.isArray(pageData) ? pageData : (pageData.content || []);
        
        select.innerHTML = '<option value="">Select a Vehicle...</option>';
        vehicles.forEach(v => {
            const option = document.createElement('option');
            option.value = v.id;
            option.textContent = `${v.vehicleNumber} (${v.brand})`;
            if (selectedId && v.id === selectedId) option.selected = true;
            select.appendChild(option);
        });
    } catch (e) {
        select.innerHTML = '<option value="">Failed to load vehicles</option>';
    }
}

async function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Add Maintenance Record';
    form.reset();
    document.getElementById('maintenanceId').value = '';
    currentMaintenanceId = null;
    
    document.getElementById('serviceDate').value = new Date().toISOString().split('T')[0];
    
    await populateVehicleSelect();
    modal.classList.add('active');
}

async function openEditModal(id) {
    try {
        const log = await Api.get(`/maintenance/${id}`);
        
        document.getElementById('modalTitle').textContent = 'Edit Maintenance Record';
        document.getElementById('maintenanceId').value = log.id;
        currentMaintenanceId = log.id;
        
        document.getElementById('garage').value = log.garage;
        document.getElementById('cost').value = log.cost;
        document.getElementById('serviceDate').value = log.serviceDate;
        document.getElementById('nextServiceDate').value = log.nextServiceDate;
        document.getElementById('status').value = log.status;
        
        await populateVehicleSelect(log.vehicle ? log.vehicle.id : null);

        modal.classList.add('active');
    } catch (error) {
        showMessage('Error fetching maintenance details: \n' + error.message, true);
    }
}

function closeModal() {
    modal.classList.remove('active');
}

async function handleFormSubmit(event) {
    event.preventDefault();

    const serviceDate = document.getElementById('serviceDate').value;
    const nextServiceDate = document.getElementById('nextServiceDate').value;

    if (new Date(nextServiceDate) <= new Date(serviceDate)) {
        showMessage('Next Scheduled Service must be after the Service Date.', true);
        return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Saving...';
    submitBtn.disabled = true;

    const payload = {
        garage: document.getElementById('garage').value,
        cost: parseFloat(document.getElementById('cost').value),
        serviceDate: serviceDate,
        nextServiceDate: nextServiceDate,
        status: document.getElementById('status').value,
        vehicle: { id: parseInt(document.getElementById('vehicleId').value) }
    };

    try {
        if (currentMaintenanceId) {
            await Api.put(`/maintenance/${currentMaintenanceId}`, payload);
            showMessage('Maintenance record updated successfully!');
        } else {
            await Api.post('/maintenance', payload);
            showMessage('Maintenance record created successfully!');
        }
        
        closeModal();
        fetchMaintenance(currentPage);
    } catch (error) {
        showMessage('Error saving maintenance record: \n' + error.message, true);
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

async function deleteMaintenance(id) {
    if (confirm("Are you sure you want to delete this maintenance record? This cannot be undone.")) {
        try {
            await Api.delete(`/maintenance/${id}`);
            showMessage('Maintenance record deleted successfully!');
            fetchMaintenance(0); 
        } catch (error) {
            showMessage('Error deleting maintenance record: \n' + error.message, true);
        }
    }
}
