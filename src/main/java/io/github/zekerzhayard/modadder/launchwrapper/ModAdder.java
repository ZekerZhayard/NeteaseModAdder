package io.github.zekerzhayard.modadder.launchwrapper;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import io.github.zekerzhayard.modadder.launchwrapper.v_1_7_10.NeteaseCoreModManager;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ModAdder implements IFMLLoadingPlugin, net.minecraftforge.fml.relauncher.IFMLLoadingPlugin {
    static {
        try {
            String mcVersion = (String) FieldUtils.readStaticField(Launch.class.getClassLoader().loadClass("net.minecraftforge.common.MinecraftForge"), "MC_VERSION");
            if (mcVersion.equals("1.7.10")) {
                NeteaseCoreModManager.discoverCoreMods(new File("."), Launch.classLoader);
            } else if (!mcVersion.equals("1.12.2")) {
                io.github.zekerzhayard.modadder.launchwrapper.v_1_8_plus.NeteaseCoreModManager.discoverCoreMods(new File("."), Launch.classLoader);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
