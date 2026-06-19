package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в Spring Boot приложение Filmorate.
 */
@SpringBootApplication
public class FilmorateApplication {

	/**
	 * Запускает Spring Boot контекст приложения.
	 *
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
	}

}
