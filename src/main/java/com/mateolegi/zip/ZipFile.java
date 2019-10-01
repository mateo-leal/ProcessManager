package com.mateolegi.zip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ZipFile {

    private static Logger LOGGER = LoggerFactory.getLogger(ZipFile.class);
    private final File destino;
    private OutputStream outputStream;

    public ZipFile(File destino) {
        this.destino = destino;
    }

    public void setOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }


}