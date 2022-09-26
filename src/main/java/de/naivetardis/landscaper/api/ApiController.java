package de.naivetardis.landscaper.api;

import de.naivetardis.landscaper.annotation.SneakyCatch;
import de.naivetardis.landscaper.service.AuthManager;
import de.naivetardis.landscaper.service.ReverseProxyService;
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
    private AuthManager authManager;

    @SneakyCatch(recoverClass = AuthManager.class, recoverMethod = "loginView")
    @RequestMapping("/**")
    public ResponseEntity<String> reverseProxy(@RequestBody(required = false) String body,
                                               HttpMethod method, HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {

        if (!authManager.isAuthenticated(request)) {
            authManager.clearCookies(request);
            authManager.storeWhileWaitingForAuth(body, method, request, response);
            return AuthManager.loginView();
        }

        return authManager.handleRequest(body, method, request, response);

    }

    @SneakyCatch(recoverClass = AuthManager.class, recoverMethod = "loginView")
    @GetMapping("/auth")
    public ResponseEntity<String> auth(@RequestParam("email") String email,
                                       @RequestParam("pswd") String pass,
                                       @RequestParam("code") String code,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {

        return authManager.auth(email, pass, code, request, response);
    }

    @SneakyCatch(recoverClass = AuthManager.class, recoverMethod = "loginView")
    @RequestMapping("/onetime")
    public ResponseEntity<String> onetime(@RequestParam("code") String code,
                                          HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {

        return authManager.authByOneTimeCode(code, request, response);
    }


}
