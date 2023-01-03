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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class FriendsDbStorageTest {

    @Container
    public final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("postgres")
                    .withUsername("postgres")
                    .withPassword("iamroot");

    @BeforeEach
    public void setUp() {
        POSTGRESQL_CONTAINER.start();
    }

    @AfterEach
    public void clean() {
        userDbStorage.clearUsers();
        friendsDbStorage.clearFriends();
    }

    private final FriendsDbStorage friendsDbStorage = new FriendsDbStorage();
    private final UserDbStorage userDbStorage = new UserDbStorage();

    private static final User user = new User("frest@mail.ru", "Oleg",
            "TerentevO", LocalDate.of(1993, 12, 3));

    private static final User user1 = new User("frest1@mail.ru", "Oleg1",
            "TerentevO1", LocalDate.of(1994, 12, 3));

    private static final User user2 = new User("frest2@mail.ru", "Oleg2",
            "TerentevO2", LocalDate.of(1995, 12, 3));

    @Test
    void test() {
        assertTrue(POSTGRESQL_CONTAINER.isRunning());
    }

    @Test
    public void givenAddValidFriend_whenAdd_thenAddFriendsAndGetFriends() {
        userDbStorage.add(user);
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        List<User> users = userDbStorage.getUsers();

        friendsDbStorage.addFriend(users.get(0).getId(), users.get(1).getId());

        Collection<User> friends = friendsDbStorage.getFriends(users.get(0).getId());
        assertThat(friends)
                .isNotEmpty()
                .hasSize(1)
                .contains(userDbStorage.getUsers().get(1));
    }

    @Test
    public void givenAddInvalidFriend_whenAdd_thenThrown() {
        userDbStorage.add(user);
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        List<User> users = userDbStorage.getUsers();

        assertThatThrownBy(() -> friendsDbStorage.addFriend(users.get(0).getId(), users.get(4).getId()))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void givenRemoveFriend_whenRemove_thenRemoveTrue() {
        userDbStorage.add(user);
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        List<User> users = userDbStorage.getUsers();
        friendsDbStorage.addFriend(users.get(0).getId(), users.get(1).getId());
        Collection<User> friends = friendsDbStorage.getFriends(users.get(0).getId());
        assertThat(friends)
                .isNotEmpty()
                .hasSize(1)
                .contains(userDbStorage.getUsers().get(1));

        assertTrue(friendsDbStorage.removeFriend(users.get(0).getId(), users.get(1).getId()));
    }

    @Test
    public void givenCommonFriend_whenCommonFriends_thenFriendListCommonFriends() {
        userDbStorage.add(user);
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        List<User> users = userDbStorage.getUsers();

        friendsDbStorage.addFriend(users.get(0).getId(), users.get(1).getId());
        friendsDbStorage.addFriend(users.get(1).getId(), users.get(0).getId());
        friendsDbStorage.addFriend(users.get(0).getId(), users.get(2).getId());
        friendsDbStorage.addFriend(users.get(1).getId(), users.get(2).getId());

        Collection<User> friends = friendsDbStorage.getCommonFriends(users.get(0).getId(), users.get(1).getId());
        assertThat(friends)
                .isNotEmpty()
                .hasSize(1)
                .contains(userDbStorage.getUsers().get(2));
    }

    @Test
    public void givenNotFindCommonFriend_whenNotFind_thenCommonFriendListIsEmpty() {
        userDbStorage.add(user);
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        List<User> users = userDbStorage.getUsers();

        friendsDbStorage.addFriend(users.get(0).getId(), users.get(1).getId());
        friendsDbStorage.addFriend(users.get(1).getId(), users.get(0).getId());
        friendsDbStorage.addFriend(users.get(0).getId(), users.get(2).getId());
        friendsDbStorage.addFriend(users.get(1).getId(), users.get(2).getId());

        Collection<User> friends = friendsDbStorage.getCommonFriends(users.get(0).getId(), users.get(2).getId());
        assertThat(friends)
                .isEmpty();
    }
}