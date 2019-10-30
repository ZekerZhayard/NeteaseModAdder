package io.github.zekerzhayard.modadder.launchwrapper.v_1_7_10;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.common.io.ByteSource;
import com.google.common.primitives.Ints;
import cpw.mods.fml.common.asm.transformers.ModAccessTransformer;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.FileListHelper;
import cpw.mods.fml.relauncher.ModListHelper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.Level;

@SuppressWarnings("unchecked")
public class NeteaseCoreModManager {
    private static final Attributes.Name COREMODCONTAINSFMLMOD = (Attributes.Name) NeteaseCoreModManager.reflectField(CoreModManager.class, "COREMODCONTAINSFMLMOD");
    private static final Attributes.Name MODTYPE = (Attributes.Name) NeteaseCoreModManager.reflectField(CoreModManager.class, "MODTYPE");
    private static final Attributes.Name MODSIDE = (Attributes.Name) NeteaseCoreModManager.reflectField(CoreModManager.class, "MODSIDE");

    public static List<String> loadedCoremods = (List<String>) NeteaseCoreModManager.reflectField(CoreModManager.class, "loadedCoremods");
    public static List<String> reparsedCoremods = (List<String>) NeteaseCoreModManager.reflectField(CoreModManager.class, "reparsedCoremods");
    private static Side side = (Side) NeteaseCoreModManager.reflectField(FMLLaunchHandler.class, "side");
    private static String mccversion = (String) NeteaseCoreModManager.reflectField(FMLInjectionData.class, "mccversion");

    private static Method methodHandleCascadingTweak = NeteaseCoreModManager.reflectMethod(CoreModManager.class, "handleCascadingTweak", File.class, JarFile.class, String.class, LaunchClassLoader.class, Integer.class);
    private static Method methodLoadCoreMod = NeteaseCoreModManager.reflectMethod(CoreModManager.class, "loadCoreMod", LaunchClassLoader.class, String.class, File.class);
    private static Method methodSetupCoreModDir = NeteaseCoreModManager.reflectMethod(CoreModManager.class, "setupCoreModDir", File.class);

    private static Object reflectField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method reflectMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static void addJar(final JarFile jar) throws IOException {
        Manifest manifest = jar.getManifest();
        String atList = manifest.getMainAttributes().getValue("FMLAT");
        if (atList == null) {
            return;
        }
        for (String at : atList.split(" ")) {
            final JarEntry jarEntry = jar.getJarEntry("META-INF/" + at);
            if (jarEntry != null) {
                try {
                    ((Map<String, String>) NeteaseCoreModManager.reflectField(Class.forName(ModAccessTransformer.class.getName(), false, NeteaseCoreModManager.class.getClassLoader().getClass().getClassLoader()), "embedded")).put(String.format("%s!META-INF/%s", jar.getName(), at), new ByteSource() {
                        @Override
                        public InputStream openStream() throws IOException {
                            return jar.getInputStream(jarEntry);
                        }
                    }.asCharSource(Charsets.UTF_8).read());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void discoverCoreMods(File mcDir, LaunchClassLoader classLoader) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        File coreMods = (File) NeteaseCoreModManager.methodSetupCoreModDir.invoke(null, mcDir); // File coreMods = setupCoreModDir(mcDir);
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        };
        File[] coreModList = coreMods.listFiles(ff);
        File versionedModDir = new File(coreMods, NeteaseCoreModManager.mccversion); // File versionedModDir = new File(coreMods, FMLInjectionData.mccversion);
        if (versionedModDir.isDirectory()) {
            File[] versionedCoreMods = versionedModDir.listFiles(ff);
            coreModList = ObjectArrays.concat(coreModList, versionedCoreMods, File.class);
        }

        coreModList = ObjectArrays.concat(coreModList, ModListHelper.additionalMods.values().toArray(new File[0]), File.class);

        coreModList = FileListHelper.sortFileList(coreModList);

        for (File coreMod : coreModList) {
            FMLRelaunchLog.fine("Examining for coremod candidacy %s", coreMod.getName());
            JarFile jar = null;
            Attributes mfAttributes;
            try {
                jar = new JarFile(coreMod);
                if (jar.getManifest() == null) {
                    // // Not a coremod and no access transformer list
                    continue;
                }
                NeteaseCoreModManager.addJar(jar);
                mfAttributes = jar.getManifest().getMainAttributes();
            } catch (IOException ioe) {
                FMLRelaunchLog.log(Level.ERROR, ioe, "Unable to read the jar file %s - ignoring", coreMod.getName());
                continue;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        // // Noise
                    }
                }
            }
            String cascadedTweaker = mfAttributes.getValue("TweakClass");
            if (cascadedTweaker != null) {
                FMLRelaunchLog.info("Loading tweaker %s from %s", cascadedTweaker, coreMod.getName());
                Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(mfAttributes.getValue("TweakOrder")));
                sortOrder = (sortOrder == null ? Integer.valueOf(0) : sortOrder);
                NeteaseCoreModManager.methodHandleCascadingTweak.invoke(null, coreMod, jar, cascadedTweaker, classLoader, sortOrder); // CoreModManager.handleCascadingTweak(coreMod, jar, cascadedTweaker, classLoader, sortOrder);
                NeteaseCoreModManager.loadedCoremods.add(coreMod.getName()); // loadedCoremods.add(coreMod.getName());
                continue;
            }
            List<String> modTypes = mfAttributes.containsKey(NeteaseCoreModManager.MODTYPE) ? Arrays.asList(mfAttributes.getValue(NeteaseCoreModManager.MODTYPE).split(",")) : ImmutableList.of("FML"); // List<String> modTypes = mfAttributes.containsKey(MODTYPE) ? Arrays.asList(mfAttributes.getValue(MODTYPE).split(",")) : ImmutableList.of("FML");

            if (!modTypes.contains("FML")) {
                FMLRelaunchLog.fine("Adding %s to the list of things to skip. It is not an FML mod,  it has types %s", coreMod.getName(), modTypes);
                NeteaseCoreModManager.loadedCoremods.add(coreMod.getName()); // loadedCoremods.add(coreMod.getName());
                continue;
            }
            String modSide = mfAttributes.containsKey(NeteaseCoreModManager.MODSIDE) ? mfAttributes.getValue(NeteaseCoreModManager.MODSIDE) : "BOTH"; // String modSide = mfAttributes.containsKey(MODSIDE) ? mfAttributes.getValue(MODSIDE) : "BOTH";
            if (!("BOTH".equals(modSide) || NeteaseCoreModManager.side.name().equals(modSide))) { // if (!("BOTH".equals(modSide) || FMLLaunchHandler.side.name().equals(modSide))) {
                FMLRelaunchLog.fine("Mod %s has ModSide meta-inf value %s, and we're %s. It will be ignored", coreMod.getName(), modSide, NeteaseCoreModManager.side.name()); // FMLRelaunchLog.fine("Mod %s has ModSide meta-inf value %s, and we're %s. It will be ignored", coreMod.getName(), modSide, FMLLaunchHandler.side.name());
                NeteaseCoreModManager.loadedCoremods.add(coreMod.getName()); // loadedCoremods.add(coreMod.getName());
                continue;
            }
            String fmlCorePlugin = mfAttributes.getValue("FMLCorePlugin");
            if (fmlCorePlugin == null) {
                // // Not a coremod
                FMLRelaunchLog.fine("Not found coremod data in %s", coreMod.getName());
                continue;
            }
            // // Support things that are mod jars, but not FML mod jars
            try {
                classLoader.addURL(coreMod.toURI().toURL());
                if (!mfAttributes.containsKey(NeteaseCoreModManager.COREMODCONTAINSFMLMOD)) { // if (!mfAttributes.containsKey(COREMODCONTAINSFMLMOD)) {
                    FMLRelaunchLog.finer("Adding %s to the list of known coremods, it will not be examined again", coreMod.getName());
                    NeteaseCoreModManager.loadedCoremods.add(coreMod.getName()); // loadedCoremods.add(coreMod.getName());
                } else {
                    FMLRelaunchLog.finer("Found FMLCorePluginContainsFMLMod marker in %s, it will be examined later for regular @Mod instances", coreMod.getName());
                    NeteaseCoreModManager.reparsedCoremods.add(coreMod.getName()); // reparsedCoremods.add(coreMod.getName());
                }
            } catch (MalformedURLException e) {
                FMLRelaunchLog.log(Level.ERROR, e, "Unable to convert file into a URL. weird");
                continue;
            }
            NeteaseCoreModManager.methodLoadCoreMod.invoke(null, classLoader, fmlCorePlugin, coreMod); // loadCoreMod(classLoader, fmlCorePlugin, coreMod);
        }
    }
}
