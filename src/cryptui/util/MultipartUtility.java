/*
 * Copyright 2017 thomas-hoer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cryptui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Ich
 */
public class MultipartUtility {

    private static final String LINE_FEED = "\r\n";

    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();
    private final String requestURL;

    public MultipartUtility(String requestURL) {
        this.requestURL = requestURL;
    }

    public void addFormField(String name, String value) {
        fields.put(name, value);
    }

    public void addFilePart(String fieldName, File uploadFile) {
        files.put(fieldName, uploadFile);
    }

    public String finish() throws IOException {

        final String boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        final HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        httpConn.setRequestProperty("Test", "Bonjour");
        try (final OutputStream outputStream = httpConn.getOutputStream();
                final PrintWriter writer = new PrintWriter(outputStream);) {
            fields.forEach((name, value) -> {
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
                writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.append(value);
                writer.append(LINE_FEED);
            });
            for (Map.Entry<String, File> file : files.entrySet()) {
                File uploadFile = file.getValue();
                String fileName = uploadFile.getName();
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"" + file.getKey() + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
                writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED).append(LINE_FEED);
                writer.flush();
                try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    outputStream.write(bytes);
                    outputStream.flush();
                }
                writer.append(LINE_FEED).flush();
            }
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                try (InputStream stream = httpConn.getInputStream()) {
                    String result = IOUtils.toString(stream, Charset.defaultCharset());
                    System.out.println(result);
                    httpConn.disconnect();
                    return result;
                }
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        }
    }
}
