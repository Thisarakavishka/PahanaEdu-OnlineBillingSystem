<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 04:01
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<div class="space-y-6">
    <div class="bg-white rounded-lg shadow-md border border-gray-200 p-6">
        <h1 class="text-2xl font-bold text-gray-800 mb-2">
            Welcome to Pahana Edu Billing System
        </h1>
        <p class="text-gray-600">
            <c:choose>
                <c:when test='<%= "admin".equals(session.getAttribute("role")) %>'>
                    Manage your bookshop operations, users, and view comprehensive reports.
                </c:when>
                <c:otherwise>
                    Create bills, manage customers, and process transactions efficiently.
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <h2 class="text-xl font-semibold text-gray-800 mt-6 mb-4">Statistics</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Total Users</p>
                    <p class="text-3xl font-bold text-gray-800 mt-2">24</p>
                </div>
                <div class="p-3 rounded-full bg-blue-100 text-blue-500">
                    <i data-feather="users" class="w-6 h-6"></i>
                </div>
            </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Total Items</p>
                    <p class="text-3xl font-bold text-gray-800 mt-2">156</p>
                </div>
                <div class="p-3 rounded-full bg-green-100 text-green-500">
                    <i data-feather="package" class="w-6 h-6"></i>
                </div>
            </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Today's Bills</p>
                    <p class="text-3xl font-bold text-gray-800 mt-2">12</p>
                </div>
                <div class="p-3 rounded-full bg-yellow-100 text-yellow-500">
                    <i data-feather="receipt" class="w-6 h-6"></i>
                </div>
            </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm font-medium text-gray-500">Revenue Today</p>
                    <p class="text-3xl font-bold text-gray-800 mt-2">₹2,450</p>
                </div>
                <div class="p-3 rounded-full bg-red-100 text-red-500">
                    <i data-feather="dollar-sign" class="w-6 h-6"></i>
                </div>
            </div>
        </div>
    </div>

    <h2 class="text-xl font-semibold text-gray-800 mt-6 mb-4">Quick Actions</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <a href="dashboard.jsp?page=customers" class="block">
            <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition duration-200 ease-in-out cursor-pointer">
                <h3 class="text-lg font-semibold text-gray-800 mb-1">Add Customer</h3>
                <p class="text-sm text-gray-600">Register a new customer account.</p>
            </div>
        </a>

        <a href="dashboard.jsp?page=items" class="block">
            <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition duration-200 ease-in-out cursor-pointer">
                <h3 class="text-lg font-semibold text-gray-800 mb-1">Add New Item</h3>
                <p class="text-sm text-gray-600">Add books and products to inventory.</p>
            </div>
        </a>
    </div>
</div>