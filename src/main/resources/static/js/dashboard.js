document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Set Current Date
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    // 2. Load Dynamic Statistics
    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            const statsData = [
                { title: 'Total Vehicles', value: data.totalVehicles, icon: '🚚', colorClass: 'bg-navy' },
                { title: 'Monthly Fuel Cost', value: '₹' + (data.monthlyFuelCost || 0).toFixed(2), icon: '💰', colorClass: 'bg-red' },
                { title: 'Avg Fuel Cost/Vehicle', value: '₹' + (data.averageFuelCostPerVehicle ? data.averageFuelCostPerVehicle.toFixed(2) : 0), icon: '⛽', colorClass: 'bg-amber' },
                { title: 'Top Fuel Vehicle', value: data.topFuelConsumingVehicle || 'N/A', icon: '🏆', colorClass: 'bg-navy' },
                { title: 'Total Documents', value: data.totalDocuments, icon: '📁', colorClass: 'bg-navy' },
                { title: 'Expiring Documents', value: data.documentsExpiringSoon, icon: '⏳', colorClass: 'bg-amber' },
                { title: 'Expired Documents', value: data.expiredDocuments, icon: '⚠️', colorClass: 'bg-red' }
            ];

            const statsGrid = document.getElementById('statsGrid');
            statsGrid.innerHTML = ''; // Clear just in case
            statsData.forEach(stat => {
                const card = document.createElement('div');
                card.className = 'stat-card';
                card.innerHTML = `
                    <div class="stat-icon ${stat.colorClass}">${stat.icon}</div>
                    <div class="stat-info">
                        <h3>${stat.title}</h3>
                        <p>${stat.value}</p>
                    </div>
                `;
                statsGrid.appendChild(card);
            });

            // Render Health Engine
            if (data.fleetHealth) {
                renderHealthEngine(data.fleetHealth);
            }

            // Initialize charts if charts.js is loaded
            if (typeof renderCharts === 'function') {
                renderCharts(data);
            }
        })
        .catch(error => console.error('Error fetching dashboard stats:', error));

    // 3. Load Dynamic Recent Vehicles Table
    fetch('/api/dashboard/recent-vehicles')
        .then(response => response.json())
        .then(vehicles => {
            const tableBody = document.getElementById('recentVehiclesTableBody');
            tableBody.innerHTML = '';
            
            if (!vehicles || vehicles.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center;">No vehicles available.</td></tr>`;
                return;
            }

            vehicles.forEach(vehicle => {
                let statusClass = '';
                let statusLabel = '';
                
                if (vehicle.status === 'AVAILABLE') { 
                    statusClass = 'status-available'; 
                    statusLabel = 'Available'; 
                } else if (vehicle.status === 'IN_SERVICE') { 
                    statusClass = 'status-inservice'; 
                    statusLabel = 'In Service'; 
                } else if (vehicle.status === 'UNDER_MAINTENANCE') { 
                    statusClass = 'status-maintenance'; 
                    statusLabel = 'Under Maintenance'; 
                }

                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><strong>${vehicle.vehicleNumber}</strong></td>
                    <td>${vehicle.brand}</td>
                    <td>${vehicle.model}</td>
                    <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
                    <td><button class="btn-action" onclick="alert('Viewing ${vehicle.vehicleNumber}')">View Details</button></td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching recent vehicles:', error));

    // 4. Load Critical Reminders
    fetch('/api/reminders/critical')
        .then(response => response.json())
        .then(pageData => {
            const tableBody = document.getElementById('criticalRemindersTableBody');
            tableBody.innerHTML = '';
            
            const reminders = pageData.content || [];
            
            if (reminders.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="4" style="text-align: center;">No critical reminders.</td></tr>`;
                return;
            }

            reminders.slice(0, 5).forEach(reminder => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><strong>${reminder.vehicleNumber}</strong></td>
                    <td>${reminder.title}</td>
                    <td>${new Date(reminder.dueDate).toLocaleDateString()}</td>
                    <td><span class="status-badge in-service" style="background:#EF4444;">CRITICAL</span></td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching critical reminders:', error));
});

function renderCharts(data) {
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded.');
        return;
    }

    // 1. Monthly Fuel Trend (Line Chart)
    const ctxTrend = document.getElementById('fuelTrendChart');
    if (ctxTrend) {
        new Chart(ctxTrend, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Fuel Expense (₹)',
                    data: data.monthlyFuelExpenseChart || [],
                    borderColor: '#1e293b',
                    backgroundColor: 'rgba(30, 41, 59, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    }

    // 2. Top Fuel Consuming Vehicles (Bar Chart)
    // We need to fetch vehicle summaries because dashboard stats doesn't return full list.
    const ctxVehicles = document.getElementById('fuelByVehicleChart');
    if (ctxVehicles) {
        fetch('/api/fuel/analytics/vehicle-summary')
            .then(res => res.json())
            .then(summaries => {
                // Sort by cost desc, take top 5
                summaries.sort((a, b) => b.totalFuelCost - a.totalFuelCost);
                const top5 = summaries.slice(0, 5);
                
                new Chart(ctxVehicles, {
                    type: 'bar',
                    data: {
                        labels: top5.map(s => s.vehicleNumber),
                        datasets: [{
                            label: 'Total Fuel Cost (₹)',
                            data: top5.map(s => s.totalFuelCost),
                            backgroundColor: '#EF4444'
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false
                    }
                });
            })
            .catch(err => console.error('Error loading top vehicles chart:', err));
    }
}

function renderHealthEngine(health) {
    // 1. Set Grade and Summary
    const badge = document.getElementById('healthGradeBadge');
    badge.textContent = health.grade;
    let color = '#10B981'; // Green
    if (health.grade === 'C' || health.grade === 'D') color = '#F59E0B'; // Yellow
    if (health.grade === 'F') color = '#EF4444'; // Red
    badge.style.color = color;

    document.getElementById('healthSummary').textContent = health.summary;

    // 2. Render Gauge Chart
    const ctx = document.getElementById('healthGaugeChart');
    if (ctx && typeof Chart !== 'undefined') {
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [health.score, 100 - health.score],
                    backgroundColor: [color, '#E2E8F0'],
                    borderWidth: 0,
                    circumference: 180,
                    rotation: 270
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '80%',
                plugins: {
                    tooltip: { enabled: false },
                    legend: { display: false }
                }
            }
        });
    }

    // 3. Render Strengths
    const ulStrengths = document.getElementById('healthStrengths');
    ulStrengths.innerHTML = '';
    if (health.strengths && health.strengths.length > 0) {
        health.strengths.forEach(s => {
            const li = document.createElement('li');
            li.textContent = s;
            ulStrengths.appendChild(li);
        });
    } else {
        ulStrengths.innerHTML = '<li style="color:#94A3B8;">No significant strengths detected.</li>';
    }

    // 4. Render Risks
    const ulRisks = document.getElementById('healthRisks');
    ulRisks.innerHTML = '';
    if (health.risks && health.risks.length > 0) {
        health.risks.forEach(r => {
            const li = document.createElement('li');
            li.textContent = r;
            ulRisks.appendChild(li);
        });
    } else {
        ulRisks.innerHTML = '<li style="color:#94A3B8;">No active risks detected.</li>';
    }

    // 5. Render Recommendations
    const ulRecs = document.getElementById('healthRecommendations');
    ulRecs.innerHTML = '';
    if (health.recommendations && health.recommendations.length > 0) {
        health.recommendations.forEach(rec => {
            const li = document.createElement('li');
            li.style.marginBottom = '10px';
            li.innerHTML = `
                <div style="font-weight: bold; color: #334155;">${rec.title} <span style="font-size: 10px; padding: 2px 6px; border-radius: 4px; background: #FEF2F2; color: #EF4444; margin-left: 5px;">${rec.priority}</span></div>
                <div style="color: #64748B;">${rec.description}</div>
            `;
            ulRecs.appendChild(li);
        });
    } else {
        ulRecs.innerHTML = '<li style="color:#94A3B8;">No recommendations at this time.</li>';
    }
}

