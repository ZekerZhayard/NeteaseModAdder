package io.github.zekerzhayard.modadder.modlauncher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

public class ModAdder implements ITransformationService {
    static {
        try {
            Method method_addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method_addURL.setAccessible(true);
            method_addURL.invoke(Thread.currentThread().getContextClassLoader(), ModAdder.class.getProtectionDomain().getCodeSource().getLocation());

            List<Path> additionalPaths = candidatesModDirTransformer(new File("").getAbsoluteFile().toPath());
            for (Path p : additionalPaths) {
                method_addURL.invoke(ModAdder.class.getClassLoader(), p.toUri().toURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String name() {
        return "NeteaseModAdder";
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public void beginScanning(IEnvironment environment) {

    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return new ArrayList<>();
    }

    static List<Path> candidatesModDirTransformer(final Path gameDirectory) {
        final Path modsDir = gameDirectory.resolve(FMLPaths.MODSDIR.relative());
        List<Path> paths = new ArrayList<>();
        List<File> files = Lists.newArrayList(FileUtils.listFiles(modsDir.toFile(), new String[] {"zip"}, false));
        for (File file : files) {
            try {
                Path p = file.toPath();
                if (!Files.isRegularFile(p) || Files.size(p) == 0) {
                    continue;
                }
                try (ZipFile zf = new ZipFile(new File(p.toUri()))) {
                    if (zf.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService") != null) {
                        paths.add(p);
                    }
                } catch (IOException ioe) {
                    LogManager.getLogger().error("Zip Error when loading jar file {}", p, ioe);
                }
            } catch (IOException | IllegalStateException ioe) {
                LogManager.getLogger().error("Error during early discovery", ioe);
            }
        }
        return paths;
    }
}
