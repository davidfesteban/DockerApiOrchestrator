package de.naivetardis.landscaper.configuration;

import de.naivetardis.landscaper.jobs.DefinitionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.devtools.filewatch.FileSystemWatcher;

//import javax.annotation.PreDestroy;
import java.io.File;

@Slf4j
public class FileWatcherConfig {

    @Value("${definition.path}")
    private String definitionPath;

/*
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addSourceDirectory(new File(definitionPath));
        fileSystemWatcher.addListener(new DefinitionListener());
        //fileSystemWatcher.start();
        log.info("Started watcher on {}", definitionPath);
        return fileSystemWatcher;
    } */

    //@PreDestroy
    public void onDestroy() throws Exception {
        //fileSystemWatcher().stop();
    }
}

