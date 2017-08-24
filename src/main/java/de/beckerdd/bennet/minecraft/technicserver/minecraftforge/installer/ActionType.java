package de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer;

import java.io.File;
import java.util.function.Predicate;

public interface ActionType {
    boolean run(File target, Predicate<String> optionals);
    boolean isPathValid(File targetDir);
    String getFileError(File targetDir);
    String getSuccessMessage();
    String getSponsorMessage();
}
