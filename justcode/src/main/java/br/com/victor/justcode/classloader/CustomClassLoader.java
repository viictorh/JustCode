package br.com.victor.justcode.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.pdfbox.io.IOUtils;

/**
 * 
 * Classe responsável pelo carregamento de jar/classes em memória em um novo classLoader. Assim, mesmo que uma biblioteca tenha variaveis estáticas, por exemplo, elas serão utilizadas em classloader diferentes não influenciando de maneira geral o sistema. <br />
 * Esta solução foi implementada num projeto para carregamento de diversas Atendentes em memória. Como a configuração era feita de forma estática na aplicação da atendente, ao abrir mais de um atendente os status estavam sendo informados para todas atendentes inviabilizando o carregamento correto.
 * 
 * @author victor.bello
 * 
 * 
 *
 */
public class CustomClassLoader extends ClassLoader {

    private static final ConcurrentHashMap<String, List<JarClass>> LOADED_JAR = new ConcurrentHashMap<>();

    private static final String                                    CURRENTLOCATION;

    static {
        String name = "attendant-emulator";
        try {
            Path path = Paths.get(CustomClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            name = path.toString().replace("\\", "/").toLowerCase();
        } catch (URISyntaxException e) {
        }
        CURRENTLOCATION = name;
        System.out.println("***************");
        System.out.println("CURRENTLOCATION: " + CURRENTLOCATION);
        System.out.println("***************");
    }

    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        try {
            String classFile = "/" + classname.replace('.', '/') + ".class";
            URL url = CustomClassLoader.class.getResource(classFile);
            String path = url.toString();
            System.out.println("CustomClassLoader path: " + path.toLowerCase());
            if (path.contains(".jar!")) {
                String jarPath = path.substring(path.indexOf("file:/") + 6, path.indexOf("!/"));
                if (!jarPath.endsWith("/rt.jar")) {
                    String lowerPath = path.toLowerCase();
                    if ((lowerPath.contains("attendantintegrator") || lowerPath.contains(CURRENTLOCATION))) {
                        String classFileName = path.substring(path.indexOf("!/") + 2, path.length());
                        List<JarClass> jarClasses = getBytes(classFileName, jarPath);
                        byte[] bodge = jarClasses.stream().filter(j -> j.fileName.equals(classFileName)).findFirst().get().content;
                        return defineClass(classname, bodge);
                    }
                }
            } else {
                byte[] bodge = Files.readAllBytes(Paths.get(url.toURI()));
                return defineClass(classname, bodge);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
        }

        return super.loadClass(classname, true);
    }

    private Class<?> defineClass(String classname, byte[] bodge) throws ClassFormatError {
        return defineClass(classname, bodge, 0, bodge.length);
    }

    List<JarClass> getBytes(String javaFileName, String jar) throws IOException {
        List<JarClass> jarClasses = LOADED_JAR.get(jar);
        if (jarClasses != null) {
            return jarClasses;
        }
        jarClasses = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        jarClasses.add(new JarClass(entry.getName(), IOUtils.toByteArray(inputStream)));
                    } catch (IOException ioException) {
                        System.out.println("Could not obtain class entry for " + entry.getName());
                        throw ioException;
                    }
                }
            }
        }
        LOADED_JAR.put(jar, jarClasses);
        return jarClasses;
    }

    private static class JarClass {
        String fileName;
        byte[] content;

        public JarClass(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            JarClass other = (JarClass) obj;
            if (fileName == null) {
                if (other.fileName != null)
                    return false;
            } else if (!fileName.equals(other.fileName))
                return false;
            return true;
        }

    }

    public static void main(String[] args) throws Exception {
        String path = "jar:file:/C:/Users/victor.bello/Desktop/Workspace/bot/attendant-emulator/src/main/resources/lib/AttendantIntegrator.jar!/voxage/components/unknowconnector/events/MyEventListener_UnknowConn.class";
        String path2 = "jar:file:/C:/Users/victor.bello/Desktop/Workspace/bot/attendant-emulator/target/attendant-emulator-1.1.0.jar!/br/com/voxage/attendantemulator/bean/Attendant.class";
        String path3 = "jar:file:/C:/Users/victor.bello/Desktop/Workspace/bot/attendant-emulator/target/attendant-emulator-1.1.0.jar!/br/com/voxage/attendantemulator/bean/Attendant.class";
        new CustomClassLoader().loadClass(path);
        new CustomClassLoader().loadClass(path2);
        new CustomClassLoader().loadClass(path3);

        String substring = path.substring(path.indexOf("file:/") + 6, path.indexOf("!/"));
        String substring2 = path.substring(path.indexOf("!/") + 2, path.length());
        System.out.println(substring);
        System.out.println(substring2);
    }

}
