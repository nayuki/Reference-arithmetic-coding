package inputstreams;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamFactory {
	InputStream getStream() throws IOException;
}
