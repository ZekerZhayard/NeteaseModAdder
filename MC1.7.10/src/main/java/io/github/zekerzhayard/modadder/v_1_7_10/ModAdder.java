package io.github.zekerzhayard.modadder.v_1_7_10;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParser;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.reflect.FieldUtils;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ModAdder implements IFMLLoadingPlugin {
    public ModAdder() throws Exception {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        // Remove all netease regular mods
        for (String modName : new File("./mods").list()) {
            if (modName.endsWith("@3@0.jar")) {
                NeteaseCoreModManager.reparsedCoremods.remove(modName);
                NeteaseCoreModManager.loadedCoremods.add(modName);
            }
        }
        
        // Remove all netease core mods
        for (ITweaker tweaker : (List<ITweaker>) Launch.blackboard.get("Tweaks")) {
            if (tweaker.getClass().getName().equals("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper")) {
                try {
                    Field field = tweaker.getClass().getField("coreModInstance");
                    field.setAccessible(true);
                    if (field.get(tweaker).getClass().getProtectionDomain().getCodeSource().getLocation().getFile().endsWith("@3@0.jar")) {
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
                } catch (Exception e) {
                    e.printStackTrace();
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
