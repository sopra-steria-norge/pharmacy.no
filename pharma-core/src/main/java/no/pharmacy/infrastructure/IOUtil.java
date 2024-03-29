package no.pharmacy.infrastructure;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.rest.RestException;
import no.pharmacy.infrastructure.rest.RestHttpException;
import no.pharmacy.infrastructure.rest.RestHttpNotFoundException;
import no.pharmacy.infrastructure.rest.RestInvalidUserException;

public class IOUtil {

    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    public static void post(String content, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        try (OutputStream output = connection.getOutputStream()) {
            copy(content, output);
        }

        checkResponse(connection);
    }

    public static String toString(URLConnection connection) throws IOException {
        try (Reader reader = IOUtil.createReader(connection)) {
            return IOUtil.toString(reader);
        }
    }

    public static String toString(InputStream in) throws IOException {
        return toString(in, StandardCharsets.UTF_8);
    }

    public static String toString(InputStream in, Charset charset) throws IOException {
        return toString(new InputStreamReader(in, charset));
    }

    public static String toString(Reader reader) throws IOException {
        try {
            char[] arr = new char[8 * 1024];
            StringBuilder buffer = new StringBuilder();
            int numCharsRead;
            while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }
            return buffer.toString();
        } finally {
            reader.close();
        }
    }

    public static void copy(String content, HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);
        copy(content, connection.getOutputStream());
        checkResponse(connection);
    }

    public static void copy(String content, OutputStream out) throws IOException {
        copy(content, out, StandardCharsets.UTF_8);
    }

    public static void copy(String content, OutputStream out, Charset charset) throws IOException {
        copy(content, new OutputStreamWriter(out, charset));
    }

    public static void copy(URL url, File file, File tempDir) throws IOException {
        File dir = file.getParentFile();
        ensureDirectory(dir);
        if (!file.exists() || file.length() == 0) {
            ensureDirectory(tempDir);
            File tempFile = new File(tempDir, file.getName());
            logger.debug("Downloading {} to temporary file {}", url, tempFile);
            try (InputStream input = url.openStream()) {
                try (FileOutputStream output = new FileOutputStream(tempFile)) {
                    copy(input, output);
                }
                logger.debug("Moving temporary file {} to {}", tempFile, file);
                tempFile.renameTo(file);
            } finally {
                tempFile.delete();
            }
        } else {
            logger.debug("{} already downloaded", file);
        }
        if (!file.isFile() || !file.canRead()) {
            throw new RuntimeException("Failed to create readable file " + url);
        }
    }

    private static void ensureDirectory(File dir) throws IOException {
        if (!dir.isDirectory() &&  !dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    public static void copy(List<String> lines, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            copy(lines, writer);
        }
    }

    public static void copy(List<String> lines, Writer writer) throws IOException {
        for (String string : lines) {
            writer.write(string);
            writer.write("\n");
        }
    }

    public static void copy(InputStream inputStream, File file) throws IOException {
        try (OutputStream output = new FileOutputStream(file)) {
            copy(inputStream, output);
        }
    }

    public static void copy(String textContent, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            copy(textContent, writer);
        }
    }

    public static void copy(String content, Writer writer) throws IOException {
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024*1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
    }

    public static int copy(Reader reader, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            return copy(reader, writer);
        }
    }

    public static int copyLines(Reader input, File dataFile) throws IOException {
        try (FileWriter writer = new FileWriter(dataFile)) {
            return copyLines(input, writer);
        }
    }

    public static int copyLines(Reader input, Writer writer) throws IOException {
        int lines = 0;
        char[] buf = new char[1024];
        int count = 0;
        while ((count = input.read(buf)) >= 0) {
            writer.write(buf, 0, count);
            for (int i=0; i<count; i++) {
                if (buf[i] == '\n') lines++;
            }
        }
        return lines;
    }

    public static int copy(Reader in, Writer out) throws IOException {
        int total = 0;
        char[] buf = new char[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
            total += count;
        }
        return total;
    }

    public static InputStreamReader openResourceReader(String file) {
        return new InputStreamReader(IOUtil.class.getResourceAsStream(file), StandardCharsets.ISO_8859_1);
    }

    public static Reader createReader(URLConnection connection) throws IOException {
        return new InputStreamReader(checkResponse(connection).getInputStream(), getCharset(connection));
    }

    private static Charset getCharset(URLConnection connection) {
        String contentType = connection.getHeaderField("Content-Type");
        if (contentType != null && contentType.contains(";charset=")) {
            String charset = contentType.substring(contentType.indexOf(";charset=") + ";charset=".length());
            return Charset.forName(charset);
        }
        return StandardCharsets.UTF_8;
    }

    public static List<String> readLines(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static HttpURLConnection checkResponse(URLConnection connection) throws IOException, RestException {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        int responseCode = httpConnection.getResponseCode();

        if (responseCode == 401 || responseCode == 403) {
            throw new RestInvalidUserException(httpConnection);
        }
        if (responseCode == 404) {
            throw new RestHttpNotFoundException(httpConnection);
        }
        if (responseCode >= 400) {
            throw new RestHttpException(httpConnection);
        }
        return httpConnection;
    }

    public static String base64String(String string) {
        return string != null ? base64String(string.getBytes()) : null;
    }

    public static String base64String(byte[] bytes) {
        return bytes != null ? Base64.getEncoder().encodeToString(bytes) : null;
    }

    public static byte[] base64Decode(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    public static InputStream zipEntry(ZipInputStream zip, String fileName) throws IOException {
        ZipEntry zipEntry;
        while((zipEntry = zip.getNextEntry()) != null) {
            if (zipEntry.getName().equals(fileName)) {
                return zip;
            }
        }
        throw new IllegalArgumentException("Can't find " + fileName + " in " + zip);
    }

    public static URL url(String string) {
        try {
            return new URL(string);
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public static InputStream dontClose(InputStream input) {
        return new FilterInputStream(input) {
            @Override
            public void close() throws IOException {
            }
        };
    }

    public static InputStream resource(String resourcePath) throws IOException {
        InputStream resourceStream = IOUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new IllegalArgumentException("Not found: " + resourcePath);
        }
        if (resourcePath.endsWith(".gz")) {
            return new GZIPInputStream(resourceStream);
        }
        return resourceStream;
    }

    public static File extract(ZipFile zipFile, ZipEntry entry, File dir) {
        File target = new File(dir, entry.getName());
        if (target.lastModified() > new File(zipFile.getName()).lastModified()) {
            logger.debug("Not extracting {} - already newer than {}", target, zipFile.getName());
            return target;
        }
        try {
            ensureDirectory(dir);
            logger.debug("Extracting {}!{} to {}", zipFile.getName(), entry, target);
            copy(zipFile.getInputStream(entry), target);
            return target;
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public static ByteArrayInputStream asInputStream(String key) {
        return new ByteArrayInputStream(Base64.getMimeDecoder().decode(key));
    }

}