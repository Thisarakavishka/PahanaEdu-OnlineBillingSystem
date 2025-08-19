<%--
  User: thisarakavishka
  Date: 2025-07-31
  Time: 03:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    // Session check and page loading logic
    if (session == null || session.getAttribute("username") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    String pageToLoad = request.getParameter("page");
    if (pageToLoad == null || pageToLoad.isEmpty()) {
        pageToLoad = "home";
    }
    if (!pageToLoad.matches("^[a-zA-Z0-9-]+$")) {
        pageToLoad = "home";
    }
    String role = (String) session.getAttribute("role");
    Integer userId = (Integer) session.getAttribute("userId");
    request.setAttribute("dynamicPagePath", "pages/" + pageToLoad + ".jsp");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pahana Edu - Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <style>
        .sidebar {
            transition: transform 0.3s ease-in-out;
        }

        .toast {
            opacity: 0;
            transform: translateX(100%);
            transition: all 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
        }

        .toast.show {
            opacity: 1;
            transform: translateX(0);
        }
    </style>

    <script>
        const CONTEXT_PATH = "<%= request.getContextPath() %>";

        function getContextPath() {
            return CONTEXT_PATH;
        }

        /**
         * ✅ FINAL FIX: This version applies colors directly as inline styles,
         * bypassing the Tailwind CDN to guarantee the colors are applied.
         */
        function showToast(message, type = 'success') {
            const container = document.getElementById('toast-container');
            if (!container) return;

            const toast = document.createElement('div');
            // Base classes for structure and text color, but NOT background color
            toast.className = 'toast flex items-center space-x-3 p-4 rounded-lg shadow-lg max-w-sm text-white';

            // Apply colors and icons directly to the element's style
            if (type === 'error') {
                toast.style.backgroundColor = '#e11d48'; // This is Tailwind's 'rose-600'
                toast.innerHTML = `<i data-feather="alert-triangle" class="w-5 h-5"></i> <span class="font-medium">\${message}</span>`;
            } else { // Default to success
                toast.style.backgroundColor = '#0d9488'; // This is Tailwind's 'teal-600'
                toast.innerHTML = `<i data-feather="check-circle" class="w-5 h-5"></i> <span class="font-medium">\${message}</span>`;
            }

            container.appendChild(toast);
            feather.replace();

            // Animate in
            setTimeout(() => toast.classList.add('show'), 10);

            // Animate out and remove
            setTimeout(() => {
                toast.classList.remove('show');
                toast.addEventListener('transitionend', () => toast.remove());
            }, 4000);
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://unpkg.com/jspdf@latest/dist/jspdf.umd.min.js"></script>
    <script src="https://unpkg.com/jspdf-autotable@latest/dist/jspdf.plugin.autotable.js"></script>
</head>
<body class="flex flex-col min-h-screen bg-gray-100 font-sans">

<div class="flex flex-1">
    <jsp:include page="sidebar.jsp"/>

    <div class="flex-1 flex flex-col lg:ml-64">
        <jsp:include page="header.jsp"/>

        <input type="hidden" id="userRoleHiddenInput" value="<%= role %>">
        <input type="hidden" id="userIdHiddenInput" value="<%= userId %>">

        <main class="flex-1 p-4 lg:p-6 overflow-auto">
            <jsp:include page="${dynamicPagePath}"/>
        </main>
    </div>
</div>

<div id="toast-container" class="fixed bottom-4 right-4 z-50 space-y-2"></div>

<script>
    // Initialize all Feather icons on the page after the content has loaded.
    feather.replace();
</script>

</body>
</html>