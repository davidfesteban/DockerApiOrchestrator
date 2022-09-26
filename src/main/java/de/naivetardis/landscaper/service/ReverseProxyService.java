package de.naivetardis.landscaper.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class ReverseProxyService {

    private DockerOrchestrator dockerOrchestrator;

    private AuthManager authManager;

    private final Set<String> tokens = new HashSet<>();


    public ResponseEntity<String> handleAccess(String body,
                                               HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return new ResponseEntity<>(Files.readString(Path.of("src/main/resources/web/login.html"), Charset.defaultCharset()), HttpStatus.OK);
    }

}
