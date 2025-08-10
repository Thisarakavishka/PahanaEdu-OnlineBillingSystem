<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-08-06
  Time: 20:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%
    String role = (String) session.getAttribute("role");
    // Server-side check: Only ADMIN should access this page directly
    if (!"ADMIN".equals(role)) {
        response.sendRedirect(request.getContextPath() + "/dashboard.jsp?page=home");
        return;
    }
%>

<div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">User Management</h1>
            <p class="text-gray-600">Manage user accounts and roles.</p>
        </div>
        <c:if test="${sessionScope.role == 'ADMIN' && sessionScope.userId == 1}">

            <button id="addUserBtn"
                    class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-5 rounded-md inline-flex items-center space-x-2 transition duration-200 ease-in-out shadow-md">
                <i data-feather="user-plus" class="w-5 h-5"></i>
                <span>Add New User</span>
            </button>
        </c:if>

    </div>

    <!-- User List Card -->
    <div class="bg-white rounded-lg shadow-md border border-gray-200">
        <div class="p-4 border-b border-gray-200 flex flex-col sm:flex-row items-center gap-4">
            <div class="relative flex-grow w-full sm:w-auto">
                <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <i data-feather="search" class="w-4 h-4 text-gray-400"></i>
                </div>
                <input type="text" id="searchInput" placeholder="Search by username or role..."
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
            <p class="text-gray-600">Loading users...</p>
        </div>

        <!-- Message Display (Success/Error) - Included from separate file -->
        <jsp:include page="../components/messages.jsp"/>

        <!-- Users Table -->
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">ID</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                        Username
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Role
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created
                        At
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 uppercase tracking-wider">
                        Actions
                    </th>
                </tr>
                </thead>
                <tbody id="userTableBody" class="bg-white divide-y divide-gray-200">
                <tr>
                    <td colspan="5" class="text-center py-8 text-gray-500">No users found.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add & Edit User Modal (HTML embedded directly in this JSP) -->
<div id="userModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-md relative">
        <button id="closeModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>
        <h2 id="modalTitle" class="text-2xl font-bold text-gray-800 mb-6 text-center">Add New User</h2>

        <form id="userForm" class="space-y-4">
            <input type="hidden" id="userIdField" name="id">

            <div>
                <label for="username" class="block text-sm font-semibold text-gray-700 mb-1">Username</label>
                <input type="text" id="username" name="username" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="usernameError" class="text-red-500 text-xs mt-1 hidden">Username is required.</p>
            </div>
            <div>
                <label for="password" class="block text-sm font-semibold text-gray-700 mb-1">Password</label>
                <input type="password" id="password" name="password"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="passwordError" class="text-red-500 text-xs mt-1 hidden">Password is required (min 6 chars).</p>
            </div>
            <div>
                <label for="confirmPassword" class="block text-sm font-semibold text-gray-700 mb-1">Confirm
                    Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword"
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="confirmPasswordError" class="text-red-500 text-xs mt-1 hidden">Passwords do not match.</p>
            </div>
            <div>
                <label for="role" class="block text-sm font-semibold text-gray-700 mb-1">Role</label>
                <select id="role" name="role" required
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm">
                    <option value="">Select Role</option>
                    <option value="ADMIN">ADMIN</option>
                    <option value="USER">USER</option>
                </select>
                <p id="roleError" class="text-red-500 text-xs mt-1 hidden">Role is required.</p>
            </div>

            <div class="flex justify-end space-x-3 mt-6">
                <button type="button" id="cancelFormBtn"
                        class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Cancel
                </button>
                <button type="submit" id="saveUserBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Save User
                </button>
            </div>
        </form>
    </div>
</div>

<!-- View User Modal -->
<div id="userViewModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-6 rounded-xl shadow-2xl w-full max-w-3xl relative">

        <!-- Close Button -->
        <button id="closeUserViewModalBtn"
                class="absolute top-4 right-4 text-gray-400 hover:text-gray-700 transition">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>

        <!-- Title -->
        <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center border-b pb-3">User Details</h2>

        <!-- Grid Layout -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">User ID</label>
                <p id="viewUserIdDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Username</label>
                <p id="viewUsernameDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Role</label>
                <p id="viewRoleDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Created By</label>
                <p id="viewCreatedByDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Created At</label>
                <p id="viewCreatedAtDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Last Updated By</label>
                <p id="viewUpdatedByDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

            <div>
                <label class="block text-sm font-semibold text-gray-600 mb-1">Last Updated At</label>
                <p id="viewUpdatedAtDisplay" class="px-3 py-2 bg-gray-50 rounded-md border text-gray-800 text-sm"></p>
            </div>

        </div>
    </div>
</div>


<script>
    // Global variables for DOM elements (declared but not assigned immediately)
    let userTableBody, loadingIndicator, messageDisplay, messageText, searchInput, refreshBtn, userModal, closeModalBtn,
        cancelFormBtn, userForm, modalTitle, saveUserBtn;

    // Form fields
    let userIdField, usernameField, passwordField, confirmPasswordField, roleField;

    // Error message elements
    let usernameError, passwordError, confirmPasswordError, roleError;

    // View Modal elements
    let userViewModal, closeUserViewModalBtn, viewUserIdDisplay, viewUsernameDisplay, viewRoleDisplay,
        viewCreatedByDisplay, viewCreatedAtDisplay, viewUpdatedByDisplay, viewUpdatedAtDisplay;


    // Get user role and ID from hidden inputs in dashboard.jsp
    const loggedInUserRole = document.getElementById('userRoleHiddenInput').value;
    const loggedInUserId = document.getElementById('userIdHiddenInput') ? parseInt(document.getElementById('userIdHiddenInput').value) : null;
    const INITIAL_ADMIN_ID = 1; // Constant for initial admin ID

    // --- Helper Functions (Defined first to ensure availability) ---

    // Function to display messages (success/error)
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
        }, 5000); // Hide after 5 seconds
    }

    // Function to clear all validation error messages
    function clearValidationErrors() {
        usernameError.classList.add('hidden');
        passwordError.classList.add('hidden');
        confirmPasswordError.classList.add('hidden');
        roleError.classList.add('hidden');
    }

    // Function to validate the form fields
    function validateForm(isEditMode) {
        clearValidationErrors(); // Clear previous errors
        let isValid = true;

        // Username Validation
        if (usernameField.value.trim() === '') {
            usernameError.textContent = 'Username is required.';
            usernameError.classList.remove('hidden');
            isValid = false;
        }

        // Password Validation (required for add, optional for edit if not changed)
        const isPasswordProvided = passwordField.value.trim() !== '';
        if (!isEditMode || isPasswordProvided) { // If adding, or editing and password is provided
            if (passwordField.value.trim() === '') {
                passwordError.textContent = 'Password is required.';
                passwordError.classList.remove('hidden');
                isValid = false;
            } else if (passwordField.value.trim().length < 6) {
                passwordError.textContent = 'Password must be at least 6 characters.';
                passwordError.classList.remove('hidden');
                isValid = false;
            } else if (passwordField.value.trim() !== confirmPasswordField.value.trim()) {
                confirmPasswordError.textContent = 'Passwords do not match.';
                confirmPasswordError.classList.remove('hidden');
                isValid = false;
            }
        }

        // Role Validation
        if (roleField.value === '') {
            roleError.textContent = 'Role is required.';
            roleError.classList.remove('hidden');
            isValid = false;
        }

        return isValid;
    }

    // Function to fetch users from the backend
    async function fetchUsers(searchTerm = '') {
        console.log('Fetching users with search term:', searchTerm);
        loadingIndicator.classList.remove('hidden');
        messageDisplay.classList.add('hidden'); // Hide any previous messages

        let url = getContextPath() + '/users';
        if (searchTerm) {
            url += '?action=list&search=' + encodeURIComponent(searchTerm);
        } else {
            url += '?action=list';
        }

        try {
            const response = await fetch(url);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch users.');
            }

            renderUsers(data);
            console.log('Users fetched and rendered successfully.');
        } catch (error) {
            console.error('Error fetching users:', error);
            showMessage(error.message || 'Failed to load users. Please try again.', 'error');
            userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-8 text-red-500">' + (error.message || 'Error loading users.') + '</td></tr>';
        } finally {
            loadingIndicator.classList.add('hidden');
        }
    }

    // Function to render users in the table
    function renderUsers(users) {
        userTableBody.innerHTML = ''; // Clear existing rows

        if (users.length === 0) {
            userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-8 text-gray-500">No users found.</td></tr>';
            return;
        }

        users.forEach(user => {
            const isCurrentUser = (loggedInUserId !== null && user.id === loggedInUserId);
            const isInitialAdminUserInRow = (user.id === 1);

            // Show row rules
            let showUsersRow = false;
            if (loggedInUserRole === 'ADMIN') {
                showUsersRow = true; // Admins see all
            } else if (loggedInUserRole === 'USER') {
                showUsersRow = isCurrentUser; // Users only see self
            }
            if (!showUsersRow) return;

            const row = document.createElement('tr');
            let rowHtml = '';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">' + user.id + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">' + user.username + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' + user.role + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' +
                (user.createdAt
                    ? new Date(user.createdAt).toLocaleDateString('en-GB', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric'
                    })
                    : '-') + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">';

            // --- VIEW ---
            rowHtml += '<button title="View User Details" class="text-blue-600 hover:text-blue-900 mr-3 view-btn" data-user-id="' + user.id + '">' +
                '<i data-feather="eye" class="w-4 h-4 inline-block align-middle"></i></button>';

            // --- EDIT ---
            let canEdit = false;
            if (loggedInUserId === 1) {
                // Initial Admin → can edit anyone
                canEdit = true;
            } else if (loggedInUserRole === 'ADMIN') {
                // Normal Admin → can only edit users
                if (user.role === 'USER') {
                    canEdit = true;
                }
            } else if (loggedInUserRole === 'USER' && isCurrentUser) {
                // Regular users can edit themselves
                canEdit = true;
            }

            if (canEdit) {
                rowHtml += '<button title="Edit User" class="text-gray-600 hover:text-gray-900 mr-3 edit-btn" data-user-id="' + user.id + '">' +
                    '<i data-feather="edit" class="w-4 h-4 inline-block align-middle"></i></button>';
            } else {
                //rowHtml += '<span class="text-gray-400">No Edit</span>';
            }

            // --- DELETE ---
            let canDelete = false;
            if (loggedInUserId === 1) {
                // Initial Admin can delete anyone
                if (!isInitialAdminUserInRow) canDelete = true; // except themselves
            } else if (loggedInUserRole === 'ADMIN') {
                // Normal Admin → can delete only users
                if (user.role === 'USER') {
                    canDelete = true;
                }
            }

            if (canDelete) {
                rowHtml += '<button title="Delete User" class="text-red-600 hover:text-red-900 mr-3 delete-btn" data-user-id="' + user.id + '">' +
                    '<i data-feather="trash-2" class="w-4 h-4 inline-block align-middle"></i></button>';
            } else if (!isInitialAdminUserInRow) {
                //    rowHtml += '<span class="text-gray-400 ml-3">No Delete</span>';
            }

            rowHtml += '</td>';
            row.innerHTML = rowHtml;
            userTableBody.appendChild(row);
        });

        feather.replace();
    }

    // Open Add User Modal
    function openAddModal() {
        console.log('Attempting to open Add User modal.');
        modalTitle.textContent = 'Add New User';
        userForm.reset(); // Clear form fields
        userIdField.value = ''; // Ensure ID is empty for new user
        usernameField.readOnly = false; // Username is editable for new user
        passwordField.required = true; // Password is required for new user
        confirmPasswordField.required = true; // Confirm password is required for new user
        clearValidationErrors(); // Clear errors when opening
        userModal.classList.remove('hidden');
        console.log('Add User modal should be visible.');
    }

    // Close Modals
    function closeUserModal() {
        userModal.classList.add('hidden');
        clearValidationErrors(); // Clear errors when closing
        console.log('User modal closed.');
    }

    function closeUserViewModal() { // Defined here
        userViewModal.classList.add('hidden');
        console.log('User View modal closed.');
    }

    // Open View User Modal
    async function openViewModal(userId) {
        console.log('Attempting to open View User modal for ID:', userId);
        try {
            const response = await fetch(getContextPath() + '/users?action=searchById&id=' + encodeURIComponent(userId));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch user details for viewing.');
            }

            viewUserIdDisplay.textContent = data.id;
            viewUsernameDisplay.textContent = data.username;
            viewRoleDisplay.textContent = data.role;
            // Display usernames, which are now provided by the servlet
            viewCreatedByDisplay.textContent = data.createdBy || '-';
            viewCreatedAtDisplay.textContent = data.createdAt
                ? new Date(data.createdAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.createdAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';
            viewUpdatedByDisplay.textContent = data.updatedBy || '-';
            viewUpdatedAtDisplay.textContent = data.updatedAt
                ? new Date(data.updatedAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.updatedAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';

            userViewModal.classList.remove('hidden');
            console.log('View User modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening view modal:', error);
            showMessage(error.message || 'Failed to load user details for viewing.', 'error');
        }
    }

    // Open Edit User Modal
    async function openEditModal(userId) {
        console.log('Attempting to open Edit User modal for ID:', userId);
        try {
            const response = await fetch(getContextPath() + '/users?action=searchById&id=' + encodeURIComponent(userId));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch user details for editing.');
            }

            modalTitle.textContent = 'Edit User';
            userIdField.value = data.id;
            usernameField.value = data.username;
            // Username should be editable based on rules
            usernameField.readOnly = false; // By default, allow editing
            if (loggedInUserRole === 'ADMIN' && data.id === INITIAL_ADMIN_ID && loggedInUserId !== INITIAL_ADMIN_ID) {
                // If logged-in user is admin (but not initial admin) and target is initial admin,
                // username should be read-only.
                usernameField.readOnly = true;
            }

            roleField.value = data.role;
            passwordField.value = ''; // Clear password fields for security
            confirmPasswordField.value = '';
            passwordField.required = false; // Password is optional for edit
            confirmPasswordField.required = false; // Confirm password is optional for edit

            clearValidationErrors(); // Clear errors when opening
            userModal.classList.remove('hidden');
            console.log('Edit User modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening edit modal:', error);
            showMessage(error.message || 'Failed to load user details for editing.', 'error');
        }
    }

    // Handle form submission (Add/Edit)
    async function handleUserFormSubmit(e) {
        e.preventDefault(); // Prevent default form submission
        console.log('User form submitted.');

        const isEdit = userIdField.value !== '';
        // Run frontend validation
        if (!validateForm(isEdit)) {
            console.log('Frontend validation failed.');
            return; // Stop submission if validation fails
        }

        const method = isEdit ? 'PUT' : 'POST';
        const url = getContextPath() + '/users';

        const formData = new URLSearchParams();
        if (isEdit) {
            formData.append('action', 'update'); // Action for update
            formData.append('id', userIdField.value);
            console.log("update user id was -", userIdField.value);
        } else {
            formData.append('action', 'add'); // Action for add
        }
        formData.append('username', usernameField.value);
        if (passwordField.value.trim() !== '') { // Only send password if it's provided
            formData.append('password', passwordField.value);
        }
        formData.append('role', roleField.value);

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
                throw new Error(data.message || `Failed to ${isEdit ? 'update' : 'add'} user.`);
            }

            showMessage(data.message, 'success');
            closeUserModal(); // Close modal
            fetchUsers(searchInput.value); // Refresh list
            console.log('User ' + (isEdit ? 'updated' : 'added') + ' successfully.');
        } catch (error) {
            console.error('Error ' + (isEdit ? 'updating' : 'adding') + ' user:', error);
            showMessage(error.message || 'Failed to ' + (isEdit ? 'update' : 'add') + ' user. Please check your inputs.', 'error');
        }
    }

    // Handle Delete User
    async function deleteUser(userId) {
        console.log('Attempting to delete user ID:', userId);
        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
            console.log('User deletion cancelled.');
            return; // User cancelled
        }

        try {
            const response = await fetch(getContextPath() + '/users?action=delete&id=' + encodeURIComponent(userId) + '&deletedBy=' + encodeURIComponent(loggedInUserId), {
                method: 'DELETE'
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to delete user.');
            }

            showMessage(data.message, 'success');
            fetchUsers(searchInput.value); // Refresh list
            console.log('User deleted successfully.');
        } catch (error) {
            console.error('Error deleting user:', error);
            showMessage(error.message || 'Failed to delete user. Please try again.', 'error');
        }
    }

    // Search functionality with debounce
    let searchTimeout;

    function handleSearchInput() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchUsers(searchInput.value);
        }, 900); // Debounce for 900ms
    }

    // --- Initialization Function ---
    // This function will be called once the page is fully loaded
    function initUserPage() {
        console.log('initUserPage() called. Assigning DOM elements and attaching listeners.');

        // Assign DOM elements once the document is ready
        userTableBody = document.getElementById('userTableBody');
        loadingIndicator = document.getElementById('loadingIndicator');
        messageDisplay = document.getElementById('messageDisplay');
        messageText = document.getElementById('messageText');
        searchInput = document.getElementById('searchInput');
        refreshBtn = document.getElementById('refreshBtn');
        userModal = document.getElementById('userModal');
        closeModalBtn = document.getElementById('closeModalBtn');
        cancelFormBtn = document.getElementById('cancelFormBtn');
        userForm = document.getElementById('userForm');
        modalTitle = document.getElementById('modalTitle');
        saveUserBtn = document.getElementById('saveUserBtn');

        userIdField = document.getElementById('userIdField');
        usernameField = document.getElementById('username');
        passwordField = document.getElementById('password');
        confirmPasswordField = document.getElementById('confirmPassword');
        roleField = document.getElementById('role');

        // Assign error message elements
        usernameError = document.getElementById('usernameError');
        passwordError = document.getElementById('passwordError');
        confirmPasswordError = document.getElementById('confirmPasswordError');
        roleError = document.getElementById('roleError');

        // View Modal elements
        userViewModal = document.getElementById('userViewModal');
        closeUserViewModalBtn = document.getElementById('closeUserViewModalBtn');
        viewUserIdDisplay = document.getElementById('viewUserIdDisplay');
        viewUsernameDisplay = document.getElementById('viewUsernameDisplay');
        viewRoleDisplay = document.getElementById('viewRoleDisplay');
        viewCreatedByDisplay = document.getElementById('viewCreatedByDisplay');
        viewCreatedAtDisplay = document.getElementById('viewCreatedAtDisplay');
        viewUpdatedByDisplay = document.getElementById('viewUpdatedByDisplay');
        viewUpdatedAtDisplay = document.getElementById('viewUpdatedAtDisplay');


        // --- Event Delegation ---
        document.body.addEventListener('click', (event) => {
            // Add New User button
            if (event.target && (event.target.id === 'addUserBtn' || event.target.closest('#addUserBtn'))) {
                console.log('Add User button clicked via delegation!');
                openAddModal();
            }
            // View user button
            if (event.target && event.target.classList.contains('view-btn')) {
                console.log('View button clicked via delegation!');
                openViewModal(event.target.dataset.userId);
            } else if (event.target && event.target.closest('.view-btn')) {
                console.log('View icon clicked via delegation!');
                openViewModal(event.target.closest('.view-btn').dataset.userId);
            }
            // Edit user button
            if (event.target && event.target.classList.contains('edit-btn')) {
                console.log('Edit button clicked via delegation!');
                openEditModal(event.target.dataset.userId);
            } else if (event.target && event.target.closest('.edit-btn')) {
                console.log('Edit icon clicked via delegation!');
                openEditModal(event.target.closest('.edit-btn').dataset.userId);
            }
            // Delete user button
            if (event.target && event.target.classList.contains('delete-btn')) {
                console.log('Delete button clicked via delegation!');
                deleteUser(event.target.dataset.userId);
            } else if (event.target && event.target.closest('.delete-btn')) {
                console.log('Delete icon clicked via delegation!');
                deleteUser(event.target.closest('.delete-btn').dataset.userId);
            }
        });

        // Attach direct listeners for modal controls and search/refresh
        if (closeModalBtn) closeModalBtn.addEventListener('click', closeUserModal);
        if (cancelFormBtn) cancelFormBtn.addEventListener('click', closeUserModal);
        if (userForm) userForm.addEventListener('submit', handleUserFormSubmit);
        if (closeUserViewModalBtn) closeUserViewModalBtn.addEventListener('click', closeUserViewModal); // Close view modal
        if (searchInput) searchInput.addEventListener('input', handleSearchInput);
        if (refreshBtn) refreshBtn.addEventListener('click', () => fetchUsers(searchInput.value));

        // Initial data load
        fetchUsers();
        feather.replace(); // Initialize icons on page load
        console.log('User page initialization complete.');
    }

    // Call the initialization function once the DOM is fully loaded
    document.addEventListener('DOMContentLoaded', initUserPage);
</script>