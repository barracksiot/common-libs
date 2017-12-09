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

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EndpointTest {
    private static final String host = "not.barracks.io";
    private static final String baseUrl = "https://" + host;

    @Test
    public void getURI_whenNoElements_shouldUrlWithSamePathAndQuery() {
        // Given
        final String path = "/test/action";
        final String query = "someKey=someValue";
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, path, query);

        // When
        final URI result = endpoint.withBase(baseUrl).getURI();

        // Then
        assertThat(result).satisfies(uri -> assertThat(uri.getRawPath()).isEqualTo(path))
                .satisfies(uri -> assertThat(uri.getRawQuery()).isEqualTo(query));
    }

    @Test
    public void getURI_shouldKeepEndOfURL() {
        // Given
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, "/a/{path}/withEndingValue");

        // When
        final URI result = endpoint.withBase(baseUrl).getURI("path");

        // Then
        assertThat(result)
                .satisfies(uri -> assertThat(uri.getRawPath()).isEqualTo("/a/path/withEndingValue"));
    }

    @Test
    public void getURI_shouldReplacePlaceholders_whenArgumentsArePassed() {
        // Given
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, "/plop/{arg1}/{arg2}");
        final String arg1 = "1/abc";
        final int arg2 = 2;

        // When
        final URI result = endpoint.withBase(baseUrl).getURI(arg1, arg2);

        // Then
        assertThat(result)
                .hasHost(host)
                .satisfies(uri -> assertThat(uri.getRawPath()).isEqualTo("/plop/1%2Fabc/2"));
    }

    @Test
    public void getURI_shouldReplacePlaceholdersAndIncludePaginationParameters_whenArgumentsArePassedWithPageableAndPathDoesntIncludeQueryParams() {
        // Given
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, "/plop/{arg1}/{arg2}");
        final String arg1 = "1";
        final int arg2 = 2;
        final Pageable pageable = new PageRequest(0, 10);

        // When
        final URI result = endpoint.withBase(baseUrl).pageable(pageable).getURI(arg1, arg2);

        // Then
        assertThat(result)
                .hasHost(host)
                .satisfies(uri -> assertThat(uri.getRawPath()).isEqualTo("/plop/1/2"))
                .hasQuery("page=0&size=10");
    }

    @Test
    public void getURI_shouldReplacePlaceholdersAndIncludePaginationParameters_whenArgumentsArePassedWithPageableAndPathIncludesQueryParams() {
        // Given
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, "/plop", "arg1={arg1}");
        final String arg1 = "/%1";
        final Pageable pageable = new PageRequest(0, 10);

        // When
        final URI pathWithArguments = endpoint.withBase(baseUrl).pageable(pageable).getURI(arg1);

        // Then
        assertThat(pathWithArguments)
                .hasHost(host)
                .hasPath("/plop")
                .satisfies(uri -> assertThat(uri.getRawQuery()).isEqualTo("arg1=%2F%251&page=0&size=10"));
    }

    @Test
    public void getURI_shouldThrowException_whenMissingArg() {
        // Given
        final Endpoint endpoint = Endpoint.from(HttpMethod.GET, "/plop", "arg1={arg1}");

        // Then When
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> endpoint.withBase(baseUrl).getURI());
    }
}