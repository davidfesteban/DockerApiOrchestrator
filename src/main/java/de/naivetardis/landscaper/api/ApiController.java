package de.naivetardis.landscaper.api;

import de.naivetardis.landscaper.annotation.SneakyCatch;
import de.naivetardis.landscaper.service.AntiDDoSService;
import de.naivetardis.landscaper.service.AuthManagerService;
import de.naivetardis.landscaper.utility.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@AllArgsConstructor
@Slf4j
public class ApiController {
    private AuthManagerService authManagerService;
    private AntiDDoSService antiDDoSService;

    @SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    @RequestMapping("/**")
    public ResponseEntity<?> reverseProxy(@RequestBody(required = false) String body,
                                          HttpMethod method, HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {

        if (!AuthUtils.isPublicSubdomain(request) && !authManagerService.isTokenPresent(request)) {
            antiDDoSService.controlTries(request);
            authManagerService.resetClientByClearingCookies(request, response);
            authManagerService.storeWhileWaitingForAuth(body, method, request, response);
            return AuthUtils.loginView();
        }

        antiDDoSService.releaseTries(request);
        log.info("Receiving request for: {}", request.getRequestURI());
        return authManagerService.handleRequest(body, method, request, response);

    }

    @SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    @GetMapping("/auth")
    public ResponseEntity<?> auth(@RequestParam("email") String email,
                                  @RequestParam("pswd") String pass,
                                  @RequestParam("code") String code,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        antiDDoSService.controlTries(request);
        return authManagerService.auth(email, pass, code, request, response);
    }

    @SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    @RequestMapping("/onetime")
    public ResponseEntity<?> onetime(@RequestParam("code") String code,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        antiDDoSService.controlTries(request);
        return authManagerService.authByOneTimeCode(code, request, response);
    }
}
