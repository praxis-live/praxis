

package net.neilcsmith.praxis.video.pgl.custom;

import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P2DCodeComponent extends CodeComponent<P2DCodeDelegate> {
    
     public P2DCodeComponent() {
        super(createInitialContext());
    }

    private static CodeContext<P2DCodeDelegate> createInitialContext() {
        return new P2DCodeContext(new P2DCodeConnector(new P2DCodeDelegate() {

            @Override
            public void draw() {
            }
        }));
    }
    
}
