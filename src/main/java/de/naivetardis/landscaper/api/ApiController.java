package de.naivetardis.landscaper.api;

import de.naivetardis.landscaper.service.AuthManagerService;
import de.naivetardis.landscaper.utility.AuthUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Semaphore;

@RestController
@AllArgsConstructor
@Slf4j
public class ApiController {
    private final Semaphore semaphore = new Semaphore(1);
    private AuthManagerService authManagerService;

    //@SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    //@RequestMapping("/**")
    //public ResponseEntity<?> reverseProxy(@RequestBody(required = false) String body,
    //                                      HttpMethod method, HttpServletRequest request,
    //                                      HttpServletResponse response) throws IOException, InterruptedException {
    //semaphore.acquire();
    //return authManagerService.handleSingleRequest(body, method, request, response);

    //if (AuthUtils.isPublicSubdomain(request) || authManagerService.isAuthenticated(request)) {
    //    semaphore.release();
    //    return authManagerService.handleSingleRequest(body, method, request, response);
    //}

    //if (authManagerService.userWaitingForAuth(request)) {
    //    return authManagerService.holdAndWaitForAuth(body, method, request, response);
    //}

    //return authManagerService.storeRequestAndShowLoginView(body, method, request, response);

    //}

    @RequestMapping(path = "/**")
    public void proxy(ProxyExchange<byte[]> proxy) throws Exception {
        proxy.forward(proxy.path());
        //return proxy.uri("http://localhost:1234").sensitive("").forward(proxy.path());
    }


    //@SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    @GetMapping("/auth")
    public ResponseEntity<?> auth(@RequestParam("email") String email,
                                  @RequestParam("pswd") String pass,
                                  @RequestParam("code") String code,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {

        if (authManagerService.isAuthSuccessful(email, pass)) {
            authManagerService.storeSingleCodeSession(code);
            return coreAuth(request, response);
        }

        return AuthUtils.loginView();
    }

    //@SneakyCatch(recoverClass = AuthUtils.class, recoverMethod = "loginView")
    @RequestMapping("/onetime")
    public ResponseEntity<?> onetime(@RequestParam("code") String code,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        if (authManagerService.isAuthSuccessful(code)) {
            authManagerService.removeSingleCodeSession(code);
            return coreAuth(request, response);
        }

        return AuthUtils.loginView();
    }

    private ResponseEntity<?> coreAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authManagerService.injectUserToken(request, response);
        ResponseEntity<?> responseEntity = authManagerService.recoverStoredRequest(request, response);

        //authManagerService.unblockPreviousRequests(request, response);

        semaphore.release();
        return responseEntity;
    }
}
