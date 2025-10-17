package br.com.dio.estoque_cerveja.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;





@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CervejaNaoEncontradaException.class)
    public ResponseEntity<ApiException> handleNotFound(CervejaNaoEncontradaException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CervejaJaExisteException.class)
    public ResponseEntity<ApiException> handleDuplicate(CervejaJaExisteException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiException> handleValidation(IllegalArgumentException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiException> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        String mensagem = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse(ex.getMessage());

        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                mensagem,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiException> handleJsonReadError(HttpMessageNotReadableException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de desserialização JSON",
                "O corpo da requisição JSON está mal formado ou contém tipo(s) de dado(s) inválido(s).",
                request.getDescription(false).replace("uri=", "")
        );
        // Este método é crucial para converter o JSON parse error (que causava o 500) para 400.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiException> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        // Monta uma lista de mídias suportadas para a mensagem
        String supported = ex.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("Nenhum");

        ApiException error = new ApiException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Tipo de Mídia Não Suportado",
                "O Content-Type enviado (" + ex.getContentType() + ") não é suportado. Tipos suportados: " + supported,
                request.getDescription(false).replace("uri=", "")
        );
        // Este método garante que a falha de Content-Type incorreto mapeie para 415.
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    // 1. Lida com parâmetros de query que não existem (ex: se 'quantidade' fosse obrigatório e não enviado)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiException> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro Ausente",
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório.",
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. Lida com o tipo de parâmetro incorreto (ex: se 'quantidade' for string em vez de número)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiException> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String mensagem = String.format("O parâmetro '%s' deve ser do tipo '%s'. Valor fornecido: '%s'",
                ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());

        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento de Método Inválido",
                mensagem,
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EstoqueExcedidoException.class)
    public ResponseEntity<ApiException> handleEstoqueExcedido(EstoqueExcedidoException ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleGeneric(Exception ex, WebRequest request) {
        ApiException error = new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno no servidor",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
