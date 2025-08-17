<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    String role = (String) session.getAttribute("role");
    // Get the current page from the request parameter to highlight the active link
    String currentPage = request.getParameter("page");
    if (currentPage == null || currentPage.isEmpty()) {
        currentPage = "home";
    }
%>
<nav id="sidebar"
     class="bg-gray-950 text-gray-200 w-64 p-4 shadow-xl h-full fixed inset-y-0 left-0 z-50 transform -translate-x-full lg:translate-x-0 sidebar flex flex-col">

    <div class="flex flex-col h-full justify-between">
        <!-- Logo and Close Button when Responsive -->
        <div class="flex items-center justify-between px-2 pt-2 pb-6 border-b border-gray-800">
            <div class="flex items-center space-x-3">
                <i data-feather="book-open" class="w-8 h-8 text-white"></i>
                <div>
                    <h1 class="text-xl font-extrabold text-white">Pahana Edu</h1>
                    <p class="text-xs text-gray-400">Billing System</p>
                </div>
            </div>
            <button id="close-button"
                    class="lg:hidden text-gray-400 hover:text-white focus:outline-none p-2 rounded-md hover:bg-gray-800 transition-colors duration-200">
                <i data-feather="x" class="w-5 h-5"></i>
            </button>
        </div>

        <!-- Navigation Links -->
        <ul class="flex-1 space-y-2 py-6 overflow-y-auto">
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "home".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=home">
                    <i data-feather="home" class="w-5 h-5"></i>
                    <span>Dashboard</span>
                </a>
            </li>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "bills".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=bills">
                    <i data-feather="user-plus" class="w-5 h-5"></i>
                    <span>Bill</span>
                </a>
            </li>
            <!-- Admin Navigation Links -->
            <% if ("ADMIN".equals(role)) { %>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "users".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=users">
                    <i data-feather="users" class="w-5 h-5"></i>
                    <span>Users</span>
                </a>
            </li>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "customers".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=customers">
                    <i data-feather="user-plus" class="w-5 h-5"></i>
                    <span>Customers</span>
                </a>
            </li>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "items".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=items">
                    <i data-feather="package" class="w-5 h-5"></i>
                    <span>Items</span>
                </a>
            </li>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "reports".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=reports">
                    <i data-feather="bar-chart-2" class="w-5 h-5"></i>
                    <span>Reports</span>
                </a>
            </li>
            <% } else { %>
            <!-- User Navigation Links -->
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "customers".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=customers">
                    <i data-feather="users" class="w-5 h-5"></i>
                    <span>Customers</span>
                </a>
            </li>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "items".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=items">
                    <i data-feather="package" class="w-5 h-5"></i>
                    <span>Items</span>
                </a>
            </li>
            <% } %>
            <li>
                <a class="flex items-center space-x-3 p-3 rounded-lg transition duration-200 ease-in-out
                          <%= "help".equals(currentPage) ? "bg-gray-800 text-white font-semibold shadow-inner" : "hover:bg-gray-800 text-gray-300 hover:text-white" %>"
                   href="dashboard.jsp?page=help">
                    <i data-feather="help-circle" class="w-5 h-5"></i>
                    <span>Help</span>
                </a>
            </li>
        </ul>

        <!-- Logout Button at the bottom -->
        <div class="mt-auto pt-4 border-t border-gray-800">
            <form action="users" method="POST">
                <input type="hidden" name="action" value="logout">
                <button type="submit"
                        class="w-full flex items-center space-x-3 p-3 rounded-lg text-red-400 hover:bg-red-600 hover:text-white font-medium transition-colors">
                    <i data-feather="log-out" class="w-5 h-5"></i>
                    <span>Logout</span>
                </button>
            </form>
        </div>
    </div>
</nav>