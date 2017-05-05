/*
 * Copyright 2017 Crown Copyright
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
package uk.gov.gchq.gaffer.store.operation.handler.compare;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.compare.Min;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MinHandlerTest {

    @Test
    public void shouldFindMinBasedOnProperty() throws OperationException, JsonProcessingException {
        // Given
        final Entity entity1 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 1)
                                                   .build();
        final Entity entity2 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 2)
                                                   .build();
        final Entity entity3 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 3)
                                                   .build();
        final Entity entity4 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 1)
                                                   .build();

        final List<Entity> input = Lists.newArrayList(entity1, entity2, entity3, entity4);

        final Min min = new Min.Builder().input(input)
                                                 .propertyName("property")
                                                 .propertyComparator(new PropertyComparator())
                                                 .build();

        final MinHandler handler = new MinHandler();

        // When
        final Element result = handler.doOperation(min, null, null);

        // Then
        assertTrue(result instanceof Entity);
        assertEquals(1, result.getProperty("property"));
    }

    @Test
    public void shouldFindMinBasedOnElement() throws OperationException {
        // Given
        final Entity entity1 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property1", 1)
                                                   .property("property2", 1)
                                                   .build();
        final Entity entity2 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property1", 2)
                                                   .property("property2", 2)
                                                   .build();
        final Entity entity3 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property1", 3)
                                                   .property("property2", 3)
                                                   .build();
        final Entity entity4 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property1", 4)
                                                   .property("property2", 4)
                                                   .build();

        final List<Entity> input = Lists.newArrayList(entity1, entity2, entity3, entity4);

        final Min min = new Min.Builder().input(input)
                                         .elementComparator(new ElementComparator())
                                         .build();

        final MinHandler handler = new MinHandler();

        // When
        final Element result = handler.doOperation(min, null, null);

        // Then
        assertTrue(result instanceof Entity);
        assertEquals(1, result.getProperty("property1"));
        assertEquals(1, result.getProperty("property2"));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionIfIterableIsEmpty() throws OperationException {
        // Given
        final List<Entity> input = Lists.newArrayList();

        final Min min = new Min.Builder().input(input)
                                         .propertyName("property")
                                         .propertyComparator(new PropertyComparator())
                                         .build();

        final MinHandler handler = new MinHandler();

        // When
        final Element result = handler.doOperation(min, null, null);
    }

    @Test
    public void shouldReturnNullIfOperationInputIsNull() throws OperationException {
        // Given
        final Min min = new Min.Builder().build();

        final MinHandler handler = new MinHandler();

        // When
        final Element result = handler.doOperation(min, null, null);

        // Then
        assertNull(result);
    }

    @Test
    public void shouldReturnNullIfBothComparatorsAreNull() throws OperationException {
        // Given
        final List<Entity> input = Lists.newArrayList();

        final Min min = new Min.Builder().input(input)
                                         .build();

        final MinHandler handler = new MinHandler();

        // When
        final Element result = handler.doOperation(min, null, null);

        // Then
        assertNull(result);
    }

    private static class ElementComparator implements Comparator<Element> {
        @Override
        public int compare(final Element o1, final Element o2) {
            final int v1 = (int) o1.getProperty("property1") * (int) o1.getProperty("property2");
            final int v2 = (int) o2.getProperty("property1") * (int) o2.getProperty("property2");
            return v1 - v2;
        }
    }

    private static class PropertyComparator implements Comparator<Object> {
        @Override
        public int compare(final Object o1, final Object o2) {
            return (int) o1 - (int) o2;
        }
    }
}
