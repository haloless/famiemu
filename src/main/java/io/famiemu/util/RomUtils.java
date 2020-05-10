package io.famiemu.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RomUtils {

    public static Path findRom(String name) {
        return Paths.get("rom", name);
    }

    public static byte[] readRomBytes(String name) throws IOException {
        return Files.readAllBytes(findRom(name));
    }


}
