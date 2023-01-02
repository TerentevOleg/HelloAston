package storage;

import model.User;

import java.util.List;

public interface FriendsStorage {

    boolean addFriend(long userId, long friendId);

    boolean removeFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);
}
