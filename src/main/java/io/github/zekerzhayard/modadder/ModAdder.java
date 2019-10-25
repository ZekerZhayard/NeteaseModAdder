package io.github.zekerzhayard.modadder;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import io.github.zekerzhayard.modadder.v_1_7_10.NeteaseCoreModManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ModAdder implements IFMLLoadingPlugin, net.minecraftforge.fml.relauncher.IFMLLoadingPlugin {
    public ModAdder() throws Exception {
        String mcVersion = (String) FieldUtils.readStaticField(LaunchClassLoader.class.getClassLoader().loadClass("net.minecraftforge.common.MinecraftForge"), "MC_VERSION");
        if (mcVersion.equals("1.7.10")) {
            NeteaseCoreModManager.discoverCoreMods(new File("."), Launch.classLoader);
        } else if (Lists.newArrayList("1.8", "1.8.8", "1.8.9", "1.9.4", "1.10.2", "1.11.2").contains(mcVersion)) {
            io.github.zekerzhayard.modadder.v_1_8_plus.NeteaseCoreModManager.discoverCoreMods(new File("."), Launch.classLoader);
        }
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
