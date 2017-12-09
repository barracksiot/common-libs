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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PublicAddressResolver {

    public static final String IP_RESOLUTION_SERVICE_1_URL = "https://api.ipify.org?format=json";
    public static final String IP_RESOLUTION_SERVICE_2_URL = "http://ipinfo.io";

    public InetAddress resolve() {
        final RestTemplate restTemplate = new RestTemplate();
        PublicAddress address;
        try {
            address = restTemplate.getForObject(IP_RESOLUTION_SERVICE_1_URL, PublicAddress.class);
        } catch (HttpStatusCodeException e) {
            address = restTemplate.getForObject(IP_RESOLUTION_SERVICE_2_URL, PublicAddress.class);
        }
        try {
            return address.toInetAddress();
        } catch (UnknownHostException e) {
            throw new PublicIpResolverException(e);
        }
    }

    public static class PublicAddress {

        private final String ip;

        @JsonCreator
        public PublicAddress(@JsonProperty("ip") String ip) {
            this.ip = ip;
        }

        public InetAddress toInetAddress() throws UnknownHostException {
            return InetAddress.getByName(ip);
        }
    }
}
