package de.naivetardis.landscaper.jobs;

import de.naivetardis.landscaper.service.DockerOrchestratorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class FileDefinitionListener implements FileChangeListener {

    private DockerOrchestratorService dockerOrchestratorService;

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        changeSet.forEach(changedFiles -> changedFiles.forEach(changedFile -> {
                    if ( /* (cfile.getType().equals(Type.MODIFY)
        || cfile.getType().equals(Type.ADD)
        || cfile.getType().equals(Type.DELETE) ) && */ !isLocked(changedFile.getFile().toPath())) {
                        dockerOrchestratorService.loadDockerDefinition(changedFile);
                        log.info("Operation: " + changedFile.getType()
                                + " On file: " + changedFile.getFile().getName() + " is done");
                    }
                })
        );
    }

    private boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

}
