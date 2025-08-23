package nckh.felix.StupidParking.util.error;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import nckh.felix.StupidParking.domain.RestResponse;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = { UsernameNotFoundException.class, BadCredentialsException.class })
    public ResponseEntity<RestResponse<Object>> handleIdException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Exception Occurs");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

}
