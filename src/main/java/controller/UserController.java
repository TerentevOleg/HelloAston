package controller;

import com.google.gson.*;
import model.User;
import service.UserService;
import storage.db.FriendsDbStorage;
import storage.db.UserDbStorage;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = "/users/*")
public class UserController extends HttpServlet {

    private static final long serialVersionUID = 100980L;

    UserDbStorage userDbStorage = new UserDbStorage();
    FriendsDbStorage friendsDbStorage = new FriendsDbStorage();
    UserService userService = new UserService(userDbStorage, friendsDbStorage);

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            add(request, response);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3 && parts[2].equals("friends")) {
                    getFriends(request, response);
                } else if (parts.length == 5 && parts[3].equals("common")) {
                    getCommonFriends(request, response);
                } else {
                    getById(request, response);
                }
            } else {
                getAll(request, response);
            }
        } catch (SQLException e) {
                throw new RuntimeException(e);
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null) {
                addToFriends(request, response);
            } else {
                update(request, response);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null) {
                removeFromFriends(request, response);
            } else {
                remove(request, response);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void add(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        JsonObject json = new Gson().fromJson(request.getReader(), JsonObject.class);

        String email = json.get("email").getAsString();
        String login = json.get("login").getAsString();
        String name = json.get("name").getAsString();
        LocalDate birthday = LocalDate.parse(json.get("birthday").getAsString(), formatter);

        User newUser = new User(email, login, name, birthday);
        userService.add(newUser);
    }

    private void getAll(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        List<User> userList = userService.getAll();
        String userJson = gsonConverter().toJson(userList);

        PrintWriter printWriter = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        printWriter.write(userJson);
        printWriter.close();
    }

    private void getById(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String id = parts[1];

        User user = userService.getById(Long.parseLong(id));
        String userJson = gsonConverter().toJson(user);

        PrintWriter printWriter = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        printWriter.write(userJson);
        printWriter.close();
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        JsonObject json = new Gson().fromJson(request.getReader(), JsonObject.class);

        long id = json.get("id").getAsLong();
        String email = json.get("email").getAsString();
        String login = json.get("login").getAsString();
        String name = json.get("name").getAsString();
        LocalDate birthday = LocalDate.parse(json.get("birthday").getAsString(), formatter);

        User updateUser = new User(id, email, login, name, birthday);
        userService.update(updateUser);
    }

    private void addToFriends(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String userId = parts[1];
        String friendId = parts[3];

        userService.addToFriends(Long.parseLong(userId), Long.parseLong(friendId));
    }

    private void removeFromFriends(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String userId = parts[1];
        String friendId = parts[3];

        userService.removeFromFriends(Long.parseLong(userId), Long.parseLong(friendId));
    }

    private void getFriends(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String id = parts[1];

        List<User> friends = userService.getFriends(Long.parseLong(id));
        String friendsJson = gsonConverter().toJson(friends);

        PrintWriter printWriter = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        printWriter.write(friendsJson);
        printWriter.close();

    }

    private void getCommonFriends(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String userId = parts[1];
        String friendId = parts[4];

        List<User> commonFriends = userService.getCommonFriends(Long.parseLong(userId), Long.parseLong(friendId));
        String friendsJson = gsonConverter().toJson(commonFriends);

        PrintWriter printWriter = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        printWriter.write(friendsJson);
        printWriter.close();
    }

    private void remove(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");
        String param = parts[1];

        userService.remove(Long.parseLong(param));
    }

    private Gson gsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        return gsonBuilder.setPrettyPrinting().create();
    }

    class LocalDateSerializer implements JsonSerializer<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        @Override
        public JsonElement serialize(LocalDate localDate, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDate));
        }
    }

    class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH));
        }
    }
}
