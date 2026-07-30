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
                { title: 'Available', value: data.availableVehicles, icon: '✅', colorClass: 'bg-green' },
                { title: 'Under Maintenance', value: data.underMaintenance, icon: '🔧', colorClass: 'bg-amber' },
                { title: 'In Service', value: data.inService, icon: '🛣️', colorClass: 'bg-red' }
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
});
