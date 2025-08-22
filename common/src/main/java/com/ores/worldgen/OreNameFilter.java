package com.ores.worldgen;

import com.ores.core.Materials;
import com.ores.ORESMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class OreNameFilter {
    private static final Set<String> KEYWORDS = new HashSet<>();

    static {
        for (Materials m : Materials.values()) {
            KEYWORDS.add(m.getId().toLowerCase(Locale.ROOT));
            String base = m.getIdBase();
            if (base != null) {
                int i = base.indexOf(":");
                if (i >= 0 && i + 1 < base.length()) {
                    KEYWORDS.add(base.substring(i + 1).toLowerCase(Locale.ROOT));
                }
            }
        }
        ORESMod.LOGGER.info("[OreNameFilter] Keywords loaded: {}", KEYWORDS);
    }

    private OreNameFilter() {}

    public static boolean shouldBlock(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) return false;
        if (ORESMod.MOD_ID.equals(id.getNamespace())) return false;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        for (String kw : KEYWORDS) {
            if (path.contains(kw)) return true;
        }
        return false;
    }
}