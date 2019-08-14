/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.code;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.praxislive.code.userapi.Ref;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Value;
import org.praxislive.core.services.TaskService;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PReference;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
class RefImpl<T> extends Ref<T> {

    private final Class<?> refType;
    private final Set<Integer> activeCalls;

    private CodeContext<?> context;
    private ReferenceDescriptor desc;

    private RefImpl(Class<?> refType) {
        this.refType = refType;
        this.activeCalls = new HashSet<>(1);
    }

    private void attach(CodeContext<?> context, ReferenceDescriptor desc) {
        this.context = context;
        this.desc = desc;
    }

    @Override
    public <K> Ref<T> asyncCompute(K key, Function<K, ? extends T> function) {
        ControlAddress to = context.locateService(TaskService.class)
                .map(ad -> ControlAddress.of(ad, TaskService.SUBMIT))
                .orElseThrow(IllegalStateException::new);
        ControlAddress from = ControlAddress.of(context.getComponent().getAddress(), desc.getID());
        TaskService.Task task = () -> PReference.of(function.apply(key));
        Call call = Call.create(to, from, context.getTime(), PReference.of(task));
        context.getLookup().find(PacketRouter.class)
                .orElseThrow(IllegalStateException::new)
                .route(call);
        activeCalls.add(call.matchID());
        return this;
    }

    private void handleAsyncResponse(Call call) {
        if (activeCalls.remove(call.matchID())) {
            if (call.isReply()) {
                try {
                    T val = (T) PReference.from(call.getArgs().get(0)).get().getReference();
                    if (!refType.isInstance(val)) {
                        throw new IllegalArgumentException(val.getClass() + " is not a " + refType);
                    }
                    init(() -> val).compute(v -> val);
                } catch (Exception ex) {
                    context.getLog().log(LogLevel.ERROR, ex);
                }
            } else {
                List<Value> args = call.args();
                if (args.isEmpty()) {
                    context.getLog().log(LogLevel.ERROR, "Error in asyncCompute on "
                            + call.to().controlID());
                } else {
                    PError err = PError.from(args.get(0)).orElse(PError.of(args.get(0).toString()));
                    context.getLog().log(LogLevel.ERROR, err);
                }
            }
        }

    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void dispose() {
        super.dispose();
        activeCalls.clear();
    }

    @Override
    protected void log(Exception ex) {
        context.getLog().log(LogLevel.ERROR, ex);
    }

    static class Descriptor extends ReferenceDescriptor {

        private final Field refField;
        private final Class<?> refType;
        private final ControlImpl control;
        private RefImpl<?> ref;

        private Descriptor(CodeConnector<?> connector, String id, Field refField, Class<?> refType) {
            super(id);
            this.refField = refField;
            this.refType = refType;
            this.control = new ControlImpl(connector, id, this);
        }

        @Override
        public void attach(CodeContext<?> context, ReferenceDescriptor previous) {
            if (previous instanceof RefImpl.Descriptor) {
                RefImpl.Descriptor pd = (RefImpl.Descriptor) previous;
                if (isCompatible(pd)) {
                    ref = pd.ref;
                    pd.ref = null;
                } else {
                    pd.dispose();
                }
            } else if (previous != null) {
                previous.dispose();
            }

            if (ref == null) {
                ref = new RefImpl<>(refType);
            }

            ref.attach(context, this);

            try {
                refField.set(context.getDelegate(), ref);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }

        }

        private boolean isCompatible(Descriptor other) {
            return refField.getGenericType().equals(other.refField.getGenericType());
        }

        @Override
        public void reset(boolean full) {
            if (full) {
                dispose();
            } else if (ref != null) {
                ref.reset();
            }
        }

        @Override
        public void dispose() {
            if (ref != null) {
                ref.dispose();
            }
        }

        ControlDescriptor getControlDescriptor() {
            return control;
        }

        static Descriptor create(CodeConnector<?> connector, Field field) {
            if (Ref.class.equals(field.getType())
                    && field.getGenericType() instanceof ParameterizedType) {
                Class<?> refType = findRefType((ParameterizedType) field.getGenericType());
                field.setAccessible(true);
                return new Descriptor(connector, field.getName(), field, refType);
            } else {
                return null;
            }
        }

        private static Class<?> findRefType(ParameterizedType type) {
            Type[] types = type.getActualTypeArguments();
            if (types.length > 0 && types[0] instanceof Class) {
                return (Class) types[0];
            } else {
                return Object.class;
            }
        }

    }

    private static class ControlImpl extends ControlDescriptor implements Control {

        private final Descriptor rd;

        ControlImpl(CodeConnector<?> connector, String id, Descriptor rd) {
            super(id, Category.Synthetic, connector.getSyntheticIndex());
            this.rd = rd;
        }

        @Override
        public ControlInfo getInfo() {
            return null;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
        }

        @Override
        public Control getControl() {
            return this;
        }

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.isReply() || call.isError()) {
                rd.ref.handleAsyncResponse(call);
            } else {
                throw new IllegalArgumentException("Reference control received unexpected call : " + call);
            }
        }

    }

}
