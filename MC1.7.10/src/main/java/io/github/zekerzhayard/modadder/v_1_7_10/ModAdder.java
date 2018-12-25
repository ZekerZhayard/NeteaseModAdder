package io.github.zekerzhayard.modadder.v_1_7_10;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.LaunchClassLoader;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ModAdder implements IFMLLoadingPlugin {
    public ModAdder() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException {
        Field fieldFMLLaunchHandlerINSTANCE = FMLLaunchHandler.class.getDeclaredField("INSTANCE");
        fieldFMLLaunchHandlerINSTANCE.setAccessible(true);
        FMLLaunchHandler fmlLaunchHandler = (FMLLaunchHandler) fieldFMLLaunchHandlerINSTANCE.get(FMLLaunchHandler.class);

        Field fieldLaunchClassLoader = FMLLaunchHandler.class.getDeclaredField("classLoader");
        fieldLaunchClassLoader.setAccessible(true);
        LaunchClassLoader classLoader = (LaunchClassLoader) fieldLaunchClassLoader.get(fmlLaunchHandler);

        Field fieldMinecraftHome = FMLLaunchHandler.class.getDeclaredField("minecraftHome");
        fieldMinecraftHome.setAccessible(true);
        File minecraftHome = (File) fieldMinecraftHome.get(fmlLaunchHandler);

        NeteaseCoreModManager.discoverCoreMods(minecraftHome, classLoader);
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
