package uk.ac.susx.tag.util;

import static com.google.common.base.Preconditions.*;

import com.google.common.io.Flushables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 20/02/2013
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
public class BytePusher extends AbstractExecutionThreadService {

    private final int bufferSize = 8192;
    private final InputStream source;
    private final OutputStream sink;

    public BytePusher(InputStream source, OutputStream sink) {
        this.source = checkNotNull(source, "source");
        this.sink = checkNotNull(sink, "sink");
    }

    @Override
    protected void run() throws Exception {
        try {
            final byte[] buffer = new byte[bufferSize];
            int nBytesRead;
            while (-1 != (nBytesRead = source.read(buffer))) {
                sink.write(buffer, 0, nBytesRead);
            }
        } finally {
            Flushables.flushQuietly(sink);
        }
    }


    public static ListenableFuture<State> push(final InputStream source, final OutputStream sink) {
        final BytePusher pusher = new BytePusher(source, sink);
        return pusher.start();
    }


    public static InputStream wrap(final InputStream source) throws IOException {

        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream();
        in.connect(out);

        push(source, out);

        return in;
    }
}
