package pahanaeduonlinebillingsystem.user.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.impl.UserDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Thisara Kavishka
 * @date 2025-08-17
 * @since 1.0
 */

/**
 * Integration Test for UserServiceImpl.
 * This test class interacts with a real database to verify the service layer's business logic.
 * IMPORTANT: This test suite will clear the 'users' table before each test.
 * Ensure you are running this against a dedicated TEST DATABASE.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOImplTest {

    private UserDAOImpl userDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        userDAO = new UserDAOImpl();
        connection = DBUtil.getConnection();
        // Clean the database before each test

        connection.prepareStatement("DELETE FROM users").executeUpdate();
        connection.prepareStatement("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper method to create a user entity
    private UserEntity createTestUserEntity(String username, Role role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword("hashedpassword");
        user.setSalt("somesalt");
        user.setRole(role);
        user.setCreatedBy(1); // Assume created by an admin
        return user;
    }

    @Test
    @Order(1)
    void testAddAndSearchByUsername() throws SQLException, ClassNotFoundException {
        // Arrange
        UserEntity user = createTestUserEntity("johndoe", Role.USER);

        // Act
        boolean result = userDAO.add(connection, user);

        // Assert
        assertEquals(result, true, "DAO should return true on successful add.");

        // Verify with search
        UserEntity foundUser = userDAO.searchByUsername(connection, "johndoe");
        assertNotNull(foundUser, "User should be found by username after being added.");
        assertEquals("johndoe", foundUser.getUsername());
        assertEquals(Role.USER, foundUser.getRole());
    }

    @Test
    @Order(2)
    void testUpdate() throws SQLException, ClassNotFoundException {
        // Arrange: Add a user first
        UserEntity originalUser = createTestUserEntity("janedoe", Role.USER);
        userDAO.add(connection, originalUser);
        UserEntity addedUser = userDAO.searchByUsername(connection, "janedoe"); // Get the user with its generated ID

        // Act: Update the user's details
        addedUser.setUsername("janedoe_updated");
        addedUser.setRole(Role.ADMIN);
        addedUser.setUpdatedBy(1);
        boolean result = userDAO.update(connection, addedUser);

        // Assert
        assertTrue(result, "DAO should return true on successful update.");

        // Verify the update
        UserEntity updatedUser = userDAO.searchById(connection, addedUser.getId());
        assertEquals("janedoe_updated", updatedUser.getUsername());
        assertEquals(Role.ADMIN, updatedUser.getRole());
        assertNotNull(updatedUser.getUpdatedAt());
    }

    @Test
    @Order(3)
    void testSoftDelete() throws SQLException, ClassNotFoundException {
        // Arrange
        UserEntity user = createTestUserEntity("todelete", Role.USER);
        userDAO.add(connection, user);
        UserEntity addedUser = userDAO.searchByUsername(connection, "todelete");

        // Act
        boolean result = userDAO.delete(connection, addedUser.getId(), 1); // User ID 1 deletes this user

        // Assert
        assertTrue(result, "DAO should return true on successful soft delete.");

        // Verify that the user cannot be found by normal search methods
        UserEntity foundUser = userDAO.searchById(connection, addedUser.getId());
        assertNull(foundUser, "A soft-deleted user should not be found by a standard search.");
    }

    @Test
    @Order(4)
    void testGetAll() throws SQLException, ClassNotFoundException {
        // Arrange
        userDAO.add(connection, createTestUserEntity("user1", Role.USER));
        userDAO.add(connection, createTestUserEntity("admin1", Role.ADMIN));
        userDAO.add(connection, createTestUserEntity("user2", Role.USER));

        // Act: Get all users
        List<UserEntity> allUsers = userDAO.getAll(connection, null);

        // Assert
        assertEquals(3, allUsers.size(), "getAll should return all non-deleted users.");
    }

    @Test
    @Order(5)
    void testGetAll_WithSearch() throws SQLException, ClassNotFoundException {
        // Arrange
        userDAO.add(connection, createTestUserEntity("test_user_alpha", Role.USER));
        userDAO.add(connection, createTestUserEntity("test_admin_beta", Role.ADMIN));
        userDAO.add(connection, createTestUserEntity("another_user_gamma", Role.USER));

        // Act: Search for users containing "user"
        Map<String, String> searchParams = Map.of("search", "user");
        List<UserEntity> foundUsers = userDAO.getAll(connection, searchParams);

        // Assert
        assertEquals(2, foundUsers.size(), "Search for 'user' should return two users.");
        assertTrue(foundUsers.stream().anyMatch(u -> u.getUsername().equals("test_user_alpha")));
        assertTrue(foundUsers.stream().anyMatch(u -> u.getUsername().equals("another_user_gamma")));
    }

    @Test
    @Order(6)
    void testGetUsersCount() throws SQLException, ClassNotFoundException {
        // Arrange: Add a known number of users to the database
        userDAO.add(connection, createTestUserEntity("count_user_1", Role.USER));
        userDAO.add(connection, createTestUserEntity("count_user_2", Role.USER));
        userDAO.add(connection, createTestUserEntity("count_admin_1", Role.ADMIN));

        // Act: Call the method to get the count of users
        int userCount = userDAO.getUsersCount(connection);

        // Assert: Verify that the count matches the number of users added
        assertEquals(3, userCount, "getUsersCount should return the exact number of non-deleted users.");
    }
}
