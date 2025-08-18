package pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl.BillDetailDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillDetailEntity;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thisara Kavishka
 * @date 2025-08-18
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BillDetailDAOImplTest {

    private BillDetailDAOImpl billDetailDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        billDetailDAO = new BillDetailDAOImpl();
        connection = DBUtil.getConnection();

        // Clean and prepare all related tables for a fresh test
        Statement stmt = connection.createStatement();
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
        stmt.execute("TRUNCATE TABLE bill_details;");
        stmt.execute("TRUNCATE TABLE bills;");
        stmt.execute("TRUNCATE TABLE items;");
        stmt.execute("TRUNCATE TABLE customers;");
        stmt.execute("TRUNCATE TABLE users;");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

        // Add prerequisite data needed for foreign keys
        stmt.execute("INSERT INTO users (id, username, password, salt, role) VALUES (1, 'test_admin', 'p', 's', 'ADMIN');");
        stmt.execute("INSERT INTO customers (id, account_number, name, phone, created_by) VALUES (1, 'CUST001', 'Test Customer', '0771112222', 1);");
        stmt.execute("INSERT INTO items (id, name, unit_price, stock_quantity, created_by) VALUES (101, 'Test Pen', 50.00, 100, 1);");
        stmt.execute("INSERT INTO items (id, name, unit_price, stock_quantity, created_by) VALUES (102, 'Test Book', 250.00, 50, 1);");
        stmt.execute("INSERT INTO bills (id, customer_id, total_amount, created_by) VALUES (1, 1, 350.00, 1);"); // A bill with ID=1
        stmt.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper method to create a test BillDetailEntity
    private BillDetailEntity createTestDetailEntity(int billId, int itemId, int units, String price, String total) {
        BillDetailEntity detail = new BillDetailEntity();
        detail.setBillId(billId);
        detail.setItemId(itemId);
        detail.setItemNameAtSale("Test Item"); // Name is stored at time of sale
        detail.setUnits(units);
        detail.setUnitPriceAtSale(new BigDecimal(price));
        detail.setTotal(new BigDecimal(total));
        detail.setCreatedBy(1);
        return detail;
    }

    @Test
    @Order(1)
    void testAddBillDetailsAndGetBillDetails() throws SQLException {
        // Arrange
        BillDetailEntity detail1 = createTestDetailEntity(1, 101, 2, "50.00", "100.00"); // 2 Pens
        BillDetailEntity detail2 = createTestDetailEntity(1, 102, 1, "250.00", "250.00"); // 1 Book
        List<BillDetailEntity> detailsToAdd = List.of(detail1, detail2);

        // Act
        // addBillDetails doesn't return anything, so we just call it. An exception will fail the test.
        assertDoesNotThrow(() -> billDetailDAO.addBillDetails(connection, detailsToAdd));

        // Verify with getBillDetails
        List<BillDetailEntity> foundDetails = billDetailDAO.getBillDetails(connection, 1);

        // Assert
        assertNotNull(foundDetails, "The list of details should not be null.");
        assertEquals(2, foundDetails.size(), "Should retrieve exactly two bill detail items.");

        // Check the details of the first item
        BillDetailEntity foundPen = foundDetails.stream().filter(d -> d.getItemId() == 101).findFirst().orElse(null);
        assertNotNull(foundPen);
        assertEquals(2, foundPen.getUnits());
        assertEquals(0, new BigDecimal("100.00").compareTo(foundPen.getTotal()));

        // Check the details of the second item
        BillDetailEntity foundBook = foundDetails.stream().filter(d -> d.getItemId() == 102).findFirst().orElse(null);
        assertNotNull(foundBook);
        assertEquals(1, foundBook.getUnits());
        assertEquals(0, new BigDecimal("250.00").compareTo(foundBook.getTotal()));
    }
}
