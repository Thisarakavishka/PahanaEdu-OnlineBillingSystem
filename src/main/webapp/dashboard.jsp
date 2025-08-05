<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    // Session check for authentication
    if (session == null || session.getAttribute("username") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }

    String pageToLoad = request.getParameter("page");
    if (pageToLoad == null || pageToLoad.isEmpty()) {
        pageToLoad = "home"; // Default page
    }

    // Basic security: Prevent directory traversal or loading arbitrary files
    if (!pageToLoad.matches("[a-zA-Z0-9-]+")) { // Allow hyphen for page names like "make-bill"
        pageToLoad = "home"; // Fallback if invalid characters are in page name
    }

    String role = (String) session.getAttribute("role");
    request.setAttribute("dynamicPagePath", "pages/" + pageToLoad + ".jsp");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pahana Edu - Dashboard</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Feather Icons CDN for modern icons -->
    <script src="https://unpkg.com/feather-icons"></script>
    <!-- Custom styles for the sidebar transition -->
    <style>
        .sidebar {
            transition: transform 0.3s ease-in-out;
        }
    </style>
</head>
<body class="flex flex-col min-h-screen bg-gray-100 font-sans">

<!-- Global function to extract context path for client-side JavaScript use -->
<!-- IMPORTANT: Defined here at the top of the body to ensure it's available globally -->
<script>
    function getContextPath() {
        const path = window.location.pathname;
        const secondSlashIndex = path.indexOf("/", 1);
        if (secondSlashIndex !== -1) {
            return path.substring(0, secondSlashIndex);
        }
        return "";
    }
</script>

<!-- Overall layout container -->
<div class="flex flex-1">
    <!-- Sidebar -->
    <jsp:include page="sidebar.jsp"/>

    <!-- Main content area -->
    <div class="flex-1 flex flex-col lg:ml-64">
        <!-- Header for mobile and desktop -->
        <jsp:include page="header.jsp"/>

        <!-- Hidden input to pass user role to JavaScript -->
        <input type="hidden" id="userRoleHiddenInput" value="<%= role %>">
        <!-- Hidden input to pass current page name to JavaScript -->
        <input type="hidden" id="currentPageHiddenInput" value="<%= pageToLoad %>">


        <!-- Main content pages -->
        <main class="flex-1 p-4 lg:p-6 overflow-auto">
            <jsp:include page="${dynamicPagePath}"/>

        </main>
    </div>
</div>

<!-- Global Feather Icons replacement (can stay here) -->
<script>
    feather.replace();
</script>
</body>
</html>