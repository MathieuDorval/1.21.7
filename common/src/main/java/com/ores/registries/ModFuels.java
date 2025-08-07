package com.ores.registries;

import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Gère le calcul et l'enregistrement des combustibles.
 */
public class ModFuels {
    // Map pour stocker les temps de combustion des items avant leur enregistrement.
    private static final Map<RegistrySupplier<Item>, Integer> FUELS_TO_REGISTER = new HashMap<>();

    /**
     * Calcule le temps de combustion pour un item donné et le stocke s'il est valide.
     * @param itemSupplier Le RegistrySupplier de l'item.
     * @param material Le matériau de base.
     * @param variant La variante de l'item.
     */
    public static void addFuel(RegistrySupplier<Item> itemSupplier, Materials material, Variants variant) {
        Integer materialBurnTime = null;
        Integer variantBurnTime = null;

        // Récupère le temps de combustion du matériau (toujours depuis ItemProps)
        if (material.getItemProps() != null) {
            materialBurnTime = material.getItemProps().burnTime();
        }

        // Récupère le temps de combustion du variant (depuis ItemProps ou BlockProps)
        if (variant.getItemProps() != null) {
            variantBurnTime = variant.getItemProps().burnTime();
        } else if (variant.getBlockProps() != null) {
            variantBurnTime = variant.getBlockProps().burnTime();
        }

        // Si les deux valeurs existent, on calcule et on stocke le résultat
        if (materialBurnTime != null && variantBurnTime != null) {
            int finalBurnTime = (materialBurnTime * variantBurnTime) / 100;
            if (finalBurnTime > 0) {
                FUELS_TO_REGISTER.put(itemSupplier, finalBurnTime);
            }
        }
    }

    /**
     * Enregistre tous les combustibles stockés dans le FuelRegistry.
     * Doit être appelé au bon moment du cycle de vie du mod (par ex. FMLCommonSetupEvent pour NeoForge).
     */
    public static void registerAll() {
        System.out.println("Registering dynamic fuels for ORES mod...");
        FUELS_TO_REGISTER.forEach((itemSupplier, burnTime) -> {
            // L'appel à .get() est nécessaire ici, mais doit être fait au bon moment
            // pour éviter les crashs sur NeoForge.
            FuelRegistry.register(burnTime, itemSupplier.get());
        });
        System.out.println(FUELS_TO_REGISTER.size() + " dynamic fuels registered.");
    }
}
