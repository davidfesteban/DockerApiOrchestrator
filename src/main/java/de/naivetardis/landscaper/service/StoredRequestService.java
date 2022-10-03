package de.naivetardis.landscaper.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class StoredRequestService {

    private final Map<String, Properties> waitingUsers = new HashMap<>();
}
