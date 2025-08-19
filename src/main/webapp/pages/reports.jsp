<%--
  User: thisarakavishka
  Date: 2025-08-19
  Time: 23:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="space-y-6">
    <div class="flex items-center justify-between mb-6">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">Financial Reports</h1>
            <p class="text-gray-600">Generate and download sales reports for a specific period.</p>
        </div>
    </div>

    <div class="bg-white p-6 rounded-lg shadow-md border">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <div>
                <label for="startDate" class="block text-sm font-semibold text-gray-700 mb-1">Start Date</label>
                <input type="date" id="startDate" name="startDate"
                       class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500">
            </div>
            <div>
                <label for="endDate" class="block text-sm font-semibold text-gray-700 mb-1">End Date</label>
                <input type="date" id="endDate" name="endDate"
                       class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500">
            </div>
            <button id="generateReportBtn"
                    class="w-full md:w-auto bg-gray-800 text-white font-semibold py-2 px-5 rounded-md flex items-center justify-center space-x-2 hover:bg-gray-700">
                <i data-feather="bar-chart-2" class="w-5 h-5"></i>
                <span>Generate Report</span>
            </button>
        </div>
    </div>

    <div id="report-display" class="hidden space-y-8">
        <div class="bg-white p-6 rounded-lg shadow-md border">
            <div class="flex justify-between items-center mb-4 border-b pb-2">
                <h3 class="text-xl font-semibold text-gray-800">Report Summary</h3>
                <button id="downloadPdfBtn"
                        class="bg-red-600 hover:bg-red-700 text-white font-semibold py-2 px-4 rounded-md inline-flex items-center space-x-2">
                    <i data-feather="download" class="w-4 h-4"></i>
                    <span>Download PDF</span>
                </button>
            </div>

            <div id="report-summary-content" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 text-lg">
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div class="lg:col-span-2 bg-white p-6 rounded-lg shadow-md border">
                <h3 class="text-xl font-semibold text-gray-800 mb-4">Top Selling Products (by Revenue)</h3>
                <div class="h-80">
                    <canvas id="topProductsChart"></canvas>
                </div>
            </div>
            <div class="lg:col-span-1 bg-white p-6 rounded-lg shadow-md border">
                <h3 class="text-xl font-semibold text-gray-800 mb-4">Top Customer</h3>
                <div id="top-customer-content" class="flex flex-col items-center justify-center h-full">
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const generateReportBtn = document.getElementById('generateReportBtn');
        const downloadPdfBtn = document.getElementById('downloadPdfBtn');
        const reportDisplay = document.getElementById('report-display');
        const reportContent = document.getElementById('report-summary-content');
        let currentReportData = null;
        let topProductsChart = null;

        /**
         * Generates and downloads a professional, multi-section PDF of the current report.
         */
        function downloadReportAsPDF() {
            if (!currentReportData) {
                showToast("No report data to download.", "error");
                return;
            }

            const {jsPDF} = window.jspdf;
            const doc = new jsPDF();
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            // --- PDF Header ---
            doc.setFont("helvetica", "bold");
            doc.setFontSize(20);
            doc.text("Pahana Edu - Financial Report", 105, 22, {align: "center"});
            doc.setFontSize(12);
            doc.setFont("helvetica", "normal");
            doc.text(`For the period: ${startDate} to ${endDate}`, 105, 30, {align: "center"});

            // --- Summary Section ---
            doc.autoTable({
                startY: 40,
                head: [['Key Metric', 'Value']],
                body: [
                    // ✅ FIX: Escaped all dollar signs in this array with a backslash (\)
                    [`Total Revenue`, `Rs. \${parseFloat(currentReportData.totalRevenue).toFixed(2)}`],
                    [`Total Bills Generated`, currentReportData.numberOfBills],
                    [`Total Items Sold`, currentReportData.totalItemsSold],
                    [`Average Bill Value`, `Rs. \${parseFloat(currentReportData.averageBillValue).toFixed(2)}`]
                ],
                theme: 'striped',
                headStyles: {fillColor: [30, 30, 30]}
            });

            // --- Top Selling Items Table ---
            if (currentReportData.topSellingItems && currentReportData.topSellingItems.length > 0) {
                doc.autoTable({
                    startY: doc.lastAutoTable.finalY + 10,
                    head: [['Top Selling Product', 'Quantity Sold', 'Total Revenue']],
                    body: currentReportData.topSellingItems.map(item => [
                        item.name,
                        item.quantity,
                        // ✅ FIX: Escaped dollar sign
                        `Rs. \${parseFloat(item.revenue).toFixed(2)}`
                    ]),
                    theme: 'grid',
                    headStyles: {fillColor: [30, 30, 30]}
                });
            }

            doc.save(`PahanaEdu_Report_${startDate}_to_${endDate}.pdf`);
        }

        generateReportBtn.addEventListener('click', async () => {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            if (!startDate || !endDate) {
                showToast('Please select both a start and end date.', 'error');
                return;
            }

            try {
                const url = `\${getContextPath()}/reports-data?startDate=\${startDate}&endDate=\${endDate}`;
                const response = await fetch(url);
                const data = await response.json();
                if (!response.ok) throw new Error(data.message || 'Failed to generate report.');

                currentReportData = data;

                // Populate Summary Cards
                reportContent.innerHTML = `
                    <div class="bg-gray-50 p-4 rounded-lg border"><p class="text-sm text-gray-500">Total Revenue</p><p class="font-bold text-2xl text-green-600">Rs. \${parseFloat(data.totalRevenue).toFixed(2)}</p></div>
                    <div class="bg-gray-50 p-4 rounded-lg border"><p class="text-sm text-gray-500">Number of Bills</p><p class="font-bold text-2xl text-blue-600">\${data.numberOfBills}</p></div>
                    <div class="bg-gray-50 p-4 rounded-lg border"><p class="text-sm text-gray-500">Items Sold</p><p class="font-bold text-2xl text-yellow-600">\${data.totalItemsSold}</p></div>
                    <div class="bg-gray-50 p-4 rounded-lg border"><p class="text-sm text-gray-500">Average Bill Value</p><p class="font-bold text-2xl text-indigo-600">Rs. \${parseFloat(data.averageBillValue).toFixed(2)}</p></div>
                `;

                // Populate Top Customer
                const topCustomerContent = document.getElementById('top-customer-content');
                if (data.topCustomer && data.topCustomer.name) {
                    topCustomerContent.innerHTML = `
                        <div class="bg-green-100 text-green-800 rounded-full p-4 mb-4"><i data-feather="award" class="w-10 h-10"></i></div>
                        <p class="font-bold text-xl text-gray-800">\${data.topCustomer.name}</p>
                        <p class="text-sm text-gray-500">Total Spent: Rs. \${parseFloat(data.topCustomer.totalSpent).toFixed(2)}</p>
                    `;
                } else {
                    topCustomerContent.innerHTML = `<p class="text-gray-500">No customer data for this period.</p>`;
                }

                // Render Pie Chart
                renderTopProductsChart(data.topSellingItems || []);

                reportDisplay.classList.remove('hidden');
                feather.replace();

            } catch (error) {
                showToast(error.message, 'error');
                reportDisplay.classList.add('hidden');
                currentReportData = null;
            }
        });

        function renderTopProductsChart(items) {
            const ctx = document.getElementById('topProductsChart').getContext('2d');
            if (topProductsChart) topProductsChart.destroy();

            if (!items || items.length === 0) {
                return; // Do nothing if there's no data
            }

            topProductsChart = new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: items.map(item => item.name),
                    datasets: [{
                        label: 'Revenue',
                        data: items.map(item => item.revenue),
                        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ec4899'],
                        hoverOffset: 4
                    }]
                },
                options: {responsive: true, maintainAspectRatio: false}
            });
        }

        downloadPdfBtn.addEventListener('click', downloadReportAsPDF);
    });
</script>