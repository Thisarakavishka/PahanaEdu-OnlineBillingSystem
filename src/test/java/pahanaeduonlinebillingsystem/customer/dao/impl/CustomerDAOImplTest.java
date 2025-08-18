package pahanaeduonlinebillingsystem.customer.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thisara Kavishka
 * @date 2025-08-18
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerDAOImplTest {

    private CustomerDAOImpl customerDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        customerDAO = new CustomerDAOImpl();
        connection = DBUtil.getConnection();
        // Clean related tables to ensure a fresh start for each test
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;").execute();
        connection.prepareStatement("TRUNCATE TABLE customers;").execute();
        connection.prepareStatement("TRUNCATE TABLE users;").execute();
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 1;").execute();

        // Add a dummy user so foreign key constraints on `created_by` pass
        connection.prepareStatement("INSERT INTO users (id, username, password, salt, role) VALUES (1, 'test_admin', 'pass', 'salt', 'ADMIN');").execute();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper method to create a test CustomerEntity
    private CustomerEntity createTestCustomerEntity(String accNum, String name, String phone) {
        CustomerEntity customer = new CustomerEntity();
        customer.setAccountNumber(accNum);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress("123 Test Street");
        customer.setUnitsConsumed(0);
        customer.setCreatedBy(1); // Assume created by the admin user
        return customer;
    }

    @Test
    @Order(1)
    void testAddAndSearchByAccountNumber() throws SQLException, ClassNotFoundException {
        // Arrange
        CustomerEntity customer = createTestCustomerEntity("ACC001", "John Doe", "0771234567");

        // Act
        boolean result = customerDAO.add(connection, customer);

        // Assert
        assertTrue(result, "DAO should return true on successful add.");

        // Verify with search
        CustomerEntity foundCustomer = customerDAO.searchByAccountNumber(connection, "ACC001");
        assertNotNull(foundCustomer, "Customer should be found by account number after being added.");
        assertEquals("John Doe", foundCustomer.getName());
        assertEquals("0771234567", foundCustomer.getPhone());
    }

    @Test
    @Order(2)
    void testUpdate() throws SQLException, ClassNotFoundException {
        // Arrange
        CustomerEntity originalCustomer = createTestCustomerEntity("ACC002", "Jane Doe", "0712223333");
        customerDAO.add(connection, originalCustomer);
        CustomerEntity addedCustomer = customerDAO.searchByAccountNumber(connection, "ACC002");

        // Act
        addedCustomer.setName("Jane Smith");
        addedCustomer.setPhone("0714445555");
        addedCustomer.setUnitsConsumed(150);
        addedCustomer.setUpdatedBy(1);
        boolean result = customerDAO.update(connection, addedCustomer);

        // Assert
        assertTrue(result, "DAO should return true on successful update.");

        // Verify the update
        CustomerEntity updatedCustomer = customerDAO.searchById(connection, addedCustomer.getId());
        assertEquals("Jane Smith", updatedCustomer.getName());
        assertEquals("0714445555", updatedCustomer.getPhone());
        assertEquals(150, updatedCustomer.getUnitsConsumed());
        assertNotNull(updatedCustomer.getUpdatedAt());
    }

    @Test
    @Order(3)
    void testSoftDelete() throws SQLException, ClassNotFoundException {
        // Arrange
        CustomerEntity customer = createTestCustomerEntity("ACC003", "Delete Me", "0765554444");
        customerDAO.add(connection, customer);
        CustomerEntity addedCustomer = customerDAO.searchByAccountNumber(connection, "ACC003");

        // Act
        boolean result = customerDAO.delete(connection, 1, addedCustomer.getAccountNumber());

        // Assert
        assertTrue(result, "DAO should return true on successful soft delete.");
        assertNull(customerDAO.searchById(connection, addedCustomer.getId()), "A soft-deleted customer should not be found.");
    }

    @Test
    @Order(4)
    void testExistsByAccountNumberAndPhone() throws SQLException, ClassNotFoundException {
        // Arrange
        customerDAO.add(connection, createTestCustomerEntity("ACC004", "Exist Test", "0751112222"));

        // Assert
        assertTrue(customerDAO.existsByAccountNumber(connection, "ACC004"), "existsByAccountNumber should return true for an existing customer.");
        assertFalse(customerDAO.existsByAccountNumber(connection, "ACC999"), "existsByAccountNumber should return false for a non-existent customer.");

        assertTrue(customerDAO.existsByPhoneNumber(connection, "0751112222"), "existsByPhoneNumber should return true for an existing number.");
        assertFalse(customerDAO.existsByPhoneNumber(connection, "0700000000"), "existsByPhoneNumber should return false for a non-existent number.");
    }

    @Test
    @Order(5)
    void testGetAll_WithSearch() throws SQLException, ClassNotFoundException {
        // Arrange
        customerDAO.add(connection, createTestCustomerEntity("CUST01", "Kamal Silva", "0771111111"));
        customerDAO.add(connection, createTestCustomerEntity("CUST02", "Nimal Perera", "0712222222"));
        customerDAO.add(connection, createTestCustomerEntity("CUST03", "Sunil Silva", "0763333333"));

        // Act
        Map<String, String> searchParams = Map.of("search", "Silva");
        List<CustomerEntity> foundCustomers = customerDAO.getAll(connection, searchParams);

        // Assert
        assertEquals(2, foundCustomers.size(), "Search for 'Silva' should return two customers.");
    }

    @Test
    @Order(6)
    void testSearchByPhone() throws SQLException, ClassNotFoundException {
        // Arrange
        customerDAO.add(connection, createTestCustomerEntity("ACC005", "Phone Test", "0719876543"));

        // Act
        CustomerEntity foundCustomer = customerDAO.searchByPhone(connection, "0719876543");
        CustomerEntity notFoundCustomer = customerDAO.searchByPhone(connection, "0700000000");

        // Assert
        assertNotNull(foundCustomer, "Customer should be found by their phone number.");
        assertEquals("Phone Test", foundCustomer.getName());
        assertNull(notFoundCustomer, "Searching for a non-existent phone number should return null.");
    }

    @Test
    @Order(7)
    void testExistsById() throws SQLException, ClassNotFoundException {
        // Arrange
        customerDAO.add(connection, createTestCustomerEntity("ACC006", "ID Exist Test", "0761112222"));
        CustomerEntity addedCustomer = customerDAO.searchByAccountNumber(connection, "ACC006");

        // Act
        boolean shouldExist = customerDAO.existsById(connection, addedCustomer.getId());
        boolean shouldNotExist = customerDAO.existsById(connection, 9999); // A non-existent ID

        // Assert
        assertTrue(shouldExist, "existsById should return true for an existing customer ID.");
        assertFalse(shouldNotExist, "existsById should return false for a non-existent ID.");
    }
}
