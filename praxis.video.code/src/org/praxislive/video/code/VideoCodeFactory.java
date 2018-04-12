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
 *
 */
package org.praxislive.video.code;

import org.praxislive.code.CodeContext;
import org.praxislive.code.CodeFactory;
import org.praxislive.core.ComponentType;

public class VideoCodeFactory extends CodeFactory<VideoCodeDelegate> {

    private final static VideoBodyContext VBC = new VideoBodyContext();

    public VideoCodeFactory(ComponentType type,
            Class<? extends VideoCodeDelegate> baseClass,
            String sourceTemplate) {
        super(VBC, type, baseClass, sourceTemplate);
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

    }

}
