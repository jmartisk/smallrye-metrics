/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package io.smallrye.metrics.setup;

import static io.smallrye.metrics.TagsUtils.parseTagsAsArray;

import javax.enterprise.inject.spi.DeploymentException;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import io.smallrye.metrics.OriginAndMetadata;
import io.smallrye.metrics.elementdesc.AnnotationInfo;
import io.smallrye.metrics.elementdesc.BeanInfo;
import io.smallrye.metrics.elementdesc.MemberInfo;
import io.smallrye.metrics.interceptors.MetricResolver;

public class MetricsMetadata {

    private MetricsMetadata() {
    }

    public static void registerMetrics(MetricRegistry registry, MetricResolver resolver, BeanInfo bean, MemberInfo element) {
        MetricResolver.Of<Counted> counted = resolver.counted(bean, element);
        if (counted.isPresent()) {
            AnnotationInfo t = counted.metricAnnotation();
            verifyThereIsOnlyOneSourceOfMetadata(t);
            Metadata metadata = getMetadata(element, counted.metricName(), t.unit(), t.description(), t.displayName(),
                    MetricType.COUNTER, t.reusable(), t.metadataName());
            registry.counter(metadata, parseTagsAsArray(t.tags()));
        }
        MetricResolver.Of<ConcurrentGauge> concurrentGauge = resolver.concurrentGauge(bean, element);
        if (concurrentGauge.isPresent()) {
            AnnotationInfo t = concurrentGauge.metricAnnotation();
            verifyThereIsOnlyOneSourceOfMetadata(t);
            Metadata metadata = getMetadata(element, concurrentGauge.metricName(), t.unit(), t.description(), t.displayName(),
                    MetricType.CONCURRENT_GAUGE, t.reusable(), t.metadataName());
            registry.concurrentGauge(metadata, parseTagsAsArray(t.tags()));
        }
        MetricResolver.Of<Metered> metered = resolver.metered(bean, element);
        if (metered.isPresent()) {
            AnnotationInfo t = metered.metricAnnotation();
            verifyThereIsOnlyOneSourceOfMetadata(t);
            Metadata metadata = getMetadata(element, metered.metricName(), t.unit(), t.description(), t.displayName(),
                    MetricType.METERED, t.reusable(), t.metadataName());
            registry.meter(metadata, parseTagsAsArray(t.tags()));
        }
        MetricResolver.Of<Timed> timed = resolver.timed(bean, element);
        if (timed.isPresent()) {
            AnnotationInfo t = timed.metricAnnotation();
            verifyThereIsOnlyOneSourceOfMetadata(t);
            Metadata metadata = getMetadata(element, timed.metricName(), t.unit(), t.description(), t.displayName(),
                    MetricType.TIMER, t.reusable(), t.metadataName());
            registry.timer(metadata, parseTagsAsArray(t.tags()));
        }
    }

    private static void verifyThereIsOnlyOneSourceOfMetadata(AnnotationInfo t) {
        // TODO: for the DeploymentExceptions, provide more context to the user
        // but this might need adding some new stuff to the AnnotationInfo so that we have something reasonable to provide here
        if (t.metadataName() != null && !t.metadataName().isEmpty()) {
            if (t.name() != null && !t.name().isEmpty()) {
                throw new DeploymentException("Specifying 'name' explicitly is forbidden if NamedMetadata is used. ");
            }
            if (t.absolute()) {
                throw new DeploymentException("Specifying 'absolute' explicitly is forbidden if NamedMetadata is used. ");
            }
            if (t.description() != null && !t.description().isEmpty()) {
                throw new DeploymentException("Specifying 'description' explicitly is forbidden if NamedMetadata is used. ");
            }
            if (t.displayName() != null && !t.displayName().isEmpty()) {
                throw new DeploymentException("Specifying 'displayName' explicitly is forbidden if NamedMetadata is used. ");
            }
            /*
             * TODO: how to (reasonably) detect that a value for the "unit" parameter was explicitly set
             * when it has different default values for different metric types?
             */
        }
    }

    public static Metadata getMetadata(Object origin, String name, String unit, String description, String displayName,
            MetricType type, boolean reusable, String metadataName) {
        Metadata metadata;
        if (metadataName != null && !metadataName.isEmpty()) {
            // named metadata definition is being used
            metadata = NamedMetadataRepository.get(metadataName);
            if (!metadata.getTypeRaw().equals(type)) {
                throw new IllegalStateException("NamedMetadata declares metric type " + metadata.getTypeRaw() + ", but " +
                        "requested metric type is " + type);
            }
            return metadata;
        } else {
            // explicitly specified metadata is being used
            metadata = Metadata.builder().withName(name)
                    .withType(type)
                    .withUnit(unit)
                    .withDescription(description)
                    .withDisplayName(displayName)
                    .reusable(reusable)
                    .build();
        }
        return new OriginAndMetadata(origin, metadata);
    }

}
