package no.pharmacy.infrastructure;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import no.pharmacy.infrastructure.rest.RestException;
import no.pharmacy.infrastructure.rest.RestHttpException;
import no.pharmacy.infrastructure.rest.RestHttpNotFoundException;
import no.pharmacy.infrastructure.rest.RestInvalidUserException;

public class IOUtil {

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

    public static void copy(URL url, File file) throws IOException {
        if (!file.exists() || file.length() == 0) {
            try (InputStream input = url.openStream()) {
                try (FileOutputStream output = new FileOutputStream(file)) {
                    copy(input, output);
                }
            }
        }
        if (!file.isFile() || !file.canRead()) {
            throw new RuntimeException("Failed to create readable file " + url);
        }
    }

    public static void copy(File source, File target) throws IOException {
        try (FileInputStream input = new FileInputStream(source)) {
            try (FileOutputStream output = new FileOutputStream(target)) {
                copy(input, output);
            }
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

    public static void copy(byte[] buf, File file) throws IOException {
        copy(new ByteArrayInputStream(buf), file);
    }

    public static void copy(InputStream input, File file) throws IOException {
        try (OutputStream output = new FileOutputStream(file)) {
            copy(input, output);
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

    public static Map<String, List<String>> parseQuery(String query) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
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

    public static byte[] toByteArray(File file) throws IOException {
        if (file == null) {
            return null;
        }
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            try (FileInputStream input = new FileInputStream(file)) {
                copy(input, buffer);
                return buffer.toByteArray();
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            copy(input, buffer);
            return buffer.toByteArray();
        }
    }

    public static byte[] toByteArray(URLConnection connection) throws IOException {
        try (InputStream input = IOUtil.checkResponse(connection).getInputStream()) {
            return toByteArray(input);
        }
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
}