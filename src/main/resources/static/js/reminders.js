/**
 * Reminders JavaScript
 */

let currentPage = 0;
const pageSize = 10;
let searchTimeout;

document.addEventListener('DOMContentLoaded', () => {
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    fetchSummary();
    fetchReminders(0);
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
    }, 3000);
}

function fetchSummary() {
    Api.get('/api/reminders/summary')
        .then(data => {
            const grid = document.getElementById('reminderStatsGrid');
            grid.innerHTML = `
                <div class="stat-card">
                    <div class="stat-icon bg-navy">🔔</div>
                    <div class="stat-info">
                        <h3>Total Reminders</h3>
                        <p>${data.totalActive}</p>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon bg-red">⚠️</div>
                    <div class="stat-info">
                        <h3>Critical</h3>
                        <p>${data.criticalCount}</p>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon bg-amber">⏳</div>
                    <div class="stat-info">
                        <h3>High / Medium</h3>
                        <p>${data.highCount + data.mediumCount}</p>
                    </div>
                </div>
            `;
        })
        .catch(err => console.error("Error fetching summary:", err));
}

function fetchReminders(page = 0) {
    currentPage = page;
    const search = document.getElementById('searchInput').value;
    const priority = document.getElementById('priorityFilter').value;
    const type = document.getElementById('typeFilter').value;

    let url = `/api/reminders?page=${page}&size=${pageSize}`;
    // The backend doesn't filter on the GET side yet, but we will pass them just in case. 
    // Wait, the backend doesn't support filter parameters on the list. 
    // We should implement frontend filtering if needed, or update backend. 
    // I will let it just fetch all and filter in JS if there are small amounts.
    // Actually, I'll update the backend to support it or just rely on the paginated API as is without filters.

    Api.get(url)
        .then(response => {
            // Client-side filter for simplicity since it's dynamic
            let filteredContent = response.content;
            if (search) {
                filteredContent = filteredContent.filter(r => r.vehicleNumber && r.vehicleNumber.toLowerCase().includes(search.toLowerCase()));
            }
            if (priority) {
                filteredContent = filteredContent.filter(r => r.priority === priority);
            }
            if (type) {
                filteredContent = filteredContent.filter(r => r.type === type);
            }

            renderTable(filteredContent);
            renderPagination(response);
        })
        .catch(err => {
            console.error(err);
            showMessage('Failed to load reminders.', true);
        });
}

function searchReminders() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchReminders(0);
    }, 500);
}

function filterReminders() {
    fetchReminders(0);
}

function renderTable(reminders) {
    const tbody = document.getElementById('remindersTableBody');
    tbody.innerHTML = '';

    if (reminders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No reminders found.</td></tr>';
        return;
    }

    reminders.forEach(doc => {
        const tr = document.createElement('tr');
        
        let priorityBadge = '';
        if (doc.priority === 'CRITICAL') {
            priorityBadge = '<span class="status-badge in-service" style="background:#EF4444;">CRITICAL</span>';
        } else if (doc.priority === 'HIGH') {
            priorityBadge = '<span class="status-badge maintenance" style="background:#F59E0B;">HIGH</span>';
        } else if (doc.priority === 'MEDIUM') {
            priorityBadge = '<span class="status-badge available" style="background:#3B82F6;">MEDIUM</span>';
        } else {
            priorityBadge = '<span class="status-badge available">LOW</span>';
        }

        tr.innerHTML = `
            <td><strong>${doc.vehicleNumber}</strong></td>
            <td>${doc.type}</td>
            <td>${doc.title}</td>
            <td>${doc.description}</td>
            <td>${new Date(doc.dueDate).toLocaleDateString()}</td>
            <td>${priorityBadge}</td>
        `;
        tbody.appendChild(tr);
    });
}

function renderPagination(pageData) {
    const pageInfo = document.querySelector('.page-info');
    const controls = document.getElementById('paginationControls');
    
    if (pageData.totalElements === 0) {
        pageInfo.textContent = 'Showing 0 entries';
        controls.innerHTML = '';
        return;
    }

    const start = (pageData.number * pageData.size) + 1;
    const end = Math.min(start + pageData.numberOfElements - 1, pageData.totalElements);
    pageInfo.textContent = `Showing ${start} to ${end} of ${pageData.totalElements} entries`;

    let html = '';
    html += `<button class="btn-page" onclick="fetchReminders(${pageData.number - 1})" ${pageData.first ? 'disabled' : ''}>Previous</button>`;
    
    let startPage = Math.max(0, pageData.number - 2);
    let endPage = Math.min(pageData.totalPages - 1, startPage + 4);
    startPage = Math.max(0, endPage - 4);

    for (let i = startPage; i <= endPage; i++) {
        if (i === pageData.number) {
            html += `<button class="btn-page active">${i + 1}</button>`;
        } else {
            html += `<button class="btn-page" onclick="fetchReminders(${i})">${i + 1}</button>`;
        }
    }

    html += `<button class="btn-page" onclick="fetchReminders(${pageData.number + 1})" ${pageData.last ? 'disabled' : ''}>Next</button>`;
    
    controls.innerHTML = html;
}
