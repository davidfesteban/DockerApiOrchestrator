package de.naivetardis.landscaper.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AllArgsConstructor
public class ReverseProxyService {

    private DockerOrchestrator dockerOrchestrator;

    public HttpServletResponse handleRequest(HttpServletRequest httpServletRequest) {

    }
}
