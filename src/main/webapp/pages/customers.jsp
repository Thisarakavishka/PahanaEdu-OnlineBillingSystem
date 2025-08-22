<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%
    String role = (String) session.getAttribute("role");
%>

<div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">Customer Management</h1>
            <p class="text-gray-600">Manage customer accounts in your system.</p>
        </div>
        <div class="flex items-center space-x-2">
            <div class="relative inline-block text-left">
                <button id="exportBtn"
                        class="bg-white border border-gray-300 text-gray-700 font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2 hover:bg-gray-50">
                    <i data-feather="download" class="w-5 h-5"></i>
                    <span>Export</span>
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
            <c:if test='<%= "ADMIN".equals(role) %>'>
                <button id="addCustomerBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-5 rounded-md inline-flex items-center space-x-2 shadow-md">
                    <i data-feather="plus-circle" class="w-5 h-5"></i>
                    <span>Add New Customer</span>
                </button>
            </c:if>
        </div>
    </div>

    <!-- Customer List Card -->
    <div class="bg-white rounded-lg shadow-md border border-gray-200">
        <div class="p-4 border-b border-gray-200 flex flex-col sm:flex-row items-center gap-4">
            <div class="relative flex-grow w-full sm:w-auto">
                <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <i data-feather="search" class="w-4 h-4 text-gray-400"></i>
                </div>
                <input type="text" id="searchInput" placeholder="Search by name, phone, or account number..."
                       class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
            </div>
            <button id="refreshBtn"
                    class="bg-gray-100 hover:bg-gray-200 text-gray-800 font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2 transition duration-200 ease-in-out shadow-sm">
                <i data-feather="refresh-cw" class="w-5 h-5"></i>
                <span>Refresh</span>
            </button>
        </div>

        <!-- Loading Indicator -->
        <div id="loadingIndicator" class="text-center py-8 hidden">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
            <p class="text-gray-600">Loading customers...</p>
        </div>

        <!-- Message Display (Success or Error) - Included from separate file -->
        <jsp:include page="../components/messages.jsp"/>

        <!-- Customers Table -->
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Account
                        No.
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Name
                    </th>
                    <c:if test='<%= "ADMIN".equals(role) %>'>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                            Address
                        </th>
                    </c:if>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                        Telephone
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">Units
                        Consumed
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider text-center">
                        Actions
                    </th>
                </tr>
                </thead>
                <tbody id="customerTableBody" class="bg-white divide-y divide-gray-200">
                <tr>
                    <td colspan="6" class="text-center py-8 text-gray-500">No customers found.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add & Edit Customer Modal -->
<div id="customerModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-md relative">
        <button id="closeModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>
        <h2 id="modalTitle" class="text-2xl font-bold text-gray-800 mb-6 text-center">Add New Customer</h2>

        <form id="customerForm" class="space-y-4">
            <input type="hidden" id="customerId" name="id">

            <div>
                <label for="accountNumber" class="block text-sm font-semibold text-gray-700 mb-1">Account Number</label>
                <input type="text" id="accountNumber" name="accountNumber" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="accountNumberError" class="text-red-500 text-xs mt-1 hidden">Account number is required.</p>
            </div>
            <div>
                <label for="name" class="block text-sm font-semibold text-gray-700 mb-1">Customer Name</label>
                <input type="text" id="name" name="name" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="nameError" class="text-red-500 text-xs mt-1 hidden">Customer name is required.</p>
            </div>
            <div>
                <label for="address" class="block text-sm font-semibold text-gray-700 mb-1">Address</label>
                <input type="text" id="address" name="address"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="addressError" class="text-red-500 text-xs mt-1 hidden">Address is required.</p>
            </div>
            <div>
                <label for="phone" class="block text-sm font-semibold text-gray-700 mb-1">Phone Number</label>
                <input type="tel" id="phone" name="phone" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="phoneError" class="text-red-500 text-xs mt-1 hidden">Valid 10-digit phone number is required.</p>
            </div>
            <div>
                <label for="unitsConsumed" class="block text-sm font-semibold text-gray-700 mb-1">Units Consumed</label>
                <input type="number" id="unitsConsumed" name="unitsConsumed" value="0" min="0"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="unitsConsumedError" class="text-red-500 text-xs mt-1 hidden">Units consumed must be a
                    non-negative number.</p>
            </div>

            <div class="flex justify-end space-x-3 mt-6">
                <button type="button" id="cancelFormBtn"
                        class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Cancel
                </button>
                <button type="submit" id="saveCustomerBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Save Customer
                </button>
            </div>
        </form>
    </div>
</div>

<!-- View Customer Modal -->
<div id="customerViewModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-2xl relative">
        <button id="closeCustomerViewModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>

        <div class="flex items-center space-x-4 mb-6 border-b pb-4">
            <div class="bg-gray-800 text-white rounded-full p-3">
                <i data-feather="user" class="w-6 h-6"></i>
            </div>
            <div>
                <h2 id="viewCustomerName" class="text-2xl font-bold text-gray-800">Customer Name</h2>
                <p id="viewCustomerAccountNumber" class="text-gray-500 font-mono"></p>
            </div>
        </div>

        <div class="mb-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Contact Information</h3>
            <div class="border rounded-lg p-4 space-y-2 text-sm bg-gray-50">
                <div class="flex justify-between">
                    <span class="text-gray-500">PHONE</span>
                    <strong id="viewCustomerPhone" class="text-gray-800"></strong>
                </div>
                <hr>
                <div class="flex justify-between">
                    <span class="text-gray-500">ADDRESS</span>
                    <strong id="viewCustomerAddress" class="text-gray-800 text-right"></strong>
                </div>
            </div>
        </div>

        <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Account History</h3>
            <div class="border rounded-lg p-4 space-y-2 text-sm text-gray-600">
                <div class="flex justify-between"><span>Total Units Consumed:</span> <strong
                        id="viewCustomerUnitsConsumed" class="text-gray-800"></strong></div>
                <hr class="my-1">
                <div class="flex justify-between"><span>Created By:</span> <strong id="viewCustomerCreatedBy"
                                                                                   class="text-gray-800"></strong></div>
                <div class="flex justify-between"><span>Created At:</span> <strong id="viewCustomerCreatedAt"
                                                                                   class="text-gray-800"></strong></div>
                <hr class="my-1">
                <div class="flex justify-between"><span>Last Updated By:</span> <strong id="viewCustomerUpdatedBy"
                                                                                        class="text-gray-800"></strong>
                </div>
                <div class="flex justify-between"><span>Last Updated At:</span> <strong id="viewCustomerUpdatedAt"
                                                                                        class="text-gray-800"></strong>
                </div>
            </div>
        </div>
    </div>
</div>


<script>
    let currentCustomers = [];
    let searchTimeout;

    let customerTableBody, loadingIndicator, messageDisplay, messageText, searchInput, refreshBtn, customerModal,
        closeModalBtn, cancelFormBtn, customerForm, modalTitle, saveCustomerBtn;

    let customerIdField, accountNumberField, nameField, addressField, phoneField, unitsConsumedField;

    let accountNumberError, nameError, addressError, phoneError, unitsConsumedError;

    let customerViewModal, closeCustomerViewModalBtn, viewCustomerId, viewCustomerAccountNumber, viewCustomerName,
        viewCustomerAddress, viewCustomerPhone, viewCustomerUnitsConsumed, viewCustomerCreatedBy, viewCustomerCreatedAt,
        viewCustomerUpdatedBy, viewCustomerUpdatedAt;

    const loggedInUserRole = document.getElementById('userRoleHiddenInput').value;
    const loggedInUserId = document.getElementById('userIdHiddenInput') ? parseInt(document.getElementById('userIdHiddenInput').value) : null;
    const INITIAL_ADMIN_ID = 1;

    const showMessage = showToast;

    async function fetchCustomers(searchTerm = '') {
        console.log('Fetching customers with search term:', searchTerm);
        loadingIndicator.classList.remove('hidden');
        messageDisplay.classList.add('hidden'); // Hide any previous messages

        // getContextPath() is defined in dashboard.jsp and should be globally available
        let url = getContextPath() + '/customers';
        if (searchTerm) {
            url += '?search=' + encodeURIComponent(searchTerm);
        }

        try {
            const response = await fetch(url);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch customers.');
            }
            currentCustomers = data;
            renderCustomers(data);
            console.log('Customers fetched and rendered successfully.');
        } catch (error) {
            console.error('Error fetching customers:', error);
            showMessage(error.message || 'Failed to load customers. Please try again.', 'error');
            customerTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-red-500">' + (error.message || 'Error loading customers.') + '</td></tr>';
        } finally {
            loadingIndicator.classList.add('hidden');
        }
    }

    function renderCustomers(customers) {
        customerTableBody.innerHTML = ''; // Clear existing rows

        if (customers.length === 0) {
            customerTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-gray-500">No customers found.</td></tr>';
            return;
        }

        customers.forEach(customer => {
            const row = document.createElement('tr');
            let rowHtml = '';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">';
            rowHtml += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">' + customer.accountNumber + '</span>';
            rowHtml += '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">' + customer.name + '</td>';

            if (loggedInUserRole === 'ADMIN') { // Use loggedInUserRole for consistency
                rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 truncate max-w-xs">' + (customer.address || '-') + '</td>';
            }

            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' + customer.phone + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">';
            rowHtml += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ' + (customer.unitsConsumed < 10 ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800') + '">' + customer.unitsConsumed + ' units</span>';
            rowHtml += '</td>';

            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">';

            // --- VIEW BUTTON (All roles can view) ---
            rowHtml += '<button title="View Customer Details" class="text-blue-600 hover:text-blue-900 mr-3 view-btn" data-account-number="' + customer.accountNumber + '">' +
                '<i data-feather="eye" class="w-4 h-4 inline-block align-middle"></i></button>';

            // --- EDIT BUTTON LOGIC ---
            let canEdit = false;
            // Admins can edit all customers
            if (loggedInUserRole === 'ADMIN') {
                canEdit = true;
            }
            // Users can only view, no edit button for them.

            if (canEdit) {
                rowHtml += '<button title="Edit Customer" class="text-gray-600 hover:text-gray-900 mr-3 edit-btn" data-account-number="' + customer.accountNumber + '">' +
                    '<i data-feather="edit" class="w-4 h-4 inline-block align-middle"></i></button>';
            }
            // --- END EDIT BUTTON LOGIC ---

            // --- DELETE BUTTON LOGIC ---
            let canDelete = false;
            // Only Initial Admin (ID 1) can delete customers
            if (loggedInUserRole === 'ADMIN' && loggedInUserId === INITIAL_ADMIN_ID) {
                canDelete = true;
            }

            if (canDelete) {
                rowHtml += '<button title="Delete Customer" class="text-red-600 hover:text-red-900 delete-btn" data-account-number="' + customer.accountNumber + '">' +
                    '<i data-feather="trash-2" class="w-4 h-4 inline-block align-middle"></i></button>';
            }
            // If not allowed to delete, no button is added.
            // --- END DELETE BUTTON LOGIC ---

            rowHtml += '</td>';
            row.innerHTML = rowHtml;
            customerTableBody.appendChild(row);
        });

        feather.replace(); // Re-render feather icons
    }

    function clearValidationErrors() {
        accountNumberError.classList.add('hidden');
        nameError.classList.add('hidden');
        addressError.classList.add('hidden');
        phoneError.classList.add('hidden');
        unitsConsumedError.classList.add('hidden');
    }

    function validateForm() {
        clearValidationErrors(); // Clear previous errors
        let isValid = true;

        // Account Number Validation
        if (accountNumberField.value.trim() === '') {
            accountNumberError.textContent = 'Account number is required.';
            accountNumberError.classList.remove('hidden');
            isValid = false;
        } else if (!/^\d{8}$/.test(accountNumberField.value.trim())) { // Assuming 8 digits for auto-generated
            accountNumberError.textContent = 'Account number must be 8 digits.';
            accountNumberError.classList.remove('hidden');
            isValid = false;
        }

        // Name Validation
        if (nameField.value.trim() === '') {
            nameError.textContent = 'Customer name is required.';
            nameError.classList.remove('hidden');
            isValid = false;
        }

        // Address Validation (required for ADMIN)
        // Check if the current user is ADMIN and if the address field is empty
        if (loggedInUserRole === 'ADMIN' && addressField.value.trim() === '') {
            addressError.textContent = 'Address is required for admin users.';
            addressError.classList.remove('hidden');
            isValid = false;
        }

        // Phone Number Validation (10 digits, starts with 0)
        const phoneRegex = /^0\d{9}$/; // Starts with 0, followed by 9 digits
        if (phoneField.value.trim() === '') {
            phoneError.textContent = 'Phone number is required.';
            phoneError.classList.remove('hidden');
            isValid = false;
        } else if (!phoneRegex.test(phoneField.value.trim())) {
            phoneError.textContent = 'Phone number must be 10 digits and start with 0 (e.g., 0712345678).';
            phoneError.classList.remove('hidden');
            isValid = false;
        }

        // Units Consumed Validation
        const units = parseInt(unitsConsumedField.value);
        if (isNaN(units) || units < 0) {
            unitsConsumedError.textContent = 'Units consumed must be a non-negative number.';
            unitsConsumedError.classList.remove('hidden');
            isValid = false;
        }

        return isValid;
    }

    function openAddModal() {
        console.log('Attempting to open Add Customer modal.');
        modalTitle.textContent = 'Add New Customer';
        customerForm.reset(); // Clear form fields
        customerIdField.value = ''; // Ensure ID is empty for new customer
        accountNumberField.readOnly = false; // Account number is editable for new customer
        accountNumberField.value = generateAccountNumber(); // Generate new account number
        accountNumberField.readOnly = true; // Make it read-only after generation
        clearValidationErrors(); // Clear errors when opening
        customerModal.classList.remove('hidden');
        console.log('Add Customer modal should be visible.');
    }

    function generateAccountNumber() {
        const now = new Date();
        const year = String(now.getFullYear()).slice(-2);
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        // Combine with a random 2-digit number to ensure uniqueness for multiple adds on same day
        const random = String(Math.floor(Math.random() * 100)).padStart(2, '0');
        return year + month + day + random; // Example: 25080542
    }

    function closeCustomerModal() {
        customerModal.classList.add('hidden');
        clearValidationErrors(); // Clear errors when closing
        console.log('Customer modal closed.');
    }

    function closeCustomerViewModal() {
        customerViewModal.classList.add('hidden');
        console.log('Customer View modal closed.');
    }

    async function openViewModal(accountNumber) {
        try {
            const response = await fetch(getContextPath() + '/customers?accountNumber=' + encodeURIComponent(accountNumber));
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Failed to fetch customer details.');

            // Populate the new modal structure
            document.getElementById('viewCustomerName').textContent = data.name;
            document.getElementById('viewCustomerAccountNumber').textContent = 'Account #' + data.accountNumber;
            document.getElementById('viewCustomerPhone').textContent = data.phone;
            document.getElementById('viewCustomerAddress').textContent = data.address || '-';
            document.getElementById('viewCustomerUnitsConsumed').textContent = data.unitsConsumed + ' units';
            document.getElementById('viewCustomerCreatedBy').textContent = data.createdBy || '-';
            document.getElementById('viewCustomerCreatedAt').textContent = data.createdAt
                ? new Date(data.createdAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.createdAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';
            document.getElementById('viewCustomerUpdatedBy').textContent = data.updatedBy || '-';
            document.getElementById('viewCustomerUpdatedAt').textContent = data.updatedAt
                ? new Date(data.updatedAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.updatedAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';


            customerViewModal.classList.remove('hidden');
            feather.replace();
        } catch (error) {
            showMessage(error.message || 'Failed to load customer details.', 'error');
        }
    }

    function downloadCustomersAsPDF() {
        if (currentCustomers.length === 0) {
            showMessage("No data to export.", "error");
            return;
        }
        const {jsPDF} = window.jspdf;
        const doc = new jsPDF();
        doc.setFont("helvetica", "bold");
        doc.setFontSize(18);
        doc.text("Customer Report - Pahana Edu", 14, 22);
        doc.setFontSize(11);
        doc.setTextColor(100);
        doc.text(`Report generated on: \${new Date().toLocaleDateString()}`, 14, 30);
        const tableColumn = ["Account No.", "Name", "Phone", "Address", "Units Consumed", "Created At"];
        const tableRows = [];
        currentCustomers.forEach(cust => {
            const custData = [
                cust.accountNumber, cust.name, cust.phone, cust.address || '-', cust.unitsConsumed, new Date(cust.createdAt).toLocaleString('en-GB')
            ];
            tableRows.push(custData);
        });
        doc.autoTable({
            startY: 38,
            head: [tableColumn],
            body: tableRows,
            theme: 'striped',
            headStyles: {fillColor: [30, 30, 30]}
        });
        doc.save('PahanaEdu_Customers_Report.pdf');
    }

    function downloadCustomersAsCSV() {
        if (currentCustomers.length === 0) {
            showMessage("No data to export.", "error");
            return;
        }
        const headers = "AccountNumber,Name,Address,Phone,UnitsConsumed";
        const csvRows = [headers];
        currentCustomers.forEach(cust => {
            const row = [`"\${cust.accountNumber}"`, `"\${cust.name}"`, `"\${cust.address || ''}"`, `"\${cust.phone}"`, cust.unitsConsumed];
            csvRows.push(row.join(','));
        });
        const csvString = csvRows.join('\\n');
        const blob = new Blob([csvString], {type: 'text/csv;charset=utf-8;'});
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", "PahanaEdu_Customers_Report.csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    async function openEditModal(accountNumber) {
        console.log('Attempting to open Edit Customer modal for account:', accountNumber);
        try {
            const response = await fetch(getContextPath() + '/customers?accountNumber=' + encodeURIComponent(accountNumber));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch customer details for editing.');
            }

            modalTitle.textContent = 'Edit Customer';
            customerIdField.value = data.id;
            accountNumberField.value = data.accountNumber;
            accountNumberField.readOnly = true; // Account number usually not editable after creation
            nameField.value = data.name;
            addressField.value = data.address || '';
            phoneField.value = data.phone;
            unitsConsumedField.value = data.unitsConsumed;

            clearValidationErrors(); // Clear errors when opening
            customerModal.classList.remove('hidden');
            console.log('Edit Customer modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening edit modal:', error);
            showMessage(error.message || 'Failed to load customer details for editing.', 'error');
        }
    }

    async function handleCustomerFormSubmit(e) {
        e.preventDefault(); // Prevent default form submission
        console.log('Customer form submitted.');

        // Run frontend validation
        if (!validateForm()) {
            console.log('Frontend validation failed.');
            return; // Stop submission if validation fails
        }

        const isEdit = customerIdField.value !== '';
        const method = isEdit ? 'PUT' : 'POST';
        const url = getContextPath() + '/customers';

        const formData = new URLSearchParams();
        if (isEdit) {
            formData.append('id', customerIdField.value);
            console.log("update customer id was -", customerIdField.value)
        }
        formData.append('accountNumber', accountNumberField.value);
        formData.append('name', nameField.value);
        formData.append('address', addressField.value);
        formData.append('phone', phoneField.value);
        formData.append('unitsConsumed', unitsConsumedField.value);

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || (isEdit ? 'Failed to update customer.' : 'Failed to add customer.'));
            }

            showMessage(data.message, 'success');
            closeCustomerModal(); // Close modal
            fetchCustomers(searchInput.value); // Refresh list
            console.log('Customer ' + (isEdit ? 'updated' : 'added') + ' successfully.');
        } catch (error) {
            console.error('Error ' + (isEdit ? 'updating' : 'adding') + ' customer:', error);
            showMessage(error.message || 'Failed to ' + (isEdit ? 'update' : 'add') + ' customer. Please check your inputs.', 'error');
        }
    }

    async function deleteCustomer(accountNumber) {
        if (!confirm('Are you sure you want to delete customer with Account Number: ' + accountNumber + '? This action cannot be undone.')) {
            console.log('Customer deletion cancelled.');
            return; // User cancelled
        }

        try {
            const response = await fetch(getContextPath() + '/customers?accountNumber=' + encodeURIComponent(accountNumber), {
                method: 'DELETE'
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to delete customer.');
            }

            showMessage(data.message, 'success');
            fetchCustomers(searchInput.value); // Refresh list
            console.log('Customer deleted successfully.');
        } catch (error) {
            console.error('Error deleting customer:', error);
            showMessage(error.message || 'Failed to delete customer. Please try again.', 'error');
        }
    }

    function handleSearchInput() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchCustomers(searchInput.value);
        }, 900); // Debounce for 900ms
    }

    function initCustomerPage() {
        console.log('initCustomerPage() called. Assigning DOM elements and attaching listeners.');

        // Assign DOM elements once the document is ready
        customerTableBody = document.getElementById('customerTableBody');
        loadingIndicator = document.getElementById('loadingIndicator');
        messageDisplay = document.getElementById('messageDisplay');
        messageText = document.getElementById('messageText');
        searchInput = document.getElementById('searchInput');
        refreshBtn = document.getElementById('refreshBtn');
        customerModal = document.getElementById('customerModal');
        closeModalBtn = document.getElementById('closeModalBtn');
        cancelFormBtn = document.getElementById('cancelFormBtn');
        customerForm = document.getElementById('customerForm');
        modalTitle = document.getElementById('modalTitle');
        saveCustomerBtn = document.getElementById('saveCustomerBtn');

        customerIdField = document.getElementById('customerId');
        accountNumberField = document.getElementById('accountNumber');
        nameField = document.getElementById('name');
        addressField = document.getElementById('address');
        phoneField = document.getElementById('phone');
        unitsConsumedField = document.getElementById('unitsConsumed');

        // Assign error message elements
        accountNumberError = document.getElementById('accountNumberError');
        nameError = document.getElementById('nameError');
        addressError = document.getElementById('addressError');
        phoneError = document.getElementById('phoneError');
        unitsConsumedError = document.getElementById('unitsConsumedError');

        // View Modal elements
        customerViewModal = document.getElementById('customerViewModal');
        closeCustomerViewModalBtn = document.getElementById('closeCustomerViewModalBtn');
        viewCustomerId = document.getElementById('viewCustomerId');
        viewCustomerAccountNumber = document.getElementById('viewCustomerAccountNumber');
        viewCustomerName = document.getElementById('viewCustomerName');
        viewCustomerAddress = document.getElementById('viewCustomerAddress');
        viewCustomerPhone = document.getElementById('viewCustomerPhone');
        viewCustomerUnitsConsumed = document.getElementById('viewCustomerUnitsConsumed');
        viewCustomerCreatedBy = document.getElementById('viewCustomerCreatedBy');
        viewCustomerCreatedAt = document.getElementById('viewCustomerCreatedAt');
        viewCustomerUpdatedBy = document.getElementById('viewCustomerUpdatedBy');
        viewCustomerUpdatedAt = document.getElementById('viewCustomerUpdatedAt');


        // --- Event Delegation ---
        document.body.addEventListener('click', (event) => {
            // Add New Customer button
            if (event.target && (event.target.id === 'addCustomerBtn' || event.target.closest('#addCustomerBtn'))) {
                console.log('Add Customer button clicked via delegation!');
                openAddModal();
            }
            // View customer button
            if (event.target && event.target.classList.contains('view-btn')) {
                console.log('View button clicked via delegation!');
                openViewModal(event.target.dataset.accountNumber);
            } else if (event.target && event.target.closest('.view-btn')) {
                console.log('View icon clicked via delegation!');
                openViewModal(event.target.closest('.view-btn').dataset.accountNumber);
            }
            // Edit customer button
            if (event.target && event.target.classList.contains('edit-btn')) {
                console.log('Edit button clicked via delegation!');
                openEditModal(event.target.dataset.accountNumber);
            } else if (event.target && event.target.closest('.edit-btn')) {
                console.log('Edit icon clicked via delegation!');
                openEditModal(event.target.closest('.edit-btn').dataset.accountNumber);
            }
            // Delete customer button
            if (event.target && event.target.classList.contains('delete-btn')) {
                console.log('Delete button clicked via delegation!');
                deleteCustomer(event.target.dataset.accountNumber);
            } else if (event.target && event.target.closest('.delete-btn')) {
                console.log('Delete icon clicked via delegation!');
                deleteCustomer(event.target.closest('.delete-btn').dataset.accountNumber);
            }
        });


        // Attach direct listeners for modal controls and search/refresh
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeCustomerModal);
        if (cancelFormBtn) cancelFormBtn.addEventListener('click', closeCustomerModal);
        if (customerForm) customerForm.addEventListener('submit', handleCustomerFormSubmit);
        if (closeCustomerViewModalBtn) closeCustomerViewModalBtn.addEventListener('click', closeCustomerViewModal);
        if (searchInput) searchInput.addEventListener('input', handleSearchInput);
        if (refreshBtn) refreshBtn.addEventListener('click', () => fetchCustomers(searchInput.value));

        // Initial data load
        fetchCustomers();
        feather.replace(); // Initialize icons on page load
        console.log('Customer page initialization complete.');
    }

    const exportBtn = document.getElementById('exportBtn');
    const exportMenu = document.getElementById('exportMenu');

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
        downloadCustomersAsPDF();
        exportMenu.classList.add('hidden');
    });

    document.getElementById('exportCsvBtn').addEventListener('click', (e) => {
        e.preventDefault();
        downloadCustomersAsCSV();
        exportMenu.classList.add('hidden');
    });

    document.addEventListener('DOMContentLoaded', initCustomerPage);
</script>