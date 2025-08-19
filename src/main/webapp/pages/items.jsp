<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-08-07
  Time: 10:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    String role = (String) session.getAttribute("role");
%>

<div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 gap-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">Item Management</h1>
            <p class="text-gray-600">Manage products, prices, and stock quantities.</p>
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
                    <div class="py-1" role="menu" aria-orientation="vertical">
                        <a href="#" id="exportPdfBtn" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                           role="menuitem">Export as PDF</a>
                        <a href="#" id="exportCsvBtn" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                           role="menuitem">Export as CSV</a>
                    </div>
                </div>
            </div>
            <c:if test='<%= "ADMIN".equals(role) %>'>
                <button id="addItemBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-5 rounded-md inline-flex items-center space-x-2 shadow-md">
                    <i data-feather="plus-circle" class="w-5 h-5"></i>
                    <span>Add New Item</span>
                </button>
            </c:if>
        </div>
    </div>

    <!-- Item List Card -->
    <div class="bg-white rounded-lg shadow-md border border-gray-200">
        <div class="p-4 border-b border-gray-200 flex flex-col sm:flex-row items-center gap-4">
            <div class="relative flex-grow w-full sm:w-auto">
                <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                    <i data-feather="search" class="w-4 h-4 text-gray-400"></i>
                </div>
                <input type="text" id="searchInput" placeholder="Search by item name..."
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
            <p class="text-gray-600">Loading items...</p>
        </div>

        <!-- Message Display (Success/Error) - Included from separate file -->
        <jsp:include page="../components/messages.jsp"/>

        <!-- Items Table -->
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">ID</th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Name
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Unit
                        Price
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Stock
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created
                        At
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider text-center">
                        Actions
                    </th>
                </tr>
                </thead>
                <tbody id="itemTableBody" class="bg-white divide-y divide-gray-200">
                <tr>
                    <td colspan="6" class="text-center py-8 text-gray-500">No items found.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add & Edit Item Modal -->
<div id="itemModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-md relative">
        <button id="closeModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>
        <h2 id="modalTitle" class="text-2xl font-bold text-gray-800 mb-6 text-center">Add New Item</h2>

        <form id="itemForm" class="space-y-4">
            <input type="hidden" id="itemIdField" name="id">

            <div>
                <label for="name" class="block text-sm font-semibold text-gray-700 mb-1">Item Name</label>
                <input type="text" id="name" name="name" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="nameError" class="text-red-500 text-xs mt-1 hidden">Item name is required.</p>
            </div>
            <div>
                <label for="unitPrice" class="block text-sm font-semibold text-gray-700 mb-1">Unit Price</label>
                <input type="number" id="unitPrice" name="unitPrice" step="0.01" min="0" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="unitPriceError" class="text-red-500 text-xs mt-1 hidden">Valid unit price is required.</p>
            </div>
            <div>
                <label for="stockQuantity" class="block text-sm font-semibold text-gray-700 mb-1">Stock Quantity</label>
                <input type="number" id="stockQuantity" name="stockQuantity" min="0" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="stockQuantityError" class="text-red-500 text-xs mt-1 hidden">Valid stock quantity is
                    required.</p>
            </div>

            <div class="flex justify-end space-x-3 mt-6">
                <button type="button" id="cancelFormBtn"
                        class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Cancel
                </button>
                <button type="submit" id="saveItemBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Save Item
                </button>
            </div>
        </form>
    </div>
</div>

<!-- View Item Modal -->
<div id="itemViewModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-2xl relative">
        <button id="closeItemViewModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>

        <div class="flex items-center space-x-4 mb-6 border-b pb-4">
            <div class="bg-gray-800 text-white rounded-full p-3">
                <i data-feather="package" class="w-6 h-6"></i>
            </div>
            <div>
                <h2 id="viewItemName" class="text-2xl font-bold text-gray-800">Item Name</h2>
                <p class="text-gray-500">Details and audit information</p>
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div class="bg-gray-50 p-4 rounded-lg border">
                <label class="block text-xs font-semibold text-gray-500 mb-1">ITEM ID</label>
                <p id="viewItemId" class="text-lg font-bold text-gray-800"></p>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg border">
                <label class="block text-xs font-semibold text-gray-500 mb-1">UNIT PRICE</label>
                <p id="viewItemUnitPrice" class="text-lg font-bold text-gray-800"></p>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg border">
                <label class="block text-xs font-semibold text-gray-500 mb-1">STOCK QUANTITY</label>
                <p id="viewItemStockQuantity" class="text-lg font-bold text-gray-800"></p>
            </div>
        </div>

        <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-2">Audit Information</h3>
            <div class="border rounded-lg p-4 space-y-2 text-sm text-gray-600">
                <div class="flex justify-between"><span>Created By:</span> <strong id="viewItemCreatedBy"
                                                                                   class="text-gray-800"></strong></div>
                <div class="flex justify-between"><span>Created At:</span> <strong id="viewItemCreatedAt"
                                                                                   class="text-gray-800"></strong></div>
                <hr>
                <div class="flex justify-between"><span>Last Updated By:</span> <strong id="viewItemUpdatedBy"
                                                                                        class="text-gray-800"></strong>
                </div>
                <div class="flex justify-between"><span>Last Updated At:</span> <strong id="viewItemUpdatedAt"
                                                                                        class="text-gray-800"></strong>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- Restock Item Modal -->
<div id="restockModal"
     class="fixed inset-0 bg-gray-900 bg-opacity-75 flex items-center justify-center p-4 z-50 hidden">
    <div class="bg-white p-8 rounded-lg shadow-xl w-full max-w-md relative">
        <button id="closeRestockModalBtn" class="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
            <i data-feather="x" class="w-6 h-6"></i>
        </button>
        <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">Restock Item</h2>

        <form id="restockForm" class="space-y-4">
            <input type="hidden" id="restockItemId" name="id">
            <input type="hidden" id="restockItemName" name="name">
            <input type="hidden" id="restockItemUnitPrice" name="unitPrice">
            <input type="hidden" id="restockItemCurrentStock" name="stockQuantity">

            <div>
                <label for="restockItemNameDisplay" class="block text-sm font-semibold text-gray-700 mb-1">Item
                    Name</label>
                <input type="text" id="restockItemNameDisplay" readonly
                       class="w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-md text-sm"/>
            </div>
            <div>
                <label for="quantityToAdd" class="block text-sm font-semibold text-gray-700 mb-1">Quantity to
                    Add</label>
                <input type="number" id="quantityToAdd" name="quantityToAdd" min="1" value="1" required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 text-sm"/>
                <p id="quantityToAddError" class="text-red-500 text-xs mt-1 hidden">Quantity must be a positive
                    number.</p>
            </div>

            <div class="flex justify-end space-x-3 mt-6">
                <button type="button" id="cancelRestockFormBtn"
                        class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Cancel
                </button>
                <button type="submit" id="saveRestockBtn"
                        class="bg-gray-800 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-md transition duration-200 ease-in-out">
                    Add Stock
                </button>
            </div>
        </form>
    </div>
</div>


<script>
    let currentItems = [];

    let itemTableBody, loadingIndicator, messageDisplay, messageText, searchInput, refreshBtn, itemModal, closeModalBtn,
        cancelFormBtn, itemForm, modalTitle, saveItemBtn;

    let itemIdField, nameField, unitPriceField, stockQuantityField;

    let nameError, unitPriceError, stockQuantityError;

    let itemViewModal, closeItemViewModalBtn, viewItemId, viewItemName, viewItemUnitPrice, viewItemStockQuantity,
        viewItemCreatedBy, viewItemCreatedAt, viewItemUpdatedBy, viewItemUpdatedAt;

    let restockModal, closeRestockModalBtn, cancelRestockFormBtn, restockForm, restockItemId, restockItemName,
        restockItemNameDisplay, restockItemUnitPrice, restockItemCurrentStock, quantityToAddField, quantityToAddError,
        saveRestockBtn;

    const loggedInUserRole = document.getElementById('userRoleHiddenInput').value;
    const loggedInUserId = document.getElementById('userIdHiddenInput') ? parseInt(document.getElementById('userIdHiddenInput').value) : null;
    const INITIAL_ADMIN_ID = 1;
    let searchTimeout;

    const showMessage = showToast;

    async function fetchItems(searchTerm = '') {
        console.log('Fetching items with search term:', searchTerm);
        loadingIndicator.classList.remove('hidden');
        messageDisplay.classList.add('hidden'); // Hide any previous messages

        let url = getContextPath() + '/items';
        if (searchTerm) {
            url += '?search=' + encodeURIComponent(searchTerm);
        }

        try {
            const response = await fetch(url);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch items.');
            }
            currentItems = data;
            renderItems(data);
            console.log('Items fetched and rendered successfully.');
        } catch (error) {
            console.error('Error fetching items:', error);
            showMessage(error.message || 'Failed to load items. Please try again.', 'error');
            itemTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-red-500">' + (error.message || 'Error loading items.') + '</td></tr>';
        } finally {
            loadingIndicator.classList.add('hidden');
        }
    }

    function renderItems(items) {
        itemTableBody.innerHTML = '';

        if (items.length === 0) {
            itemTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-gray-500">No items found.</td></tr>';
            return;
        }

        items.forEach(item => {
            const row = document.createElement('tr');
            let rowHtml = '';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">' + item.id + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">' + item.name + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Rs. ' + parseFloat(item.unitPrice).toFixed(2) + '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">';
            rowHtml += '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ' + (item.stockQuantity < 10 ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800') + '">' + item.stockQuantity + ' units</span>';
            rowHtml += '</td>';
            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' +
                (item.createdAt
                    ? new Date(item.createdAt).toLocaleDateString('en-GB', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric'
                    })
                    : '-') +
                '</td>';

            rowHtml += '<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-center">';

            // --- VIEW BUTTON (All roles can view) ---
            rowHtml += '<button title="View Item Details" class="text-blue-600 hover:text-blue-900 mr-3 view-btn" data-item-id="' + item.id + '">' +
                '<i data-feather="eye" class="w-4 h-4 inline-block align-middle"></i></button>';

            // --- EDIT BUTTON LOGIC (Only Admins can edit) ---
            let canEdit = false;
            if (loggedInUserRole === 'ADMIN') {
                canEdit = true; // All admins can edit any item
            }

            if (canEdit) {
                rowHtml += '<button title="Edit Item" class="text-gray-600 hover:text-gray-900 mr-3 edit-btn" data-item-id="' + item.id + '">' +
                    '<i data-feather="edit" class="w-4 h-4 inline-block align-middle"></i></button>';
            } else {
                // If not allowed to edit, show 'No Edit' or nothing, depending on preference
                // For users, they just won't see the button.
            }
            // --- END EDIT BUTTON LOGIC ---

            // --- RESTOCK BUTTON LOGIC (Only Admins can restock) ---
            let canRestock = false;
            if (loggedInUserRole === 'ADMIN') {
                canRestock = true; // All admins can restock any item
            }

            if (canRestock) {
                rowHtml += '<button title="Restock Item" class="text-purple-600 hover:text-purple-900 mr-3 restock-btn" data-item-id="' + item.id + '">' +
                    '<i data-feather="truck" class="w-4 h-4 inline-block align-middle"></i></button>';
            }
            // --- END RESTOCK BUTTON LOGIC ---

            // --- DELETE BUTTON LOGIC (Only Initial Admin can delete) ---
            let canDelete = false;
            if (loggedInUserRole === 'ADMIN' && loggedInUserId === INITIAL_ADMIN_ID) {
                canDelete = true; // Only initial admin can delete items
            }

            if (canDelete) {
                rowHtml += '<button title="Delete Item" class="text-red-600 hover:text-red-900 delete-btn" data-item-id="' + item.id + '">' +
                    '<i data-feather="trash-2" class="w-4 h-4 inline-block align-middle"></i></button>';
            } else if (loggedInUserRole === 'ADMIN') { // For other admins, explicitly show 'No Delete'
                // rowHtml += '<span class="text-gray-400 ml-3">No Delete</span>'; // Removed this to just hide the button
            }
            // For 'USER' role, no delete button or text is added by default.
            // --- END DELETE BUTTON LOGIC ---

            rowHtml += '</td>';
            row.innerHTML = rowHtml;
            itemTableBody.appendChild(row);
        });

        feather.replace();
    }

    function clearValidationErrors() {
        nameError.classList.add('hidden');
        unitPriceError.classList.add('hidden');
        stockQuantityError.classList.add('hidden');
        if (quantityToAddError) quantityToAddError.classList.add('hidden');
    }

    function validateForm() {
        clearValidationErrors();
        let isValid = true;

        if (nameField.value.trim() === '') {
            nameError.textContent = 'Item name is required.';
            nameError.classList.remove('hidden');
            isValid = false;
        }

        const unitPrice = parseFloat(unitPriceField.value);
        if (isNaN(unitPrice) || unitPrice < 0) {
            unitPriceError.textContent = 'Unit price must be a non-negative number.';
            unitPriceError.classList.remove('hidden');
            isValid = false;
        }

        const stockQuantity = parseInt(stockQuantityField.value);
        if (isNaN(stockQuantity) || stockQuantity < 0) {
            stockQuantityError.textContent = 'Stock quantity must be a non-negative integer.';
            stockQuantityError.classList.remove('hidden');
            isValid = false;
        }

        return isValid;
    }

    function validateRestockForm() {
        if (quantityToAddError) quantityToAddError.classList.add('hidden');
        let isValid = true;
        const quantity = parseInt(quantityToAddField.value);
        if (isNaN(quantity) || quantity <= 0) {
            quantityToAddError.textContent = 'Quantity to add must be a positive number.';
            quantityToAddError.classList.remove('hidden');
            isValid = false;
        }
        return isValid;
    }

    function openAddModal() {
        console.log('Attempting to open Add Item modal.');
        modalTitle.textContent = 'Add New Item';
        itemForm.reset();
        itemIdField.value = '';
        clearValidationErrors();
        itemModal.classList.remove('hidden');
        console.log('Add Item modal should be visible.');
    }

    function closeItemModal() {
        itemModal.classList.add('hidden');
        clearValidationErrors();
        console.log('Item modal closed.');
    }

    function closeItemViewModal() {
        itemViewModal.classList.add('hidden');
        console.log('Item View modal closed.');
    }

    function closeRestockModal() {
        restockModal.classList.add('hidden');
        clearValidationErrors();
        console.log('Restock modal closed.');
    }

    async function openViewModal(itemId) {
        console.log('Attempting to open View Item modal for ID:', itemId);
        try {
            const response = await fetch(getContextPath() + '/items?id=' + encodeURIComponent(itemId));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch item details for viewing.');
            }

            viewItemId.textContent = data.id;
            viewItemName.textContent = data.name;
            viewItemUnitPrice.textContent = 'Rs. ' + parseFloat(data.unitPrice).toFixed(2);
            viewItemStockQuantity.textContent = data.stockQuantity + ' units';
            viewItemCreatedBy.textContent = data.createdBy || '-';
            viewItemCreatedAt.textContent = data.createdAt
                ? new Date(data.createdAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.createdAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';
            viewItemUpdatedBy.textContent = data.updatedBy || '-';
            viewItemUpdatedAt.textContent = data.createdAt
                ? new Date(data.updatedAt).toLocaleDateString('en-GB', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            }) + ' - ' + new Date(data.updatedAt).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';

            itemViewModal.classList.remove('hidden');
            console.log('View Item modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening view modal:', error);
            showMessage(error.message || 'Failed to load item details for viewing.', 'error');
        }
    }

    async function openEditModal(itemId) {
        console.log('Attempting to open Edit Item modal for ID:', itemId);
        try {
            const response = await fetch(getContextPath() + '/items?id=' + encodeURIComponent(itemId));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch item details for editing.');
            }

            modalTitle.textContent = 'Edit Item';
            itemIdField.value = data.id;
            nameField.value = data.name;
            unitPriceField.value = parseFloat(data.unitPrice).toFixed(2);
            stockQuantityField.value = data.stockQuantity;

            clearValidationErrors();
            itemModal.classList.remove('hidden');
            console.log('Edit Item modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening edit modal:', error);
            showMessage(error.message || 'Failed to load item details for editing.', 'error');
        }
    }

    async function openRestockModal(itemId) {
        console.log('Attempting to open Restock modal for ID:', itemId);
        try {
            const response = await fetch(getContextPath() + '/items?id=' + encodeURIComponent(itemId));
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to fetch item details for restock.');
            }

            restockItemId.value = data.id;
            restockItemName.value = data.name;
            restockItemNameDisplay.value = data.name;
            restockItemUnitPrice.value = data.unitPrice;
            restockItemCurrentStock.value = data.stockQuantity;
            quantityToAddField.value = 1;
            clearValidationErrors();
            restockModal.classList.remove('hidden');
            console.log('Restock modal should be visible with data:', data);
        } catch (error) {
            console.error('Error opening restock modal:', error);
            showMessage(error.message || 'Failed to load item details for restock.', 'error');
        }
    }

    async function handleItemFormSubmit(e) {
        e.preventDefault();
        console.log('Item form submitted.');

        if (!validateForm()) {
            console.log('Frontend validation failed.');
            return;
        }

        const isEdit = itemIdField.value !== '';
        const method = isEdit ? 'PUT' : 'POST';
        const url = getContextPath() + '/items';

        const formData = new URLSearchParams();
        if (isEdit) {
            formData.append('action', 'update');
            formData.append('id', itemIdField.value);
        } else {
            formData.append('action', 'add');
        }
        formData.append('name', nameField.value);
        formData.append('unitPrice', unitPriceField.value);
        formData.append('stockQuantity', stockQuantityField.value);

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
                throw new Error(data.message || `Failed to ${isEdit ? 'update' : 'add'} item.`);
            }

            showMessage(data.message, 'success');
            closeItemModal();
            fetchItems(searchInput.value);
            console.log('Item ' + (isEdit ? 'updated' : 'added') + ' successfully.');
        } catch (error) {
            console.error('Error ' + (isEdit ? 'updating' : 'adding') + ' item:', error);
            showMessage(error.message || 'Failed to ' + (isEdit ? 'update' : 'add') + ' item. Please check your inputs.', 'error');
        }
    }

    async function handleRestockSubmit(e) {
        e.preventDefault();
        console.log('Restock form submitted.');

        if (!validateRestockForm()) {
            console.log('Restock frontend validation failed.');
            return;
        }

        const url = getContextPath() + '/items';
        const quantityToAdd = parseInt(quantityToAddField.value);

        const formData = new URLSearchParams();
        formData.append('action', 'restock');
        formData.append('id', restockItemId.value);
        formData.append('quantityToAdd', quantityToAddField.value);

        try {
            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to restock item.');
            }

            showMessage(data.message, 'success');
            closeRestockModal();
            fetchItems(searchInput.value);
            console.log('Item restocked successfully.');
        } catch (error) {
            console.error('Error restock item:', error);
            showMessage(error.message || 'Failed to restock item. Please check your inputs.', 'error');
        }
    }

    async function deleteItem(itemId) {
        console.log('Attempting to delete item ID:', itemId);
        if (!confirm('Are you sure you want to delete this item? This action cannot be undone.')) {
            console.log('Item deletion cancelled.');
            return;
        }

        try {
            const response = await fetch(getContextPath() + '/items?id=' + encodeURIComponent(itemId), {
                method: 'DELETE'
            });
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to delete item.');
            }

            showMessage(data.message, 'success');
            fetchItems(searchInput.value);
            console.log('Item deleted successfully.');
        } catch (error) {
            console.error('Error deleting item:', error);
            showMessage(error.message || 'Failed to delete item. Please try again.', 'error');
        }
    }

    function downloadItemsAsPDF() {
        if (currentItems.length === 0) {
            showMessage("No data to export.", "error");
            return;
        }
        const {jsPDF} = window.jspdf;
        const doc = new jsPDF();

        doc.setFont("helvetica", "bold");
        doc.setFontSize(18);
        doc.text("Item Inventory Report - Pahana Edu", 14, 22);
        doc.setFontSize(11);
        doc.setTextColor(100);
        doc.text(`Report generated on: \${new Date().toLocaleDateString()}`, 14, 30);

        const tableColumn = ["ID", "Name", "Unit Price (Rs.)", "Stock Quantity", "Created At"];
        const tableRows = [];

        currentItems.forEach(item => {
            const itemData = [
                item.id,
                item.name,
                parseFloat(item.unitPrice).toFixed(2),
                item.stockQuantity,
                new Date(item.createdAt).toLocaleDateString('en-GB')
            ];
            tableRows.push(itemData);
        });

        doc.autoTable({
            startY: 38,
            head: [tableColumn],
            body: tableRows,
            theme: 'striped',
            headStyles: {fillColor: [30, 30, 30]}
        });

        doc.save('PahanaEdu_Items_Report.pdf');
    }

    function downloadItemsAsCSV() {
        if (currentItems.length === 0) {
            showMessage("No data to export.", "error");
            return;
        }
        const headers = "ItemID,Name,UnitPrice,StockQuantity,CreatedAt";
        const csvRows = [headers];

        currentItems.forEach(item => {
            const row = [
                item.id,
                `"\${item.name}"`,
                item.unitPrice,
                item.stockQuantity,
                `"\${new Date(item.createdAt).toISOString()}"`
            ];
            csvRows.push(row.join(','));
        });

        const csvString = csvRows.join('\\n');
        const blob = new Blob([csvString], {type: 'text/csv;charset=utf-8;'});
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", "PahanaEdu_Items_Report.csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    function handleSearchInput() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchItems(searchInput.value);
        }, 900);
    }

    function initItemPage() {
        console.log('initItemPage() called. Assigning DOM elements and attaching listeners.');

        itemTableBody = document.getElementById('itemTableBody');
        loadingIndicator = document.getElementById('loadingIndicator');
        messageDisplay = document.getElementById('messageDisplay');
        messageText = document.getElementById('messageText');
        searchInput = document.getElementById('searchInput');
        refreshBtn = document.getElementById('refreshBtn');
        itemModal = document.getElementById('itemModal');
        closeModalBtn = document.getElementById('closeModalBtn');
        cancelFormBtn = document.getElementById('cancelFormBtn');
        itemForm = document.getElementById('itemForm');
        modalTitle = document.getElementById('modalTitle');
        saveItemBtn = document.getElementById('saveItemBtn');

        itemIdField = document.getElementById('itemIdField');
        nameField = document.getElementById('name');
        unitPriceField = document.getElementById('unitPrice');
        stockQuantityField = document.getElementById('stockQuantity');

        nameError = document.getElementById('nameError');
        unitPriceError = document.getElementById('unitPriceError');
        stockQuantityError = document.getElementById('stockQuantityError');

        itemViewModal = document.getElementById('itemViewModal');
        closeItemViewModalBtn = document.getElementById('closeItemViewModalBtn');
        viewItemId = document.getElementById('viewItemId');
        viewItemName = document.getElementById('viewItemName');
        viewItemUnitPrice = document.getElementById('viewItemUnitPrice');
        viewItemStockQuantity = document.getElementById('viewItemStockQuantity');
        viewItemCreatedBy = document.getElementById('viewItemCreatedBy');
        viewItemCreatedAt = document.getElementById('viewItemCreatedAt');
        viewItemUpdatedBy = document.getElementById('viewItemUpdatedBy');
        viewItemUpdatedAt = document.getElementById('viewItemUpdatedAt');

        restockModal = document.getElementById('restockModal');
        closeRestockModalBtn = document.getElementById('closeRestockModalBtn');
        cancelRestockFormBtn = document.getElementById('cancelRestockFormBtn');
        restockForm = document.getElementById('restockForm');
        restockItemId = document.getElementById('restockItemId');
        restockItemName = document.getElementById('restockItemName'); // Hidden field
        restockItemNameDisplay = document.getElementById('restockItemNameDisplay'); // Display field
        restockItemUnitPrice = document.getElementById('restockItemUnitPrice'); // Hidden field
        restockItemCurrentStock = document.getElementById('restockItemCurrentStock'); // Hidden field
        quantityToAddField = document.getElementById('quantityToAdd');
        quantityToAddError = document.getElementById('quantityToAddError');
        saveRestockBtn = document.getElementById('saveRestockBtn');


        document.body.addEventListener('click', (event) => {
            if (event.target && (event.target.id === 'addItemBtn' || event.target.closest('#addItemBtn'))) {
                console.log('Add Item button clicked via delegation!');
                openAddModal();
            }

            if (event.target && event.target.classList.contains('view-btn')) {
                console.log('View button clicked via delegation!');
                openViewModal(event.target.dataset.itemId);
            } else if (event.target && event.target.closest('.view-btn')) {
                console.log('View icon clicked via delegation!');
                openViewModal(event.target.closest('.view-btn').dataset.itemId);
            }

            if (event.target && event.target.classList.contains('edit-btn')) {
                console.log('Edit button clicked via delegation!');
                openEditModal(event.target.dataset.itemId);
            } else if (event.target && event.target.closest('.edit-btn')) {
                console.log('Edit icon clicked via delegation!');
                openEditModal(event.target.closest('.edit-btn').dataset.itemId);
            }

            if (event.target && event.target.classList.contains('restock-btn')) {
                console.log('Restock button clicked via delegation!');
                openRestockModal(event.target.dataset.itemId);
            } else if (event.target && event.target.closest('.restock-btn')) {
                console.log('Restock icon clicked via delegation!');
                openRestockModal(event.target.closest('.restock-btn').dataset.itemId);
            }

            if (event.target && event.target.classList.contains('delete-btn')) {
                console.log('Delete button clicked via delegation!');
                deleteItem(event.target.dataset.itemId);
            } else if (event.target && event.target.closest('.delete-btn')) {
                console.log('Delete icon clicked via delegation!');
                deleteItem(event.target.closest('.delete-btn').dataset.itemId);
            }
        });

        if (closeModalBtn) closeModalBtn.addEventListener('click', closeItemModal);
        if (cancelFormBtn) cancelFormBtn.addEventListener('click', closeItemModal);
        if (itemForm) itemForm.addEventListener('submit', handleItemFormSubmit);
        if (closeItemViewModalBtn) closeItemViewModalBtn.addEventListener('click', closeItemViewModal);
        if (closeRestockModalBtn) closeRestockModalBtn.addEventListener('click', closeRestockModal);
        if (cancelRestockFormBtn) cancelRestockFormBtn.addEventListener('click', closeRestockModal);
        if (restockForm) restockForm.addEventListener('submit', handleRestockSubmit);
        if (searchInput) searchInput.addEventListener('input', handleSearchInput);
        if (refreshBtn) refreshBtn.addEventListener('click', () => fetchItems(searchInput.value));

        fetchItems();
        feather.replace();
        console.log('Item page initialization complete.');
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
        downloadItemsAsPDF();
        exportMenu.classList.add('hidden');
    });

    document.getElementById('exportCsvBtn').addEventListener('click', (e) => {
        e.preventDefault();
        downloadItemsAsCSV();
        exportMenu.classList.add('hidden');
    });

    document.addEventListener('DOMContentLoaded', initItemPage);
</script>