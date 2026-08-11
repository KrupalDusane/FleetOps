document.addEventListener('DOMContentLoaded', async () => {
    // Populate username
    const username = localStorage.getItem('fleetops_username');
    if (username) {
        document.getElementById('currentUser').textContent = `User: ${username}`;
    } else {
        window.location.href = '/login';
    }

    // Populate dropdowns
    await populateDropdowns();
    toggleFilters();
});

async function populateDropdowns() {
    try {
        // Vehicles
        const vData = await Api.get('/vehicles', { size: 500 });
        const vehicles = Array.isArray(vData) ? vData : (vData.content || []);
        const vSelect = document.getElementById('vehicleId');
        vehicles.forEach(v => {
            const opt = document.createElement('option');
            opt.value = v.id;
            opt.textContent = `${v.vehicleNumber} (${v.brand})`;
            vSelect.appendChild(opt);
        });

        // Drivers
        const dData = await Api.get('/drivers', { size: 500 });
        const drivers = Array.isArray(dData) ? dData : (dData.content || []);
        const dSelect = document.getElementById('driverId');
        drivers.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.firstName} ${d.lastName}`;
            dSelect.appendChild(opt);
        });
    } catch (e) {
        console.error("Error populating dropdowns", e);
    }
}

function toggleFilters() {
    const reportType = document.getElementById('reportType').value;
    
    // Hide all first
    document.getElementById('vehicleFilterGroup').style.display = 'none';
    document.getElementById('driverFilterGroup').style.display = 'none';
    document.getElementById('statusFilterGroup').style.display = 'none';
    document.getElementById('dateFilterGroup').style.display = 'none';

    if (reportType === 'VEHICLE') {
        document.getElementById('vehicleFilterGroup').style.display = 'block';
        document.getElementById('statusFilterGroup').style.display = 'block';
    } else if (reportType === 'DRIVER') {
        document.getElementById('driverFilterGroup').style.display = 'block';
        document.getElementById('statusFilterGroup').style.display = 'block';
    } else if (reportType === 'FUEL' || reportType === 'MAINTENANCE') {
        document.getElementById('vehicleFilterGroup').style.display = 'block';
        document.getElementById('dateFilterGroup').style.display = 'flex';
        if (reportType === 'MAINTENANCE') {
            document.getElementById('statusFilterGroup').style.display = 'block';
        }
    } else if (reportType === 'FLEET_HEALTH') {
        // No filters for global fleet health snapshot
    }
}

function generateReport(format) {
    const msg = document.getElementById('reportMessage');
    msg.style.display = 'none';

    const reportType = document.getElementById('reportType').value;
    
    // Collect parameters
    const params = new URLSearchParams();
    params.append('reportType', reportType);
    params.append('format', format);

    if (reportType === 'VEHICLE' || reportType === 'FUEL' || reportType === 'MAINTENANCE') {
        const vid = document.getElementById('vehicleId').value;
        if (vid) params.append('vehicleId', vid);
    }
    
    if (reportType === 'DRIVER') {
        const did = document.getElementById('driverId').value;
        if (did) params.append('driverId', did);
    }
    
    if (reportType === 'VEHICLE' || reportType === 'DRIVER' || reportType === 'MAINTENANCE') {
        const status = document.getElementById('status').value;
        if (status) params.append('status', status);
    }
    
    if (reportType === 'FUEL' || reportType === 'MAINTENANCE') {
        const sd = document.getElementById('startDate').value;
        const ed = document.getElementById('endDate').value;
        if (sd) params.append('startDate', sd);
        if (ed) params.append('endDate', ed);
    }

    const token = localStorage.getItem('fleetops_token');
    
    fetch(`/api/reports/export?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error("Access Denied: Only ADMINs can generate reports.");
            }
            throw new Error("Failed to generate report.");
        }
        
        // Extract filename from headers if possible
        let filename = `fleetops_${reportType.toLowerCase()}_report.${format}`;
        const disposition = response.headers.get('Content-Disposition');
        if (disposition && disposition.indexOf('filename=') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches != null && matches[1]) { 
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        
        return response.blob().then(blob => ({ blob, filename }));
    })
    .then(({blob, filename}) => {
        // Trigger download
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
    })
    .catch(error => {
        msg.textContent = error.message;
        msg.style.display = 'block';
    });
}
