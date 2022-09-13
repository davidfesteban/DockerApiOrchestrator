package es.misei.dockerapiorchestrator.configuration;

import es.misei.dockerapiorchestrator.jobs.DefinitionListener;
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

    @Value("${definition.path}")
    private String definitionPath;

    @Bean
    public FileSystemWatcher fileSystemWatcher() {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addSourceDirectory(new File(definitionPath));
        fileSystemWatcher.addListener(new DefinitionListener());
        fileSystemWatcher.start();
        log.info("Started watcher on {}", definitionPath);
        return fileSystemWatcher;
    }

    @PreDestroy
    public void onDestroy() throws Exception {
        fileSystemWatcher().stop();
    }
}

