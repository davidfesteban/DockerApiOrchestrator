package es.misei.dockerapiorchestrator.jobs;

import es.misei.dockerapiorchestrator.repository.impl.GoogleDynDNS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Component
@Slf4j
public class DefinitionListener implements FileChangeListener {

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        for(ChangedFiles cfiles : changeSet) {
            for(ChangedFile cfile: cfiles.getFiles()) {
                if( /* (cfile.getType().equals(Type.MODIFY)
                     || cfile.getType().equals(Type.ADD)
                     || cfile.getType().equals(Type.DELETE) ) && */ !isLocked(cfile.getFile().toPath())) {
                    log.info("Operation: " + cfile.getType()
                            + " On file: "+ cfile.getFile().getName() + " is done");
                }
            }
        }
    }

    private boolean isLocked(Path path) {
        try (FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE); FileLock lock = ch.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            return true;
        }
    }

}
