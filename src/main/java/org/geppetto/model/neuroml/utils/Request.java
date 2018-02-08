package org.geppetto.model.neuroml.utils;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.geppetto.core.utilities.URLReader;

public class Request implements Callable<InputStream> {

    private URL url;

    public Request(URL url) {
        this.url = url;
    }

    @Override
    public InputStream call() throws Exception {
        return new ByteArrayInputStream(URLReader.readStringFromURL(url).getBytes(StandardCharsets.UTF_8));
    }

}
