package com.mateolegi.git;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
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
    private final File localPath;
    private Git git = null;
    private final CredentialsProvider cp;
    private final ProgressMonitor monitor = new TextProgressMonitor(new PrintWriter(System.out));

    public GitManager(File localPath, String user, String password) {
        this.localPath = localPath;
        cp = new UsernamePasswordCredentialsProvider(user, password);
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

    public void cloneRepo(String remotePath) throws GitAPIException {
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

    public String nextVersion(String remotePath) {
        try {
            var actualVersion = getActualVersion(remotePath);
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

    public Map<String, Ref> getListVersions(String remotePath) throws GitAPIException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return getVersionList(remotePath).entrySet().stream()
                    .map(entry -> {
                        try {
                            RevCommit commit = walk.parseCommit(entry.getValue().getObjectId());
                            String newKey = entry.getKey() + " - " + commit.getFullMessage();
                            return new Pair(newKey, entry.getValue());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).collect(Collectors.toUnmodifiableMap(Pair::getKey, Pair::getValue));
        }
    }

    private Map<String, Ref> getVersionList(String remotePath) throws GitAPIException {
        var p = Pattern.compile("\\d+\\.\\d+\\.\\d+",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return listRemoteBranches(remotePath).entrySet().stream()
                .peek(entry -> System.out.println(entry.getKey()))
                .map(entry -> {
                    String branch = entry.getKey().split("refs/heads/")[1];
                    return new Pair(branch, entry.getValue());
                }).filter(pair -> p.matcher(pair.getKey()).matches())
                .collect(Collectors.toUnmodifiableMap(Pair::getKey, Pair::getValue));
    }

    private Map<String, Ref> listRemoteBranches(String remotePath) throws GitAPIException {
        return Git.lsRemoteRepository()
                .setCredentialsProvider(cp)
                .setHeads(true)
                .setTags(false)
                .setRemote(remotePath)
                .callAsMap();
    }

    private String getActualVersion(String remotePath) throws GitAPIException {
        return getVersionList(remotePath).entrySet().stream()
                .map(Pair::toPair)
                .max(Pair::compareTo)
                .map(Pair::getKey)
                .orElseThrow();
    }

    private void requireOpenGitRepo() {
        Objects.requireNonNull(git, "Primero llama openRepo() para abrir el repositorio.");
    }

    static class Pair implements Comparable<Pair> {
        private final String key;
        private Ref value;
        @Contract(pure = true)
        Pair(String key, Ref value) {
            this.key = key;
            this.value = value;
        }
        String getKey() {
            return key;
        }
        Ref getValue() {
            return value;
        }
        @NotNull
        @Contract("_ -> new")
        static Pair toPair(@NotNull Map.Entry<String, Ref> entry) {
            return new Pair(entry.getKey(), entry.getValue());
        }
        @Override
        public int compareTo(@NotNull Pair o) {
            var splitedVersion1 = Stream.of(this.getKey().split("\\."))
                    .map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
            var splitedVersion2 = Stream.of(o.getKey().split("\\."))
                    .map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
            return compareVersions(splitedVersion1, splitedVersion2, 0);
        }
        private static int compareVersions(@NotNull List<Integer> splitedVersion1,
                                           @NotNull List<Integer> splitedVersion2, int pos) {
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
    }
}
