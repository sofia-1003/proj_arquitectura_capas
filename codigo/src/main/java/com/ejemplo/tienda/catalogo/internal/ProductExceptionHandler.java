package com.ejemplo.tienda.catalogo.internal;

import com.ejemplo.tienda.catalogo.ProductInUseException;
import com.ejemplo.tienda.catalogo.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Only applies to this module's controller; each module translates its own errors.
@RestControllerAdvice(assignableTypes = ProductController.class)
class ProductExceptionHandler {

    // ProductNotFoundException -> 404 Not Found
    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Producto no encontrado");
        return problem;
    }

    // ProductInUseException -> 409 Conflict
    @ExceptionHandler(ProductInUseException.class)
    ProblemDetail handleInUse(ProductInUseException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Producto en uso");
        return problem;
    }
}
