package com.mateolegi.git;

import com.mateolegi.despliegues_audiencias.util.Configuration;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class GitManagerTest {

    private static final Configuration CONFIGURATION = new Configuration();

    @Test
    void openRepo() throws IOException {
        var localPath = new File(CONFIGURATION.getOutputDirectory());
        var localRepo = new File(localPath, ".git");
        if (localRepo.exists()) {
            var git = new GitManager(new File(CONFIGURATION.getOutputDirectory()),
                    CONFIGURATION.getGitUser(), CONFIGURATION.getGitPassword());
            git.openRepo();
            assertTrue(git.isOpen());
        }
    }

    @Test
    void cloneRepo() throws IOException, GitAPIException {
        var tempFolderClone = Files.createTempDirectory("git-clone").toFile();
        var git = new GitManager(tempFolderClone, CONFIGURATION.getGitUser(), CONFIGURATION.getGitPassword());
        git.cloneRepo(CONFIGURATION.getGitRemote());
        var tempGitFolder = new File(tempFolderClone, ".git");
        assertTrue(tempGitFolder.exists());
        assertTrue(tempGitFolder.isDirectory());
        tempFolderClone.deleteOnExit();
    }

    @Test
    void addToRepo() {
    }

    @Test
    void checkoutMaster() {
    }

    @Test
    void createBranch() {
    }

    @Test
    void commitToRepo() {
    }

    @Test
    void pushToRepo() {
    }

    @Test
    void nextVersion() {
    }

    @Test
    void getListVersions() {
    }
}