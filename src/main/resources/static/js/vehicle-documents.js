/**
 * Vehicle Documents JavaScript
 */

let currentPage = 0;
const pageSize = 10;
let searchTimeout;

document.addEventListener('DOMContentLoaded', () => {
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    fetchDocuments(0);
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

function fetchDocuments(page = 0) {
    currentPage = page;
    const vehicleId = document.getElementById('searchInput').value;
    const documentType = document.getElementById('typeFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;

    let url = `/documents?page=${page}&size=${pageSize}`;
    if (vehicleId) url += `&vehicleId=${vehicleId}`;
    if (documentType) url += `&documentType=${documentType}`;
    if (statusFilter) url += `&statusFilter=${statusFilter}`;

    Api.get(url)
        .then(response => {
            renderTable(response.content);
            renderPagination(response);
        })
        .catch(err => {
            console.error(err);
            showMessage('Failed to load documents.', true);
        });
}

function searchDocuments() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        fetchDocuments(0);
    }, 500);
}

function filterDocuments() {
    fetchDocuments(0);
}

function renderTable(documents) {
    const tbody = document.getElementById('documentsTableBody');
    tbody.innerHTML = '';

    if (documents.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No documents found.</td></tr>';
        return;
    }

    documents.forEach(doc => {
        const tr = document.createElement('tr');
        
        let statusBadge = '';
        if (doc.status === 'VALID') {
            statusBadge = '<span class="status-badge available">VALID</span>';
        } else if (doc.status === 'EXPIRING_SOON') {
            statusBadge = '<span class="status-badge maintenance">EXPIRING SOON</span>';
        } else {
            statusBadge = '<span class="status-badge in-service" style="background:#EF4444;">EXPIRED</span>';
        }

        tr.innerHTML = `
            <td>#${doc.vehicle.vehicleNumber}</td>
            <td>${doc.documentType}</td>
            <td>${doc.documentNumber || '-'}</td>
            <td>${doc.issueDate ? new Date(doc.issueDate).toLocaleDateString() : '-'}</td>
            <td>${new Date(doc.expiryDate).toLocaleDateString()}</td>
            <td>${statusBadge}</td>
            <td>${new Date(doc.uploadedAt).toLocaleDateString()}</td>
            <td class="action-cell">
                <a href="/api/documents/${doc.id}/download" target="_blank" class="btn-action edit" title="Download">⬇️</a>
                <button class="btn-action delete" onclick="deleteDocument(${doc.id})" title="Delete">🗑️</button>
            </td>
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
    
    html += `<button class="btn-page" onclick="fetchDocuments(${pageData.number - 1})" ${pageData.first ? 'disabled' : ''}>Previous</button>`;
    
    let startPage = Math.max(0, pageData.number - 2);
    let endPage = Math.min(pageData.totalPages - 1, startPage + 4);
    startPage = Math.max(0, endPage - 4);

    for (let i = startPage; i <= endPage; i++) {
        if (i === pageData.number) {
            html += `<button class="btn-page active">${i + 1}</button>`;
        } else {
            html += `<button class="btn-page" onclick="fetchDocuments(${i})">${i + 1}</button>`;
        }
    }

    html += `<button class="btn-page" onclick="fetchDocuments(${pageData.number + 1})" ${pageData.last ? 'disabled' : ''}>Next</button>`;
    
    controls.innerHTML = html;
}

function openAddModal() {
    document.getElementById('documentForm').reset();
    document.getElementById('modalTitle').textContent = 'Upload Document';
    document.getElementById('documentModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('documentModal').style.display = 'none';
}

function handleFormSubmit(e) {
    e.preventDefault();
    
    const form = document.getElementById('documentForm');
    const formData = new FormData(form);

    fetch('/api/documents/upload', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(async (response) => {
        if (response.ok) {
            closeModal();
            showMessage('Document uploaded successfully.');
            fetchDocuments(currentPage);
        } else {
            const data = await response.json();
            showMessage(data.message || 'Validation failed.', true);
        }
    })
    .catch(err => {
        console.error(err);
        showMessage('Error saving document.', true);
    });
}

function deleteDocument(id) {
    if (confirm('Are you sure you want to delete this document?')) {
        Api.delete(`/documents/${id}`)
            .then(() => {
                showMessage('Document deleted successfully.');
                fetchDocuments(currentPage);
            })
            .catch(err => {
                showMessage(err.message || 'Failed to delete.', true);
            });
    }
}
