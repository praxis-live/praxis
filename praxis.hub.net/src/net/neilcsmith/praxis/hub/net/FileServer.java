/*
 * #%
 * Forked from NanoHttpd-Webserver
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * Copyright (C) 2016 Neil C Smith
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.neilcsmith.praxis.hub.net;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class FileServer extends NanoHTTPD {

    static class Info {

        private final int port;

        private Info(int port) {
            this.port = port;
        }

        int getPort() {
            return port;
        }
    }

    /**
     * Default Index file names.
     */
    @SuppressWarnings("serial")
    public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {

        {
            add("index.html");
            add("index.htm");
        }
    };

    private final boolean quiet = true;
    private final File homeDir;
    private final Info info;

    FileServer(int port, File homeDir) {
        super(null, port);
        if (!homeDir.isAbsolute() && !homeDir.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.homeDir = homeDir;
        this.info = new Info(port);
    }

    Info getInfo() {
        return info;
    }
    
    private boolean canServeUri(String uri) {
        File f;
        try {
            f = new File(uri).getCanonicalFile();
            while (f != null) {
                if (f.equals(homeDir)) {
                    return true;
                }
                f = f.getParentFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return false;
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
     * instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if ("/".equals(tok)) {
                newUri += "/";
            } else if (" ".equals(tok)) {
                newUri += "%20";
            } else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return newUri;
    }

    private String findIndexFileInDirectory(File directory) {
        for (String fileName : INDEX_FILE_NAMES) {
            File indexFile = new File(directory, fileName);
            if (indexFile.isFile()) {
                return fileName;
            }
        }
        return null;
    }

    protected Response getForbiddenResponse(String s) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    protected Response getInternalErrorResponse(String s) {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
    }

    protected Response getNotFoundResponse() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }

    protected String listDirectory(String uri, File f) {
        String heading = "Directory " + uri;
        StringBuilder msg
                = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" + "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n"
                        + "// -->\n" + "</style>" + "</head><body><h1>" + heading + "</h1>");

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }

        List<String> files = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir)
                            .append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10000 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }

    public static Response newFixedLengthResponse(IStatus status, String mimeType, String message) {
        Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    private Response respond(Map<String, String> headers, IHTTPSession session, String uri) {
        return defaultRespond(headers, session, uri);
    }

    private Response defaultRespond(Map<String, String> headers, IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        // Prohibit getting out of current directory
        if (uri.contains("../")) {
            return getForbiddenResponse("Won't serve ../ for security reasons.");
        }

        if (!canServeUri(uri)) {
            return getNotFoundResponse();
        }

        // Browsers get confused without '/' after the directory, send a
        // redirect.
        File f = new File(uri);
        if (f.isDirectory() && !uri.endsWith("/")) {
            uri += "/";
            Response res
                    = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }

        if (f.isDirectory()) {
            // First look for index files (index.html, index.htm, etc) and if
            // none found, list the directory if readable.
            String indexFile = findIndexFileInDirectory(f);
            if (indexFile == null) {
                if (f.canRead()) {
                    // No index file, list the directory if it is readable
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f));
                } else {
                    return getForbiddenResponse("No directory listing.");
                }
            } else {
                return respond(headers, session, uri + indexFile);
            }
        }
        String mimeTypeForFile = getMimeTypeForFile(uri);
        Response response = null;
        response = serveFile(uri, headers, f, mimeTypeForFile);
        return response != null ? response : getNotFoundResponse();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();

        if (!this.quiet) {
            System.out.println(session.getMethod() + " '" + uri + "' ");

            Iterator<String> e = header.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
            }
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
            }
        }

//        for (File homeDir : this.rootDirs) {
//            // Make sure we won't die of an exception later
//            if (!homeDir.isDirectory()) {
//                return getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
//            }
//        }
        return respond(Collections.unmodifiableMap(header), session, uri);
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                // return the size of the file
                // 4xx responses are not trumped by if-none-match
                res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                res.addHeader("Content-Range", "bytes */" + fileLen);
                res.addHeader("ETag", etag);
            } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                // full-file-fetch request
                // would return entire file
                // respond with not-modified
                res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                // range request that doesn't match current etag
                // would return entire (different) file
                // respond with not-modified

                res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else {
                // supply the file
                res = newFixedFileResponse(file, mime);
                res.addHeader("Content-Length", "" + fileLen);
                res.addHeader("ETag", etag);
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

}
