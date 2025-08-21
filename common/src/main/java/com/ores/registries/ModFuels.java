/**
 * ORES MOD | __mathieu
 * Handles the calculation and registration of fuel burn times for items.
 */
package com.ores.registries;

import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ModFuels {

    // -=-=-=- CONSTANTS -=-=-=-
    private static final Map<RegistrySupplier<Item>, Integer> FUELS_TO_REGISTER = new HashMap<>();

    // -=-=-=- PUBLIC API -=-=-=-
    public static void addFuel(RegistrySupplier<Item> itemSupplier, Materials material, Variants variant) {
        Materials.ItemProps materialProps = material.getItemProps();
        if (materialProps == null || materialProps.burnTime() == null) {
            return;
        }

        Integer variantBurnTime = null;
        if (variant.getItemProps() != null) {
            variantBurnTime = variant.getItemProps().burnTime();
        } else if (variant.getBlockProps() != null) {
            variantBurnTime = variant.getBlockProps().burnTime();
        }

        if (variantBurnTime != null) {
            int finalBurnTime = (materialProps.burnTime() * variantBurnTime) / 100;
            if (finalBurnTime > 0) {
                FUELS_TO_REGISTER.put(itemSupplier, finalBurnTime);
            }
        }
    }

    public static void registerAll() {
        if (FUELS_TO_REGISTER.isEmpty()) return;

        ORESMod.LOGGER.info("Registering {} dynamic fuels...", FUELS_TO_REGISTER.size());
        FUELS_TO_REGISTER.forEach((itemSupplier, burnTime) -> FuelRegistry.register(burnTime, itemSupplier.get()));
        ORESMod.LOGGER.info("Dynamic fuels registered successfully.");
    }
}
