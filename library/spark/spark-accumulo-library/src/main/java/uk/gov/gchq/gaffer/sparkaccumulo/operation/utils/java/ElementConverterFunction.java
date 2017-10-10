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
package uk.gov.gchq.gaffer.sparkaccumulo.operation.utils.java;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import uk.gov.gchq.gaffer.accumulostore.key.AccumuloElementConverter;
import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.store.schema.Schema;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElementConverterFunction implements PairFlatMapFunction<Element, Key, Value> {
    private static final long serialVersionUID = -3259752069724639102L;
    private transient Schema schema;
    private final byte[] schemaBytes;
    private transient AccumuloElementConverter keyConverter;
    private final Class<? extends AccumuloElementConverter> keyConverterClass;

    public ElementConverterFunction(final Schema schema, final AccumuloElementConverter keyConverter) {
        this.schema = schema;
        schemaBytes = schema.toCompactJson();
        this.keyConverter = keyConverter;
        keyConverterClass = keyConverter.getClass();
    }

    @Override
    public Iterator<Tuple2<Key, Value>> call(final Element element) throws Exception {
        if (null == schema) {
            schema = Schema.fromJson(schemaBytes);
        }
        if (null == keyConverter) {
            try {
                keyConverter = keyConverterClass.getConstructor(Schema.class).newInstance(schema);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to instantiate key converter: " + keyConverterClass.getName(), e);
            }
        }
        final List<Tuple2<Key, Value>> tuples = new ArrayList<>(2);
        final Pair<Key, Key> keys = keyConverter.getKeysFromElement(element);
        final Value value = keyConverter.getValueFromElement(element);
        tuples.add(new Tuple2<>(keys.getFirst(), value));
        final Key second = keys.getSecond();
        if (null != second) {
            tuples.add(new Tuple2<>(second, value));
        }
        return tuples.listIterator();
    }
}
