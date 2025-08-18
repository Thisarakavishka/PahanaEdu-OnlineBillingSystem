package pahanaeduonlinebillingsystem.item.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thisara Kavishka
 * @date 2025-08-18
 * @since 1.0
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemServiceImplTest {

    private ItemServiceImpl itemService;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        itemService = new ItemServiceImpl();
        connection = DBUtil.getConnection();
        // Clean tables
        connection.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;").execute();
        connection.prepareStatement("TRUNCATE TABLE items;").execute();
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

    // Helper to create a test ItemDTO
    private ItemDTO createTestItemDTO(String name, String price, int stock) {
        ItemDTO item = new ItemDTO();
        item.setName(name);
        item.setUnitPrice(new BigDecimal(price));
        item.setStockQuantity(stock);
        item.setCreatedBy(1);
        return item;
    }

    @Test
    @Order(1)
    void testAdd_FailsWhenItemNameExists() throws SQLException, ClassNotFoundException {
        // Arrange
        itemService.add(createTestItemDTO("Duplicate Pen", "60.00", 100));

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> itemService.add(createTestItemDTO("Duplicate Pen", "70.00", 50))
        );
        assertEquals(ExceptionType.ITEM_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @Order(2)
    void testRestockItem_Success() throws SQLException, ClassNotFoundException {
        // Arrange
        itemService.add(createTestItemDTO("Pencil", "20.00", 50));
        ItemDTO item = itemService.searchByName("Pencil");

        // Act
        boolean result = itemService.restockItem(item.getId(), 30, 1); // Restock 30 units

        // Assert
        assertTrue(result);
        ItemDTO restockedItem = itemService.searchById(item.getId());
        assertEquals(80, restockedItem.getStockQuantity(), "Stock should be 50 + 30 = 80.");
    }

    @Test
    @Order(3)
    void testRestockItem_FailsForNonExistentItem() {
        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> itemService.restockItem(999, 10, 1) // Item with ID 999 does not exist
        );
        assertEquals(ExceptionType.ITEM_NOT_FOUND, exception.getExceptionType());
    }

    @Test
    @Order(4)
    void testAdd_Success() throws SQLException, ClassNotFoundException {
        // Arrange
        ItemDTO item = createTestItemDTO("New Unique Item", "99.99", 5);

        // Act
        boolean result = itemService.add(item);

        // Assert
        assertTrue(result, "Adding a unique item should be successful.");

        // Verify it was actually saved
        ItemDTO savedItem = itemService.searchByName("New Unique Item");
        assertNotNull(savedItem);
        assertEquals(5, savedItem.getStockQuantity());
    }

    @Test
    @Order(5)
    void testUpdate_FailsWhenNewNameAlreadyExists() throws SQLException, ClassNotFoundException {
        // Arrange: Create two initial items
        itemService.add(createTestItemDTO("Pen", "50.00", 100));
        itemService.add(createTestItemDTO("Book", "300.00", 50));

        ItemDTO itemToUpdate = itemService.searchByName("Book"); // We want to rename "Book"
        itemToUpdate.setName("Pen"); // Try to rename it to "Pen", which already exists

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> itemService.update(itemToUpdate),
                "Updating an item to a name that already exists should throw an exception."
        );

        assertEquals(ExceptionType.ITEM_ALREADY_EXISTS, exception.getExceptionType());
    }
}