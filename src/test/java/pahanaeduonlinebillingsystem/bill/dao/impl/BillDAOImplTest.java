package pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl.BillDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
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
class BillDAOImplTest {

    private BillDAOImpl billDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        billDAO = new BillDAOImpl();
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

        // Add prerequisite data
        stmt.execute("INSERT INTO users (id, username, password, salt, role) VALUES (1, 'test_admin', 'p', 's', 'ADMIN');");
        stmt.execute("INSERT INTO customers (id, account_number, name, address, phone, units_consumed, created_by) VALUES (1, 'CUST001', 'Test Customer', '123 Street', '0771112222', 0, 1);");
        stmt.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private BillEntity createTestBillEntity() {
        BillEntity bill = new BillEntity();
        bill.setCustomerId(1);
        bill.setTotalAmount(new BigDecimal("1500.00"));
        bill.setCreatedBy(1);
        return bill;
    }

    @Test
    @Order(1)
    void testAddBillAndSearchById() throws SQLException, ClassNotFoundException {
        // Arrange
        BillEntity bill = createTestBillEntity();

        // Act
        int generatedId = billDAO.addBill(connection, bill);

        // Assert
        assertTrue(generatedId > 0, "addBill should return a positive generated ID.");

        // Verify with search
        BillEntity foundBill = billDAO.searchById(connection, generatedId);
        assertNotNull(foundBill, "Bill should be found by its generated ID.");
        assertEquals(1, foundBill.getCustomerId());
        assertEquals(0, new BigDecimal("1500.00").compareTo(foundBill.getTotalAmount()));
    }

    @Test
    @Order(2)
    void testSoftDelete() throws SQLException, ClassNotFoundException {
        // Arrange
        BillEntity bill = createTestBillEntity();
        int generatedId = billDAO.addBill(connection, bill);
        assertTrue(generatedId > 0, "Bill must be added before it can be deleted.");

        // Act
        boolean result = billDAO.delete(connection, 1, generatedId); // Admin 1 deletes the bill

        // Assert
        assertTrue(result, "DAO should return true on successful soft delete.");
        assertNull(billDAO.searchById(connection, generatedId), "A soft-deleted bill should not be found.");
    }

    @Test
    @Order(3)
    void testGetAll() throws SQLException, ClassNotFoundException {
        // Arrange
        billDAO.addBill(connection, createTestBillEntity());
        billDAO.addBill(connection, createTestBillEntity());

        // Act
        List<BillEntity> allBills = billDAO.getAll(connection, null);

        // Assert
        assertEquals(2, allBills.size(), "getAll should return all non-deleted bills.");
    }
}