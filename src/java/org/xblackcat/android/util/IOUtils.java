package org.xblackcat.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public final class IOUtils {
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private final static Pattern SIZE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([kmgt])?b?");

    private IOUtils() {
    }

    public static Bitmap loadImage(String imageUrl) throws IOException {
        return loadImage(imageUrl, 1);
    }

    public static Bitmap loadImage(String imageUrl, int sampleSize) throws IOException {
        URL url = new URL(imageUrl);
        InputStream in = url.openStream();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeStream(in, null, options);
        } finally {
            in.close();
        }
    }

    public static InputStream getInputStream(String sourceUrl) throws IOException {
        return getInputStream(sourceUrl, true);
    }

    public static InputStream getInputStream(String sourceUrl, boolean checkGZippedUrl) throws IOException {
        boolean isGZip = false;
        // Check compressed version first

        InputStream stream = null;
        if (checkGZippedUrl) {
            try {
                Log.d("OpenInputStream", "Try to load a compressed version of " + sourceUrl);
                URL documentUrl = new URL(sourceUrl + ".gz");
                URLConnection conn = documentUrl.openConnection();
                stream = conn.getInputStream();
                isGZip = true;
            } catch (IOException e) {
                stream = null;
            }
        }

        if (stream == null) {
            Log.d("OpenInputStream", "Try to load a plain version of " + sourceUrl);
            URL documentUrl = new URL(sourceUrl);
            URLConnection conn = documentUrl.openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            stream = conn.getInputStream();
            isGZip = "gzip".equals(conn.getContentEncoding());
        }

        if (isGZip) {
            Log.i("OpenInputStream", "Use GZip stream for url " + sourceUrl);
            stream = new GZIPInputStream(stream);
        }
        return stream;
    }

    public static Document loadXML(String sourceUrl) throws IOException, ParserConfigurationException, SAXException {
        InputStream stream = getInputStream(sourceUrl);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try {
            long start = System.currentTimeMillis();
            final Document document = builder.parse(stream);
            Log.i("LoadXML", "XML is loaded in " + (System.currentTimeMillis() - start) + " ms. Url: " + sourceUrl);
            return document;
        } finally {
            stream.close();
        }
    }

    public static String toString(InputStream stream) throws IOException {
        final ByteArrayOutputStream target = new ByteArrayOutputStream();
        InputStream source;
        if (stream instanceof BufferedInputStream) {
            source = stream;
        } else {
            source = new BufferedInputStream(stream);
        }

        try {
            int b;
            while ((b = source.read()) != -1) {
                target.write(b);
            }
        } finally {
            target.close();
        }

        return target.toString("UTF-8");
    }

    public static JSONObject loadJSOObject(String sourceUrl) throws IOException, JSONException {
        final InputStream inputStream = getInputStream(sourceUrl);
        try {
            return new JSONObject(toString(inputStream));
        } finally {
            inputStream.close();
        }
    }

    public static JSONArray loadJSONArray(String sourceUrl) throws IOException, JSONException {
        final InputStream inputStream = getInputStream(sourceUrl);
        try {
            return new JSONArray(toString(inputStream));
        } finally {
            inputStream.close();
        }
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p/>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 1.3
     */
    public static long copy(InputStream input, OutputStream output)
            throws IOException {
        return copy(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p/>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p/>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @param buffer the buffer to use for the copy
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.2
     */
    public static long copy(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long parseSize(String sizeStr) {
        final Matcher m = SIZE_PATTERN.matcher(sizeStr.toLowerCase());
        if (m.matches()) {
            String size = m.group(1);
            String modifier = m.group(2);

            double v;
            try {
                v = Double.parseDouble(size);
            } catch (NumberFormatException e) {
                return -1;
            }

            if (modifier != null && modifier.length() != 0) {
                switch (modifier.charAt(0)) {
                    case 't':
                        v *= 1024;
                    case 'g':
                        v *= 1024;
                    case 'm':
                        v *= 1024;
                    case 'k':
                        v *= 1024;
                }
            }

            return (long) v;
        }

        return -1;
    }

    public static String convertMicrosecondsToDuration(long milliseconds) {
        //because api<9 http://stackoverflow.com/a/625624/1788598
        milliseconds /= 1000;
        int seconds = (int) (milliseconds % 60);
        milliseconds /= 60;
        int minutes = (int) (milliseconds % 60);
        milliseconds /= 60;
        int hours = (int) (milliseconds % 24);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
