let currentPage = 0;
const pageSize = 15;

document.addEventListener('DOMContentLoaded', () => {
    fetchDeletedRecords(0);
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
    toast.textContent = msg;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function fetchDeletedRecords(page = 0) {
    currentPage = page;
    const entityType = document.getElementById('entityTypeFilter').value;
    const tableBody = document.getElementById('deletedRecordsTableBody');
    const tableHeaders = document.getElementById('tableHeaders');

    tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 20px;">Loading...</td></tr>`;

    // Dynamic headers based on entity type
    if (entityType === 'vehicles') {
        tableHeaders.innerHTML = `<th>ID</th><th>Vehicle No / Brand</th><th>Deleted By</th><th>Deleted At</th><th>Action</th>`;
    } else if (entityType === 'drivers') {
        tableHeaders.innerHTML = `<th>ID</th><th>Name / License</th><th>Deleted By</th><th>Deleted At</th><th>Action</th>`;
    } else if (entityType === 'fuel-logs') {
        tableHeaders.innerHTML = `<th>ID</th><th>Vehicle / Date / Cost</th><th>Deleted By</th><th>Deleted At</th><th>Action</th>`;
    } else if (entityType === 'maintenance') {
        tableHeaders.innerHTML = `<th>ID</th><th>Garage / Date / Cost</th><th>Deleted By</th><th>Deleted At</th><th>Action</th>`;
    }

    Api.get(`/admin/deleted/${entityType}?page=${page}&size=${pageSize}`)
        .then(data => {
            renderTable(data, entityType);
        })
        .catch(err => {
            tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #EF4444;">Failed to load records. ${err.message}</td></tr>`;
            showMessage('Error loading records: ' + err.message, true);
        });
}

function renderTable(pageData, entityType) {
    const tableBody = document.getElementById('deletedRecordsTableBody');
    tableBody.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 20px; color: #666;">No deleted records found.</td></tr>`;
        renderPagination(pageData);
        return;
    }

    pageData.content.forEach(record => {
        let identifier = 'Unknown';
        
        if (entityType === 'vehicles') {
            identifier = `<strong>${record.vehicleNumber}</strong> - ${record.brand} ${record.model}`;
        } else if (entityType === 'drivers') {
            identifier = `<strong>${record.name}</strong> - ${record.licenseNumber}`;
        } else if (entityType === 'fuel-logs') {
            let vNo = record.vehicle ? record.vehicle.vehicleNumber : 'Unknown';
            identifier = `<strong>Vehicle: ${vNo}</strong> - ${record.fuelDate} - $${record.totalCost || 0}`;
        } else if (entityType === 'maintenance') {
            identifier = `<strong>${record.garage}</strong> - ${record.serviceDate} - $${record.cost || 0}`;
        }

        const dateStr = record.deletedAt ? new Date(record.deletedAt).toLocaleString() : 'N/A';
        const byUser = record.deletedBy || 'System';

        // Endpoint type for restoration is slightly different for fuel-log
        let restoreType = entityType;
        if (entityType === 'vehicles') restoreType = 'vehicle';
        if (entityType === 'drivers') restoreType = 'driver';
        if (entityType === 'fuel-logs') restoreType = 'fuel-log';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${record.id}</td>
            <td>${identifier}</td>
            <td>${byUser}</td>
            <td>${dateStr}</td>
            <td>
                <button class="restore-btn" onclick="restoreRecord('${restoreType}', ${record.id})">Restore</button>
            </td>
        `;
        tableBody.appendChild(row);
    });

    renderPagination(pageData);
}

function restoreRecord(entityType, id) {
    if (!confirm('Are you sure you want to restore this record?')) return;

    Api.post(`/admin/restore/${entityType}/${id}`, {})
    .then(() => {
        showMessage('Record restored successfully!');
        fetchDeletedRecords(currentPage);
    })
    .catch(err => {
        showMessage('Error: ' + err.message, true);
    });
}

function renderPagination(pageData) {
    const paginationDiv = document.getElementById('paginationControls');
    
    if (!pageData || pageData.totalElements === 0) {
        paginationDiv.innerHTML = `<span class="page-info">Showing 0 entries</span><div class="page-buttons"></div>`;
        return;
    }

    const start = pageData.number * pageData.size + 1;
    const end = Math.min(start + pageData.numberOfElements - 1, pageData.totalElements);
    
    let html = `<span class="page-info">Showing ${start} to ${end} of ${pageData.totalElements} entries</span>
                <div class="page-buttons">
                    <button class="btn-page" ${pageData.first ? 'disabled' : ''} onclick="fetchDeletedRecords(${pageData.number - 1})">Previous</button>`;
    
    for (let i = 0; i < (pageData.totalPages || 1); i++) {
        html += `<button class="btn-page ${i === pageData.number ? 'active' : ''}" onclick="fetchDeletedRecords(${i})">${i + 1}</button>`;
    }
    
    html += `<button class="btn-page" ${pageData.last ? 'disabled' : ''} onclick="fetchDeletedRecords(${pageData.number + 1})">Next</button>
             </div>`;
             
    paginationDiv.innerHTML = html;
}
