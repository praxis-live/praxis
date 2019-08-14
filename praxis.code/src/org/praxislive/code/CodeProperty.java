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

import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.logging.LogBuilder;
import org.praxislive.logging.LogLevel;

class CodeProperty<D extends CodeDelegate>
        implements Control {

    public final static String MIME_TYPE = "text/x-praxis-java";

    private final CodeFactory<D> factory;
    private final ControlInfo info;
    private CodeContext<D> context;
    private Call activeCall;
    private Call taskCall;
    private List<Value> keys;
    private boolean latestSet;
    private long latest;
    private ControlAddress contextFactory;

    private CodeProperty(CodeFactory<D> factory, ControlInfo info) {
        this.factory = factory;
        this.info = info;
    }

    @SuppressWarnings("unchecked")
    protected void attach(CodeContext<?> context) {
        this.context = (CodeContext<D>) context;
        contextFactory = null;
    }

    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isRequest()) {
            processInvoke(call, router);
        } else if (call.isReply()) {
            processReturn(call, router);
        } else {
            processError(call, router);
        }
    }

    private void processInvoke(Call call, PacketRouter router) {
        List<Value> args = call.args();
        long time = call.time();
        if (args.size() > 0 && isLatest(time)) {
            try {
                String code = args.get(0).toString();
                CodeContextFactoryService.Task task
                        = new CodeContextFactoryService.Task(
                                factory,
                                code,
                                context.getLogLevel(),
                                context.getDelegate().getClass());
                if (contextFactory == null) {
                    contextFactory = ControlAddress.of(
                            context.locateService(CodeContextFactoryService.class)
                            .orElseThrow(ServiceUnavailableException::new),
                            CodeContextFactoryService.NEW_CONTEXT);
                }
                taskCall = Call.create(contextFactory, context.getAddress(this), time, PReference.of(task));
                router.route(taskCall);
                // managed to start task ok
                setLatest(time);
                if (activeCall != null) {
                    router.route(activeCall.reply(activeCall.args()));
                }
                activeCall = call;
            } catch (Exception ex) {
                router.route(call.error(PError.of(ex)));
            }
        } else {
            router.route(call.reply(keys));
        }
    }

    private void processReturn(Call call, PacketRouter router) {
        try {
            if (taskCall == null || taskCall.matchID() != call.matchID()) {
                //LOG.warning("Unexpected Call received\n" + call.toString());
                return;
            }
            taskCall = null;
            CodeContextFactoryService.Result result
                    = (CodeContextFactoryService.Result) ((PReference) call.args().get(0)).getReference();
            keys = activeCall.args();
            router.route(activeCall.reply(keys));
            activeCall = null;
            context.flush();
            context.getComponent().install((CodeContext<D>) result.getContext());
            LogBuilder log = result.getLog();
            context.log(log);
        } catch (Exception ex) {
            router.route(activeCall.error(PError.of(ex)));
        }
    }

    private void processError(Call call, PacketRouter router) throws Exception {
        if (taskCall == null || taskCall.matchID() != call.matchID()) {
            //LOG.warning("Unexpected Call received\n" + call.toString());
            return;
        }
        router.route(activeCall.error(call.args()));
        activeCall = null;
        List<Value> args = call.args();
        PError err;
        if (args.size() > 0) {
            try {
                err = PError.coerce(args.get(0));
            } catch (ValueFormatException ex) {
                err = PError.of(ex, args.get(0).toString());
            }
        } else {
            err = PError.of("");
        }
        context.getLog().log(LogLevel.ERROR, err);
        context.flush();
    }

    private void setLatest(long time) {
        latestSet = true;
        latest = time;
    }

    private boolean isLatest(long time) {
        if (latestSet) {
            return (time - latest) >= 0;
        } else {
            return true;
        }
    }

    static class Descriptor<D extends CodeDelegate>
            extends ControlDescriptor {

        private final CodeFactory<D> factory;
        private final ControlInfo info;
        private CodeProperty<?> control;

        public Descriptor(CodeFactory<D> factory, int index) {
            super("code", Category.Internal, index);
            this.factory = factory;
            this.info = createInfo(factory);
        }

        private ControlInfo createInfo(CodeFactory<D> factory) {
            return ControlInfo.createPropertyInfo(
                    new ArgumentInfo[]{
                        ArgumentInfo.of(PString.class,
                                PMap.of(
                                        PString.KEY_MIME_TYPE, MIME_TYPE,
                                        ArgumentInfo.KEY_TEMPLATE, factory.getSourceTemplate(),
                                        ClassBodyContext.KEY, factory.getClassBodyContext().getClass().getName()
                                ))
                    },
                    new Value[]{PString.EMPTY},
                    PMap.EMPTY);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Control previous) {
            if (previous instanceof CodeProperty
                    && ((CodeProperty<?>) previous).factory == factory) {
                control = (CodeProperty<?>) previous;
            } else {
                control = new CodeProperty<>(factory, info);
            }
            control.attach(context);
        }

        @Override
        public Control getControl() {
            return control;
        }

        @Override
        public ControlInfo getInfo() {
            return info;
        }

    }

}
