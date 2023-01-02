package model;

import lombok.With;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Film {

    private static final int MAX_FILM_DESCRIPTION_LENGTH = 200;
    private final static String MIN_FILM_RELEASE_DATE = "1895-12-28";

    private long id;                   // идентификатор

    @NotBlank(message = "Film name is blank")
    private String name;            // название

    @Size(max = MAX_FILM_DESCRIPTION_LENGTH,
            message = "Film description length is grater then " + MAX_FILM_DESCRIPTION_LENGTH)
    private String description;     // описание

    //@DateConstraint(minDate = MIN_FILM_RELEASE_DATE,
            //message = "Film release date is earlier than " + MIN_FILM_RELEASE_DATE)
    private LocalDate releaseDate;  // дата релиза

    @Positive(message = "Film duration is not positive")
    private int duration;      // продолжительность

    @NotNull
    private Mpa mpa;

    @With
    private List<Genre> genres;

    private int rate;

    public Film(long id, String name, String description, LocalDate releaseDate,
                int duration, Mpa mpa, List<Genre> genres, int rate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres;
        this.rate = rate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Mpa getMpa() {
        return mpa;
    }

    public void setMpa(Mpa mpa) {
        this.mpa = mpa;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return id == film.id && duration == film.duration && rate == film.rate &&
                Objects.equals(name, film.name) && Objects.equals(description, film.description) &&
                Objects.equals(releaseDate, film.releaseDate) && Objects.equals(mpa, film.mpa) &&
                Objects.equals(genres, film.genres);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, releaseDate, duration, mpa, genres, rate);
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate=" + releaseDate +
                ", duration=" + duration +
                ", mpa=" + mpa +
                ", genres=" + genres +
                ", rate=" + rate +
                '}';
    }
}
