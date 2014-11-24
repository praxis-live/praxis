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
 *
 */
package net.neilcsmith.praxis.video.code;

import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.CodeFactory;

public class VideoCodeFactory extends CodeFactory<VideoCodeDelegate> {

    private final static VideoBodyContext VBC = new VideoBodyContext();

    private final boolean emptyDefault;

    public VideoCodeFactory(String type) {
        super(VBC, type, VideoBodyContext.TEMPLATE);
        emptyDefault = true;
    }

    public VideoCodeFactory(String type, String sourceTemplate) {
        super(VBC, type, sourceTemplate);
        emptyDefault = false;
    }

    @Override
    public Task<VideoCodeDelegate> task() {
        return new VideoContextCreator();
    }

    private class VideoContextCreator extends Task<VideoCodeDelegate> {

        private VideoContextCreator() {
            super(VideoCodeFactory.this);
        }

        @Override
        protected CodeContext<VideoCodeDelegate> createCodeContext(VideoCodeDelegate delegate) {
            return new VideoCodeContext(new VideoCodeConnector(this, delegate));
        }

        @Override
        protected VideoCodeDelegate createDefaultDelegate() throws Exception {
            if (emptyDefault) {
                return new VideoCodeDelegate() {
                };
            } else {
                return super.createDefaultDelegate();
            }
        }

    }

}
