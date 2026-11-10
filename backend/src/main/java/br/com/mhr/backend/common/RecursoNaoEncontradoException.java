package br.com.mhr.backend.common;

/** Lançada quando um recurso buscado por id/slug não existe; vira HTTP 404 (ver {@link ApiExceptionHandler}). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
