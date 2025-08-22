<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 04:01
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String role = (String) session.getAttribute("role");
%>

<div class="space-y-8">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-indigo-500">
            <div class="bg-indigo-100 text-indigo-600 p-3 rounded-full"><i data-feather="dollar-sign"
                                                                           class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Total Revenue</p>
                <p id="totalRevenue" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-blue-500">
            <div class="bg-blue-100 text-blue-600 p-3 rounded-full"><i data-feather="users" class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Total Customers</p>
                <p id="totalCustomers" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-green-500">
            <div class="bg-green-100 text-green-600 p-3 rounded-full"><i data-feather="package" class="w-6 h-6"></i>
            </div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Items in Stock</p>
                <p id="totalItems" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-yellow-500">
            <div class="bg-yellow-100 text-yellow-600 p-3 rounded-full"><i data-feather="file-text" class="w-6 h-6"></i>
            </div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Bills Generated</p>
                <p id="totalBills" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 bg-white p-6 rounded-lg shadow-md">
            <h3 class="text-xl font-semibold text-gray-800 mb-4">Weekly Sales Activity</h3>
            <div class="h-80">
                <canvas id="weeklySalesChart"></canvas>
            </div>
        </div>

        <div class="lg:col-span-1 space-y-8">
            <div class="bg-white p-6 rounded-lg shadow-md">
                <h3 class="text-xl font-semibold text-gray-800 mb-4">Quick Actions</h3>
                <div class="space-y-3">
                    <a href="dashboard.jsp?page=bills"
                       class="w-full flex items-center space-x-3 p-3 rounded-lg bg-gray-800 text-white hover:bg-gray-700 transition-colors"><i
                            data-feather="plus-circle" class="w-5 h-5"></i><span>Generate New Bill</span></a>
                    <% if ("ADMIN".equals(role)) { %>
                    <a href="dashboard.jsp?page=customers"
                       class="w-full flex items-center space-x-3 p-3 rounded-lg bg-gray-100 hover:bg-gray-200 transition-colors">
                        <i data-feather="user-plus" class="w-5 h-5 text-gray-700"></i><span>Add New Customer</span>
                    </a>
                    <% } %>
                </div>
            </div>
            <div class="bg-white p-6 rounded-lg shadow-md">
                <h3 class="text-xl font-semibold text-gray-800 mb-4">Recent Bills</h3>
                <div class="overflow-y-auto h-48">
                    <table class="min-w-full">
                        <tbody id="recentBillsTableBody" class="divide-y divide-gray-200"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="bg-white p-6 rounded-lg shadow-md"><h3 class="text-xl font-semibold text-gray-800 mb-4">Top Selling
            Items</h3>
            <ul id="topItemsList" class="space-y-3"></ul>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md"><h3 class="text-xl font-semibold text-gray-800 mb-4">Top
            Customers</h3>
            <ul id="topCustomersList" class="space-y-3"></ul>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-md"><h3 class="text-xl font-semibold text-gray-800 mb-4">Top
            Performing Users</h3>
            <ul id="topUsersList" class="space-y-3"></ul>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        let weeklyChart = null; // Variable to hold the chart instance

        /**
         * ✅ FIX: This is the complete, working chart rendering function.
         * It correctly handles cases where there is no sales data for a given day.
         */
        function renderWeeklySalesChart(weeklySalesData) {
            const ctx = document.getElementById('weeklySalesChart').getContext('2d');

            const labels = [];
            const dataPoints = [];

            // Create a lookup map for sales data. The 'en-CA' locale reliably gives YYYY-MM-DD format.
            const salesMap = new Map(
                (weeklySalesData || []).map(item => [new Date(item.date).toLocaleDateString('en-CA'), item.total])
            );

            // Generate labels and data for the last 7 days, regardless of whether there were sales.
            for (let i = 6; i >= 0; i--) {
                const d = new Date();
                d.setDate(d.getDate() - i);

                const dateString = d.toLocaleDateString('en-CA'); // YYYY-MM-DD
                const dayLabel = d.toLocaleDateString('en-US', {weekday: 'short'}); // "Mon", "Tue", etc.

                labels.push(dayLabel);
                dataPoints.push(salesMap.get(dateString) || 0); // Use sale total, or 0 if no sales on that day
            }

            if (weeklyChart) {
                weeklyChart.destroy(); // Destroy old chart instance before creating a new one
            }

            weeklyChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Daily Sales (Rs)',
                        data: dataPoints,
                        backgroundColor: 'rgba(16, 185, 129, 0.6)', // A nice green color
                        borderColor: 'rgba(16, 185, 129, 1)',
                        borderWidth: 1,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {y: {beginAtZero: true}},
                    plugins: {legend: {display: false}}
                }
            });
        }

        function populateList(elementId, data, unit = 'Rs.') {
            const list = document.getElementById(elementId);
            list.innerHTML = '';
            if (!data || data.length === 0) {
                list.innerHTML = `<li class="text-gray-500">No data for this period.</li>`;
                return;
            }
            data.forEach(item => {
                const li = document.createElement('li');
                li.className = 'flex justify-between items-center text-sm border-b pb-2';
                const value = unit === 'bills' ? `\${item.value} bills` : `Rs. \${parseFloat(item.value).toFixed(2)}`;
                li.innerHTML = `<span class="text-gray-600">\${item.name}</span><strong class="font-semibold text-gray-800">\${value}</strong>`;
                list.appendChild(li);
            });
        }

        async function fetchDashboardData() {
            try {
                const response = await fetch(getContextPath() + '/dashboard-data');
                const data = await response.json();
                if (!response.ok) throw new Error(data.message || 'Failed to load dashboard data');

                // Populate Stat Cards
                document.getElementById('totalRevenue').textContent = `Rs. \${parseFloat(data.totalRevenue).toFixed(2)}`;
                document.getElementById('totalCustomers').textContent = data.totalCustomers;
                document.getElementById('totalItems').textContent = data.totalItems;
                document.getElementById('totalBills').textContent = data.totalBills;

                // Populate Leaderboards
                populateList('topItemsList', data.topSellingItems);
                populateList('topCustomersList', data.topCustomers);
                populateList('topUsersList', data.topPerformingUsers, 'bills');

                // Populate Recent Bills
                const tableBody = document.getElementById('recentBillsTableBody');
                tableBody.innerHTML = '';
                if (data.recentBills && data.recentBills.length > 0) {
                    data.recentBills.forEach(bill => {
                        const row = document.createElement('tr');
                        row.className = 'hover:bg-gray-50';
                        row.innerHTML = `<td class="py-3 text-sm text-gray-600">\${bill.customerName}</td><td class="py-3 text-sm text-gray-800 text-right font-medium">Rs. \${parseFloat(bill.totalAmount).toFixed(2)}</td>`;
                        tableBody.appendChild(row);
                    });
                } else {
                    tableBody.innerHTML = '<tr><td colspan="2" class="text-center py-8 text-gray-500">No recent bills.</td></tr>';
                }

                // Render Chart
                renderWeeklySalesChart(data.weeklySales);

            } catch (error) {
                console.error('Error fetching dashboard data:', error);
                showMessage(error.message, 'error');
            } finally {
                feather.replace();
            }
        }

        fetchDashboardData();
    });
</script>