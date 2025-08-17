package pahanaeduonlinebillingsystem.user.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thisara Kavishka
 * @date 2025-08-17
 * @since 1.0
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceImplTest {

    private UserServiceImpl userService;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        userService = new UserServiceImpl();
        connection = DBUtil.getConnection();
        // Clean the users table before each test to ensure test isolation
        connection.prepareStatement("DELETE FROM users").executeUpdate();
        // Reset auto-increment to 1 for predictable IDs
        connection.prepareStatement("ALTER TABLE users AUTO_INCREMENT = 1").executeUpdate();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper method to create a basic user for tests
    private UserDTO createTestUserDTO(String username, String password, Role role) {
        UserDTO user = new UserDTO();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setCreatedBy(1); // Assume created by an admin
        return user;
    }

    @Test
    @Order(1)
    void testAddUser_Success() throws ClassNotFoundException {
        // Arrange
        UserDTO user = createTestUserDTO("testuser", "password123", Role.USER);

        // Act
        boolean result = userService.add(user);

        // Assert
        assertTrue(result, "User should be added successfully.");

        // Verify by fetching the user back
        UserDTO addedUser = userService.searchByUsername("testuser");
        assertNotNull(addedUser, "Added user should not be null when searched.");
        assertEquals("testuser", addedUser.getUsername());
        assertNotEquals("password123", addedUser.getPassword(), "Password should be hashed and not stored in plain text.");
    }

    @Test
    @Order(2)
    void testAddUser_FailsOnDuplicateUsername() throws ClassNotFoundException {
        // Arrange
        UserDTO user1 = createTestUserDTO("duplicateuser", "password123", Role.USER);
        userService.add(user1); // Add the user first

        UserDTO user2 = createTestUserDTO("duplicateuser", "anotherpass", Role.ADMIN);

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> userService.add(user2),
                "Adding a user with a duplicate username should throw an exception."
        );
        assertEquals(ExceptionType.USER_ALREADY_EXISTS, exception.getExceptionType());
    }

    @Test
    @Order(3)
    void testAuthenticate_SuccessAndFailure() throws ClassNotFoundException {
        // Arrange
        UserDTO user = createTestUserDTO("authuser", "correctpassword", Role.ADMIN);
        userService.add(user);

        // Act & Assert (Successful Authentication)
        UserDTO authenticatedUser = userService.authenticate("authuser", "correctpassword");
        assertNotNull(authenticatedUser, "Authentication should succeed with correct credentials.");
        assertEquals("authuser", authenticatedUser.getUsername());
        assertEquals(Role.ADMIN, authenticatedUser.getRole());

        // Act & Assert (Failed Authentication - Wrong Password)
        PahanaEduOnlineBillingSystemException wrongPassException = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> userService.authenticate("authuser", "wrongpassword")
        );
        assertEquals(ExceptionType.INVALID_CREDENTIALS, wrongPassException.getExceptionType());

        // Act & Assert (Failed Authentication - User Not Found)
        PahanaEduOnlineBillingSystemException notFoundException = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> userService.authenticate("nonexistentuser", "anypassword")
        );
        assertEquals(ExceptionType.INVALID_CREDENTIALS, notFoundException.getExceptionType());
    }

    @Test
    @Order(4)
    void testDelete_Success() throws ClassNotFoundException {
        // Arrange
        UserDTO admin = createTestUserDTO("admin_deleter", "password123", Role.ADMIN);
        userService.add(admin); // This will have ID 1

        UserDTO userToDelete = createTestUserDTO("usertodelete", "password123", Role.USER);
        userService.add(userToDelete); // This will have ID 2

        // Act
        boolean result = userService.delete(2, 1); // User 1 deletes user 2

        // Assert
        assertTrue(result, "Delete operation should return true on success.");

        // Verify user is soft-deleted (cannot be searched)
        assertNull(userService.searchById(2), "Soft-deleted user should not be found by searchById.");
    }

    @Test
    @Order(5)
    void testDelete_FailsWhenDeletingInitialAdmin() throws ClassNotFoundException {
        // Arrange
        UserDTO initialAdmin = createTestUserDTO("initialadmin", "password", Role.ADMIN);
        userService.add(initialAdmin); // This user will get ID 1

        UserDTO anotherAdmin = createTestUserDTO("anotheradmin", "password", Role.ADMIN);
        userService.add(anotherAdmin); // This user will get ID 2

        // Act & Assert
        PahanaEduOnlineBillingSystemException exception = assertThrows(
                PahanaEduOnlineBillingSystemException.class,
                () -> userService.delete(1, 2) // User 2 attempts to delete the initial admin (ID 1)
        );

        assertEquals(ExceptionType.UNAUTHORIZED_ACCESS, exception.getExceptionType(), "Should throw UNAUTHORIZED_ACCESS when trying to delete initial admin.");
    }
}
