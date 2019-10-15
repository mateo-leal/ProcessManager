package com.mateolegi.zip;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ZipFile {

    private final File file;
    private OutputStream outputStream;

    public ZipFile(File file) {
        this.file = file;
    }

    public ZipFile setOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public void zip(List<File> sources) throws IOException {
        new Zipping(outputStream).zip(file, sources);
    }

    public void unzip(File destinationFolder) throws IOException {
        new Unzipping(outputStream).unzip(file, destinationFolder);
    }
}