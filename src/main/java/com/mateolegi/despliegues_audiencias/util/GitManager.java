package com.mateolegi.despliegues_audiencias.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mateolegi.despliegues_audiencias.util.DeployNumbers.*;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class GitManager {
    private final Configuration CONFIGURATION = new Configuration();
    private final File localPath = new File(CONFIGURATION.getOutputDirectory());
    private final String remotePath = CONFIGURATION.getGitRemote();
    private Git git = null;
    private final CredentialsProvider cp;
    private final ProgressMonitor monitor = new TextProgressMonitor(new PrintWriter(System.out));

    public GitManager() {
        var name = CONFIGURATION.getGitUser();
        var password = CONFIGURATION.getGitPassword();
        cp = new UsernamePasswordCredentialsProvider(name, password);
    }

    public void openRepo() throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(localPath, ".git"))
                .readEnvironment()
                .findGitDir()
                .setMustExist(true)
                .build();
        this.git = new Git(repository);
    }

    public void cloneRepo() throws GitAPIException {
        this.git = Git.cloneRepository()
                .setProgressMonitor(monitor)
                .setURI(remotePath)
                .setBranchesToClone(Collections.singletonList("refs/heads/master"))
                .setBranch("master")
                .setDirectory(localPath)
                .setCredentialsProvider(cp)
                .call();
    }

    public void addToRepo() throws GitAPIException {
        requireOpenGitRepo();
        git.add().addFilepattern(getDeploymentVersion() + ".zip").call();
    }

    public void checkoutMaster() throws GitAPIException {
        requireOpenGitRepo();
        git.checkout()
                .setName("master")
                .setProgressMonitor(monitor)
                .call();
    }

    public void createBranch() throws GitAPIException {
        requireOpenGitRepo();
        git.checkout()
                .setProgressMonitor(monitor)
                .setStartPoint("origin/master")
                .setForceRefUpdate(true)
                .setName(getDeploymentVersion())
                .setCreateBranch(true)
                .call();
    }

    public void commitToRepo() throws GitAPIException {
        requireOpenGitRepo();
        git.commit()
                .setMessage("Backoffice " + getBackofficeVersion()
                        + ", Audiencias " + getAudienciasVersion()
                        + " despliegue " + getDeploymentNumber())
                .call();
    }

    public void pushToRepo() throws GitAPIException {
        requireOpenGitRepo();
        git.push()
                .setProgressMonitor(monitor)
                .setCredentialsProvider(cp)
                .setForce(true)
                .setPushAll()
                .call();
    }

    private Map<String, Ref> listRemoteBranches() throws GitAPIException {
        return Git.lsRemoteRepository()
                .setCredentialsProvider(cp)
                .setHeads(true)
                .setTags(false)
                .setRemote(remotePath)
                .callAsMap();
    }

    public String nextVersion() {
        try {
            var actualVersion = getActualVersion();
            var splitedVersion = Stream.of(actualVersion.split("\\."))
                    .map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
            return Stream.of(splitedVersion.get(0), splitedVersion.get(1), splitedVersion.get(2) + 1)
                    .map(String::valueOf)
                    .collect(Collectors.joining("."));
        } catch (Exception e) {
            Platform.runLater(() -> new Alert(WARNING, "No se detecta conexión con el servidor de Git. " +
                    "Si continúa no podrá realizar el despliegue en el servidor de pruebas.").show());
            return "";
        }
    }

    private String getActualVersion() throws GitAPIException {
        return listRemoteBranches().keySet().stream()
                .map(s -> s.split("refs/heads/")[1])
                .filter(s -> {
                    var p = Pattern.compile("\\d+\\.\\d+\\.\\d+",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    return p.matcher(s).matches();
                })
                .max((o1, o2) -> {
                    var splitedVersion1 = Stream.of(o1.split("\\."))
                            .map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
                    var splitedVersion2 = Stream.of(o2.split("\\."))
                            .map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
                    return compareVersions(splitedVersion1, splitedVersion2, 0);
                }).orElseThrow();
    }

    private int compareVersions(List<Integer> splitedVersion1, List<Integer> splitedVersion2, int pos) {
        int o1 = splitedVersion1.get(pos);
        int o2 = splitedVersion2.get(pos);
        if (o1 > o2) {
            return 1;
        } else if (o1 == o2) {
            return compareVersions(splitedVersion1, splitedVersion2, pos + 1);
        } else {
            return -1;
        }
    }

    private void requireOpenGitRepo() {
        Objects.requireNonNull(git, "Primero llama openRepo() para abrir el repositorio.");
    }
}
