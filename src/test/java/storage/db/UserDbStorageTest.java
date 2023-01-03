package storage.db;

import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDbStorageTest {

    @Container
    public final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("iamroot");

    /*@Container
    private PostgreSQLContainer POSTGRESQL_CONTAINER = new PostgreSQLContainer()
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("iamroot");*/

    @BeforeEach
    public void setUp() {
        POSTGRESQL_CONTAINER.start();
    }

    @AfterEach
    public void clean() {
        userDbStorage.clearUsers();
    }

    private final UserDbStorage userDbStorage = new UserDbStorage();

    private static final User user = new User("frest@mail.ru", "Oleg",
            "TerentevO", LocalDate.parse("1946-08-20"));

    private static final User user1 = new User("frest1@mail.ru", "Oleg1",
            "TerentevO1", LocalDate.of(1994, 12, 3));

    private static final User user2 = new User("frest2@mail.ru", "Oleg2",
            "TerentevO2", LocalDate.of(1995, 12, 3));

    @Test
    void test() {
        assertTrue(POSTGRESQL_CONTAINER.isRunning());
    }

    @Test
    public void givenFindAllUsersWithEmptyList_whenFind_thenListSizeZero() {
        List<User> users = userDbStorage.getUsers();

        assertEquals(0, users.size());
    }

    @Test
    void givenFindUserByValidId_whenFind_thenFindUser() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user4 = new User(id, "frest@mail.ru", "Oleg",
                "TerentevO", LocalDate.parse("1946-08-20"));

        Optional<User> userOptional = userDbStorage.getById(id);
        assertThat(userOptional)
                .isPresent()
                .hasValue(user4);

        Collection<User> users = userDbStorage.getUsers();
        assertThat(users)
                .isNotEmpty()
                .hasSize(1)
                .contains(user4);
    }

    @Test
    void givenNewUserWithNullEmail_whenCreated_thenThrown() throws Exception {
        User user3 = new User(null, "Oleg",
                "TerentevO", LocalDate.parse("1946-08-20"));

        assertThatThrownBy(() -> userDbStorage.add(user3))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: add sql exception.");
    }

    @Test
    void givenNewUserWithNullLogin_whenCreated_thenThrown() throws Exception {
        User user3 = new User("fres@mail.ru", null,
                "TerentevO", LocalDate.parse("1946-08-20"));

        assertThatThrownBy(() -> userDbStorage.add(user3))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: add sql exception.");
    }

    @Test
    void givenNewUserWithNullName_whenCreated_thenThrown() throws Exception {
        User user3 = new User("fres@mail.ru", "Oleg",
                null, LocalDate.parse("1946-08-20"));

        assertThatThrownBy(() -> userDbStorage.add(user3))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: add sql exception.");
    }

    @Test
    void givenNewUserWithNullBirthday_whenCreated_thenThrown() throws Exception {
        User user3 = new User("fres@mail.ru", "Oleg",
                "TerentevO", null);

        assertThatThrownBy(() -> userDbStorage.add(user3))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenNewValidUser_whenCreated_thenUserCreated() throws Exception {
        long id = userDbStorage.add(user);

        assertEquals(1, id);
    }

    @Test
    void givenFindUserByInvalidId_whenNotFind_thenThrown() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));

        assertTrue(user3.isPresent());
        assertTrue(userDbStorage.getById(5).isEmpty());
    }

    @Test
    void givenFindUser_whenFind_thenFindUser() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();

        assertTrue(userDbStorage.getById(id).isPresent());
    }

    @Test
    void givenUpdateValidUser_whenUpdated_thenUserUpdated() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user = new User(id, "frest1@mail.ru", "Oleg1",
                "TerentevO1", LocalDate.parse("2000-08-20"));
        userDbStorage.update(user);

        assertEquals(userDbStorage.getById(id), Optional.of(user));
    }

    @Test
    void givenUpdateNullUserEmail_whenUpdated_thenThrown() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user = new User(id, null, "Oleg1",
                "TerentevO1", LocalDate.parse("2000-08-20"));

        assertThatThrownBy(() -> userDbStorage.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: update sql exception.");
    }

    @Test
    void givenUpdateNullUserLogin_whenUpdated_thenThrown() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user = new User(id, "fres@mail.ru", null,
                "TerentevO1", LocalDate.parse("2000-08-20"));

        assertThatThrownBy(() -> userDbStorage.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: update sql exception.");
    }

    @Test
    void givenUpdateNullUserName_whenUpdated_thenThrown() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user = new User(id, "fres@mail.ru", "Oleg",
                null, LocalDate.parse("2000-08-20"));

        assertThatThrownBy(() -> userDbStorage.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UserDbStorage: update sql exception.");
    }

    @Test
    void givenUpdateNullBirthday_whenUpdated_thenThrown() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        User user = new User(id, "fres@mail.ru", "Oleg",
                "TerentevO", null);

        assertThatThrownBy(() -> userDbStorage.update(user))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenRemoveValidUser_whenRemoved_thenListSizeZero() throws Exception {
        userDbStorage.add(user);
        Optional<User> user3 = Optional.ofNullable(userDbStorage.getUsers().get(0));
        long id = user3.get().getId();
        List<User> users = userDbStorage.getUsers();

        assertEquals(1, users.size());

        userDbStorage.remove(id);
        List<User> users1 = userDbStorage.getUsers();

        assertEquals(0, users1.size());
    }

    private void clearDb() {
        Collection<User> users = userDbStorage.getUsers();
        if (!users.isEmpty()) {
            for (int i = 0; i < users.size(); i++) {
                userDbStorage.remove(i);
            }
        }
    }

}
