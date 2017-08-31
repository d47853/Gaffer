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
package uk.gov.gchq.gaffer.spark.serialisation.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import uk.gov.gchq.gaffer.core.exception.GafferRuntimeException;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.ToBytesSerialiser;

public abstract class WrappedKryoSerializer<S extends ToBytesSerialiser<T>, T> extends Serializer<T> {
    protected S serialiser;

    public WrappedKryoSerializer(final S serialiser) {
        this.serialiser = serialiser;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final T obj) {
        final byte[] serialised;
        try {
            serialised = serialiser.serialise(obj);
        } catch (final SerialisationException e) {
            throw new GafferRuntimeException("Exception serialising "
                                                     + obj.getClass().getSimpleName()
                                                     + " to a byte array", e);
        }
        output.writeInt(serialised.length);
        output.writeBytes(serialised);
    }

    @Override
    public T read(final Kryo kryo, final Input input, final Class<T> type) {
        final int serialisedLength = input.readInt();
        final byte[] serialised = input.readBytes(serialisedLength);
        try {
            return serialiser.deserialise(serialised);
        } catch (final SerialisationException e) {
            throw new GafferRuntimeException("Exception deserialising "
                                                     + type.getSimpleName()
                                                     + " to a byte array", e);
        }
    }
}
