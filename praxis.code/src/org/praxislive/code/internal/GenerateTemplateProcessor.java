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
 */
package org.praxislive.code.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.praxislive.code.GenerateTemplate;

/**
 * Annotation processor for {@link GenerateTemplate}. See annotation for usage.
 * 
 * @author Neil C Smith - http://www.neilcsmith.net
 */
@SupportedAnnotationTypes("org.praxislive.code.GenerateTemplate")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class GenerateTemplateProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(GenerateTemplate.class)) {
            if (e.getKind().isClass()) {
                TypeElement type = (TypeElement) e;
                String path = type.getAnnotation(GenerateTemplate.class).value();
                generateTemplate(type, path);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@GenerateTemplate must be used on a class", e);
            }
        }
        return true;
    }

    private void generateTemplate(TypeElement type, String path) {
        
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generating template file at " + path + " for " + type.getSimpleName() + ".java");
        
        FileObject source;
        try {
            source = getJavaSourceFile(type);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Cannot open source file to create " + path, type);
            return;
        }

        FileObject template;
        try {
            template = getOutputTemplateFile(type, path);
        } catch (FilerException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Template file already exists " + path, type);
            return;
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error creating template file" + path, type);
            return;
        }

        try {
            processSourceToTemplate(type, path, source, template);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Error creating template file" + path, type);
        }

    }

    private FileObject getJavaSourceFile(TypeElement type) throws IOException {
        String fileName = type.getSimpleName() + ".java";
        Name pkg = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName();
        return processingEnv.getFiler().getResource(
                StandardLocation.SOURCE_PATH, pkg, fileName);
    }

    private FileObject getOutputTemplateFile(TypeElement type, String path) throws IOException {
        Name pkg = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName();
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                pkg, path, type);
    }

    private void processSourceToTemplate(TypeElement type, String path,
            FileObject source, FileObject template)
            throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(source.openInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(template.openOutputStream(), "UTF-8"))) {
            String line;
            boolean writing = false, mismatched = false;
            while ((line = in.readLine()) != null) {
                if (writing) {
                    if (line.contains(GenerateTemplate.TOKEN_END)) {
                        writing = false;
                    } else if (line.contains(GenerateTemplate.TOKEN_BEGIN)) {
                        mismatched = true;
                    } else {
                        out.append(line);
                        out.append("\n"); // don't want system line ending!
                    }
                } else {
                    if (line.contains(GenerateTemplate.TOKEN_BEGIN)) {
                        writing = true;
                    } else if (line.contains(GenerateTemplate.TOKEN_END)) {
                        mismatched = true;
                    }
                }
            }
            
            if (writing || mismatched) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Mismatched tokens generating " + path + " for " + type.getSimpleName() + ".java");
            }
            
        }
    }

}
