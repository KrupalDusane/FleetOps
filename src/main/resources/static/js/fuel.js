/**
 * Fuel Logs Management JavaScript
 */

let currentPage = 0;
const pageSize = 10;
let currentSort = 'fuelDate,desc';
let currentFuelLogId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    await loadVehicleFilters();
    fetchFuelLogs(0);
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
    const tableBody = document.getElementById('fuelTableBody');
    tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #64748B; font-weight: 500;">Loading fuel logs...</td></tr>`;
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

async function fetchFuelLogs(page = 0) {
    showLoading();
    currentPage = page;
    
    const dateFilter = document.getElementById('dateFilter').value;
    const vehicleFilter = document.getElementById('vehicleFilter').value;
    const minCostFilter = document.getElementById('minCostFilter')?.value;
    const maxCostFilter = document.getElementById('maxCostFilter')?.value;
    const minQtyFilter = document.getElementById('minQtyFilter')?.value;
    const maxQtyFilter = document.getElementById('maxQtyFilter')?.value;
    
    const params = { page: currentPage, size: pageSize, sort: currentSort };
    let endpoint = '/fuel-logs';
    
    if (dateFilter || vehicleFilter || minCostFilter || maxCostFilter || minQtyFilter || maxQtyFilter) {
        endpoint = '/fuel-logs/search';
        if (dateFilter) params.fuelDate = dateFilter;
        if (vehicleFilter) params.vehicleId = vehicleFilter;
        if (minCostFilter) params.minCost = minCostFilter;
        if (maxCostFilter) params.maxCost = maxCostFilter;
        if (minQtyFilter) params.minQty = minQtyFilter;
        if (maxQtyFilter) params.maxQty = maxQtyFilter;
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
        document.getElementById('fuelTableBody').innerHTML = 
            `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #EF4444; font-weight: 500;">Failed to load fuel logs.<br>${error.message}</td></tr>`;
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
    const tableBody = document.getElementById('fuelTableBody');
    tableBody.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 30px; color: #64748B;">No fuel logs found.</td></tr>`;
        renderPagination(pageData);
        return;
    }

    pageData.content.forEach(log => {
        const vehicleInfo = log.vehicle ? `${log.vehicle.vehicleNumber}` : 'Unknown';
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(log.fuelDate)}</td>
            <td><strong>${vehicleInfo}</strong></td>
            <td>${log.odometerAtFueling.toLocaleString()}</td>
            <td>${log.fuelQuantity.toFixed(2)} L</td>
            <td>${formatCurrency(log.pricePerLitre)}</td>
            <td class="money-text">${formatCurrency(log.totalCost)}</td>
            <td>
                <button class="btn-action" onclick="openEditModal(${log.id})">Edit</button>
                <button class="btn-danger" onclick="deleteFuelLog(${log.id})">Delete</button>
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
                    <button class="btn-page" ${pageData.first ? 'disabled' : ''} onclick="fetchFuelLogs(${pageData.number - 1})">Previous</button>`;
    
    for (let i = 0; i < (pageData.totalPages || 1); i++) {
        html += `<button class="btn-page ${i === pageData.number ? 'active' : ''}" onclick="fetchFuelLogs(${i})">${i + 1}</button>`;
    }
    
    html += `<button class="btn-page" ${pageData.last ? 'disabled' : ''} onclick="fetchFuelLogs(${pageData.number + 1})">Next</button>
             </div>`;
             
    paginationDiv.innerHTML = html;
}

function filterFuelLogs() {
    fetchFuelLogs(0);
}

function clearFilters() {
    document.getElementById('dateFilter').value = '';
    document.getElementById('vehicleFilter').value = '';
    if (document.getElementById('minCostFilter')) document.getElementById('minCostFilter').value = '';
    if (document.getElementById('maxCostFilter')) document.getElementById('maxCostFilter').value = '';
    if (document.getElementById('minQtyFilter')) document.getElementById('minQtyFilter').value = '';
    if (document.getElementById('maxQtyFilter')) document.getElementById('maxQtyFilter').value = '';
    fetchFuelLogs(0);
}

let filterTimeout;
function delayFilter() {
    clearTimeout(filterTimeout);
    filterTimeout = setTimeout(() => {
        fetchFuelLogs(0);
    }, 500);
}

/* Modal Logic */
const modal = document.getElementById('fuelModal');
const form = document.getElementById('fuelForm');

function calculateTotal() {
    const qty = parseFloat(document.getElementById('fuelQuantity').value) || 0;
    const price = parseFloat(document.getElementById('pricePerLitre').value) || 0;
    const total = qty * price;
    document.getElementById('totalCostDisplay').textContent = formatCurrency(total);
}

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
    document.getElementById('modalTitle').textContent = 'Add Fuel Entry';
    form.reset();
    document.getElementById('fuelLogId').value = '';
    currentFuelLogId = null;
    document.getElementById('totalCostDisplay').textContent = '₹0.00';
    
    // Default to today
    document.getElementById('fuelDate').value = new Date().toISOString().split('T')[0];
    
    await populateVehicleSelect();
    modal.classList.add('active');
}

async function openEditModal(id) {
    try {
        const log = await Api.get(`/fuel-logs/${id}`);
        
        document.getElementById('modalTitle').textContent = 'Edit Fuel Entry';
        document.getElementById('fuelLogId').value = log.id;
        currentFuelLogId = log.id;
        
        document.getElementById('fuelDate').value = log.fuelDate;
        document.getElementById('odometerAtFueling').value = log.odometerAtFueling;
        document.getElementById('fuelQuantity').value = log.fuelQuantity;
        document.getElementById('pricePerLitre').value = log.pricePerLitre;
        
        calculateTotal();
        await populateVehicleSelect(log.vehicle ? log.vehicle.id : null);

        modal.classList.add('active');
    } catch (error) {
        showMessage('Error fetching fuel log details: \n' + error.message, true);
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

    const payload = {
        fuelDate: document.getElementById('fuelDate').value,
        odometerAtFueling: parseInt(document.getElementById('odometerAtFueling').value),
        fuelQuantity: parseFloat(document.getElementById('fuelQuantity').value),
        pricePerLitre: parseFloat(document.getElementById('pricePerLitre').value),
        vehicle: { id: parseInt(document.getElementById('vehicleId').value) }
    };

    try {
        if (currentFuelLogId) {
            await Api.put(`/fuel-logs/${currentFuelLogId}`, payload);
            showMessage('Fuel entry updated successfully!');
        } else {
            await Api.post('/fuel-logs', payload);
            showMessage('Fuel entry created successfully!');
        }
        
        closeModal();
        fetchFuelLogs(currentPage);
    } catch (error) {
        showMessage('Error saving fuel entry: \n' + error.message, true);
    } finally {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

async function deleteFuelLog(id) {
    if (confirm("Are you sure you want to delete this fuel record? This cannot be undone.")) {
        try {
            await Api.delete(`/fuel-logs/${id}`);
            showMessage('Fuel entry deleted successfully!');
            fetchFuelLogs(0); 
        } catch (error) {
            showMessage('Error deleting fuel entry: \n' + error.message, true);
        }
    }
}
