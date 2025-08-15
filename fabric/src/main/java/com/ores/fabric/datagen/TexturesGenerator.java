package com.ores.fabric.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.ores.ORESMod;
import com.ores.core.Materials;
import com.ores.core.Variants;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TexturesGenerator implements DataProvider {

    private final FabricDataOutput output;
    private final Path autogenRoot;

    public TexturesGenerator(FabricDataOutput output) {
        this.output = output;
        this.autogenRoot = output.getModContainer().findPath("assets/ores/textures/autogen")
                .orElseThrow(() -> new IllegalStateException("Could not find autogen textures directory"));
    }

    @Override
    public @NotNull CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Materials material : Materials.values()) {
            for (Variants variant : Variants.values()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        generateTexture(material, variant, cachedOutput);
                    } catch (IOException e) {
                        System.err.println("Failed to generate texture for " + variant.getFormattedId(material.getId()));
                        e.printStackTrace();
                    }
                });
                futures.add(future);
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private void generateTexture(Materials material, Variants variant, CachedOutput cachedOutput) throws IOException {
        String formattedId = variant.getFormattedId(material.getId());

        Materials.VanillaExclusions vanillaExclusions = material.getVanillaExclusions();
        if (vanillaExclusions != null) {
            List<String> exclusions = vanillaExclusions.excludedVariantIds();
            if (exclusions != null && exclusions.contains(formattedId)) {
                return;
            }
        }

        String fileName = formattedId + ".png";
        boolean isBlockTexture = switch (variant.getCategory()) {
            case BLOCK, FALLING_BLOCK, INVERTED_FALLING_BLOCK, GLASS, ORE, FALLING_ORE, INVERTED_FALLING_ORE -> true;
            default -> false;
        };
        String subFolder = isBlockTexture ? "block" : "item";

        Path outputPath = this.output.getOutputFolder().resolve("assets/" + ORESMod.MOD_ID + "/textures/" + subFolder + "/" + fileName);
        Path noGenPath = autogenRoot.resolve("noGen").resolve(fileName);

        byte[] textureData;

        if (Files.exists(noGenPath)) {
            textureData = Files.readAllBytes(noGenPath);
        } else {
            Variants.Category category = variant.getCategory();
            if (category == Variants.Category.ORE || category == Variants.Category.FALLING_ORE || category == Variants.Category.INVERTED_FALLING_ORE) {
                textureData = generateOreTexture(material, variant);
            } else {
                textureData = generatePaletteTexture(material, variant);
            }
        }

        if (textureData != null) {
            HashCode hashCode = Hashing.sha1().hashBytes(textureData);
            cachedOutput.writeIfNeeded(outputPath, textureData, hashCode);
        }
    }

    private byte[] generateOreTexture(Materials material, Variants variant) throws IOException {
        Variants.OreProps oreProps = variant.getOreProps();
        if (oreProps == null) return null;

        ResourceLocation stoneLocation = ResourceLocation.parse(oreProps.idStone());
        Path stonePath = autogenRoot.resolve("oreStones").resolve(stoneLocation.getNamespace()).resolve(stoneLocation.getPath() + ".png");
        Path overlayPath = autogenRoot.resolve("oreOverlay").resolve(material.getId() + ".png");

        if (!Files.exists(stonePath) || !Files.exists(overlayPath)) {
            System.err.println("Missing source texture for ore: " + stonePath + " or " + overlayPath);
            return null;
        }

        BufferedImage stoneImage = ImageIO.read(stonePath.toFile());
        BufferedImage overlayImage = ImageIO.read(overlayPath.toFile());

        BufferedImage finalImage = new BufferedImage(stoneImage.getWidth(), stoneImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = finalImage.createGraphics();
        g.drawImage(stoneImage, 0, 0, null);
        g.drawImage(overlayImage, 0, 0, null);
        g.dispose();

        return toByteArray(finalImage);
    }

    private byte[] generatePaletteTexture(Materials material, Variants variant) throws IOException {
        Variants.ColorType colorType = null;
        if (variant.getItemProps() != null) {
            colorType = variant.getItemProps().color();
        } else if (variant.getBlockProps() != null) {
            colorType = variant.getBlockProps().colorType();
        }

        String templateName = variant.getFormattedId("material") + ".png";

        if (colorType == null) return null;

        String paletteType = colorType.name().toLowerCase();

        Path templatePath = autogenRoot.resolve("oreVariants").resolve(templateName);
        Path colorPalettePath = autogenRoot.resolve("color_palettes").resolve(paletteType).resolve(material.getId() + ".png");
        Path grayscalePalettePath = autogenRoot.resolve("color_palettes").resolve(paletteType).resolve("base.png");

        if (!Files.exists(templatePath) || !Files.exists(colorPalettePath) || !Files.exists(grayscalePalettePath)) {
            System.err.println("Missing source files for palette swap: " + templatePath + ", " + colorPalettePath + ", or " + grayscalePalettePath);
            return null;
        }

        BufferedImage templateImage = ImageIO.read(templatePath.toFile());
        BufferedImage colorPalette = ImageIO.read(colorPalettePath.toFile());
        BufferedImage grayscalePalette = ImageIO.read(grayscalePalettePath.toFile());

        Map<Integer, Integer> colorMap = new HashMap<>();
        for (int y = 0; y < grayscalePalette.getHeight(); y++) {
            for (int x = 0; x < grayscalePalette.getWidth(); x++) {
                int grayRGB = grayscalePalette.getRGB(x, y);
                int colorRGB = colorPalette.getRGB(x, y);
                colorMap.put(grayRGB, colorRGB);
            }
        }

        BufferedImage finalImage = new BufferedImage(templateImage.getWidth(), templateImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < templateImage.getHeight(); y++) {
            for (int x = 0; x < templateImage.getWidth(); x++) {
                int templateRGB = templateImage.getRGB(x, y);
                if ((templateRGB >> 24) == 0x00) {
                    finalImage.setRGB(x, y, templateRGB);
                } else {
                    int mappedColor = colorMap.getOrDefault(templateRGB, templateRGB);
                    finalImage.setRGB(x, y, mappedColor);
                }
            }
        }

        return toByteArray(finalImage);
    }

    private byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    @Override
    public @NotNull String getName() {
        return "ORES Textures Generator";
    }
}
