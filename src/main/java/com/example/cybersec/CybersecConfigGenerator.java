package com.example.cybersec;

import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Populate the config and startup files with environment specifics
 *
 */
public class CybersecConfigGenerator
{
    private static StringSubstitutor substitutor;
    private static Path envConfigDirPath;

    private static void generateConfigFile(Path templatePath) {
        Path sourceFilePath = templatePath.getFileName();
        String sourceFilename = sourceFilePath.toString();
        Path destFilePath = envConfigDirPath.resolve(sourceFilePath);
        try {
            if (sourceFilename.endsWith(".sh") || sourceFilename.endsWith(".properties")) {
                System.out.printf("Populating template %s\n", templatePath.toString());
                try (BufferedWriter writer = Files.newBufferedWriter(destFilePath)) {
                    Files.lines(templatePath).forEach(line -> {
                        try {
                            writer.write(substitutor.replace(line));
                            writer.newLine();
                        } catch (IOException e) {
                            fail(String.format("Config template population from %s to %s failed.", templatePath, destFilePath), e);
                        }
                    });
                }
            } else {
                System.out.printf("Copying file %s to %s\n", templatePath, destFilePath);
                Files.copy(templatePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            fail(String.format("Config creation from %s to %s failed.", templatePath, destFilePath), e);
        }
    }

    private static void fail(String message, Exception e) {
        System.err.println(message);
        if (e != null) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    private static void fail(String message) {
        fail(message, null);
    }

    public static void main( String[] args ) {
        if (args.length != 2) {
            fail("config_generator <env dir> <env template path> ");
        }
        String envDir = args[0];
        Path envPropertiesPath = Paths.get(envDir, "env.properties");
        Path envTemplateDir = Paths.get(args[1]);

        createConfigDir(envDir);

        try (InputStream propertiesStream = Files.newInputStream(envPropertiesPath))  {
            // read the name value pairs for the environment
            Properties envProperties = new Properties();
            envProperties.load(propertiesStream);
            // convert to a map
            substitutor = new StringSubstitutor(envProperties.stringPropertyNames().stream().collect(Collectors.toMap(k -> k, envProperties::getProperty)));

            // populate each template
            try  (Stream<Path> templates = Files.walk(envTemplateDir)) {
                templates.filter(Files::isRegularFile).forEach(CybersecConfigGenerator::generateConfigFile);
            }
        } catch (IOException e) {
            fail(String.format("Could not open environment properties file %s", envPropertiesPath.toString()));
        }

    }

    private static void createConfigDir(String envDir) {
        Path envDirPath = Paths.get(envDir);
        envConfigDirPath = Paths.get(envDir, "configs");

        if (Files.isDirectory(envDirPath) && Files.isWritable(envDirPath)) {
            // create config directory if it doesn't exist already
            if (!Files.exists(envConfigDirPath)) {
                try {
                    System.out.printf("Creating config directory %s\n", envConfigDirPath.toString());
                    Files.createDirectories(envConfigDirPath);
                } catch (IOException e) {
                    fail(String.format("Could not create config directory %s", envConfigDirPath.toString()), e);
                }
            }
        } else {
            fail(String.format("Config directory %s must exist and be writable.", envDir));
        }
    }
}
