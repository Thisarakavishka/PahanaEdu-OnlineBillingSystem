<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-08-21
  Time: 10:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="space-y-6">
    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">System History & Archive</h1>
            <p class="text-gray-600">View and restore deleted records from the system.</p>
        </div>
        <div class="relative inline-block text-left">
            <button id="exportBtn"
                    class="bg-white border border-gray-300 text-gray-700 font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2 hover:bg-gray-50">
                <i data-feather="download" class="w-5 h-5"></i>
                <span>Export Current View</span>
            </button>
            <div id="exportMenu"
                 class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 hidden z-10">
                <div class="py-1">
                    <a href="#" id="exportPdfBtn" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Export
                        as PDF</a>
                    <a href="#" id="exportCsvBtn" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Export
                        as CSV</a>
                </div>
            </div>
        </div>
    </div>

    <div class="bg-white rounded-lg shadow-md border border-gray-200">
        <div class="border-b">
            <nav id="history-tabs" class="flex space-x-1 p-2" aria-label="Tabs"></nav>
        </div>

        <div id="loadingIndicator" class="text-center py-16">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
            <p class="text-gray-600">Loading history...</p>
        </div>

        <div id="tab-content" class="p-4 hidden">
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        // --- State, Constants & Elements ---
        const tabsContainer = document.getElementById('history-tabs');
        const contentContainer = document.getElementById('tab-content');
        const loadingIndicator = document.getElementById('loadingIndicator');
        const exportBtn = document.getElementById('exportBtn');
        const exportMenu = document.getElementById('exportMenu');
        const loggedInUserId = parseInt(document.getElementById('userIdHiddenInput').value, 10);
        const INITIAL_ADMIN_ID = 1;
        let historyData = {};
        let activeTab = '';

        const showMessage = showToast;

        // --- Tab Management ---
        function selectTab(type) {
            activeTab = type;
            tabsContainer.querySelectorAll('button').forEach(btn => {
                const isSelected = btn.dataset.type === type;
                btn.classList.toggle('bg-gray-200', isSelected);
                btn.classList.toggle('text-gray-800', isSelected);
                btn.classList.toggle('font-semibold', isSelected);
            });
            contentContainer.querySelectorAll('.tab-pane').forEach(pane => {
                pane.classList.toggle('hidden', pane.id !== `content-\${type}`);
            });
            feather.replace();
        }

        function renderContent(type, data) {
            const canRestore = (loggedInUserId === INITIAL_ADMIN_ID);
            const container = document.createElement('div');
            container.className = 'overflow-x-auto';
            const table = document.createElement('table');
            table.className = 'min-w-full divide-y';
            const thead = document.createElement('thead');
            thead.className = 'bg-gray-50';
            const tbody = document.createElement('tbody');
            tbody.className = 'divide-y';

            if (!data || data.length === 0) {
                container.innerHTML = `<div class="text-center py-12 text-gray-500">No deleted records found.</div>`;
                return container;
            }

            let headers = [];

            switch (type) {
                case 'deletedUsers':
                    headers = ['ID', 'Username', 'Role', 'Deleted At', 'Action'];
                    data.forEach(user => {
                        const row = document.createElement('tr');
                        row.className = 'hover:bg-gray-50';
                        const actionButton = canRestore
                            ? `<button class="font-semibold text-green-600 hover:text-green-800 restore-btn flex text-center" data-id="\${user.id}" data-type="user">Restore</button>`
                            : 'View Only';

                        let rowHtml = '';
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">#\${user.id}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm font-medium text-gray-800">\${user.username}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${user.role}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${new Date(user.deletedAt).toLocaleString('en-GB')}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-center">\${actionButton}</td>`;
                        row.innerHTML = rowHtml;
                        tbody.appendChild(row);
                    });
                    break;
                case 'deletedCustomers':
                    headers = ['Acc. No.', 'Name', 'Phone', 'Deleted At', 'Action'];
                    data.forEach(cust => {
                        const row = document.createElement('tr');
                        row.className = 'hover:bg-gray-50';
                        const actionButton = canRestore
                            ? `<button class="font-semibold text-green-600 hover:text-green-800 restore-btn" data-id="\${cust.id}" data-type="customer">Restore</button>`
                            : 'N/A';

                        let rowHtml = '';
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${cust.accountNumber}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm font-medium text-gray-800">\${cust.name}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${cust.phone}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${new Date(cust.deletedAt).toLocaleString('en-GB')}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-center">\${actionButton}</td>`;
                        row.innerHTML = rowHtml;
                        tbody.appendChild(row);
                    });
                    break;
                case 'deletedItems':
                    headers = ['ID', 'Name', 'Unit Price', 'Deleted At', 'Action'];
                    data.forEach(item => {
                        const row = document.createElement('tr');
                        row.className = 'hover:bg-gray-50';
                        const actionButton = canRestore
                            ? `<button class="font-semibold text-green-600 hover:text-green-800 restore-btn" data-id="\${item.id}" data-type="item">Restore</button>`
                            : 'N/A';

                        let rowHtml = '';
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">#\${item.id}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm font-medium text-gray-800">\${item.name}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">Rs. \${parseFloat(item.unitPrice).toFixed(2)}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-sm text-gray-600">\${new Date(item.deletedAt).toLocaleString('en-GB')}</td>`;
                        rowHtml += `<td class="px-4 py-3 text-center">\${actionButton}</td>`;
                        row.innerHTML = rowHtml;
                        tbody.appendChild(row);
                    });
                    break;
            }

            // Build the table header using a safe loop
            let headerRowHtml = '<tr>';
            for (const h of headers) {
                headerRowHtml += `<th class="px-4 py-2 text-left text-xs font-semibold text-gray-600 uppercase">\${h}</th>`;
            }
            headerRowHtml += '</tr>';
            thead.innerHTML = headerRowHtml;

            table.append(thead, tbody);
            container.appendChild(table);
            return container;
        }

        // --- Main Data Fetching ---
        async function fetchHistoryData() {
            loadingIndicator.classList.remove('hidden');
            contentContainer.classList.add('hidden');
            try {
                const response = await fetch(`\${getContextPath()}/history-data`);
                const data = await response.json();
                if (!response.ok) throw new Error(data.message);

                historyData = data;
                tabsContainer.innerHTML = '';
                contentContainer.innerHTML = '';
                const availableTabs = [];

                if (data.deletedUsers) {
                    tabsContainer.innerHTML += `<button data-type="deletedUsers" class="px-3 py-2 font-medium text-sm rounded-md text-gray-500">Users</button>`;
                    const pane = document.createElement('div');
                    pane.id = 'content-deletedUsers';
                    pane.className = 'tab-pane hidden';
                    pane.appendChild(renderContent('deletedUsers', data.deletedUsers));
                    contentContainer.appendChild(pane);
                    availableTabs.push('deletedUsers');
                }
                if (data.deletedCustomers) {
                    tabsContainer.innerHTML += `<button data-type="deletedCustomers" class="px-3 py-2 font-medium text-sm rounded-md text-gray-500">Customers</button>`;
                    const pane = document.createElement('div');
                    pane.id = 'content-deletedCustomers';
                    pane.className = 'tab-pane hidden';
                    pane.appendChild(renderContent('deletedCustomers', data.deletedCustomers));
                    contentContainer.appendChild(pane);
                    if (availableTabs.length === 0) availableTabs.push('deletedCustomers');
                }
                if (data.deletedItems) {
                    tabsContainer.innerHTML += `<button data-type="deletedItems" class="px-3 py-2 font-medium text-sm rounded-md text-gray-500">Items</button>`;
                    const pane = document.createElement('div');
                    pane.id = 'content-deletedItems';
                    pane.className = 'tab-pane hidden';
                    pane.appendChild(renderContent('deletedItems', data.deletedItems));
                    contentContainer.appendChild(pane);
                    if (availableTabs.length === 0) availableTabs.push('deletedItems');
                }

                if (availableTabs.length > 0) {
                    selectTab(availableTabs[0]);
                } else {
                    contentContainer.innerHTML = `<div class="text-center py-12 text-gray-500">No history records found.</div>`;
                }

            } catch (error) {
                showMessage(error.message, 'error');
            } finally {
                loadingIndicator.classList.add('hidden');
                contentContainer.classList.remove('hidden');
            }
        }

        // --- Export Functions ---
        function downloadAsPDF() { /* Your existing implementation */
        }

        function downloadAsCSV() { /* Your existing implementation */
        }

        // --- Event Listeners ---
        tabsContainer.addEventListener('click', (e) => {
            if (e.target.tagName === 'BUTTON') selectTab(e.target.dataset.type);
        });

        contentContainer.addEventListener('click', async (e) => {
            if (e.target.classList.contains('restore-btn')) {
                const id = e.target.dataset.id;
                const type = e.target.dataset.type;
                if (confirm(`Are you sure you want to restore this \${type}?`)) {
                    try {
                        const formData = new URLSearchParams({id, type});
                        const response = await fetch(`\${getContextPath()}/history-data`, {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: formData
                        });
                        const data = await response.json();
                        if (!response.ok) throw new Error(data.message);
                        showMessage(data.message, 'success');
                        fetchHistoryData();
                    } catch (error) {
                        showMessage(error.message, 'error');
                    }
                }
            }
        });

        if (exportBtn) {
            exportBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                exportMenu.classList.toggle('hidden');
            });
        }
        document.addEventListener('click', (e) => {
            if (exportMenu && !exportBtn.contains(e.target)) exportMenu.classList.add('hidden');
        });
        document.getElementById('exportPdfBtn').addEventListener('click', (e) => {
            e.preventDefault();
            downloadAsPDF();
            exportMenu.classList.add('hidden');
        });
        document.getElementById('exportCsvBtn').addEventListener('click', (e) => {
            e.preventDefault();
            downloadAsCSV();
            exportMenu.classList.add('hidden');
        });

        // --- Initial Load ---
        fetchHistoryData();
    });
</script>