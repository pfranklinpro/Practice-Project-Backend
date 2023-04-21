package paulfranklin.paulfranklin.practice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import paulfranklin.paulfranklin.practice.dtos.requests.NewFriendRequest;
import paulfranklin.paulfranklin.practice.dtos.responses.Principal;
import paulfranklin.paulfranklin.practice.entities.User;
import paulfranklin.paulfranklin.practice.exceptions.InvalidAuthException;
import paulfranklin.paulfranklin.practice.exceptions.InvalidFriendshipException;
import paulfranklin.paulfranklin.practice.exceptions.InvalidRequestException;
import paulfranklin.paulfranklin.practice.exceptions.InvalidUserException;
import paulfranklin.paulfranklin.practice.services.FriendshipService;
import paulfranklin.paulfranklin.practice.services.TokenService;
import paulfranklin.paulfranklin.practice.services.UserService;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/friendships")
public class FriendshipController {
    private final Logger logger = LoggerFactory.getLogger(FriendshipController.class);
    private final FriendshipService friendshipService;
    private final TokenService tokenService;
    private final UserService userService;

    public FriendshipController(FriendshipService friendshipService, TokenService tokenService, UserService userService) {
        this.friendshipService = friendshipService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<String> getFriends(HttpServletRequest servReq) {
        String token = servReq.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            throw new InvalidRequestException("Missing token");
        }

        Principal principal = tokenService.retrievePrincipalFromToken(token);

        try {
            Optional<User> userOptional = userService.getUserByUsername(principal.getUsername());
            userOptional.orElseGet(InvalidAuthException::userNotFound);
        } catch (InvalidAuthException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        try {
            return friendshipService.getFriendNamesByUserId(principal.getUserId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("new")
    @ResponseStatus(HttpStatus.CREATED)
    public void newFriend(@RequestBody NewFriendRequest req, HttpServletRequest servReq) {
        String token = servReq.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            throw new InvalidRequestException("Missing token");
        }
        if (req.getFriendName() == null) {
            throw new InvalidRequestException("Missing friend name");
        }

        Principal principal = tokenService.retrievePrincipalFromToken(token);

        try {
            Optional<User> userOptional = userService.getUserByUsername(principal.getUsername());
            userOptional.orElseGet(InvalidAuthException::userNotFound);
        } catch (InvalidAuthException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        try {
            Optional<User> userOptional = userService.getUserByUsername(principal.getUsername());
            User user = userOptional.orElseGet(InvalidAuthException::userNotFound);

            Optional<User> friendOptional = userService.getUserByUsername(req.getFriendName());
            User friend = friendOptional.orElseGet(InvalidAuthException::friendNotFound);

            friendshipService.createNewFriendship(user, friend);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidFriendshipException("Could not create the friendship");
        } catch (IllegalArgumentException e) {
            throw new InvalidFriendshipException(e.getMessage());
        } catch (InvalidUserException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @DeleteMapping("delete/{friendName}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteFriend(@PathVariable(name="friendName") String friendName, HttpServletRequest servReq) {
        String token = servReq.getHeader("authorization");
        if (token == null || token.isEmpty()) {
            throw new InvalidRequestException("Missing token");
        }

        Principal principal = tokenService.retrievePrincipalFromToken(token);

        try {
            Optional<User> userOptional = userService.getUserByUsername(principal.getUsername());
            userOptional.orElseGet(InvalidAuthException::userNotFound);
        } catch (InvalidAuthException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        try {
            friendshipService.deleteFriendship(principal.getUserId(), friendName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestException.class)
    public InvalidRequestException handledRequestException (InvalidRequestException e) {
        logger.error(e.getMessage());
        return e;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(InvalidFriendshipException.class)
    public InvalidFriendshipException handledFriendshipException (InvalidFriendshipException e) {
        logger.info(e.getMessage());
        return e;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUserException.class)
    public InvalidUserException handledUserException (InvalidUserException e) {
        logger.error(e.getMessage());
        return e;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidAuthException.class)
    public InvalidAuthException handledAuthException (InvalidAuthException e) {
        logger.warn(e.getMessage());
        return e;
    }
}
