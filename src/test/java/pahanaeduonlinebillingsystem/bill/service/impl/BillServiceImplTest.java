package pahanaeduonlinebillingsystem.bill.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.impl.BillServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.impl.ItemDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;
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
class BillServiceImplTest {

    private BillServiceImpl billService;
    private Connection connection;

    // DAOs needed for setup and verification
    private ItemDAOImpl itemDAO;
    private CustomerDAOImpl customerDAO;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        billService = new BillServiceImpl();
        itemDAO = new ItemDAOImpl();
        customerDAO = new CustomerDAOImpl();
        connection = DBUtil.getConnection();

        // Clean and prepare all related tables
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
        stmt.execute("INSERT INTO customers (id, account_number, name, address, phone, units_consumed, created_by) VALUES (1, 'CUST001', 'Test Customer', '123 Street', '0771112222', 50, 1);"); // Starts with 50 units
        stmt.execute("INSERT INTO items (id, name, unit_price, stock_quantity, created_by) VALUES (1, 'Test Pen', 50.00, 100, 1);"); // 100 in stock
        stmt.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @Order(1)
    void testGenerateBill_Success_UpdatesStockAndCustomerUnits() throws ClassNotFoundException, SQLException {
        // Arrange
        BillDetailDTO billDetail = new BillDetailDTO();
        billDetail.setItemId(1); // Test Pen
        billDetail.setUnits(10); // Buying 10 pens

        BillDTO billDTO = new BillDTO();
        billDTO.setCustomerId(1);
        billDTO.setDetails(List.of(billDetail));

        // Act
        BillDTO generatedBill = billService.generateBill(billDTO, 1); // Admin 1 generates the bill

        // Assert
        // 1. Verify the bill itself
        assertNotNull(generatedBill);
        assertTrue(generatedBill.getId() > 0);
        assertEquals(0, new BigDecimal("500.00").compareTo(generatedBill.getTotalAmount())); // 10 units * 50.00
        assertEquals(1, generatedBill.getDetails().size());
        assertEquals("Test Pen", generatedBill.getDetails().get(0).getItemNameAtSale());

        // 2. Verify item stock was reduced
        ItemEntity updatedItem = itemDAO.searchById(connection, 1);
        assertEquals(90, updatedItem.getStockQuantity(), "Item stock should be reduced from 100 to 90.");

        // 3. Verify customer units were increased
        CustomerEntity updatedCustomer = customerDAO.searchById(connection, 1);
        assertEquals(60, updatedCustomer.getUnitsConsumed(), "Customer units should increase from 50 to 60.");
    }

    @Test
    @Order(2)
    void testGenerateBill_FailsOnInsufficientStock_AndRollsBack() throws SQLException, ClassNotFoundException {
        // Arrange
        BillDetailDTO billDetail = new BillDetailDTO();
        billDetail.setItemId(1); // Test Pen
        billDetail.setUnits(101); // Trying to buy 101, but only 100 in stock

        BillDTO billDTO = new BillDTO();
        billDTO.setCustomerId(1);
        billDTO.setDetails(List.of(billDetail));

        // Act & Assert: Check that the correct exception is thrown
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> billService.generateBill(billDTO, 1)
        );
        assertEquals(ExceptionType.INSUFFICIENT_STOCK, exception.getExceptionType());

        // Assert Rollback: Verify that the database state has NOT changed
        // 1. Verify item stock was NOT reduced
        ItemEntity itemAfterFail = itemDAO.searchById(connection, 1);
        assertEquals(100, itemAfterFail.getStockQuantity(), "Item stock should remain 100 after a failed transaction.");

        // 2. Verify customer units were NOT increased
        CustomerEntity customerAfterFail = customerDAO.searchById(connection, 1);
        assertEquals(50, customerAfterFail.getUnitsConsumed(), "Customer units should remain 50 after a failed transaction.");
    }
}