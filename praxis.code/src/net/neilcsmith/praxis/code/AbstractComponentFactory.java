/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Root;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractComponentFactory implements ComponentFactory {

    private final Map<ComponentType, MetaData> componentMap;

    protected AbstractComponentFactory() {
        componentMap = new LinkedHashMap<>();
    }

    @Override
    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    @Override
    public ComponentType[] getRootComponentTypes() {
        return new ComponentType[0];
    }

    @Override
    public Component createComponent(ComponentType type) throws ComponentInstantiationException {
        MetaData data = componentMap.get(type);
        if (data == null) {
            throw new IllegalArgumentException();
        }
        try {
            return data.getCodeFactory().createComponent();
        } catch (Exception ex) {
            throw new ComponentInstantiationException(ex);
        }
    }

    @Override
    public Root createRootComponent(ComponentType type) throws ComponentInstantiationException {
        throw new IllegalArgumentException();
    }

    @Override
    public ComponentFactory.MetaData<? extends Component> getMetaData(ComponentType type) {
        return componentMap.get(type);
    }

    @Override
    public ComponentFactory.MetaData<? extends Root> getRootMetaData(ComponentType type) {
        return null;
    }

    protected void addComponent(Data info) {
        componentMap.put(info.factory.getComponentType(), info.toMetaData());
    }

    protected Data data(CodeFactory<?> factory) {
        return new Data(factory);
    }

    protected String source(String location) {
        try (InputStream is = getClass().getResourceAsStream(location);
                Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");) {
            return s.hasNext() ? s.next() : ""; 
        } catch (Exception e) {
            Logger.getLogger(AbstractComponentFactory.class.getName())
                    .log(Level.SEVERE, "Failed to read source file", e);
            return null;
        }
    }

    private static class MetaData extends ComponentFactory.MetaData<Component> {

        private final CodeFactory<?> factory;
        private final boolean test;
        private final boolean deprecated;
        private final ComponentType replacement;
        private final Lookup lookup;

        private MetaData(
                CodeFactory<?> factory,
                boolean test,
                boolean deprecated,
                ComponentType replacement,
                Lookup lookup) {
            this.factory = factory;
            this.test = test;
            this.deprecated = deprecated;
            this.replacement = replacement;
            this.lookup = lookup;
        }

        @Override
        @Deprecated
        public Class<Component> getComponentClass() {
            return Component.class;
        }

        @Override
        public boolean isTest() {
            return test;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public ComponentType getReplacement() {
            return replacement;
        }

        @Override
        public Lookup getLookup() {
            return lookup == null ? super.getLookup() : lookup;
        }

        private CodeFactory<?> getCodeFactory() {
            return factory;
        }

    }

    public static class Data {

        private final CodeFactory<?> factory;
        private boolean test;
        private boolean deprecated;
        private ComponentType replacement;

        private Data(CodeFactory<?> factory) {
            this.factory = factory;
        }

        public Data test() {
            test = true;
            return this;
        }

        public Data deprecated() {
            deprecated = true;
            return this;
        }

        public Data replacement(String type) {
            replacement = ComponentType.create(type);
            deprecated = true;
            return this;
        }

        private MetaData toMetaData() {
            return new MetaData(factory, test, deprecated, replacement, null);
        }
    }
}
