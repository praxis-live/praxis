/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.video.java.components;

import java.util.logging.Logger;
import net.neilcsmith.praxis.video.java.VideoCodeDelegate;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractAsyncProperty;
import net.neilcsmith.praxis.java.impl.AbstractJavaComponent;
import org.codehaus.janino.ClassBodyEvaluator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractJavaVideoComponent extends AbstractJavaComponent {

    private final static Logger LOG = Logger.getLogger(AbstractJavaVideoComponent.class.getName());
    
    private VideoCodeDelegate currentDelegate;

    private static final String[] IMPORTS = {
        "net.neilcsmith.ripl.*",
        "net.neilcsmith.ripl.ops.*",
        "static net.neilcsmith.praxis.java.Constants.*",
        "static net.neilcsmith.praxis.video.java.VideoConstants.*"
    };

    protected void setupCodeControl() {
        registerControl("code", new DelegateCompiler());
        
    }

    protected void installVideoDelegate(VideoCodeDelegate delegate) {
        if (currentDelegate != null) {
            uninstallDelegate(currentDelegate);
        }
        if (delegate != null) {
            installDelegate(delegate);
        }
        currentDelegate = delegate;
        installToDelegator(delegate);
    }

    protected abstract void installToDelegator(VideoCodeDelegate delegate);

    private class DelegateCompiler extends AbstractAsyncProperty<VideoCodeDelegate> {

        private DelegateCompiler() {
            super(PString.info(), VideoCodeDelegate.class, PString.EMPTY);
        }

        @Override
        protected TaskService.Task createTask(CallArguments keys) throws Exception {
            Argument code;
            if (keys.getSize() < 1 || (code = keys.get(0)).isEmpty()) {
                return null;
            } else {
                return new CompilerTask(code.toString());
            }

        }

        @Override
        protected void valueChanged(long time) {
            installVideoDelegate(getValue());
        }

        @Override
        protected void taskError(long time) {
            Logger.getLogger(getClass().getName()).warning("Error loading class");
        }
    }

    private class CompilerTask implements TaskService.Task {

        private final String code;

        private CompilerTask(String code) {
            this.code = code;
        }

        public Argument execute() throws Exception {
            ClassBodyEvaluator compiler = new ClassBodyEvaluator();
            compiler.setExtendedType(VideoCodeDelegate.class);
            compiler.setDefaultImports(IMPORTS.clone());
            compiler.cook(code);
            VideoCodeDelegate delegate = (VideoCodeDelegate) compiler.getClazz().newInstance();
            return PReference.wrap(delegate);
        }
    }
}
