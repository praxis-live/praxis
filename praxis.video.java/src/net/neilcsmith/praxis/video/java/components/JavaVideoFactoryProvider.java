
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

            addComponent("video:code:composite", data(JavaVideoComposite.class)
                    .deprecated().replacement("video:custom"));
            
            addComponent("video:test:java:processor", data(JavaVideoProcessor.class).test().deprecated());
        }

    }

}
