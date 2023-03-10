package storage;

import model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getUsers();

    Optional<User> getById(long id);

    long add(User user);

    boolean update(User user);

    boolean remove(long userId);
}
