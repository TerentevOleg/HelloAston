package service;

import exception.DbCreateEntityFaultException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.Film;
import model.Genre;
import model.Mpa;
import storage.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final UserStorage userStorage;
    private final LikesStorage likesStorage;

    public FilmService(FilmStorage filmStorage, MpaStorage mpaStorage, GenreStorage genreStorage,
                       FilmGenreStorage filmGenreStorage, UserStorage userStorage, LikesStorage likesStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.userStorage = userStorage;
        this.likesStorage = likesStorage;
    }


    public List<Film> getAll() {
        Map<Long, List<Genre>> genres = filmGenreStorage.getAllFilmGenres();
        return addFieldsToFilms(filmStorage.getAll(), genres);
    }

    public Film add(Film film) {
        checkMpaExists(film);
        checkGenresExist(film);
        long id = filmStorage.add(film);
        filmGenreStorage.addFilmGenres(id, film.getGenres());
        film = filmStorage.getById(id).orElseThrow(() ->
                        new DbCreateEntityFaultException(
                                String.format("Film (id=%s) hasn't been added to database", id)))
                .withGenres(filmGenreStorage.getGenresByFilmId(id));
        log.debug("Add film: {}", film);
        return film;
    }

    public Film update(Film film) {
        checkMpaExists(film);
        checkGenresExist(film);
        long id = film.getId();
        if (!filmStorage.update(film)) {
            throw new NotFoundException(String.format("Film id = %s not found." ,film.getId()));
        }
        filmGenreStorage.deleteFilmGenres(id);
        filmGenreStorage.addFilmGenres(id, film.getGenres());
        film = filmStorage.getById(id).orElseThrow(() ->
                        new DbCreateEntityFaultException(
                                String.format("Film (id=%s) hasn't been updated in database", id)))
                .withGenres(filmGenreStorage.getGenresByFilmId(id));
        log.debug("Update film {}", film);
        return film;
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Film id=%s not found", id)))
                .withGenres(filmGenreStorage.getGenresByFilmId(id));
    }

    public void addLike(long filmId, long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        likesStorage.addLike(filmId, userId);
        log.debug("Add like to film id={} by user id={}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        likesStorage.removeLike(filmId, userId);
        log.debug("Remove like from film id={} by user id={}", filmId, userId);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        return addFieldsToFilms(commonFilms);
    }

    public List<Film> getFilmsLikedByUser(long userId) {
        checkUserExists(userId);
        List<Film> films = filmStorage.getFilmsLikedByUser(userId);
        return addFieldsToFilms(films);
    }

    public void remove(long filmId) {
        if(filmStorage.remove(filmId)) {
            log.debug("Film id = {} removed", filmId);
        } else {
            throw new NotFoundException(String.format("Film id = %s not found", filmId));
        }
    }

    private void checkFilmExists(long id) {
        filmStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Film id=%s not found", id)));
    }

    private void checkUserExists(long id) {
        userStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User id=%s not found", id)));
    }

    private void checkMpaExists(Film film) {
        Mpa mpa = film.getMpa();
        if (Objects.nonNull(mpa)) {
            int id = mpa.getId();
            mpaStorage.getById(id)
                    .orElseThrow(() ->
                            new NotFoundException(String.format("Mpa rating with id=%s not found", id)));
        }
    }

    private void checkGenresExist(Film film) {
        if (Objects.isNull(film.getGenres())) {
            return;
        }
        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        Map<Integer, Genre> genres = genreStorage.getByIds(genreIds);
        for (Integer id : genreIds) {
            if (Objects.isNull(genres.get(id))) {
                throw new NotFoundException(String.format("Genre with id=%s not found", id));
            }
        }
    }

    // Добавление к коллекции фильмов полей со списками жанров и режиссёров
    private List<Film> addFieldsToFilms(List<Film> films) {
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Long, List<Genre>> genres = filmGenreStorage.getGenresByFilmIds(filmIds);
        return addFieldsToFilms(films, genres);
    }

    private List<Film> addFieldsToFilms(List<Film> films,
                                        Map<Long, List<Genre>> genres) {
        return films.stream()
                .map(film -> addFieldsToFilm(film, genres))
                .collect(Collectors.toList());
    }

    private Film addFieldsToFilm(Film film, Map<Long, List<Genre>> genres) {
        long filmId = film.getId();
        return film
                .withGenres(genres.getOrDefault(filmId, List.of()));
    }
}