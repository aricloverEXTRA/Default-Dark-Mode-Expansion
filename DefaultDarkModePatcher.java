package com.aric3435.defaultdarkmodepatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ResourcePackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DefaultDarkModePatcher implements ClientModInitializer {

    public static final String MOD_ID = "default_dark_mode_patcher";
    // The file name for the Default Dark Mode resource pack.
    public static final String DEFAULT_PACK_FILENAME = "Default-Dark-Mode-1.20.2+-2024.6.0.zip";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Updated pack.mcmeta content using the original structure but replacing 34 with 61.
    private static final String UPDATED_PACK_MCMETA = """
        {
            "pack": {
                "pack_format": 18,
                "supported_formats": {
                    "min_inclusive": 18,
                    "max_inclusive": 61
                },
                "description": "Welcome to the dark side!\\n\\u00a78by nebulr \\u2022 1.20.2+ \\u2022 2024.6.0"
            }
        }
        """;

    // Updated shader file content for rendertype_text.fsh.
    private static final String UPDATED_RENDTYPE_TEXT_FSH = """
        #version 150

        #moj_import <fog.glsl>
        #moj_import <minecraft:dynamictransforms.glsl>

        uniform sampler2D Sampler0;

        in float vertexDistance;
        in vec4 vertexColor;
        in vec2 texCoord0;

        out vec4 fragColor;

        void main() {
            vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
            if (color.a < 0.1) {
                discard;
            }
            
            if (color.r > 0.2479 && color.r < 0.2481
                && color.g > 0.2479 && color.g < 0.2481
                && color.b > 0.2479 && color.b < 0.2481) {
                color = vec4(0.6667, 0.6667, 0.6667, 1.0);
            }
            
            vec3 pos = vec3(0.0, 0.0, vertexDistance);
            float sphericalDist = fog_spherical_distance(pos);
            float cylindricalDist = fog_cylindrical_distance(pos);
            
            fragColor = apply_fog(color, sphericalDist, cylindricalDist, 
                                FogEnvironmentalStart, FogEnvironmentalEnd,
                                FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
        }
        """;

    // Updated shader file content for rendertype_text_intensity.fsh.
    private static final String UPDATED_RENDTYPE_TEXT_INTENSITY_FSH = """
        #version 150

        #moj_import <fog.glsl>
        #moj_import <minecraft:dynamictransforms.glsl>

        uniform sampler2D Sampler0;

        in float vertexDistance;
        in vec4 vertexColor;
        in vec2 texCoord0;

        out vec4 fragColor;

        void main() {
            vec4 color = texture(Sampler0, texCoord0).rrrr * vertexColor * ColorModulator;
            if (color.a < 0.1) {
                discard;
            }
            
            if (color.r > 0.2479 && color.r < 0.2481
                && color.g > 0.2479 && color.g < 0.2481
                && color.b > 0.2479 && color.b < 0.2481) {
                color = vec4(0.6667, 0.6667, 0.6667, 1.0);
            }
            
            vec3 pos = vec3(0.0, 0.0, vertexDistance);
            float sphericalDist = fog_spherical_distance(pos);
            float cylindricalDist = fog_cylindrical_distance(pos);
            
            fragColor = apply_fog(color, sphericalDist, cylindricalDist, 
                                FogEnvironmentalStart, FogEnvironmentalEnd,
                                FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
        }
        """;

    @Override
    public void onInitializeClient() {
        // When the Resource Pack screen is initialized, check for Default Dark Mode and add the "P" patch button.
        ScreenEvents.AFTER_INIT.register((screen, client) -> {
            if (screen instanceof ResourcePackScreen) {
                Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
                Path resourcePacksDir = gameDir.resolve("resourcepacks");
                Path defaultPackPath = resourcePacksDir.resolve(DEFAULT_PACK_FILENAME);
                if (Files.exists(defaultPackPath)) {
                    // Place the patch button in the upper right corner (adjust position as needed).
                    int buttonWidth = 20, buttonHeight = 20;
                    int x = screen.width - buttonWidth - 10;
                    int y = 10;
                    ButtonWidget patchButton = new ButtonWidget(
                            x,
                            y,
                            buttonWidth,
                            buttonHeight,
                            new LiteralText("P"),
                            button -> patchResourcePack(defaultPackPath)
                    );
                    screen.addDrawableChild(patchButton);
                    LOGGER.info("Patch button added to Resource Pack screen.");
                }
            }
        });
    }

    /**
     * Patches the Default Dark Mode resource pack by:
     * 1. Extracting the pack into a temporary directory.
     * 2. Overwriting pack.mcmeta and the two shader files.
     * 3. Repackaging and replacing the original zip.
     */
    private void patchResourcePack(Path packPath) {
        LOGGER.info("Starting patch process for resource pack: {}", packPath.toString());
        try {
            // Create a temporary directory for extraction.
            Path tempDir = Files.createTempDirectory("ddm_patch_");
            LOGGER.info("Created temporary directory: {}", tempDir);
            
            // Unzip the resource pack.
            unzip(packPath.toFile(), tempDir.toFile());
            LOGGER.info("Extracted resource pack to temporary directory.");
            
            // Update pack.mcmeta.
            Path packMcmeta = tempDir.resolve("pack.mcmeta");
            Files.writeString(packMcmeta, UPDATED_PACK_MCMETA, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            LOGGER.info("Updated pack.mcmeta.");
            
            // Update shader files.
            Path shaderDir = tempDir.resolve("assets/minecraft/shaders/core");
            Path shaderFile1 = shaderDir.resolve("rendertype_text.fsh");
            Path shaderFile2 = shaderDir.resolve("rendertype_text_intensity.fsh");
            Files.writeString(shaderFile1, UPDATED_RENDTYPE_TEXT_FSH, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            Files.writeString(shaderFile2, UPDATED_RENDTYPE_TEXT_INTENSITY_FSH, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            LOGGER.info("Updated shader files.");
            
            // Create a new patch zip file.
            File patchedZip = new File(packPath.toString() + ".patched.zip");
            zipDirectory(tempDir.toFile(), patchedZip);
            LOGGER.info("Repackaged patched resource pack.");
            
            // Replace the original resource pack with the patched version.
            Files.delete(packPath);
            Files.move(patchedZip.toPath(), packPath);
            LOGGER.info("Replaced original resource pack with patched version.");
            
            // Delete temporary files.
            deleteDirectoryRecursively(tempDir.toFile());
            LOGGER.info("Cleaned up temporary files.");
            
            // Notify the player.
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(new LiteralText("Default Dark Mode has been successfully patched!"), false);
            }
        } catch (Exception e) {
            LOGGER.error("Error during patching: ", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(new LiteralText("Failed to patch Default Dark Mode resource pack."), false);
            }
        }
    }

    /**
     * Unzips a zip file to the given target directory.
     */
    private void unzip(File zipFile, File targetDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = newFile(targetDir, entry);
                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Helps prevent Zip Slip vulnerabilities.
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory: " + zipEntry.getName());
        }
        return destFile;
    }

    /**
     * Zips the given directory into a zip file.
     */
    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFile(sourceDir, sourceDir, zos);
        }
    }

    /**
     * Recursively zips files starting from sourceFile.
     */
    private void zipFile(File rootDir, File sourceFile, ZipOutputStream zos) throws IOException {
        if (sourceFile.isDirectory()) {
            for (File file : sourceFile.listFiles()) {
                zipFile(rootDir, file, zos);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                String zipEntryName = rootDir.toURI().relativize(sourceFile.toURI()).getPath();
                ZipEntry zipEntry = new ZipEntry(zipEntryName);
                zos.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     */
    private void deleteDirectoryRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteDirectoryRecursively(child);
            }
        }
        if (!file.delete()) {
            LOGGER.warn("Unable to delete: {}", file.getAbsolutePath());
        }
    }
}
