<%--
  Created by IntelliJ IDEA.
  User: thisarakavishka
  Date: 2025-07-29
  Time: 20:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Pahana Edu - Login</title>

  <!-- Tailwind CSS CDN -->
  <script src="https://cdn.tailwindcss.com"></script>

  <!-- Feather Icons CDN -->
  <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">

<!-- Centered Container -->
<div class="w-full max-w-md px-4">
  <!-- Header -->
  <div class="text-center mb-6">
    <div class="w-16 h-16 bg-black text-white rounded-full flex items-center justify-center mx-auto mb-3">
      <i data-feather="book-open" class="w-7 h-7"></i>
    </div>
    <h1 class="text-2xl font-bold">Pahana Edu</h1>
    <p class="text-gray-500 text-sm">Online Billing System</p>
  </div>

  <!-- Login Card -->
  <div class="bg-white shadow-lg rounded-lg p-8">
    <!-- Form Header -->
    <div class="text-center mb-6">
      <h2 class="text-2xl font-bold">Sign In</h2>
      <p class="text-gray-500 text-sm mt-3">Enter your credentials to access the billing system</p>
    </div>

    <!-- Login Form -->
    <form action="LoginServlet" method="post" class="space-y-5">
      <!-- Username -->
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 mb-1">Username</label>
        <div class="relative">
                    <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                        <i data-feather="user" class="w-4 h-4"></i>
                    </span>
          <input type="text" id="username" name="username" placeholder="Enter your username" required
                 class="w-full pl-10 pr-3 py-2 border border-gray-300 text-sm rounded-md focus:outline-none focus:ring-2 focus:ring-black"/>
        </div>
      </div>

      <!-- Password -->
      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
        <div class="relative">
                    <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                        <i data-feather="lock" class="w-4 h-4"></i>
                    </span>
          <input type="password" id="password" name="password" placeholder="Enter your password" required
                 class="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-black"/>
        </div>
      </div>

      <!-- Submit Button -->
      <div class="pt-1">
        <button type="submit"
                class="w-full bg-black text-white py-2 rounded-md hover:bg-gray-800 font-semibold">
          Sign In
        </button>
      </div>
    </form>

    <!-- Footer -->
    <footer class="mt-6 text-center text-xs text-gray-400">
      © 2025 Pahana Edu. All rights reserved.
    </footer>
  </div>
</div>

<!-- Activate Feather Icons -->
<script>
  feather.replace()
</script>

</body>
</html>

