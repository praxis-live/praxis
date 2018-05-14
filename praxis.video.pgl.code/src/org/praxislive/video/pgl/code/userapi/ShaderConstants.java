package org.praxislive.video.pgl.code.userapi;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class ShaderConstants {

    private ShaderConstants() {
    }
    
    final static String DEFAULT_VERTEX_SHADER
            = "uniform mat4 transformMatrix;\n" //
            + "uniform mat4 texMatrix;\n\n" //
            + "attribute vec4 position;\n" //

            + "attribute vec4 color;\n"
            + "attribute vec2 texCoord;\n\n"//

            + "varying vec4 vertColor;\n" //
            + "varying vec4 vertTexCoord;\n\n" //

            + "void main()\n" //
            + "{\n" //
            + "  vertColor = color;\n" //
            + "  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);\n\n" //
            + "  gl_Position = transformMatrix * position;\n" //
            + "}\n";

    final static String DEFAULT_FRAGMENT_SHADER
            = "uniform sampler2D texture;\n\n" //

            + "uniform vec2 texOffset;\n\n" //

            + "varying vec4 vertColor;\n" //
            + "varying vec4 vertTexCoord;\n\n" //

            + "void main() {\n"//
            + "  gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;\n" //
            + "}";

}
