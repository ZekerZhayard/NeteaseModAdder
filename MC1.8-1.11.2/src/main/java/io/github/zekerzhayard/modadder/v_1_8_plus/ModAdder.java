package io.github.zekerzhayard.modadder.v_1_8_plus;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class ModAdder implements IFMLLoadingPlugin {
    public ModAdder() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException , NoSuchFieldException, SecurityException {
        if (!Lists.newArrayList("1.8", "1.8.8", "1.8.9", "1.9.4", "1.10.2", "1.11.2").contains(ForgeVersion.mcVersion)) {
            return;
        }
        
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
