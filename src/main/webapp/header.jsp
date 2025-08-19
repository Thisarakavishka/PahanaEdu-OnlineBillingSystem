<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String username = (String) session.getAttribute("username");
%>
<header class="bg-white shadow-sm p-4 flex justify-between items-center">
    <button id="menu-button" class="lg:hidden text-gray-600 hover:text-gray-900">
        <i data-feather="menu" class="w-6 h-6"></i>
    </button>

    <div class="hidden md:block">
        <h2 class="text-lg font-semibold text-gray-800">Welcome back, <%= username %>!</h2>
        <p class="text-sm text-gray-500">Here's a summary of your system's activity.</p>
    </div>

    <div class="flex items-center space-x-4">
        <div class="bg-gray-800 text-white rounded-full w-10 h-10 flex items-center justify-center font-bold text-lg">
            <%-- Display the first letter of the username --%>
            <%= username != null && !username.isEmpty() ? Character.toUpperCase(username.charAt(0)) : '?' %>
        </div>
    </div>
</header>