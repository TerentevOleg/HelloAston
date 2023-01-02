package storage;

import model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    List<Film> getAll();

    List<Film> getMostPopularFilms(int count);

    List<Film> getFilmsLikedByUser(long userId);

    Optional<Film> getById(long id);

    long add(Film film);

    boolean update(Film film);

    List<Film> getCommonFilms(long userId, long friendId);

    boolean remove(long filmId);
}
