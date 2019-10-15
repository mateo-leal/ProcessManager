package com.mateolegi.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EmitterOutputStream extends OutputStream {

    private static final PrintStream out = System.out;
    private static List<Consumer<String>> consumers = new ArrayList<>();
    private final BidirectionalStream bidirectionalStream;

    public EmitterOutputStream() throws IOException {
        bidirectionalStream = new BidirectionalStream();
        emittingDaemon();
    }

    /**
     * Writes the specified <code>byte</code> to the piped output stream.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param      b   the <code>byte</code> to be written.
     * @exception IOException if the pipe is <a href=#BROKEN> broken</a>,
     *          {@link java.io.PipedOutputStream#connect(java.io.PipedInputStream) unconnected},
     *          closed, or if an I/O error occurs.
     */
    @Override
    public void write(int b)  throws IOException {
        bidirectionalStream.getOutputStream().write(b);
        out.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this piped output stream.
     * This method blocks until all the bytes are written to the output
     * stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception IOException if the pipe is <a href=#BROKEN> broken</a>,
     *          {@link java.io.PipedOutputStream#connect(java.io.PipedInputStream) unconnected},
     *          closed, or if an I/O error occurs.
     */
    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        bidirectionalStream.getOutputStream().write(b, off, len);
        out.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out.
     * This will notify any readers that bytes are waiting in the pipe.
     *
     * @exception IOException if an I/O error occurs.
     */
    public synchronized void flush() throws IOException {
        bidirectionalStream.getOutputStream().flush();
    }

    public static void on(Consumer<String> then) {
        consumers.add(then);
    }

    private void emittingDaemon() {
        var inputStream = bidirectionalStream.getInputStream();
        new Thread(() -> {
            while (true) {
                synchronized (inputStream) {
                    try {
                        emit((char) inputStream.read());
                    } catch (IOException ignored) {
                    }
                }
            }
        }).start();
    }

    private void emit(char b) {
        consumers.forEach(callback -> {
            try {
                callback.accept(String.valueOf(b));
            } catch (Exception ignored) { }
        });
    }
}
