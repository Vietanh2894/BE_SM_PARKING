package nckh.felix.StupidParking.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import nckh.felix.StupidParking.util.error.IdInvalidException;

@RestController
public class HomeController {

    @GetMapping("/")

    public String home() throws IdInvalidException {
        // if (true)
        // throw new IdInvalidException("checkmate me");
        return "update Welcome to the Stupid Parking API";
    }
}
