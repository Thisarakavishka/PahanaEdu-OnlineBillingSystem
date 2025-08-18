package pahanaeduonlinebillingsystem.item.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.impl.ItemDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.impl.UserDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
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
class ItemDAOImplTest {

    private ItemDAOImpl itemDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        itemDAO = new ItemDAOImpl();
        connection = DBUtil.getConnection();
        // Clean tables to ensure test isolation
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;").execute();
        connection.prepareStatement("TRUNCATE TABLE items;").execute();
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

    // Helper method to create a test ItemEntity
    private ItemEntity createTestItemEntity(String name, String price, int stock) {
        ItemEntity item = new ItemEntity();
        item.setName(name);
        item.setUnitPrice(new BigDecimal(price));
        item.setStockQuantity(stock);
        item.setCreatedBy(1); // Assume created by the admin user
        return item;
    }

    @Test
    @Order(1)
    void testAddAndSearchByName() throws SQLException, ClassNotFoundException {
        // Arrange
        ItemEntity item = createTestItemEntity("Test Pen", "50.00", 100);

        // Act
        boolean result = itemDAO.add(connection, item);

        // Assert
        assertTrue(result, "DAO should return true on successful add.");

        // Verify with search
        ItemEntity foundItem = itemDAO.searchByName(connection, "Test Pen");
        assertNotNull(foundItem, "Item should be found by name after being added.");
        assertEquals("Test Pen", foundItem.getName());
        assertEquals(0, new BigDecimal("50.00").compareTo(foundItem.getUnitPrice()));
    }

    @Test
    @Order(2)
    void testUpdate() throws SQLException, ClassNotFoundException {
        // Arrange
        ItemEntity originalItem = createTestItemEntity("Test Book", "250.00", 50);
        itemDAO.add(connection, originalItem);
        ItemEntity addedItem = itemDAO.searchByName(connection, "Test Book");

        // Act
        addedItem.setName("Updated Test Book");
        addedItem.setStockQuantity(75);
        addedItem.setUpdatedBy(1);
        boolean result = itemDAO.update(connection, addedItem);

        // Assert
        assertTrue(result, "DAO should return true on successful update.");

        // Verify the update
        ItemEntity updatedItem = itemDAO.searchById(connection, addedItem.getId());
        assertEquals("Updated Test Book", updatedItem.getName());
        assertEquals(75, updatedItem.getStockQuantity());
        assertNotNull(updatedItem.getUpdatedAt());
    }

    @Test
    @Order(3)
    void testSoftDelete() throws SQLException, ClassNotFoundException {
        // Arrange
        ItemEntity item = createTestItemEntity("Eraser", "20.00", 200);
        itemDAO.add(connection, item);
        ItemEntity addedItem = itemDAO.searchByName(connection, "Eraser");

        // Act
        boolean result = itemDAO.delete(connection, 1, addedItem.getId()); // Admin 1 deletes the item

        // Assert
        assertTrue(result, "DAO should return true on successful soft delete.");

        // Verify that the item cannot be found by normal search methods
        assertNull(itemDAO.searchById(connection, addedItem.getId()), "A soft-deleted item should not be found.");
    }

    @Test
    @Order(4)
    void testGetAll_WithSearch() throws SQLException, ClassNotFoundException {
        // Arrange
        itemDAO.add(connection, createTestItemEntity("Blue Pen", "50.00", 100));
        itemDAO.add(connection, createTestItemEntity("Red Pen", "50.00", 100));
        itemDAO.add(connection, createTestItemEntity("Black Marker", "120.00", 80));

        // Act
        Map<String, String> searchParams = Map.of("search", "Pen");
        List<ItemEntity> foundItems = itemDAO.getAll(connection, searchParams);

        // Assert
        assertEquals(2, foundItems.size(), "Search for 'Pen' should return two items.");
    }

    @Test
    @Order(5)
    void testGetAll_WithoutSearch() throws SQLException, ClassNotFoundException {
        // Arrange
        itemDAO.add(connection, createTestItemEntity("Item A", "10.00", 10));
        itemDAO.add(connection, createTestItemEntity("Item B", "20.00", 20));

        // Act: Call getAll with null parameters to fetch all items
        List<ItemEntity> allItems = itemDAO.getAll(connection, null);

        // Assert
        assertEquals(2, allItems.size(), "getAll with no search parameters should return all items.");
    }

    @Test
    @Order(6)
    void testGetItemsCount() throws SQLException, ClassNotFoundException {
        // Arrange
        itemDAO.add(connection, createTestItemEntity("Item X", "10.00", 10));
        itemDAO.add(connection, createTestItemEntity("Item Y", "20.00", 20));
        itemDAO.add(connection, createTestItemEntity("Item Z", "30.00", 30));

        // Act
        int count = itemDAO.getItemsCount(connection);

        // Assert
        assertEquals(3, count, "getItemsCount should return the exact number of non-deleted items.");
    }
}