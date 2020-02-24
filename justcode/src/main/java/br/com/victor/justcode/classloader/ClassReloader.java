package br.com.victor.justcode.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassReloader {
    private List<URLClassLoader> classLoaders = new ArrayList<>();
    private List<Class<?>>       classesName  = new ArrayList<>();
    private List<Object>         instances    = new ArrayList<>();
    private static final String  JAR1         = "classreloader/AttendantBot-1.jar";
    private static final String  JAR2         = "classreloader/AttendantBot-2.jar";

    public static void main(String[] args) throws Exception {
        ClassReloader classReloader = new ClassReloader();
        classReloader.loadJar(JAR1);
        classReloader.executeClasses();
        classReloader.loadJar(JAR2);
        classReloader.executeClasses();
        classReloader.executeInstances();
        System.out.println("finish");
    }

    private void executeInstances() throws Exception {
        for (Object obj : instances) {
            obj.getClass().getDeclaredMethod("hello").invoke(obj);
        }
    }

    private void executeClasses() throws Exception {
        for (Class<?> clazz : classesName) {
            Object instance = clazz.newInstance();
            clazz.getDeclaredMethod("hello").invoke(instance);
            instances.add(instance);
        }
    }

    private void loadJar(String jar) throws URISyntaxException {
        classLoaders.clear();
        classesName.clear();
        URL res = getClass().getClassLoader().getResource(jar);
        File file = Paths.get(res.toURI()).toFile();
        loadClasses(file);
    }

    private void loadClasses(File file) {
        Path path = file.toPath();
        if (Files.exists(path)) {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { file.toURI().toURL() }, getClass().getClassLoader());
                for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                        Class<?> loadClass = urlClassLoader.loadClass(className);
                        if (loadClass.getSimpleName().equals("AttendantIntegration")) {
                            classesName.add(loadClass);
                        }
                    }
                }
                classLoaders.add(urlClassLoader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
