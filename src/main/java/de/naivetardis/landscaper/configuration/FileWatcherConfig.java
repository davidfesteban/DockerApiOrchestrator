package de.naivetardis.landscaper.configuration;

import de.naivetardis.landscaper.jobs.FileDefinitionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;

@Configuration
@Slf4j
public class FileWatcherConfig {

    private final FileDefinitionListener fileDefinitionListener;
    @Value("${definition.path}")
    private String definitionPath;

    public FileWatcherConfig(FileDefinitionListener fileDefinitionListener) {
        this.fileDefinitionListener = fileDefinitionListener;
    }

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addSourceDirectory(new File(definitionPath));
        fileSystemWatcher.addListener(fileDefinitionListener);
        fileSystemWatcher.start();
        log.info("Started watcher on {}", definitionPath);
        return fileSystemWatcher;
    }

    @PreDestroy
    public void onDestroy() throws Exception {
        fileSystemWatcher().stop();
    }
}

