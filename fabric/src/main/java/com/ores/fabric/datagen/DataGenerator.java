package com.ores.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Enregistrement des fournisseurs de données côté serveur (tables de butin)
        pack.addProvider(ModLootTableProvider::new);
        pack.addProvider(ModRecipeProvider::new);

        // Enregistrement des fournisseurs de données côté client (modèles de blocs/objets)
        pack.addProvider(ModModelProvider::new);
    }
}