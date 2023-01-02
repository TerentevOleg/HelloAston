package storage;

import model.Mpa;

import java.util.List;
import java.util.Optional;

public interface MpaStorage {

    List<Mpa> getAll();

    Optional<Mpa> getById(long id);
}
