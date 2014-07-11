/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.video.code.custom;

import net.neilcsmith.praxis.video.code.VideoCodeContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CustomVideoContext extends VideoCodeContext<CustomVideoDelegate> {

    public CustomVideoContext(CustomVideoConnector connector) {
        super(connector);
    }
    
}
