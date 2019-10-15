package com.mateolegi.zip;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Unzipping {

    private Writer writer;

    Unzipping(OutputStream stream) {
        writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
    }

    void unzip(File zipFile, @NotNull File destinationFolder) throws IOException {
        if (destinationFolder.isFile()) {
            throw new IOException("destionationFolder cannot be a file.");
        }
        var buffer = new byte[1024];
        var zis = new ZipInputStream(new FileInputStream(zipFile));
        var zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            var newFile = newFile(destinationFolder, zipEntry);
            var fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    @NotNull
    private File newFile(File destinationDir, @NotNull ZipEntry zipEntry) throws IOException {
        if (writer != null) {
            writer.write("Unzipping " + zipEntry.getName() + ".\n");
        }
        var destFile = new File(destinationDir, zipEntry.getName());
        var destDirPath = destinationDir.getCanonicalPath();
        var destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
