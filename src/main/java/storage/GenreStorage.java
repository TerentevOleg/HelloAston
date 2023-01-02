package storage;

import model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> getAll();

    Optional<Genre> getById(long id);

    Map<Integer, Genre> getByIds(List<Integer> genreIds);
}
