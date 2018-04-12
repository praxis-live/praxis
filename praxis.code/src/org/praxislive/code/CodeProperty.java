/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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

import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
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
    private CallArguments keys;
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
        switch (call.getType()) {
            case INVOKE:
            case INVOKE_QUIET:
                processInvoke(call, router);
                break;
            case RETURN:
                processReturn(call, router);
                break;
            case ERROR:
                processError(call, router);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void processInvoke(Call call, PacketRouter router) {
        CallArguments args = call.getArgs();
        long time = call.getTimecode();
        if (args.getSize() > 0 && isLatest(time)) {
            try {
                String code = args.get(0).toString();
                CodeContextFactoryService.Task task
                        = new CodeContextFactoryService.Task(
                                factory,
                                code,
                                context.getLogLevel(),
                                context.getDelegate().getClass());
                if (contextFactory == null) {
                    contextFactory = ControlAddress.create(
                            context.locateService(CodeContextFactoryService.class)
                            .orElseThrow(ServiceUnavailableException::new),
                            CodeContextFactoryService.NEW_CONTEXT);
                }
                taskCall = Call.createCall(contextFactory, context.getAddress(this), time, PReference.wrap(task));
                router.route(taskCall);
                // managed to start task ok
                setLatest(time);
                if (activeCall != null) {
                    router.route(Call.createReturnCall(activeCall, activeCall.getArgs()));
                }
                activeCall = call;
            } catch (Exception ex) {
                router.route(Call.createErrorCall(call, PError.create(ex)));
            }
        } else {
            router.route(Call.createReturnCall(call, keys));
        }
    }

    private void processReturn(Call call, PacketRouter router) {
        try {
            if (taskCall == null || taskCall.getMatchID() != call.getMatchID()) {
                //LOG.warning("Unexpected Call received\n" + call.toString());
                return;
            }
            taskCall = null;
            CodeContextFactoryService.Result result
                    = (CodeContextFactoryService.Result) ((PReference) call.getArgs().get(0)).getReference();
            keys = activeCall.getArgs();
            router.route(Call.createReturnCall(activeCall, keys));
            activeCall = null;
            context.flush();
            context.getComponent().install((CodeContext<D>) result.getContext());
            LogBuilder log = result.getLog();
            context.log(log);
        } catch (Exception ex) {
            router.route(Call.createErrorCall(activeCall, PError.create(ex)));
        }
    }

    private void processError(Call call, PacketRouter router) throws Exception {
        if (taskCall == null || taskCall.getMatchID() != call.getMatchID()) {
            //LOG.warning("Unexpected Call received\n" + call.toString());
            return;
        }
        router.route(Call.createErrorCall(activeCall, call.getArgs()));
        activeCall = null;
        CallArguments args = call.getArgs();
        PError err;
        if (args.getSize() > 0) {
            try {
                err = PError.coerce(args.get(0));
            } catch (ValueFormatException ex) {
                err = PError.create(ex, args.get(0).toString());
            }
        } else {
            err = PError.create("");
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
                        ArgumentInfo.create(PString.class,
                                PMap.create(
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
