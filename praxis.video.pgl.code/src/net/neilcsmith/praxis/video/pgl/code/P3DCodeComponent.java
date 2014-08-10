

package net.neilcsmith.praxis.video.pgl.code;

import net.neilcsmith.praxis.code.CodeComponent;
import net.neilcsmith.praxis.code.CodeContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class P3DCodeComponent extends CodeComponent<P3DCodeDelegate> {
    
     public P3DCodeComponent() {
        super(createInitialContext());
    }

    private static CodeContext<P3DCodeDelegate> createInitialContext() {
        return new P3DCodeContext(new P3DCodeConnector(new P3DCodeDelegate() {

            @Override
            public void draw() {
            }
        }));
    }
    
}
