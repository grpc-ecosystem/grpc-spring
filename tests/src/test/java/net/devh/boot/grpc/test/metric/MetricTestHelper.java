/*
 * Copyright (c) 2016-2019 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.boot.grpc.test.metric;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * A class with helper methods related to testing metrics.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
public final class MetricTestHelper {

    private static final Comparator<Id> METER_ID_NAME_COMPARATOR = comparing(Id::getName);
    private static final Comparator<Id> METER_ID_TYPE_COMPARATOR = comparing(Id::getType, comparing(Enum::name));
    private static final Comparator<Tag> METER_TAG_COMPARATOR =
            comparing(Tag::getKey).thenComparing(comparing(Tag::getValue));
    private static final Comparator<List<Tag>> METER_TAGS_COMPARATOR = (l, r) -> {
        final Iterator<Tag> lit = l.iterator();
        final Iterator<Tag> rit = r.iterator();
        while (lit.hasNext() && rit.hasNext()) {
            final Tag lTag = lit.next();
            final Tag rTag = rit.next();
            final int result = METER_TAG_COMPARATOR.compare(lTag, rTag);
            if (result != 0) {
                return result;
            }
        }
        return l.size() - r.size();
    };
    private static final Comparator<Id> METER_ID_TAGS_COMPARATOR = comparing(Id::getTags, METER_TAGS_COMPARATOR);
    private static final Comparator<Id> METER_ID_COMPARATOR = METER_ID_TYPE_COMPARATOR
            .thenComparing(METER_ID_NAME_COMPARATOR)
            .thenComparing(METER_ID_TAGS_COMPARATOR);
    private static final Comparator<Meter> METER_COMPARATOR = comparing(Meter::getId, METER_ID_COMPARATOR);

    /**
     * Logs a sorted and readable list of meters using the debug level. Useful for debugging.
     *
     * @param meters The meters to be logged.
     */
    public static void logMeters(final Collection<? extends Meter> meters) {
        if (!log.isDebugEnabled()) {
            return;
        }
        // The original collection is usually unmodifiable
        final List<Meter> sortedMeters = new ArrayList<>(meters);
        Collections.sort(sortedMeters, METER_COMPARATOR);

        log.debug("Found meters:");
        for (final Meter meter : sortedMeters) {
            final Id id = meter.getId();
            final String type = id.getType().name();
            final String name = id.getName();
            final Map<String, String> tagMap = new LinkedHashMap<>(); // Tags are already sorted
            for (final Tag tag : id.getTags()) {
                tagMap.put(tag.getKey(), tag.getValue());
            }
            log.debug("- {} {} {}", type, name, tagMap);
        }
    }

}
