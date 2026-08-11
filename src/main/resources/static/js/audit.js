document.addEventListener('DOMContentLoaded', () => {
    let currentPage = 0;
    const pageSize = 15;

    function fetchAuditLogs(page = 0) {
        const searchInput = document.getElementById('searchInput').value.trim();
        let url = `/api/audit-logs?page=${page}&size=${pageSize}`;
        
        // Simple search mapping (server supports username, action, entityName)
        if (searchInput) {
            url += `&action=${encodeURIComponent(searchInput)}`; 
            // In a real app we'd have dedicated inputs, but we'll map general search to 'action' for simplicity
        }

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        throw new Error('Access Denied. Only ADMIN can view audit logs.');
                    }
                    throw new Error('Failed to fetch audit logs');
                }
                return response.json();
            })
            .then(data => {
                renderTable(data.content);
                renderPagination(data);
                currentPage = data.number;
            })
            .catch(error => {
                console.error(error);
                const tableBody = document.getElementById('auditTableBody');
                tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${error.message}</td></tr>`;
            });
    }

    function renderTable(logs) {
        const tableBody = document.getElementById('auditTableBody');
        tableBody.innerHTML = '';

        if (!logs || logs.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center;">No audit logs found.</td></tr>`;
            return;
        }

        logs.forEach(log => {
            const timestamp = new Date(log.timestamp).toLocaleString();
            
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${timestamp}</td>
                <td><strong>${log.username}</strong></td>
                <td><span class="status-badge" style="background: #eef2ff; color: #4f46e5;">${log.action}</span></td>
                <td>${log.entityName} ${log.entityId ? `(#${log.entityId})` : ''}</td>
                <td>${log.description || '-'}</td>
                <td>
                    ${(log.oldValue || log.newValue) ? 
                        `<button class="expand-btn" onclick="toggleDetails(${log.id})">View JSON</button>` 
                        : '<span style="color: #999;">N/A</span>'}
                </td>
            `;
            tableBody.appendChild(tr);

            if (log.oldValue || log.newValue) {
                const detailsTr = document.createElement('tr');
                detailsTr.id = `details-${log.id}`;
                detailsTr.className = 'details-row';
                
                const oldFmt = formatJson(log.oldValue);
                const newFmt = formatJson(log.newValue);

                detailsTr.innerHTML = `
                    <td colspan="6" class="details-cell">
                        <div class="diff-container">
                            <div>
                                <strong>Old Value (Before)</strong>
                                <div class="json-viewer">${oldFmt}</div>
                            </div>
                            <div>
                                <strong>New Value (After)</strong>
                                <div class="json-viewer">${newFmt}</div>
                            </div>
                        </div>
                    </td>
                `;
                tableBody.appendChild(detailsTr);
            }
        });
    }

    function renderPagination(data) {
        const pagination = document.getElementById('paginationControls');
        pagination.innerHTML = '';

        if (data.totalPages <= 1) return;

        if (!data.first) {
            const prevBtn = document.createElement('button');
            prevBtn.className = 'btn-secondary';
            prevBtn.textContent = 'Previous';
            prevBtn.onclick = () => fetchAuditLogs(data.number - 1);
            pagination.appendChild(prevBtn);
        }

        const info = document.createElement('span');
        info.textContent = ` Page ${data.number + 1} of ${data.totalPages} `;
        info.style.margin = '0 15px';
        pagination.appendChild(info);

        if (!data.last) {
            const nextBtn = document.createElement('button');
            nextBtn.className = 'btn-secondary';
            nextBtn.textContent = 'Next';
            nextBtn.onclick = () => fetchAuditLogs(data.number + 1);
            pagination.appendChild(nextBtn);
        }
    }

    function formatJson(str) {
        if (!str) return 'null';
        try {
            if (str.startsWith('Error serializing')) return str;
            const obj = JSON.parse(str);
            return JSON.stringify(obj, null, 2);
        } catch (e) {
            return str;
        }
    }

    document.getElementById('searchBtn').addEventListener('click', () => {
        fetchAuditLogs(0);
    });

    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            fetchAuditLogs(0);
        }
    });

    // Initial fetch
    fetchAuditLogs(0);
});

window.toggleDetails = function(id) {
    const row = document.getElementById(`details-${id}`);
    if (row) {
        row.classList.toggle('active');
    }
};
