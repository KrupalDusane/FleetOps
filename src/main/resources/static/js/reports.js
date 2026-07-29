/**
 * Reports & Analytics JavaScript
 * Fetches data across all modules to build a unified frontend dashboard.
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Set Current Date
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    await loadReportData();
});

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount || 0);
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

async function loadReportData() {
    try {
        // Fetch max 1000 records from each module to build stats purely on frontend
        // Using Promise.all for concurrent fetching
        const [vehiclesRes, driversRes, fuelRes, maintRes] = await Promise.all([
            Api.get('/vehicles', { size: 1000 }),
            Api.get('/drivers', { size: 1000 }),
            Api.get('/fuel-logs', { size: 1000 }),
            Api.get('/maintenance', { size: 1000 })
        ]);

        // Normalize paginated data vs array data
        const vehicles = Array.isArray(vehiclesRes) ? vehiclesRes : (vehiclesRes.content || []);
        const drivers = Array.isArray(driversRes) ? driversRes : (driversRes.content || []);
        const fuelLogs = Array.isArray(fuelRes) ? fuelRes : (fuelRes.content || []);
        const maintLogs = Array.isArray(maintRes) ? maintRes : (maintRes.content || []);

        hideLoading();
        buildCards(vehicles, drivers, fuelLogs, maintLogs);
        buildCharts(vehicles, fuelLogs, maintLogs);
        buildActivityTable(fuelLogs, maintLogs);

    } catch (error) {
        document.getElementById('loadingOverlay').innerHTML = `
            <div style="color: #EF4444; text-align: center;">
                <h3>Error Loading Dashboard Data</h3>
                <p>${error.message}</p>
            </div>
        `;
    }
}

function hideLoading() {
    document.getElementById('loadingOverlay').classList.remove('active');
    document.getElementById('reportsStatsGrid').style.display = 'grid';
    document.getElementById('reportsChartsRow').style.display = 'grid';
    document.getElementById('reportsActivityRow').style.display = 'block';
}

function buildCards(vehicles, drivers, fuelLogs, maintLogs) {
    // Total Vehicles
    document.getElementById('kpiTotalVehicles').textContent = vehicles.length;
    
    // Active Drivers (ON_DUTY)
    const activeDrivers = drivers.filter(d => d.status === 'ON_DUTY').length;
    document.getElementById('kpiActiveDrivers').textContent = activeDrivers;
    
    // Total Fuel
    const totalFuel = fuelLogs.reduce((sum, log) => sum + (log.totalCost || 0), 0);
    document.getElementById('kpiTotalFuel').textContent = formatCurrency(totalFuel);
    
    // Total Maintenance
    const totalMaint = maintLogs.reduce((sum, log) => sum + (log.cost || 0), 0);
    document.getElementById('kpiTotalMaintenance').textContent = formatCurrency(totalMaint);
}

function buildCharts(vehicles, fuelLogs, maintLogs) {
    // 1. Vehicle Status Doughnut Chart
    const statusCounts = { 'AVAILABLE': 0, 'IN_SERVICE': 0, 'MAINTENANCE': 0 };
    vehicles.forEach(v => {
        if (statusCounts[v.status] !== undefined) {
            statusCounts[v.status]++;
        }
    });

    const ctxStatus = document.getElementById('vehicleStatusChart').getContext('2d');
    new Chart(ctxStatus, {
        type: 'doughnut',
        data: {
            labels: ['Available', 'In Service', 'Maintenance'],
            datasets: [{
                data: [statusCounts['AVAILABLE'], statusCounts['IN_SERVICE'], statusCounts['MAINTENANCE']],
                backgroundColor: ['#10B981', '#00D4FF', '#F59E0B'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });

    // 2. Trend Bar Chart (Aggregating costs by month for current year)
    const currentYear = new Date().getFullYear();
    const monthlyFuel = new Array(12).fill(0);
    const monthlyMaint = new Array(12).fill(0);

    fuelLogs.forEach(log => {
        if (!log.fuelDate) return;
        const d = new Date(log.fuelDate);
        if (d.getFullYear() === currentYear) {
            monthlyFuel[d.getMonth()] += (log.totalCost || 0);
        }
    });

    maintLogs.forEach(log => {
        if (!log.serviceDate) return;
        const d = new Date(log.serviceDate);
        if (d.getFullYear() === currentYear) {
            monthlyMaint[d.getMonth()] += (log.cost || 0);
        }
    });

    const ctxTrend = document.getElementById('costTrendChart').getContext('2d');
    new Chart(ctxTrend, {
        type: 'bar',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            datasets: [
                {
                    label: 'Fuel Cost',
                    data: monthlyFuel,
                    backgroundColor: 'rgba(0, 212, 255, 0.7)',
                    borderRadius: 4
                },
                {
                    label: 'Maintenance Cost',
                    data: monthlyMaint,
                    backgroundColor: 'rgba(239, 68, 68, 0.7)',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) { return '$' + value; }
                    }
                }
            },
            plugins: {
                legend: { position: 'top' }
            }
        }
    });
}

function buildActivityTable(fuelLogs, maintLogs) {
    const tableBody = document.getElementById('activityTableBody');
    tableBody.innerHTML = '';

    // Normalize and combine arrays
    const combined = [];
    fuelLogs.forEach(log => {
        combined.push({
            date: new Date(log.fuelDate),
            module: 'FUEL',
            desc: `Refueled ${log.vehicle ? log.vehicle.vehicleNumber : 'Vehicle'} (${log.fuelQuantity}L)`,
            cost: log.totalCost || 0
        });
    });

    maintLogs.forEach(log => {
        combined.push({
            date: new Date(log.serviceDate),
            module: 'MAINTENANCE',
            desc: `Service at ${log.garage} for ${log.vehicle ? log.vehicle.vehicleNumber : 'Vehicle'}`,
            cost: log.cost || 0
        });
    });

    // Sort descending by date
    combined.sort((a, b) => b.date - a.date);

    // Take top 5
    const recent = combined.slice(0, 5);

    if (recent.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:20px; color:#64748B;">No recent activity found.</td></tr>`;
        return;
    }

    recent.forEach(act => {
        const badgeClass = act.module === 'FUEL' ? 'module-fuel' : 'module-maint';
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(act.date)}</td>
            <td><span class="module-badge ${badgeClass}">${act.module}</span></td>
            <td>${act.desc}</td>
            <td class="money-text">${formatCurrency(act.cost)}</td>
        `;
        tableBody.appendChild(row);
    });
}
