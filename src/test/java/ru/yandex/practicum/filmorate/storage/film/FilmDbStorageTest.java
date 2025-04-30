package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, MpaStorage.class, MpaService.class, GenreService.class, GenreStorage.class, LikeStorage.class})
class FilmDbStorageTest {
    private Film film;
    private Mpa mpa;

    @Autowired
    private final FilmDbStorage filmDbStorage;

    @Autowired
    private MpaStorage mpaStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблицы перед каждым тестом
        jdbcTemplate.execute("MERGE INTO ratings_mpa (id, name, description)" +
                "    VALUES (1, 'G', 'У фильма нет возрастных ограничений')," +
                "           (2, 'PG', 'Детям рекомендуется смотреть фильм с родителями')," +
                "           (3, 'PG-13', 'Детям до 13 лет просмотр нежелателен')," +
                "           (4, 'R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого')," +
                "           (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');");
        mpa = mpaStorage.getMpaById(1);
        film = new Film(1L, "name", "description", LocalDate.of(2000, 10, 5),
                150, new HashSet<>(), mpa, new HashSet<>());
    }


    @AfterEach
    void tearDown() {
        log.info("Очищаю базу данных");
        jdbcTemplate.execute("DELETE FROM films");
        log.info("База данных очищена");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        log.info("Счетчик сброшен");
    }

    @Test
    void createFilmAndGetFilmByIdTest() {
        filmDbStorage.create(film);
        Optional<Film> filmOptional = Optional.ofNullable(filmDbStorage.findById(1L));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    void testFindAll() {
        filmDbStorage.create(film);
        List<Film> films = List.of(film);
        assertEquals(films, filmDbStorage.findAll());
    }

    @Test
    void testUpdateFilm() {
        filmDbStorage.create(film);
        Film film1 = new Film(1L, "newName", "newDescription", LocalDate.of(2000, 10, 5),
                150, new HashSet<>(), mpa, new HashSet<>());
        filmDbStorage.update(film1);
        Optional<Film> filmOptional = Optional.ofNullable(filmDbStorage.findById(1L));
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "newName"))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("description", "newDescription"))
        ;
    }

    @Test
    void testDelete() {
        filmDbStorage.create(film);
        filmDbStorage.delete(1L);
        assertEquals(0, filmDbStorage.findAll().size());
    }
}