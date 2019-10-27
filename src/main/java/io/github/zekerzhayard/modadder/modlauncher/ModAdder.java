package io.github.zekerzhayard.modadder.modlauncher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformationServiceDecorator;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ModAdder implements ITransformationService {
    private static boolean initialized = false;
    private static Map<String, TransformationServiceDecorator> serviceLookup = new HashMap<>();
    private static Object transformationServicesHandler;

    public ModAdder() throws Exception {
        if (initialized) {
            return;
        }
        initialized = true;

        Method method_addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method_addURL.setAccessible(true);
        method_addURL.invoke(Thread.currentThread().getContextClassLoader(), this.getClass().getProtectionDomain().getCodeSource().getLocation());

        transformationServicesHandler = FieldUtils.readDeclaredField(Launcher.INSTANCE, "transformationServicesHandler", true);
        Logger logger = (Logger) FieldUtils.readDeclaredStaticField(transformationServicesHandler.getClass(), "LOGGER", true);
        Marker modAdder = MarkerManager.getMarker("NETEASEMODADDER");
        List<Path> additionalPaths = this.candidatesModDirTransformer(new File(".").toPath());
        logger.debug(modAdder, "Found additional transformation services from discovery services: {}", additionalPaths);
        ClassLoader cl = this.getClass().getClassLoader();
        for (Path p : additionalPaths) {
            method_addURL.invoke(cl, p.toUri().toURL());
        }

        ServiceLoader<ITransformationService> transformationServices = ServiceLoader.load(ITransformationService.class, cl);
        for (Iterator<ITransformationService> iterator = transformationServices.iterator(); iterator.hasNext(); ) {
            try {
                iterator.next();
            } catch (ServiceConfigurationError e) {
                logger.fatal(modAdder, "Encountered serious error loading transformation service, expect problems", e);
            }
        }

        Constructor<TransformationServiceDecorator> constructor = TransformationServiceDecorator.class.getDeclaredConstructor(ITransformationService.class);
        constructor.setAccessible(true);
        for (ITransformationService transformationService : transformationServices) {
            serviceLookup.put(transformationService.name(), constructor.newInstance(transformationService));
        }
        String str = StringUtils.join(serviceLookup.keySet(), ",");
        logger.debug(modAdder, "Found transformer services : [{}]", str);
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
    @SuppressWarnings("unchecked")
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
        try {
            Field field_serviceLookup = transformationServicesHandler.getClass().getDeclaredField("serviceLookup");
            field_serviceLookup.setAccessible(true);
            Map<String, TransformationServiceDecorator> map = (Map<String, TransformationServiceDecorator>) field_serviceLookup.get(transformationServicesHandler);

            Method method_onLoad = TransformationServiceDecorator.class.getDeclaredMethod("onLoad", IEnvironment.class, Set.class);
            method_onLoad.setAccessible(true);

            Map<String, TransformationServiceDecorator> newMap = new HashMap<>();
            for (Map.Entry<String, TransformationServiceDecorator> entry : serviceLookup.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    newMap.put(entry.getKey(), map.get(entry.getKey()));
                    continue;
                }
                method_onLoad.invoke(entry.getValue(), env, otherServices);
                newMap.put(entry.getKey(), entry.getValue());
            }
            field_serviceLookup.set(transformationServicesHandler, newMap);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException e) {
            throw new IncompatibleEnvironmentException(e.getMessage());
        }
    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return new ArrayList<>();
    }

    private List<Path> candidatesModDirTransformer(final Path gameDirectory) {
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
