package com.paperspacecraft.login3j.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@UtilityClass
public class ExtensionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionUtil.class);
    private static final String EXTENSION_ENTRY_POINT_REF = "com.paperspacecraft.login3j.ext.Extension";
    private static final String EXTENSION_ENTRY_POINT_METHOD = "load";

    public static void loadExtensions() {
        Path extPath = Paths.get(System.getProperty("user.dir"), "ext");
        if (!Files.isDirectory(extPath)) {
            return;
        }
        try (Stream<Path> fileStream = Files.list(extPath)){
            fileStream.filter(Files::isRegularFile).forEach(ExtensionUtil::loadExtension);
        } catch (IOException e) {
            LOG.error("Could not retrieve extensions", e);
        }
    }

    private static void loadExtension(Path path) {
        URL url;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            LOG.error("Could not parse extension URI", e);
            return;
        }
        URLClassLoader childClassLoader = new URLClassLoader(
                new URL[] {url},
                ExtensionUtil.class.getClassLoader()
        );
        try {
            Class<?> classToLoad = Class.forName(EXTENSION_ENTRY_POINT_REF, true, childClassLoader);
            Method method = classToLoad.getDeclaredMethod(EXTENSION_ENTRY_POINT_METHOD);
            Object instance = classToLoad.newInstance();
            method.invoke(instance);
            LOG.info("Loaded extension {}", url);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOG.error("Could not find extension entry-point for module {}", url, e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Could not run extension code for module {}", url, e);
        }
    }
}
