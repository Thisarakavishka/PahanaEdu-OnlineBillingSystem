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
        <c:if test='<%= "ADMIN".equals(role) %>'>
            <button id="addCustomerBtn"
                    class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-5 rounded-md inline-flex items-center space-x-2 transition duration-200 ease-in-out shadow-md">
                <i data-feather="plus-circle" class="w-5 h-5"></i>
                <span>Add New Customer</span>
            </button>
        </c:if>
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
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Units
                        Consumed
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
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

<%-- All JavaScript template literals (backticks ` and ${}) MUST be escaped with a backslash (\) to prevent JSP parsing errors. --%>
<script>
    // Global variables for DOM elements (declared but not assigned immediately)
    let customerTableBody;
    let loadingIndicator;
    let messageDisplay;
    let messageText;
    let searchInput;
    let refreshBtn;
    let customerModal;
    let closeModalBtn;
    let cancelFormBtn;
    let customerForm;
    let modalTitle;
    let saveCustomerBtn;

    // Form fields (declared but not assigned immediately)
    let customerIdField;
    let accountNumberField;
    let nameField;
    let addressField;
    let phoneField;
    let unitsConsumedField;

    // Error message elements
    let accountNumberError;
    let nameError;
    let addressError;
    let phoneError;
    let unitsConsumedError;

    // Get user role from a hidden input in dashboard.jsp ,This value is passed from the server-side JSP and is available globally.
    let userRole = document.getElementById('userRoleHiddenInput').value;

    // Function to display messages (success or error), Hide after 5 seconds
    function showMessage(message, type = 'success') {
        messageText.textContent = message;
        messageDisplay.classList.remove('hidden', 'bg-red-100', 'border-red-400', 'text-red-700', 'bg-green-100', 'border-green-400', 'text-green-700');
        if (type === 'success') {
            messageDisplay.classList.add('bg-green-100', 'border-green-400', 'text-green-700');
        } else {
            messageDisplay.classList.add('bg-red-100', 'border-red-400', 'text-red-700');
        }
        messageDisplay.classList.remove('hidden');
        setTimeout(() => {
            messageDisplay.classList.add('hidden');
        }, 5000);
    }

    // Function to fetch customers from the backend
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

    // Function to render customers in the table
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

            if (userRole === 'ADMIN') {
                rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 truncate max-w-xs">' + (customer.address || '-') + '</td>';
            }

            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' + customer.phone + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">';
            rowHtml += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">' + customer.unitsConsumed + ' units</span>';
            rowHtml += '</td>';

            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">';

            if (userRole === 'ADMIN') {
                rowHtml += '<button class="text-gray-600 hover:text-gray-900 mr-3 edit-btn" data-account-number="' + customer.accountNumber + '">' +
                    '<i data-feather="edit" class="w-4 h-4 inline-block align-middle"></i> Edit</button>';
                rowHtml += '<button class="text-red-600 hover:text-red-900 delete-btn" data-account-number="' + customer.accountNumber + '">' +
                    '<i data-feather="trash-2" class="w-4 h-4 inline-block align-middle"></i> Delete</button>';
            } else {
                rowHtml += '<button class="text-green-600 hover:text-green-900 view-bill-btn" data-account-number="' + customer.accountNumber + '">' +
                    '<i data-feather="file-text" class="w-4 h-4 inline-block align-middle"></i> View Bill</button>';
            }

            rowHtml += '</td>';
            row.innerHTML = rowHtml;
            customerTableBody.appendChild(row);
        });

        feather.replace(); // Re-render feather icons
    }

    // Function to clear all validation error messages
    function clearValidationErrors() {
        accountNumberError.classList.add('hidden');
        nameError.classList.add('hidden');
        addressError.classList.add('hidden');
        phoneError.classList.add('hidden');
        unitsConsumedError.classList.add('hidden');
    }

    // Function to validate the form fields
    function validateForm() {
        clearValidationErrors(); // Clear previous errors
        let isValid = true;

        // Account Number Validation
        if (accountNumberField.value.trim() === '') {
            accountNumberError.textContent = 'Account number is required.';
            accountNumberError.classList.remove('hidden');
            isValid = false;
        } else if (!/^\d{8}$/.test(accountNumberField.value.trim())) { // Assuming 6 digits for auto-generated
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

        // Address Validation (assuming it's required for ADMIN)
        if (userRole === 'ADMIN' && addressField.value.trim() === '') {
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

    // Open Add Customer Modal
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

    // Generate a simple 6-digit account number (e.g., YYMMDD or random)
    function generateAccountNumber() {
        const now = new Date();
        const year = String(now.getFullYear()).slice(-2);
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        // Combine with a random 2-digit number to ensure uniqueness for multiple adds on same day
        const random = String(Math.floor(Math.random() * 100)).padStart(2, '0');
        return year + month + day + random; // Example: 25080542
    }

    // Close Modal
    function closeCustomerModal() {
        customerModal.classList.add('hidden');
        clearValidationErrors(); // Clear errors when closing
        console.log('Customer modal closed.');
    }

    // Open Edit Customer Modal
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

    // Handle form submission (Add/Edit)
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

    // Handle Delete Customer
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

    // Search functionality with debounce
    let searchTimeout;

    function handleSearchInput() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchCustomers(searchInput.value);
        }, 900); // Debounce for 300ms
    }

    // --- Initialization Function ---
    // This function will be called once the page is fully loaded
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


        // Get user role from the hidden input
        const userRoleHiddenInput = document.getElementById('userRoleHiddenInput');
        if (userRoleHiddenInput) {
            userRole = userRoleHiddenInput.value;
            console.log('User role detected:', userRole);
        } else {
            console.warn("Hidden input 'userRoleHiddenInput' not found. User role might not be correctly set.");
            userRole = 'GUEST'; // Default or fallback
        }

        // --- Event Delegation for Add Customer Button ---
        // Attach listener to a parent element (e.g., document.body or a specific container)
        document.body.addEventListener('click', (event) => {
            // Check for the "Add New Customer" button
            if (event.target && (event.target.id === 'addCustomerBtn' || event.target.closest('#addCustomerBtn'))) {
                console.log('Add Customer button clicked via delegation!');
                openAddModal();
            }
            // Handle edit/delete/view-bill buttons via delegation
            if (event.target && event.target.classList.contains('edit-btn')) {
                console.log('Edit button clicked via delegation!');
                openEditModal(event.target.dataset.accountNumber);
            } else if (event.target && event.target.closest('.edit-btn')) { // For icon clicks inside button
                console.log('Edit icon clicked via delegation!');
                openEditModal(event.target.closest('.edit-btn').dataset.accountNumber);
            }

            if (event.target && event.target.classList.contains('delete-btn')) {
                console.log('Delete button clicked via delegation!');
                deleteCustomer(event.target.dataset.accountNumber);
            } else if (event.target && event.target.closest('.delete-btn')) { // For icon clicks inside button
                console.log('Delete icon clicked via delegation!');
                deleteCustomer(event.target.closest('.delete-btn').dataset.accountNumber);
            }

            if (event.target && event.target.classList.contains('view-bill-btn')) {
                console.log('View Bill button clicked via delegation!');
                const accountNumber = event.target.dataset.accountNumber;
                // window.location.href = getContextPath() + '/dashboard.jsp?page=bill&accountNumber=' + accountNumber; // Use string concatenation
                showMessage('Viewing bill for ' + accountNumber, 'info'); // Use string concatenation
            } else if (event.target && event.target.closest('.view-bill-btn')) { // For icon clicks inside button
                console.log('View Bill icon clicked via delegation!');
                const accountNumber = event.target.closest('.view-bill-btn').dataset.accountNumber;
                showMessage('Viewing bill for ' + accountNumber, 'info'); // Use string concatenation
            }
        });


        // Attach direct listeners for modal controls and search/refresh
        // These elements are part of the modal, which is loaded with customers.jsp
        // They should be available when initCustomerPage runs due to the dynamic script loading strategy.
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeCustomerModal);
        if (cancelFormBtn) cancelFormBtn.addEventListener('click', closeCustomerModal);
        if (customerForm) customerForm.addEventListener('submit', handleCustomerFormSubmit);
        if (searchInput) searchInput.addEventListener('input', handleSearchInput);
        if (refreshBtn) refreshBtn.addEventListener('click', () => fetchCustomers(searchInput.value));

        // Initial data load
        fetchCustomers();
        feather.replace(); // Initialize icons on page load
        console.log('Customer page initialization complete.');
    }

    // Call the initialization function once the DOM is fully loaded
    document.addEventListener('DOMContentLoaded', initCustomerPage);
</script>