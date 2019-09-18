/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.metrics.setup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.metrics.Metadata;

public class NamedMetadataRepository {

    // TODO: should also be keyed by TCCL or something to distinguish applications in an appserver?
    private static final Map<String, Metadata> namedMetadata = new ConcurrentHashMap<>();

    static void add(String name, Metadata metadata) {
        namedMetadata.put(name, metadata);
    }

    public static Metadata get(String name) {
        return namedMetadata.get(name);
    }

    static void clear() {
        namedMetadata.clear();
    }
}
