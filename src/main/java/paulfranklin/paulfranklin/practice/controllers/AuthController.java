package paulfranklin.paulfranklin.practice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import paulfranklin.paulfranklin.practice.dtos.requests.NewLoginRequest;
import paulfranklin.paulfranklin.practice.dtos.responses.Principal;
import paulfranklin.paulfranklin.practice.entities.User;
import paulfranklin.paulfranklin.practice.exceptions.InvalidAuthException;
import paulfranklin.paulfranklin.practice.exceptions.InvalidRequestException;
import paulfranklin.paulfranklin.practice.services.TokenService;
import paulfranklin.paulfranklin.practice.services.UserService;

import java.sql.SQLException;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Principal login(@RequestBody NewLoginRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            throw new InvalidRequestException("Missing username or password");
        }

        User user;
        try {
            Optional<User> userOptional = userService.getUser(req);
            user = userOptional.orElseGet(InvalidAuthException::userNotFound);

            if (userService.badLoginPassword(user, req)) {
                throw new InvalidAuthException("Wrong password");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        Principal principal = new Principal(user.getUserId(), user.getUsername(), user.getActive(), user.getUserRole().getRole());
        tokenService.createNewToken(principal);

        return principal;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(InvalidAuthException.class)
    public InvalidAuthException handledAuthException (InvalidAuthException e) {
        logger.warn(e.getMessage());
        return e;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestException.class)
    public InvalidRequestException handledRequestException (InvalidRequestException e) {
        logger.debug(e.getMessage());
        return e;
    }
}
