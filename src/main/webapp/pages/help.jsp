<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-08-09
  Time: 18:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%
    String loggedInUserRole = (String) session.getAttribute("role");
    Integer loggedInUserId = (Integer) session.getAttribute("userId");
    final int INITIAL_ADMIN_ID = 1;
%>

<div class="space-y-8 p-4 lg:p-6 bg-gray-100 min-h-full rounded-lg shadow-inner">

    <!-- Page Header -->
    <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800 mb-2">Help & Support Center</h1>
        <p class="text-gray-600">Find answers to your questions and learn how to use the system effectively.</p>
    </div>

    <%-- Introduction based on Role --%>
    <div class="bg-white p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-3">Welcome to your Help Guide!</h2>
        <c:choose>
            <c:when test='<%= "ADMIN".equals(loggedInUserRole) && loggedInUserId != null && loggedInUserId == INITIAL_ADMIN_ID %>'>
                <p class="text-gray-700">As the <span class="font-bold text-purple-700">Initial System Administrator (ID: <%= INITIAL_ADMIN_ID %>)</span>, you have full control and access to all features. This guide will help you manage the entire system, including other users and all business operations. Your role is critical for system integrity.</p>
            </c:when>
            <c:when test='<%= "ADMIN".equals(loggedInUserRole) %>'>
                <p class="text-gray-700">As an <span class="font-bold text-gray-800">Administrator</span>, you have extensive privileges to manage users, items, and view comprehensive reports. This guide will help you navigate your responsibilities efficiently.</p>
            </c:when>
            <c:when test='<%= "USER".equals(loggedInUserRole) %>'>
                <p class="text-gray-700">As a standard <span class="font-bold text-blue-700">User</span>, this guide will assist you with your day-to-day tasks such as creating bills, managing your assigned customers, and viewing item details.</p>
            </c:when>
            <c:otherwise>
                <p class="text-gray-700">Welcome! This help guide provides an overview of the Pahana Edu Billing System. Please log in to access features tailored to your role.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <hr class="border-gray-300">

    <!-- Quick Start Guides Section -->
    <div class="bg-white p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center space-x-2">
            <i data-feather="book-open" class="w-6 h-6 text-gray-600"></i>
            <span>Quick Start Guides</span>
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <!-- Guide: How to Log In (All Roles) -->
            <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="log-in" class="w-4 h-4"></i> How to Log In</h3>
                <p class="text-sm text-gray-600">Enter your username and password on the login page to access your dashboard.</p>
            </div>

            <c:if test='<%= "ADMIN".equals(loggedInUserRole) %>'>
                <!-- Guide: Add New User (Admin Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="user-plus" class="w-4 h-4"></i> Add New User</h3>
                    <p class="text-sm text-gray-600">Navigate to 'User Management', click 'Add New User', fill in details, and set their role (Admin/User).</p>
                </div>
                <!-- Guide: Manage Items (Admin Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="package" class="w-4 h-4"></i> Manage Items</h3>
                    <p class="text-sm text-gray-600">Go to 'Item Management' to add new items, update prices, or restock quantities.</p>
                </div>
                <!-- Guide: Manage Customers (Admin Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="users" class="w-4 h-4"></i> Manage Customers</h3>
                    <p class="text-sm text-gray-600">In 'Customer Management', you can add, edit, or delete customer accounts.</p>
                </div>
                <!-- Guide: View Reports (Admin Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="bar-chart-2" class="w-4 h-4"></i> View Reports</h3>
                    <p class="text-sm text-gray-600">Access 'Reports' from the sidebar to view sales data, stock levels, and user activity.</p>
                </div>
            </c:if>

            <c:if test='<%= "USER".equals(loggedInUserRole) %>'>
                <!-- Guide: Create New Bill (User Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="receipt" class="w-4 h-4"></i> Create New Bill</h3>
                    <p class="text-sm text-gray-600">Start a new transaction, select items, and process payments for customers.</p>
                </div>
                <!-- Guide: View Items (User Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="package" class="w-4 h-4"></i> View Items</h3>
                    <p class="text-sm text-gray-600">Browse available items and check their current stock quantities.</p>
                </div>
                <!-- Guide: View My Customers (User Only) -->
                <div class="p-4 border rounded-lg bg-gray-50 hover:shadow-md transition">
                    <h3 class="font-semibold text-gray-800 mb-2 flex items-center space-x-2"><i data-feather="users" class="w-4 h-4"></i> View My Customers</h3>
                    <p class="text-sm text-gray-600">Access information for customers assigned to you.</p>
                </div>
            </c:if>
        </div>
    </div>

    <hr class="border-gray-300">

    <!-- System Features Section -->
    <div class="bg-white p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center space-x-2">
            <i data-feather="info" class="w-6 h-6 text-gray-600"></i>
            <span>Key System Features</span>
        </h2>
        <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li><span class="font-medium">Secure User Authentication:</span> Role-based access for different user types.</li>
            <li><span class="font-medium">Comprehensive Item Management:</span> Track products, prices, and stock.</li>
            <li><span class="font-medium">Efficient Customer Management:</span> Maintain customer accounts and details.</li>
            <li><span class="font-medium">Real-time Billing:</span> Streamlined process for creating and managing invoices.</li>
            <li><span class="font-medium">Audit Trails:</span> Track who created, updated, or deleted records.</li>
            <li><span class="font-medium">Responsive Design:</span> Optimized for various devices (desktop, tablet, mobile).</li>
        </ul>
    </div>

    <hr class="border-gray-300">

    <!-- Contact Support Section -->
    <div class="bg-white p-6 rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center space-x-2">
            <i data-feather="life-buoy" class="w-6 h-6 text-gray-600"></i>
            <span>Need More Help?</span>
        </h2>
        <p class="text-gray-700 mb-4">If you have further questions or encounter any issues, please don't hesitate to contact our support team:</p>
        <div class="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-6">
            <div class="flex items-center space-x-2 text-gray-700">
                <i data-feather="phone" class="w-5 h-5 text-gray-600"></i>
                <span>Phone: +94 11 234 5678</span>
            </div>
            <div class="flex items-center space-x-2 text-gray-700">
                <i data-feather="mail" class="w-5 h-5 text-gray-600"></i>
                <span>Email: support@pahanaedu.com</span>
            </div>
        </div>
    </div>

</div>

<script>
    // This script replaces feather icons after the content is loaded
    document.addEventListener('DOMContentLoaded', () => {
        feather.replace();
    });
</script>