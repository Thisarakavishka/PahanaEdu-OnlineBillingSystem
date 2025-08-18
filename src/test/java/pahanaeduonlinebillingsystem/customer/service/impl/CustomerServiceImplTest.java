package pahanaeduonlinebillingsystem.customer.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thisara Kavishka
 * @date 2025-08-18
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerServiceImplTest {

    private CustomerServiceImpl customerService;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        customerService = new CustomerServiceImpl();
        connection = DBUtil.getConnection();
        // Clean related tables for test isolation
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;").execute();
        connection.prepareStatement("TRUNCATE TABLE customers;").execute();
        connection.prepareStatement("TRUNCATE TABLE users;").execute();
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 1;").execute();
        // Add a dummy user for foreign key constraints
        connection.prepareStatement("INSERT INTO users (id, username, password, salt, role) VALUES (1, 'test_admin', 'pass', 'salt', 'ADMIN');").execute();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper to create a test CustomerDTO
    private CustomerDTO createTestCustomerDTO(String accNum, String name, String phone) {
        CustomerDTO customer = new CustomerDTO();
        customer.setAccountNumber(accNum);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress("123 Test Street, Colombo");
        customer.setUnitsConsumed(0);
        customer.setCreatedBy(1);
        return customer;
    }

    @Test
    @Order(1)
    void testAdd_Success() throws SQLException, ClassNotFoundException {
        // Arrange
        CustomerDTO customer = createTestCustomerDTO("CUST001", "Amal Perera", "0771234567");

        // Act
        boolean result = customerService.add(customer);

        // Assert
        assertTrue(result, "Adding a unique customer should be successful.");
    }

    @Test
    @Order(2)
    void testAdd_FailsOnDuplicateAccountNumber() throws SQLException, ClassNotFoundException {
        // Arrange
        customerService.add(createTestCustomerDTO("CUST002", "Kamal Silva", "0711111111"));

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> customerService.add(createTestCustomerDTO("CUST002", "Nimal Fonseka", "0722222222"))
        );
        assertEquals(ExceptionType.CUSTOMER_ACCOUNT_NUMBER_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @Order(3)
    void testAdd_FailsOnDuplicatePhoneNumber() throws SQLException, ClassNotFoundException {
        // Arrange
        customerService.add(createTestCustomerDTO("CUST003", "Sunil Bandara", "0765555555"));

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> customerService.add(createTestCustomerDTO("CUST004", "Kasun Jayasuriya", "0765555555"))
        );
        assertEquals(ExceptionType.CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @Order(4)
    void testUpdate_Success() throws SQLException, ClassNotFoundException {
        // Arrange
        customerService.add(createTestCustomerDTO("CUST005", "Initial Name", "0701234567"));
        CustomerDTO customerToUpdate = customerService.searchByAccountNumber("CUST005");

        // Act
        customerToUpdate.setName("Updated Name");
        customerToUpdate.setAddress("456 New Road, Galle");
        boolean result = customerService.update(customerToUpdate);

        // Assert
        assertTrue(result);
        CustomerDTO updatedCustomer = customerService.searchById(customerToUpdate.getId());
        assertEquals("Updated Name", updatedCustomer.getName());
        assertEquals("456 New Road, Galle", updatedCustomer.getAddress());
    }

    @Test
    @Order(5)
    void testUpdate_FailsForNonExistentCustomer() {
        // Arrange
        CustomerDTO nonExistentCustomer = createTestCustomerDTO("CUST999", "Ghost User", "0700000000");

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> customerService.update(nonExistentCustomer)
        );
        assertEquals(ExceptionType.CUSTOMER_NOT_FOUND, exception.getExceptionType());
    }

    @Test
    @Order(6)
    void testUpdate_FailsWhenChangingToExistingPhone() throws SQLException, ClassNotFoundException {
        // Arrange
        customerService.add(createTestCustomerDTO("CUST006", "User One", "0777777777"));
        customerService.add(createTestCustomerDTO("CUST007", "User Two", "0778888888"));

        CustomerDTO customerToUpdate = customerService.searchByAccountNumber("CUST007");
        customerToUpdate.setPhone("0777777777"); // Attempt to change phone to User One's number

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> customerService.update(customerToUpdate)
        );
        assertEquals(ExceptionType.CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @Order(7)
    void testDelete_Success() throws SQLException, ClassNotFoundException {
        // Arrange
        customerService.add(createTestCustomerDTO("CUST008", "ToDelete", "0713334444"));
        CustomerDTO customer = customerService.searchByAccountNumber("CUST008");

        // Act
        boolean result = customerService.delete(1, customer.getAccountNumber()); // Admin with ID 1 deletes the customer

        // Assert
        assertTrue(result, "Service should return true for a successful delete.");

        // Verify by trying to search for the deleted customer, which should now fail
        assertThrows(PahanaEduOnlineBillingSystemException.class, () -> {
            customerService.searchById(customer.getId());
        });
    }
}