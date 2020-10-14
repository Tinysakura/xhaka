package com.tinysakura.xhaka.common.servlet.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/10/14
 */
public class HttpParser {

    private static final StringManager sm = StringManager.getManager(HttpParser.class);

    private static final Log log = LogFactory.getLog(HttpParser.class);

    private static final int ARRAY_SIZE = 128;

    private static final boolean[] IS_CONTROL = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_SEPARATOR = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_TOKEN = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HEX = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_NOT_REQUEST_TARGET = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HTTP_PROTOCOL = new boolean[ARRAY_SIZE];
    private static final boolean[] REQUEST_TARGET_ALLOW = new boolean[ARRAY_SIZE];

    static {
        String prop = System.getProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow");
        if (prop != null) {
            for (int i = 0; i < prop.length(); i++) {
                char c = prop.charAt(i);
                if (c == '{' || c == '}' || c == '|') {
                    REQUEST_TARGET_ALLOW[c] = true;
                } else {
                    log.warn(sm.getString("httpparser.invalidRequestTargetCharacter",
                            Character.valueOf(c)));
                }
            }
        }

        for (int i = 0; i < ARRAY_SIZE; i++) {
            // Control> 0-31, 127
            if (i < 32 || i == 127) {
                IS_CONTROL[i] = true;
            }

            // Separator
            if (    i == '(' || i == ')' || i == '<' || i == '>'  || i == '@'  ||
                    i == ',' || i == ';' || i == ':' || i == '\\' || i == '\"' ||
                    i == '/' || i == '[' || i == ']' || i == '?'  || i == '='  ||
                    i == '{' || i == '}' || i == ' ' || i == '\t') {
                IS_SEPARATOR[i] = true;
            }

            // Token: Anything 0-127 that is not a control and not a separator
            if (!IS_CONTROL[i] && !IS_SEPARATOR[i] && i < 128) {
                IS_TOKEN[i] = true;
            }

            // Hex: 0-9, a-f, A-F
            if ((i >= '0' && i <='9') || (i >= 'a' && i <= 'f') || (i >= 'A' && i <= 'F')) {
                IS_HEX[i] = true;
            }

            // Not valid for request target.
            // Combination of multiple rules from RFC7230 and RFC 3986. Must be
            // ASCII, no controls plus a few additional characters excluded
            if (IS_CONTROL[i] || i > 127 ||
                    i == ' ' || i == '\"' || i == '#' || i == '<' || i == '>' || i == '\\' ||
                    i == '^' || i == '`'  || i == '{' || i == '|' || i == '}') {
                if (!REQUEST_TARGET_ALLOW[i]) {
                    IS_NOT_REQUEST_TARGET[i] = true;
                }
            }

            // Not valid for HTTP protocol
            // "HTTP/" DIGIT "." DIGIT
            if (i == 'H' || i == 'T' || i == 'P' || i == '/' || i == '.' || (i >= '0' && i <= '9')) {
                IS_HTTP_PROTOCOL[i] = true;
            }
        }
    }


    public static String unquote(String input) {
        if (input == null || input.length() < 2) {
            return input;
        }

        int start;
        int end;

        // Skip surrounding quotes if there are any
        if (input.charAt(0) == '"') {
            start = 1;
            end = input.length() - 1;
        } else {
            start = 0;
            end = input.length();
        }

        StringBuilder result = new StringBuilder();
        for (int i = start ; i < end; i++) {
            char c = input.charAt(i);
            if (input.charAt(i) == '\\') {
                i++;
                result.append(input.charAt(i));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }


    public static boolean isToken(int c) {
        // Fast for correct values, slower for incorrect ones
        try {
            return IS_TOKEN[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isHex(int c) {
        // Fast for correct values, slower for some incorrect ones
        try {
            return IS_HEX[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    public static boolean isNotRequestTarget(int c) {
        // Fast for valid request target characters, slower for some incorrect
        // ones
        try {
            return IS_NOT_REQUEST_TARGET[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return true;
        }
    }


    public static boolean isHttpProtocol(int c) {
        // Fast for valid HTTP protocol characters, slower for some incorrect
        // ones
        try {
            return IS_HTTP_PROTOCOL[c];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }


    // Skip any LWS and return the next char
    static int skipLws(StringReader input, boolean withReset) throws IOException {

        if (withReset) {
            input.mark(1);
        }
        int c = input.read();

        while (c == 32 || c == 9 || c == 10 || c == 13) {
            if (withReset) {
                input.mark(1);
            }
            c = input.read();
        }

        if (withReset) {
            input.reset();
        }
        return c;
    }

    static SkipResult skipConstant(StringReader input, String constant) throws IOException {
        int len = constant.length();

        int c = skipLws(input, false);

        for (int i = 0; i < len; i++) {
            if (i == 0 && c == -1) {
                return SkipResult.EOF;
            }
            if (c != constant.charAt(i)) {
                input.skip(-(i + 1));
                return SkipResult.NOT_FOUND;
            }
            if (i != (len - 1)) {
                c = input.read();
            }
        }
        return SkipResult.FOUND;
    }

    /**
     * @return  the token if one was found, the empty string if no data was
     *          available to read or <code>null</code> if data other than a
     *          token was found
     */
    static String readToken(StringReader input) throws IOException {
        StringBuilder result = new StringBuilder();

        int c = skipLws(input, false);

        while (c != -1 && isToken(c)) {
            result.append((char) c);
            c = input.read();
        }
        // Skip back so non-token character is available for next read
        input.skip(-1);

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     * @return the quoted string if one was found, null if data other than a
     *         quoted string was found or null if the end of data was reached
     *         before the quoted string was terminated
     */
    static String readQuotedString(StringReader input, boolean returnQuoted) throws IOException {

        int c = skipLws(input, false);

        if (c != '"') {
            return null;
        }

        StringBuilder result = new StringBuilder();
        if (returnQuoted) {
            result.append('\"');
        }
        c = input.read();

        while (c != '"') {
            if (c == -1) {
                return null;
            } else if (c == '\\') {
                c = input.read();
                if (returnQuoted) {
                    result.append('\\');
                }
                result.append(c);
            } else {
                result.append((char) c);
            }
            c = input.read();
        }
        if (returnQuoted) {
            result.append('\"');
        }

        return result.toString();
    }

    static String readTokenOrQuotedString(StringReader input, boolean returnQuoted)
            throws IOException {

        // Go back so first non-LWS character is available to be read again
        int c = skipLws(input, true);

        if (c == '"') {
            return readQuotedString(input, returnQuoted);
        } else {
            return readToken(input);
        }
    }

    /**
     * Token can be read unambiguously with or without surrounding quotes so
     * this parsing method for token permits optional surrounding double quotes.
     * This is not defined in any RFC. It is a special case to handle data from
     * buggy clients (known buggy clients for DIGEST auth include Microsoft IE 8
     * &amp; 9, Apple Safari for OSX and iOS) that add quotes to values that
     * should be tokens.
     *
     * @return the token if one was found, null if data other than a token or
     *         quoted token was found or null if the end of data was reached
     *         before a quoted token was terminated
     */
    static String readQuotedToken(StringReader input) throws IOException {

        StringBuilder result = new StringBuilder();
        boolean quoted = false;

        int c = skipLws(input, false);

        if (c == '"') {
            quoted = true;
        } else if (c == -1 || !isToken(c)) {
            return null;
        } else {
            result.append((char) c);
        }
        c = input.read();

        while (c != -1 && isToken(c)) {
            result.append((char) c);
            c = input.read();
        }

        if (quoted) {
            if (c != '"') {
                return null;
            }
        } else {
            // Skip back so non-token character is available for next read
            input.skip(-1);
        }

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     * LHEX can be read unambiguously with or without surrounding quotes so this
     * parsing method for LHEX permits optional surrounding double quotes. Some
     * buggy clients (libwww-perl for DIGEST auth) are known to send quoted LHEX
     * when the specification requires just LHEX.
     *
     * <p>
     * LHEX are, literally, lower-case hexadecimal digits. This implementation
     * allows for upper-case digits as well, converting the returned value to
     * lower-case.
     *
     * @return  the sequence of LHEX (minus any surrounding quotes) if any was
     *          found, or <code>null</code> if data other LHEX was found
     */
    static String readLhex(StringReader input) throws IOException {

        StringBuilder result = new StringBuilder();
        boolean quoted = false;

        int c = skipLws(input, false);

        if (c == '"') {
            quoted = true;
        } else if (c == -1 || !isHex(c)) {
            return null;
        } else {
            if ('A' <= c && c <= 'F') {
                c -= ('A' - 'a');
            }
            result.append((char) c);
        }
        c = input.read();

        while (c != -1 && isHex(c)) {
            if ('A' <= c && c <= 'F') {
                c -= ('A' - 'a');
            }
            result.append((char) c);
            c = input.read();
        }

        if (quoted) {
            if (c != '"') {
                return null;
            }
        } else {
            // Skip back so non-hex character is available for next read
            input.skip(-1);
        }

        if (c != -1 && result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    static double readWeight(StringReader input, char delimiter) throws IOException {
        int c = skipLws(input, false);
        if (c == -1 || c == delimiter) {
            // No q value just whitespace
            return 1;
        } else if (c != 'q') {
            // Malformed. Use quality of zero so it is dropped.
            skipUntil(input, c, delimiter);
            return 0;
        }
        // RFC 7231 does not allow whitespace here but be tolerant
        c = skipLws(input, false);
        if (c != '=') {
            // Malformed. Use quality of zero so it is dropped.
            skipUntil(input, c, delimiter);
            return 0;
        }

        // RFC 7231 does not allow whitespace here but be tolerant
        c = skipLws(input, false);

        // Should be no more than 3 decimal places
        StringBuilder value = new StringBuilder(5);
        int decimalPlacesRead = 0;
        if (c == '0' || c == '1') {
            value.append((char) c);
            c = input.read();
            if (c == '.') {
                value.append('.');
            } else if (c < '0' || c > '9') {
                decimalPlacesRead = 3;
            }
            while (true) {
                c = input.read();
                if (c >= '0' && c <= '9') {
                    if (decimalPlacesRead < 3) {
                        value.append((char) c);
                        decimalPlacesRead++;
                    }
                } else if (c == delimiter || c == 9 || c == 32 || c == -1) {
                    break;
                } else {
                    // Malformed. Use quality of zero so it is dropped and skip until
                    // EOF or the next delimiter
                    skipUntil(input, c, delimiter);
                    return 0;
                }
            }
        } else {
            // Malformed. Use quality of zero so it is dropped and skip until
            // EOF or the next delimiter
            skipUntil(input, c, delimiter);
            return 0;
        }

        double result = Double.parseDouble(value.toString());
        if (result > 1) {
            return 0;
        }
        return result;
    }


    /**
     * Skips all characters until EOF or the specified target is found. Normally
     * used to skip invalid input until the next separator.
     */
    static SkipResult skipUntil(StringReader input, int c, char target) throws IOException {
        while (c != -1 && c != target) {
            c = input.read();
        }
        if (c == -1) {
            return SkipResult.EOF;
        } else {
            return SkipResult.FOUND;
        }
    }
}