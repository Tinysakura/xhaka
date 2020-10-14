package com.tinysakura.xhaka.common.servlet.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/10/14
 */
public class AcceptLanguage {

    private final Locale locale;
    private final double quality;

    protected AcceptLanguage(Locale locale, double quality) {
        this.locale = locale;
        this.quality = quality;
    }

    public Locale getLocale() {
        return locale;
    }

    public double getQuality() {
        return quality;
    }


    public static List<AcceptLanguage> parse(StringReader input) throws IOException {

        List<AcceptLanguage> result = new ArrayList<>();

        do {
            // Token is broader than what is permitted in a language tag
            // (alphanumeric + '-') but any invalid values that slip through
            // will be caught later
            String languageTag = HttpParser.readToken(input);
            if (languageTag == null) {
                // Invalid tag, skip to the next one
                HttpParser.skipUntil(input, 0, ',');
                continue;
            }

            if (languageTag.length() == 0) {
                // No more data to read
                break;
            }

            // See if a quality has been provided
            double quality = 1;
            SkipResult lookForSemiColon = HttpParser.skipConstant(input, ";");
            if (lookForSemiColon == SkipResult.FOUND) {
                quality = HttpParser.readWeight(input, ',');
            }

            if (quality > 0) {
                result.add(new AcceptLanguage(Locale.forLanguageTag(languageTag), quality));
            }
        } while (true);

        return result;
    }
}

