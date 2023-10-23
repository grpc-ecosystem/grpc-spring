/*
 * Copyright (c) 2016-2023 The gRPC-Spring Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.devh.boot.grpc.test.util;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

public class DynamicTestCollection implements Iterable<DynamicTest> {

    private final List<DynamicTest> tests = new ArrayList<>();

    public static DynamicTestCollection create() {
        return new DynamicTestCollection();
    }

    private DynamicTestCollection() {}

    @Override
    public Iterator<DynamicTest> iterator() {
        return this.tests.iterator();
    }

    public DynamicTestCollection add(final String name, final Executable executable) {
        this.tests.add(dynamicTest(name, executable));
        return this;
    }

}
