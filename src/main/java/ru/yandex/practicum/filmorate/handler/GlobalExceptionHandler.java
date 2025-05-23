package ru.yandex.practicum.filmorate.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleFilmNotFound(FilmNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentNotValid(MethodArgumentNotValidException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handle(Exception e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleGenreNotFound(GenreNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRatingNotFound(RatingNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleDirectorNotFound(DirectorNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleReviewNotFound(ReviewNotFoundException e) {
        return ApiError.builder().description(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }
}
