/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.video.java.components;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class JavaVideoFactoryProvider implements ComponentFactoryProvider {


    private static Factory instance = new Factory();

    public ComponentFactory getFactory() {
        return instance;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            addComponent("video:test:java:processor", JavaVideoProcessor.class);
            addComponent("video:test:java:composite", JavaVideoComposite.class);
        }

    }

}
