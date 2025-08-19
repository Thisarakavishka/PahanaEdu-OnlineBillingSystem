<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 04:01
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="space-y-8">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-blue-500">
            <div class="bg-blue-100 text-blue-600 p-3 rounded-full"><i data-feather="users" class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Total Customers</p>
                <p id="totalCustomers" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-green-500">
            <div class="bg-green-100 text-green-600 p-3 rounded-full"><i data-feather="package" class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Items in Stock</p>
                <p id="totalItems" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-yellow-500">
            <div class="bg-yellow-100 text-yellow-600 p-3 rounded-full"><i data-feather="file-text" class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Total Bills Generated</p>
                <p id="totalBills" class="text-2xl font-bold text-gray-800">...</p>
            </div>
        </div>

        <div class="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4 border-l-4 border-indigo-500">
            <div class="bg-indigo-100 text-indigo-600 p-3 rounded-full"><i data-feather="dollar-sign" class="w-6 h-6"></i></div>
            <div>
                <p class="text-sm text-gray-500 font-medium">Total Revenue</p>
                <p class="text-2xl font-bold text-gray-800">Soon</p>
            </div>
        </div>
    </div>

    <div class="bg-white p-6 rounded-lg shadow-md">
        <h3 class="text-xl font-semibold text-gray-800 mb-4">Recent Activity</h3>
        <p class="text-gray-500">A table of recent bills or a chart could be displayed here in the future.</p>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        /**
         * Fetches all dashboard data from your new servlet in a single call
         * and populates the stat cards.
         */
        async function fetchDashboardData() {
            try {
                // getContextPath() is available globally from dashboard.jsp
                const response = await fetch(getContextPath() + '/dashboard-data');
                const data = await response.json();

                if (response.ok) {
                    document.getElementById('totalCustomers').textContent = data.totalCustomers;
                    document.getElementById('totalItems').textContent = data.totalItems;
                    document.getElementById('totalBills').textContent = data.totalBills;
                } else {
                    throw new Error(data.message || 'Failed to load dashboard data');
                }
            } catch (error) {
                console.error('Error fetching dashboard data:', error);
                // Update cards to show an error state
                document.getElementById('totalCustomers').textContent = 'Error';
                document.getElementById('totalItems').textContent = 'Error';
                document.getElementById('totalBills').textContent = 'Error';
            } finally {
                // Re-render any new icons
                feather.replace();
            }
        }

        // Fetch the data as soon as the page loads
        fetchDashboardData();
    });
</script>