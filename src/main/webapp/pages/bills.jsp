<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-08-15
  Time: 18:030
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    String role = (String) session.getAttribute("role");
    Integer userId = (Integer) session.getAttribute("userId");
    final int INITIAL_ADMIN_ID = 1;
%>

<style>
    .step {
        display: none;
        opacity: 0;
        transition: all 0.3s ease-in-out;
        transform: translateX(30px);
    }

    .step.active {
        display: block;
        opacity: 1;
        transform: translateX(0);
    }

    .modal-content::-webkit-scrollbar {
        display: none;
    }

    .modal-content {
        -ms-overflow-style: none;
        scrollbar-width: none;
    }

    @media print {
        body * {
            visibility: hidden;
        }

        #printable-receipt, #printable-receipt * {
            visibility: visible;
        }

        #printable-receipt {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            margin: 0;
            padding: 20px;
            font-size: 12pt;
            color: #000;
        }

        #printable-receipt .shadow-lg, #printable-receipt .border {
            box-shadow: none !important;
            border: none !important;
        }

        #printable-receipt .bg-gray-50 {
            background-color: transparent !important;
        }

        #printable-receipt .text-gray-800 {
            color: #000 !important;
        }

        @page {
            size: auto;
            margin: 20mm;
        }
    }
</style>

<div class="space-y-6">
    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">Bill Management</h1>
            <p class="text-gray-600">Create new bills and manage past orders.</p>
        </div>
        <c:if test='<%= "ADMIN".equals(role) %>'>
            <button id="generateBillBtn"
                    class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-5 rounded-md inline-flex items-center space-x-2 shadow-md">
                <i data-feather="plus-circle" class="w-5 h-5"></i><span>Generate New Bill</span>
            </button>
        </c:if>
    </div>

    <div class="bg-white rounded-lg shadow-md border border-gray-200">
        <div class="p-4 border-b flex flex-col sm:flex-row items-center gap-4">
            <div class="relative flex-grow w-full sm:w-auto">
                <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none"><i
                        data-feather="search" class="w-4 h-4 text-gray-400"></i></div>
                <input type="text" id="billSearchInput" placeholder="Search by customer name or account no."
                       class="w-full pl-10 pr-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
            </div>
            <button id="billRefreshBtn"
                    class="bg-gray-100 hover:bg-gray-200 text-gray-800 font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2 shadow-sm">
                <i data-feather="refresh-cw" class="w-5 h-5"></i><span>Refresh</span>
            </button>
        </div>
        <div id="billLoadingIndicator" class="text-center py-8 hidden">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
            <p class="text-gray-600">Loading bills...</p>
        </div>
        <jsp:include page="../components/messages.jsp"/>
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Bill
                        ID
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                        Customer
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Total
                        Amount
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created
                        By
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created
                        At
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">
                        Actions
                    </th>
                </tr>
                </thead>
                <tbody id="billTableBody" class="bg-white divide-y divide-gray-200"></tbody>
            </table>
        </div>
    </div>
</div>

<div id="billGenerationModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-4xl relative overflow-hidden">
        <button id="closeBillGenModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800 z-20"><i
                data-feather="x" class="w-6 h-6"></i></button>
        <div class="flex justify-between items-center mb-6">
            <h2 class="text-2xl font-bold text-gray-800">Generate New Bill</h2>
            <div id="stepIndicator" class="text-sm font-semibold text-gray-500 bg-gray-100 px-3 py-1 rounded-full">Step
                1 of 3
            </div>
        </div>
        <div class="modal-content overflow-y-auto" style="max-height: 70vh;">
            <div id="step1" class="step">
                <h3 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">Step 1: Choose Customer</h3>
                <div class="space-y-4 mt-4">
                    <label for="customerPhoneSearch" class="block text-sm font-semibold text-gray-700 mb-1">Search
                        Customer by Phone Number</label>
                    <input type="tel" id="customerPhoneSearch" placeholder="Enter phone number and press Enter..."
                           class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                    <p id="customerSearchError" class="text-red-500 text-xs mt-1 hidden"></p>
                    <div id="customerDetailsDisplay" class="p-4 bg-gray-50 rounded-md border space-y-2 hidden">
                        <p class="text-lg font-bold text-green-700">Customer Found!</p>
                        <p><span class="font-semibold">Name:</span> <span id="billCustomerName"></span></p>
                        <p><span class="font-semibold">Account No:</span> <span id="billCustomerAccountNo"></span></p>
                        <input type="hidden" id="billCustomerId">
                    </div>
                </div>
            </div>
            <div id="step2" class="step">
                <h3 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">Step 2: Add Items to Bill</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="p-4 bg-gray-50 rounded-md border h-fit"><p class="text-sm font-semibold text-gray-600">
                        BILLING TO:</p>
                        <p id="step2CustomerName" class="text-lg font-bold text-gray-800"></p></div>
                    <div class="space-y-2">
                        <label for="itemSearchInput" class="block text-sm font-semibold text-gray-700 mb-1">Search for
                            an item</label>
                        <input type="text" id="itemSearchInput" placeholder="Start typing item name..."
                               class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                        <div id="itemSearchResults"
                             class="bg-white border rounded-md max-h-40 overflow-y-auto hidden"></div>
                    </div>
                </div>
                <div id="billItemsContainer" class="mt-4"></div>
            </div>
            <div id="step3" class="step">
                <h3 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">Step 3: Confirm and Finalize</h3>
                <div class="space-y-6">
                    <div class="p-4 bg-gray-50 rounded-lg border"><p class="text-sm font-semibold text-gray-600">
                        CUSTOMER</p>
                        <p id="confirmCustomerName" class="text-xl font-bold text-gray-800"></p></div>
                    <div id="confirmItemsContainer"></div>
                </div>
            </div>
        </div>
        <div class="mt-8 pt-4 border-t flex justify-between items-center">
            <button id="backBtn"
                    class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-5 rounded-md hidden">Back
            </button>
            <div class="flex-grow"></div>
            <button id="nextBtn"
                    class="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-5 rounded-md disabled:opacity-50"
                    disabled>Next
            </button>
            <button id="finalizeBillBtn"
                    class="bg-green-600 hover:bg-green-700 text-white font-semibold py-2 px-5 rounded-md hidden">
                Finalize Bill
            </button>
        </div>
    </div>
</div>

<div id="viewBillModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-3xl relative">
        <button id="closeViewBillModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800 z-10"><i
                data-feather="x" class="w-6 h-6"></i></button>
        <div id="printable-receipt">
            <div class="text-center mb-8">
                <i data-feather="book-open" class="w-12 h-12 text-gray-800 mx-auto"></i>
                <h1 class="text-3xl font-extrabold text-gray-900">Pahana Edu</h1>
                <p class="text-md text-gray-500">Official Receipt</p>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <h3 class="text-lg font-semibold mb-2 text-gray-800 border-b pb-1">Bill Information</h3>
                    <div class="space-y-1 text-sm">
                        <p><span class="font-semibold">Bill ID:</span> #<span id="viewBillId"></span></p>
                        <p><span class="font-semibold">Created By:</span> <span id="viewBillGeneratedBy"></span></p>
                        <p><span class="font-semibold">Created At:</span> <span id="viewBillGeneratedAt"></span></p>
                    </div>
                </div>
                <div>
                    <h3 class="text-lg font-semibold mb-2 text-gray-800 border-b pb-1">Customer Information</h3>
                    <div class="space-y-1 text-sm">
                        <p><span class="font-semibold">Name:</span> <span id="viewBillCustomerName"></span></p>
                        <p><span class="font-semibold">Account No:</span> <span id="viewBillCustomerAccountNo"></span>
                        </p>
                    </div>
                </div>
            </div>
            <hr class="my-6">
            <h3 class="text-lg font-semibold mb-3 text-gray-800">Order Summary</h3>
            <div class="overflow-x-auto rounded-lg border">
                <table class="min-w-full">
                    <thead class="bg-gray-50">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                            Item
                        </th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                            Price
                        </th>
                        <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">
                            Quantity
                        </th>
                        <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 uppercase tracking-wider">
                            Subtotal
                        </th>
                    </tr>
                    </thead>
                    <tbody id="viewBillItemsTableBody" class="bg-white divide-y divide-gray-200 text-sm"></tbody>
                </table>
            </div>
            <div class="mt-4 flex justify-end">
                <div class="w-full md:w-2/5 p-4 bg-gray-50 rounded-lg">
                    <p class="text-lg font-bold flex justify-between"><span>Total Amount:</span> <span
                            id="viewBillTotalAmount"></span></p>
                </div>
            </div>
            <div class="text-center mt-8 text-xs text-gray-400">
                <p>Thank you for your business!</p>
            </div>
        </div>
        <div class="mt-8 pt-4 border-t flex justify-end">
            <button id="printBillBtn"
                    class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2">
                <i data-feather="printer" class="w-4 h-4"></i>
                <span>Print Receipt</span>
            </button>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        // --- State, Constants & All Element References ---
        let currentStep = 1, currentCustomer = null, currentBillItems = [], searchTimeout;
        const loggedInUserRole = document.getElementById('userRoleHiddenInput').value;
        const loggedInUserId = document.getElementById('userIdHiddenInput') ? parseInt(document.getElementById('userIdHiddenInput').value, 10) : null;
        const INITIAL_ADMIN_ID = 1;

        const modal = document.getElementById('billGenerationModal');
        const viewModal = document.getElementById('viewBillModal');
        const nextBtn = document.getElementById('nextBtn');
        const backBtn = document.getElementById('backBtn');
        const finalizeBillBtn = document.getElementById('finalizeBillBtn');
        const billTableBody = document.getElementById('billTableBody');
        const customerPhoneSearch = document.getElementById('customerPhoneSearch');
        const itemSearchInput = document.getElementById('itemSearchInput');

        // --- Helper Functions ---
        function getContextPath() {
            return "<%= request.getContextPath() %>";
        }

        const showMessage = showToast;

        // --- Step Navigation & UI Updates ---
        function showStep(stepNumber) {
            document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
            document.getElementById(`step\${stepNumber}`).classList.add('active');
            document.getElementById('stepIndicator').textContent = `Step \${stepNumber} of 3`;
            currentStep = stepNumber;
            updateNavButtons();
        }

        function updateNavButtons() {
            backBtn.classList.toggle('hidden', currentStep === 1);
            nextBtn.classList.toggle('hidden', currentStep === 3);
            finalizeBillBtn.classList.toggle('hidden', currentStep !== 3);
            if (currentStep === 1) nextBtn.disabled = !currentCustomer;
            else if (currentStep === 2) nextBtn.disabled = currentBillItems.length === 0;
        }

        function nextStep() {
            if (currentStep < 3) {
                if (currentStep === 1) {
                    document.getElementById('step2CustomerName').textContent = currentCustomer.name;
                    renderBillItemsTable('billItemsContainer', true);
                }
                if (currentStep === 2) {
                    document.getElementById('confirmCustomerName').textContent = currentCustomer.name;
                    renderBillItemsTable('confirmItemsContainer', false);
                }
                showStep(currentStep + 1);
            }
        }

        function renderBillItemsTable(containerId, withControls) {
            const container = document.getElementById(containerId);
            const total = currentBillItems.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
            const tableWrapper = document.createElement('div');
            tableWrapper.className = 'overflow-x-auto rounded-lg border border-gray-200';
            const table = document.createElement('table');
            table.className = 'min-w-full divide-y divide-gray-200';
            const thead = document.createElement('thead');
            thead.className = 'bg-gray-50';
            let headerHtml = '<tr><th class="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Item</th><th class="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Price</th><th class="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider w-32">Quantity</th><th class="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Total</th>';
            if (withControls) headerHtml += '<th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">Action</th>';
            headerHtml += '</tr>';
            thead.innerHTML = headerHtml;
            const tbody = document.createElement('tbody');
            tbody.className = 'bg-white divide-y divide-gray-200';
            if (currentBillItems.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-8 text-gray-500">No items added yet.</td></tr>';
            } else {
                currentBillItems.forEach(item => {
                    const row = document.createElement('tr');
                    const quantityControlHtml = withControls ?
                        `<div class="flex items-center justify-center space-x-2">
                            <button class="text-gray-500 hover:text-blue-600 qty-decrease-btn" data-item-id="\${item.id}"><i data-feather="minus-circle" class="w-4 h-4"></i></button>
                            <span>\${item.quantity}</span>
                            <button class="text-gray-500 hover:text-blue-600 qty-increase-btn" data-item-id="\${item.id}"><i data-feather="plus-circle" class="w-4 h-4"></i></button>
                         </div>` : item.quantity;
                    row.innerHTML = `<td class="px-4 py-3 text-sm text-gray-900">\${item.name}</td><td class="px-4 py-3 text-sm text-gray-500">Rs. \${item.unitPrice.toFixed(2)}</td><td class="px-4 py-3 text-sm text-gray-500">\${quantityControlHtml}</td><td class="px-4 py-3 text-sm text-gray-500">Rs. \${(item.unitPrice * item.quantity).toFixed(2)}</td>\${withControls ? '<td class="px-4 py-3 text-center"><button class="text-red-500 hover:text-red-700 remove-item-btn" data-item-id="\${item.id}"><i data-feather="trash-2" class="w-4 h-4"></i></button></td>' : ''}`;
                    tbody.appendChild(row);
                });
            }
            table.append(thead, tbody);
            tableWrapper.appendChild(table);
            const totalDiv = document.createElement('div');
            totalDiv.className = 'mt-4 p-4 text-right bg-gray-50 rounded-lg border';
            totalDiv.innerHTML = `<span class="font-bold text-xl text-gray-800">Total Amount: <span id="billTotalAmount">Rs. \${total.toFixed(2)}</span></span>`;
            container.innerHTML = '';
            container.append(tableWrapper, totalDiv);
            feather.replace();
            updateNavButtons();
        }

        // --- Data Fetching and Logic ---
        async function fetchCustomersForBill(phone) {
            const customerSearchError = document.getElementById('customerSearchError');
            const customerDetailsDisplay = document.getElementById('customerDetailsDisplay');
            customerSearchError.classList.add('hidden');
            customerDetailsDisplay.classList.add('hidden');
            currentCustomer = null;
            if (!phone || phone.length < 10) {
                customerSearchError.textContent = "Please enter a valid 10-digit phone number.";
                customerSearchError.classList.remove('hidden');
                updateNavButtons();
                return;
            }
            try {
                const response = await fetch(`\${getContextPath()}/customers?phone=\${encodeURIComponent(phone)}`);
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || "Customer not found.");
                }
                currentCustomer = await response.json();
                document.getElementById('billCustomerName').textContent = currentCustomer.name;
                document.getElementById('billCustomerAccountNo').textContent = currentCustomer.accountNumber;
                document.getElementById('billCustomerId').value = currentCustomer.id;
                customerDetailsDisplay.classList.remove('hidden');
            } catch (error) {
                customerSearchError.textContent = error.message;
                customerSearchError.classList.remove('hidden');
            } finally {
                updateNavButtons();
            }
        }

        async function handleItemSearchInput() {
            const itemSearchResults = document.getElementById('itemSearchResults');
            const searchTerm = itemSearchInput.value.trim();
            clearTimeout(searchTimeout);
            if (searchTerm.length < 2) {
                itemSearchResults.innerHTML = '';
                itemSearchResults.classList.add('hidden');
                return;
            }
            searchTimeout = setTimeout(async () => {
                try {
                    const response = await fetch(`\${getContextPath()}/items?search=\${encodeURIComponent(searchTerm)}`);
                    const data = await response.json();
                    itemSearchResults.innerHTML = '';
                    if (response.ok && Array.isArray(data) && data.length > 0) {
                        data.forEach(item => {
                            const div = document.createElement('div');
                            div.className = 'p-2 cursor-pointer hover:bg-gray-200';
                            div.innerHTML = `<span>\${item.name} - Rs. \${parseFloat(item.unitPrice).toFixed(2)} (Stock: \${item.stockQuantity})</span>`;
                            div.onclick = () => {
                                addItemToBill(item);
                                itemSearchInput.value = '';
                                itemSearchResults.classList.add('hidden');
                            };
                            itemSearchResults.appendChild(div);
                        });
                        itemSearchResults.classList.remove('hidden');
                    } else {
                        itemSearchResults.classList.add('hidden');
                    }
                } catch (error) {
                    console.error('Error searching items:', error);
                }
            }, 300);
        }

        function addItemToBill(item) {
            const existingItem = currentBillItems.find(i => i.id === item.id);
            if (existingItem) {
                if (existingItem.quantity < item.stockQuantity) existingItem.quantity++;
                else showMessage(`Maximum stock for '\${item.name}' reached.`, 'error');
            } else {
                if (item.stockQuantity > 0) currentBillItems.push({
                    ...item,
                    unitPrice: parseFloat(item.unitPrice),
                    quantity: 1,
                    maxStock: item.stockQuantity
                });
                else showMessage(`'\${item.name}' is out of stock.`, 'error');
            }
            renderBillItemsTable('billItemsContainer', true);
        }

        async function handleFinalizeBill() {
            if (currentBillItems.length === 0 || !currentCustomer) {
                showMessage("Cannot finalize an empty bill.", 'error');
                return;
            }
            const formData = new URLSearchParams();
            formData.append('customerId', currentCustomer.id);
            currentBillItems.forEach(item => {
                formData.append('item_id', item.id);
                formData.append('units', item.quantity);
            });
            try {
                const response = await fetch(`\${getContextPath()}/bills`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: formData.toString()
                });
                const data = await response.json();
                if (!response.ok) throw new Error(data.message || 'Failed to finalize bill.');

                showMessage("Bill generated successfully!", 'success');
                modal.classList.add('hidden');
                fetchBills();
                populateViewBillModal(data.bill);
                viewModal.classList.remove('hidden');
            } catch (error) {
                showMessage(error.message, 'error');
            }
        }

        async function fetchBills(searchTerm = '') {
            document.getElementById('billLoadingIndicator').classList.remove('hidden');
            billTableBody.innerHTML = '';
            let url = `\${getContextPath()}/bills`;
            if (searchTerm) url += `?search=\${encodeURIComponent(searchTerm)}`;
            try {
                const response = await fetch(url);
                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.message || 'Failed to fetch bills.');
                }
                const bills = await response.json();
                renderBills(bills);
            } catch (error) {
                showMessage(error.message, 'error');
                billTableBody.innerHTML = `<tr><td colspan="6" class="text-center py-8 text-red-500">\${error.message}</td></tr>`;
            } finally {
                document.getElementById('billLoadingIndicator').classList.add('hidden');
            }
        }

        function renderBills(bills) {
            billTableBody.innerHTML = '';
            if (!bills || bills.length === 0) {
                billTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-gray-500">No bills found.</td></tr>';
                return;
            }
            bills.forEach(bill => {
                const row = document.createElement('tr');
                let rowHtml = '';
                rowHtml += '<td class="px-6 py-4 text-sm text-gray-900">' + bill.id + '</td>';
                rowHtml += '<td class="px-6 py-4 text-sm font-medium text-gray-900">' + (bill.customerName || 'N/A') + '</td>';
                rowHtml += '<td class="px-6 py-4 text-sm text-gray-500">' + (bill.totalAmount ? 'Rs. ' + parseFloat(bill.totalAmount).toFixed(2) : 'N/A') + '</td>';
                rowHtml += '<td class="px-6 py-4 text-sm text-gray-500">' + (bill.generatedByUsername || 'N/A') + '</td>';
                rowHtml += '<td class="px-6 py-4 text-sm text-gray-500">' +
                    (bill.generatedAt
                        ? new Date(bill.generatedAt).toLocaleDateString('en-GB', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric'
                        })
                        : '-') + '</td>';
                rowHtml += '<td class="px-6 py-4 text-center text-sm font-medium">';
                rowHtml += '<button title="View Bill" class="text-blue-600 hover:text-blue-900 mr-3 view-bill-btn" data-bill-id="' + bill.id + '"><i data-feather="eye" class="w-5 h-5"></i></button>';
                rowHtml += '</td>';
                row.innerHTML = rowHtml;
                billTableBody.appendChild(row);
            });
            feather.replace();
        }

        async function openViewBillModal(billId) {
            try {
                const response = await fetch(`\${getContextPath()}/bills?id=\${billId}`);
                if (!response.ok) throw new Error('Failed to fetch bill details.');
                const billData = await response.json();
                populateViewBillModal(billData);
                viewModal.classList.remove('hidden');
            } catch (error) {
                showMessage(error.message, 'error');
            }
        }

        function populateViewBillModal(data) {
            document.getElementById('viewBillId').textContent = data.id;
            document.getElementById('viewBillGeneratedBy').textContent = data.generatedByUsername || '-';
            document.getElementById('viewBillGeneratedAt').textContent = (data.generatedAt
                ? new Date(data.generatedAt).toLocaleDateString('en-GB', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: true
                })
                : '-')
            document.getElementById('viewBillTotalAmount').textContent = `Rs. \${parseFloat(data.totalAmount).toFixed(2)}`;
            document.getElementById('viewBillCustomerName').textContent = data.customerName || '-';
            document.getElementById('viewBillCustomerAccountNo').textContent = data.customerAccountNumber || '-';
            const viewItemsBody = document.getElementById('viewBillItemsTableBody');
            viewItemsBody.innerHTML = '';
            if (data.details && Array.isArray(data.details)) {
                data.details.forEach(detail => {
                    const row = document.createElement('tr');
                    row.innerHTML = `<td class="px-6 py-4 text-left">\${detail.itemNameAtSale}</td><td class="px-6 py-4 text-left">Rs. \${parseFloat(detail.unitPriceAtSale).toFixed(2)}</td><td class="px-6 py-4 text-center">\${detail.units}</td><td class="px-6 py-4 text-right">Rs. \${parseFloat(detail.total).toFixed(2)}</td>`;
                    viewItemsBody.appendChild(row);
                });
            }
        }

        // --- Event Listeners ---
        const billRefreshBtn = document.getElementById('billRefreshBtn');
        const billSearchInput = document.getElementById('billSearchInput');
        const generateBillBtn = document.getElementById('generateBillBtn');

        if (generateBillBtn) {
            generateBillBtn.addEventListener('click', () => {
                currentStep = 1;
                currentCustomer = null;
                currentBillItems = [];
                document.getElementById('customerPhoneSearch').value = '';
                document.getElementById('customerDetailsDisplay').classList.add('hidden');
                document.getElementById('customerSearchError').classList.add('hidden');
                document.getElementById('billItemsContainer').innerHTML = '';
                document.getElementById('confirmItemsContainer').innerHTML = '';
                modal.classList.remove('hidden');
                showStep(1);
            });
        }

        if (billRefreshBtn) {
            billRefreshBtn.addEventListener('click', () => fetchBills(billSearchInput.value));
        }

        if (billSearchInput) {
            billSearchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => fetchBills(billSearchInput.value), 300);
            });
        }

        document.getElementById('closeBillGenModalBtn').addEventListener('click', () => modal.classList.add('hidden'));
        document.getElementById('closeViewBillModalBtn').addEventListener('click', () => viewModal.classList.add('hidden'));
        nextBtn.addEventListener('click', nextStep);
        backBtn.addEventListener('click', () => showStep(currentStep - 1));
        finalizeBillBtn.addEventListener('click', handleFinalizeBill);
        itemSearchInput.addEventListener('input', handleItemSearchInput);

        customerPhoneSearch.addEventListener('keydown', e => {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchCustomersForBill(e.target.value);
            }
        });

        modal.addEventListener('click', (e) => {
            const button = e.target.closest('button[data-item-id]');
            if (!button) return;
            const itemId = parseInt(button.dataset.itemId, 10);
            const item = currentBillItems.find(i => i.id === itemId);
            if (!item) return;
            if (button.classList.contains('remove-item-btn')) {
                currentBillItems = currentBillItems.filter(i => i.id !== itemId);
            } else if (button.classList.contains('qty-increase-btn')) {
                if (item.quantity < item.maxStock) item.quantity++; else showMessage(`Maximum stock for '\${item.name}' reached.`, 'error');
            } else if (button.classList.contains('qty-decrease-btn')) {
                item.quantity--;
                if (item.quantity === 0) currentBillItems = currentBillItems.filter(i => i.id !== itemId);
            }
            renderBillItemsTable('billItemsContainer', true);
        });

        billTableBody.addEventListener('click', (e) => {
            const viewBtn = e.target.closest('.view-bill-btn');
            if (viewBtn) openViewBillModal(viewBtn.dataset.billId);
        });

        document.getElementById('printBillBtn').addEventListener('click', () => window.print());

        // --- Initial Load ---
        fetchBills();
    });
</script>