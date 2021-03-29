package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockLessThanZeroException extends Exception {

    public BeerStockLessThanZeroException(Long id) {
        super(String.format("Stock capacity of beer with id %s cannot be below 0", id));
    }
}
