document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Set Current Date
    const dateElement = document.getElementById('currentDate');
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    dateElement.textContent = new Date().toLocaleDateString('en-US', options);

    // 2. Load Placeholder Statistics
    const statsData = [
        { title: 'Total Vehicles', value: '124', icon: '🚚', colorClass: 'bg-navy' },
        { title: 'Available', value: '86', icon: '✅', colorClass: 'bg-green' },
        { title: 'Under Maintenance', value: '12', icon: '🔧', colorClass: 'bg-amber' },
        { title: 'In Service', value: '26', icon: '🛣️', colorClass: 'bg-red' }
    ];

    const statsGrid = document.getElementById('statsGrid');
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

    // 3. Load Placeholder Recent Vehicles Table
    const recentVehicles = [
        { number: 'FLT-1042', brand: 'Volvo', model: 'VNL 860', status: 'AVAILABLE', statusClass: 'status-available' },
        { number: 'FLT-2099', brand: 'Freightliner', model: 'Cascadia', status: 'IN SERVICE', statusClass: 'status-inservice' },
        { number: 'FLT-3011', brand: 'Kenworth', model: 'T680', status: 'MAINTENANCE', statusClass: 'status-maintenance' },
        { number: 'FLT-1088', brand: 'Peterbilt', model: '579', status: 'AVAILABLE', statusClass: 'status-available' },
        { number: 'FLT-4005', brand: 'Mack', model: 'Anthem', status: 'IN SERVICE', statusClass: 'status-inservice' }
    ];

    const tableBody = document.getElementById('recentVehiclesTableBody');
    recentVehicles.forEach(vehicle => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${vehicle.number}</strong></td>
            <td>${vehicle.brand}</td>
            <td>${vehicle.model}</td>
            <td><span class="status-badge ${vehicle.statusClass}">${vehicle.status}</span></td>
            <td><button class="btn-action" onclick="alert('Viewing ${vehicle.number}')">View Details</button></td>
        `;
        tableBody.appendChild(row);
    });

});
