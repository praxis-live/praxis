/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neilcsmith.praxis.video.pgl.custom.userapi;

import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class PShader {
    
    private processing.opengl.PShader shader;

    public void set(String name, int x) {
        shader.set(name, x);
    }

    public void set(String name, int x, int y) {
        shader.set(name, x, y);
    }

    public void set(String name, int x, int y, int z) {
        shader.set(name, x, y, z);
    }

    public void set(String name, int x, int y, int z, int w) {
        shader.set(name, x, y, z, w);
    }

    public void set(String name, double x) {
        shader.set(name, (float)x);
    }

    public void set(String name, double x, double y) {
        shader.set(name, (float)x, (float)y);
    }

    public void set(String name, double x, double y, double z) {
        shader.set(name, (float)x, (float)y, (float)z);
    }

    public void set(String name, double x, double y, double z, double w) {
        shader.set(name, (float)x, (float)y, (float)z, (float)w);
    }

    public void set(String name, boolean x) {
        shader.set(name, x);
    }

    public void set(String name, boolean x, boolean y) {
        shader.set(name, x, y);
    }

    public void set(String name, boolean x, boolean y, boolean z) {
        shader.set(name, x, y, z);
    }

    public void set(String name, boolean x, boolean y, boolean z, boolean w) {
        shader.set(name, x, y, z, w);
    }

    public void set(String name, PImage tex) {
        shader.set(name, tex);
    }
    
    protected abstract processing.opengl.PShader unwrap();
    
}
