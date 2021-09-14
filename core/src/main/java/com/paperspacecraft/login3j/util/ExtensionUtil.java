package com.paperspacecraft.login3j.util;

import com.paperspacecraft.login3j.Main;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    private static final String EXTENSIONS_ERROR = "Could not retrieve extensions";

    public static void loadExtensions() {
        Path extPath = null;
        try {
            extPath = Paths.get(Main.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (extPath.endsWith("classes")) {
                extPath = extPath.resolve("../ext");
            } else {
                extPath = extPath.resolve("ext");
            }
            LOG.info("Extensions path {}", extPath);
        } catch (URISyntaxException e) {
            LOG.error(EXTENSIONS_ERROR, e);
        }
        if (extPath == null || !Files.isDirectory(extPath)) {
            return;
        }
        try (Stream<Path> fileStream = Files.list(extPath)){
            fileStream.filter(Files::isRegularFile).forEach(ExtensionUtil::loadExtension);
        } catch (IOException e) {
            LOG.error(EXTENSIONS_ERROR, e);
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
