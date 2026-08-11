// charts.js - Renders dashboard analytics charts

function renderCharts(stats) {
    if (typeof Chart === 'undefined') {
        console.error("Chart.js is not loaded.");
        return;
    }

    // 1. Monthly Fuel Expense Chart
    const fuelExpenseCtx = document.getElementById('fuelExpenseChart');
    if (fuelExpenseCtx && stats.monthlyFuelExpenseChart) {
        new Chart(fuelExpenseCtx, {
            type: 'bar',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Fuel Expense ($)',
                    data: stats.monthlyFuelExpenseChart,
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    }

    // 2. Vehicle Status Distribution Chart
    const statusCtx = document.getElementById('statusChart');
    if (statusCtx && stats.vehicleStatusDistributionChart) {
        const labels = Object.keys(stats.vehicleStatusDistributionChart);
        const data = Object.values(stats.vehicleStatusDistributionChart);
        
        new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: [
                        'rgba(75, 192, 192, 0.6)', // Available
                        'rgba(54, 162, 235, 0.6)', // In Service
                        'rgba(255, 206, 86, 0.6)'  // Maintenance
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    }

    // 3. Maintenance Trend Chart
    const maintenanceCtx = document.getElementById('maintenanceTrendChart');
    if (maintenanceCtx && stats.maintenanceTrendChart) {
        new Chart(maintenanceCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Maintenance Count',
                    data: stats.maintenanceTrendChart,
                    fill: false,
                    borderColor: 'rgba(255, 159, 64, 1)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, ticks: { stepSize: 1 } }
                }
            }
        });
    }
}
