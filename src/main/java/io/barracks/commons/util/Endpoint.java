/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.commons.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Endpoint {
    private final HttpMethod method;
    private final String path;
    private final String query;

    private Endpoint(HttpMethod method, String path) {
        this(method, path, null);
    }

    private Endpoint(HttpMethod method, String path, String query) {
        this.method = method;
        this.path = path;
        this.query = query;
    }

    public static Endpoint from(HttpMethod method, String path) {
        return new Endpoint(method, path);
    }

    public static Endpoint from(HttpMethod method, String path, String query) {
        return new Endpoint(method, path, query);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getQuery() {
        return query;
    }

    public String getPath() {
        return path;
    }

    public <T> EntityBuilder<T> withBase(String base) {
        return new EntityBuilder<>(base);
    }

    public final class EntityBuilder<T> {
        private final String base;
        private T body;
        private Pageable pageable;
        private HttpHeaders headers;
        private MultiValueMap<String, String> queryParams;

        private EntityBuilder(String baseUrl) {
            this.base = baseUrl;
        }

        public EntityBuilder body(T body) {
            this.body = body;
            return this;
        }

        public EntityBuilder pageable(Pageable pageable) {
            this.pageable = pageable;
            return this;
        }

        public EntityBuilder headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public EntityBuilder queryParams(MultiValueMap<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public RequestEntity<T> getRequestEntity(Object... args) {
            return new RequestEntity<>(body, headers, method, getURI(args));
        }

        public URI getURI(Object... args) {
            return UriComponentsBuilder.fromHttpUrl(expandUrl(args))
                    .queryParams(serializePageable())
                    .queryParams(serializeQueryParams())
                    .build(true)
                    .toUri();
        }


        private String expandUrl(Object... args) {
            final String preFormatted = base + (path == null ? "" : path) + (query == null ? "" : "?" + query);
            final Matcher matcher = Pattern.compile("(\\{\\p{Alnum}*})").matcher(preFormatted);
            List<String> elements = new ArrayList<>();
            int searchIdx = 0;
            while (matcher.find(searchIdx) && matcher.group(1) != null) {
                elements.add(preFormatted.substring(searchIdx, matcher.start()));
                elements.add(null);
                searchIdx = matcher.end();
            }
            if (searchIdx == preFormatted.length()) {
                elements.add("");
            } else {
                elements.add(preFormatted.substring(searchIdx));
            }
            if (elements.isEmpty()) {
                elements.add(preFormatted);
            }
            if (args.length != (elements.size() - 1) / 2) {
                System.out.println(elements);
                throw new IllegalArgumentException(
                        "Failed format '" + preFormatted + "', " +
                                "expected " + ((elements.size() - 1) / 2) + " arguments, " +
                                "" + args.length + " provided"
                );
            }
            Iterator<String> iterator = Arrays.asList(serializeArgs(args)).iterator();
            return elements.stream()
                    .map(element ->
                            element == null ? iterator.next() : element
                    )
                    .collect(Collectors.joining());
        }

        private String[] serializeArgs(Object... args) {
            return Stream.of(args)
                    .map(object -> object == null ? "" : urlEncode(object.toString()))
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
        }

        private MultiValueMap<String, String> serializePageable() {
            final MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
            if (pageable != null) {
                result.set("page", String.valueOf(pageable.getPageNumber()));
                result.set("size", String.valueOf(pageable.getPageSize()));
                if (pageable.getSort() != null) {
                    for (Sort.Order order : pageable.getSort()) {
                        final String sortValue = String.join(",", urlEncode(order.getProperty()), order.getDirection().name().toLowerCase(Locale.US));
                        result.add("sort", sortValue);
                    }
                }
            }
            return result;
        }

        private MultiValueMap<String, String> serializeQueryParams() {
            if (this.queryParams == null)
                return null;
            return new LinkedMultiValueMap<>(
                    queryParams.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> urlEncode(entry.getKey()),
                                    entry -> entry.getValue().stream()
                                            .map(this::urlEncode)
                                            .collect(Collectors.toList())
                            ))
            );
        }

        private String urlEncode(String str) {
            try {
                if (str != null) {
                    return URLEncoder.encode(str, UTF_8.name()).replaceAll("\\+", "%20");
                }
                return "";
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Should not happen", e);
            }
        }
    }
}