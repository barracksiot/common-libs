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

package io.barracks.commons.monitoring;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import io.barracks.commons.util.PublicAddressResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MonitoringConfigurerAdapter extends MetricsConfigurerAdapter {

    @Value("${io.barracks.monitoring.graphite.host}")
    private String graphiteHost;

    @Value("${io.barracks.monitoring.graphite.port}")
    private int graphitePort;

    @Value("${io.barracks.monitoring.metrics.prefix}")
    private String metricsPrefix;

    @Autowired
    private SystemPublicMetrics systemPublicMetrics;
    private MetricRegistry metricRegistry;

    protected GraphiteReporter buildGraphiteReporter(MetricRegistry metricRegistry) {
        final GraphiteSender graphite = new Graphite(new InetSocketAddress(getGraphiteHost(), getGraphitePort()));
        return GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith(getMetricsPrefix())
                .convertRatesTo(TimeUnit.MINUTES)
                .convertDurationsTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
    }

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        registerReporter(buildGraphiteReporter(metricRegistry)).start(1, TimeUnit.MINUTES);
    }

    public MetricRegistry getMetricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
        metricRegistry.register("jvm.garbage-collector", new GarbageCollectorMetricSet());
        return metricRegistry;
    }

    @Scheduled(fixedDelay = 30000)
    public void exportPublicMetrics() {
        for (Metric<?> metric : systemPublicMetrics.metrics()) {
            Counter counter = metricRegistry.counter(metric.getName());
            counter.dec(counter.getCount());
            counter.inc(Double.valueOf(metric.getValue().toString()).longValue());
        }
    }

    public String getGraphiteHost() {
        return graphiteHost;
    }

    public int getGraphitePort() {
        return graphitePort;
    }

    public String getMetricsPrefix() {
        return metricsPrefix
                .replace("{publicAddress}", new PublicAddressResolver().resolve().getHostAddress().replace('.', '_'))
                .replace("{uuid}", UUID.randomUUID().toString().replace("-", ""));
    }

    @Bean
    public FilterRegistrationBean instrumentedFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new InstrumentedFilter());
        registration.addInitParameter("name-prefix", "web");
        return registration;
    }

    @Bean
    public MetricsServletContextListener metricsServletContextListener() {
        return new MetricsServletContextListener();
    }

}
