package com.mateolegi.zip;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipping {

    /**
     * This method is the entry point for creating a zip file.
     *
     * @param destinationDir: absolute path of the distinaition folder.
     * @param zipName:        destination file name to be zipped e.g. myZipfile.zip
     * @param sourceDirs:     multiple source directories to be zipped.
     * @throws IOException
     */
    public void ZipDirs(String destinationDir, String zipName, List<String> sourceDirs)
            throws IOException {
        File destinationDirFile = new File(destinationDir);
        File zipFile = new File(destinationDir + File.separatorChar + zipName);
        if (!destinationDirFile.exists()) {
            if (!destinationDirFile.mkdirs()) {
                throw new RuntimeException("cannot create directories ");
            }
        } else {
            boolean exists = zipFile.exists();
            if (exists && !zipFile.delete()) {
                throw new RuntimeException("cannot delete existing zip file: " +
                        zipFile.getAbsolutePath());
            } else if (exists) {
                System.out.println("Zip file already exists: " +
                        zipFile.getAbsolutePath());
                return;
            }
        }
        createZip(zipFile, sourceDirs);
    }

    private void createZip(File destination, List<String> sourceDirs) throws IOException {
        try (var out = new ZipOutputStream(new FileOutputStream(destination))) {
            for (String sourceDir : sourceDirs) {
                File sourceDirFile = new File(sourceDir);
                if (!sourceDirFile.exists()) {
                    throw new FileNotFoundException(sourceDir);
                }
                addDirectory(sourceDirFile.getName(), sourceDirFile.getAbsolutePath(), sourceDirFile, out);
            }
        }
    }

    @NotNull
    private String fileToRelativePath(@NotNull File file, @NotNull String baseDir) {
        return file.getAbsolutePath().substring(baseDir.length() + 1);
    }

    private void addFile(String baseDirName, String baseDir, File file, @NotNull final ZipOutputStream out) {
        try (var in = new BufferedInputStream(new FileInputStream(file))) {
            var zipEntry = new ZipEntry(baseDirName + File.separatorChar + fileToRelativePath(file, baseDir));
            var attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            zipEntry.setLastModifiedTime(attr.lastModifiedTime());
            zipEntry.setCreationTime(attr.creationTime());
            zipEntry.setLastAccessTime(attr.lastAccessTime());
            zipEntry.setTime(attr.lastModifiedTime().toMillis());
            zipEntry.setSize(attr.size());
            out.putNextEntry(zipEntry);
            var b = new byte[1024];
            int count;
            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }
            out.closeEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addDirectory(String baseDirName, String baseDir, @NotNull File dirFile, final ZipOutputStream out) {
        var files = Objects.requireNonNullElse(dirFile.listFiles(), new File[0]);
        Stream.of(files).forEach(file -> {
            if (file.isDirectory()) {
                addDirectory(baseDirName, baseDir, file, out);
            } else {
                addFile(baseDirName, baseDir, file, out);
            }
        });
    }
}
