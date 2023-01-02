package service;

import exception.DbCreateEntityFaultException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.User;
import storage.FriendsStorage;
import storage.UserStorage;

import java.util.List;
import java.util.Objects;

@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendsStorage friendsStorage;

    public UserService(UserStorage userStorage, FriendsStorage friendsStorage) {
        this.userStorage = userStorage;
        this.friendsStorage = friendsStorage;
    }

    public List<User> getAll() {
        return userStorage.getUsers();
    }

    public User add(User user) {
        userValidationByName(user);
        long id = user.getId();
        userStorage.add(user);
        user = userStorage.getById(id).orElseThrow(() ->
                new DbCreateEntityFaultException(String.format("User (id=%s) hasn't been added to database", id)));
        log.debug("Added user {}", user);
        return user;
    }

    public User update(User user) {
        userValidationByName(user);
        if (!userStorage.update(user)) {
            throw new NotFoundException(String.format("User id = %s not found", user.getId()));
        }
        long id = user.getId();
        user = userStorage.getById(id).orElseThrow(() ->
                new DbCreateEntityFaultException(String.format("User (id=%s) hasn't been updated in database", id)));
        log.debug("Updated user {}", user);
        return user;
    }

    private void userValidationByName(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.isBlank()) {
            user.setName(user.getLogin());
            log.debug("User login=%s: {}", user);
        }
    }

    public User getById(long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User id=%s not found", id)));
    }

    public void addToFriends(long userId, long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendsStorage.addFriend(userId, friendId);
        log.debug("Add friends id={} and id={}", userId, friendId);
    }

    public void removeFromFriends(long userId, long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendsStorage.removeFriend(userId, friendId);
        log.debug("Remove friends id={} and id={}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        checkUserExists(userId);
        return friendsStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        checkUserExists(userId);
        checkUserExists(otherId);
        return friendsStorage.getCommonFriends(userId, otherId);
    }

    public void remove(long userId) {
        if (userStorage.remove(userId)) {
            log.debug("User id = {} removed", userId);
        } else {
            throw new NotFoundException((String.format("User id = %s not found", userId)));
        }
    }

    private void checkUserExists(long id) {
        userStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User id=%s not found", id)));
    }
}