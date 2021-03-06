package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockExceededException extends Exception {

    public BeerStockExceededException(Long id, int max) {
        super(String.format("stock capacity of beer with id %s cannot be above max %s", id, max));
    }
}
