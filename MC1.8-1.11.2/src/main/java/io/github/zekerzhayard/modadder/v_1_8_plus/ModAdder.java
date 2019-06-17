package io.github.zekerzhayard.modadder.v_1_8_plus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.JsonParser;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.lang3.reflect.FieldUtils;

@IFMLLoadingPlugin.SortingIndex(-100)
public class ModAdder implements IFMLLoadingPlugin {
    public ModAdder() throws Exception {
        String mcVersion = (String) FieldUtils.readStaticField(ForgeVersion.class, "mcVersion");
        if (!Lists.newArrayList("1.8", "1.8.8", "1.8.9", "1.9.4", "1.10.2", "1.11.2").contains(mcVersion)) {
            return;
        }
        
        FMLLaunchHandler fmlLaunchHandler = (FMLLaunchHandler) FieldUtils.readDeclaredStaticField(FMLLaunchHandler.class, "INSTANCE", true);
        File minecraftHome = (File) FieldUtils.readDeclaredField(fmlLaunchHandler, "minecraftHome", true);
        LaunchClassLoader classLoader = (LaunchClassLoader) FieldUtils.readDeclaredField(fmlLaunchHandler, "classLoader", true);

        NeteaseCoreModManager.discoverCoreMods(minecraftHome, classLoader);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getASMTransformerClass() {
        File settings = new File("./config/modadder_settings.json");
        if (!settings.isFile()) {
            return null;
        }
        try {
            if (new JsonParser().parse(new FileReader(settings)).getAsJsonObject().get("loadNeteaseMods").getAsBoolean()) {
                return null;
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        // Remove all netease regular mods
        for (String modName : new File("./mods").list()) {
            if (modName.endsWith("@3@0.jar")) {
                NeteaseCoreModManager.candidateModFiles.remove(modName);
                NeteaseCoreModManager.ignoredModFiles.add(modName);
            }
        }
        
        // Remove all netease core mods
        for (ITweaker tweaker : (List<ITweaker>) Launch.blackboard.get("Tweaks")) {
            if (tweaker.getClass().getName().equals("net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper")) {
                for (Field field : tweaker.getClass().getDeclaredFields()) {
                    if (field.getName().equals("coreModInstance")) {
                        field.setAccessible(true);
                        try {
                            if (field.get(tweaker).getClass().getName().startsWith("com.netease.mc.")) {
                                field.set(tweaker, new IFMLLoadingPlugin() {
                                    @Override
                                    public void injectData(Map<String, Object> data) {
                                        
                                    }
                                    
                                    @Override
                                    public String getSetupClass() {
                                        return null;
                                    }
                                    
                                    @Override
                                    public String getModContainerClass() {
                                        return null;
                                    }
                                    
                                    @Override
                                    public String getAccessTransformerClass() {
                                        return null;
                                    }
                                    
                                    @Override
                                    public String[] getASMTransformerClass() {
                                        return null;
                                    }
                                });
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
