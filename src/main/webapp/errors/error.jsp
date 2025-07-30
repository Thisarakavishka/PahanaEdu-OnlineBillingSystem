<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-31
  Time: 04:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %> <%-- Corrected JSTL URI for Jakarta EE 9+ --%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pahana Edu - Error</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-100 min-h-screen flex flex-col items-center justify-center p-4">

<div class="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full relative">
    <a href="javascript:history.back()"
       class="absolute top-4 left-4 inline-flex items-center space-x-1
              text-gray-600 hover:text-gray-800 hover:bg-gray-100 px-3 py-2
              rounded-md text-sm font-medium transition duration-300">
        <i data-feather="arrow-left" class="w-4 h-4"></i>
        <span>Go Back</span>
    </a>

    <div class="text-red-500 mb-4 pt-8"> <%-- Added padding-top to account for the absolute button --%>
        <svg class="mx-auto w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
    </div>
    <h1 class="text-3xl font-bold text-gray-800 mb-3">Oops! Something went wrong.</h1>
    <p class="text-gray-600 mb-6">We apologize for the inconvenience. An unexpected error occurred on our server.</p>

    <c:if test="${pageContext.exception != null}">
        <div class="bg-red-50 border border-red-200 text-red-700 p-4 rounded-md text-left text-sm mb-6">
            <p class="font-semibold">Error Details:</p>
            <p class="font-mono text-xs break-all">
                    ${pageContext.exception.message != null ? pageContext.exception.message : 'No specific message available.'}
            </p>
        </div>
    </c:if>

    <a href="<%= request.getContextPath() %>/dashboard.jsp"
       class="inline-block bg-black text-white py-2 px-6 rounded-md hover:bg-gray-800 font-semibold transition duration-300">
        Go to Dashboard
    </a>
</div>

<script>
    feather.replace();
</script>
</body>
</html>