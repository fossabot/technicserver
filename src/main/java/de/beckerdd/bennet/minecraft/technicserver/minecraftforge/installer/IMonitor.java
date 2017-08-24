package de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer;

public interface IMonitor {
    void setMaximum(int max);
    void setNote(String note);
    void setProgress(int progress);
    void close();
}
