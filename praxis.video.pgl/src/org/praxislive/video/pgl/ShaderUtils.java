package org.praxislive.video.pgl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class ShaderUtils {

    private final static Logger LOG = Logger.getLogger(ShaderUtils.class.getName());

    private ShaderUtils() {
    }

    static String convertOldVertexCode(String shader) {
        try {
            LOG.log(Level.FINE, "Vertex Input :\n{0}", shader);
            StringBuilder sb = new StringBuilder();
            boolean addTexMatrix = !shader.contains("texMatrix");
            BufferedReader reader = new BufferedReader(new StringReader(shader));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("varying")) {
                    line = line.replaceAll("varying *vec2 *v_texCoords", "varying vec4 vertTexCoord");
                } else if (line.contains("uniform")) {
                    String replacement = addTexMatrix
                            ? "uniform mat4 transformMatrix;\nuniform mat4 texMatrix"
                            : "uniform mat4 transformMatrix";
                    line = line.replaceAll("uniform *mat4 *u_projectionViewMatrix", replacement);
                } else if (line.contains("attribute")) {
                    line = line.replace("a_texCoord0", "texCoord");
                } else {
                    line = line.replace("a_texCoord0", "(texMatrix * vec4(texCoord, 1.0, 1.0))");
                    line = line.replace("v_texCoords", "vertTexCoord");
                    line = line.replace("u_projectionViewMatrix", "transformMatrix");
                }
                line = line.replace("a_position", "position");
                line = line.replace("a_color", "color");
                line = line.replace("v_color", "vertColor");
                line = line.replace("u_texture", "texture");
                sb.append(line);
                sb.append("\n");
            }
            shader = sb.toString();
            LOG.log(Level.FINE, "Vertex Output :\n{0}", shader);
            return shader;
        } catch (IOException ex) {
            Logger.getLogger(ShaderUtils.class.getName()).log(Level.SEVERE, null, ex);
            return shader;
        }
    }

    static String convertOldFragmentCode(String shader) {
        try {
            LOG.log(Level.FINE, "Fragment Input :\n{0}", shader);
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(shader));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("varying")) {
                    line = line.replaceAll("varying *vec2 *v_texCoords", "varying vec4 vertTexCoord");
                } else {
                    line = line.replace("v_texCoords", "vertTexCoord.st");
                }
                line = line.replace("v_color", "vertColor");
                line = line.replace("u_texture", "texture");
                sb.append(line);
                sb.append("\n");
            }
            shader = sb.toString();
            LOG.log(Level.FINE, "Fragment Output :\n{0}", shader);
            return shader;
        } catch (IOException ex) {
            Logger.getLogger(ShaderUtils.class.getName()).log(Level.SEVERE, null, ex);
            return shader;
        }
    }

}
