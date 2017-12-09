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

package io.barracks.commons.configuration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedErrorMessage extends ErrorMessage {
    private Map<String, JsonNode> properties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, JsonNode> any() {
        return properties;
    }

    @JsonAnySetter
    public void set(String name, JsonNode value) {
        properties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o) && o instanceof ExtendedErrorMessage) {
            Object this$properties = this.properties;
            Object that$properties = ((ExtendedErrorMessage) o).properties;
            if (this$properties == null) {
                if (that$properties == null) {
                    return true;
                }
            } else {
                return this$properties.equals(that$properties);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result * 59 + (properties == null ? 43 : properties.hashCode());
    }
}