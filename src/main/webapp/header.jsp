<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>
<%
    // Format date as "Friday, August 1, 2025"
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
    String today = sdf.format(new Date());
%>

<header class="bg-white text-gray-800 p-4 sticky top-0 z-30 flex items-center justify-between lg:justify-end border-b border-gray-200">
    <div class="lg:hidden">
        <button id="menu-button" class="text-gray-500 hover:text-gray-900 focus:outline-none">
            <i data-feather="menu" class="w-6 h-6"></i>
        </button>
    </div>
    <div class="flex flex-col lg:flex-row lg:items-center lg:gap-6 text-sm text-gray-500">
        <span><%= today %></span>
        <span>Welcome, <span class="font-semibold text-gray-700"><%= session.getAttribute("username") %></span></span>
    </div>
</header>