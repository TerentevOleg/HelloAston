import service.UserService;
import storage.FriendsStorage;
import storage.UserStorage;
import storage.db.FriendsDbStorage;
import storage.db.UserDbStorage;

public class Main {
    public static void main(String[] args) {
        UserDbStorage userDbStorage = new UserDbStorage();
        FriendsDbStorage friendsDbStorage = new FriendsDbStorage();
        UserService userService = new UserService(userDbStorage, friendsDbStorage);
        System.out.println(userService.getAll());
    }
}
