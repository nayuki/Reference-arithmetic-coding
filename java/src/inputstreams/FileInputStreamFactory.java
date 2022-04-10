package inputstreams;

import java.io.*;

public class FileInputStreamFactory implements InputStreamFactory {

    private final String path;

    public FileInputStreamFactory(String path) {
        this.path = path;
    }

    @Override
    public InputStream getStream() {
        try {
            return new BufferedInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
