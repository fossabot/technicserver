package de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeDoesNotMatchJsonNodeSelectorException;
import argo.jdom.JsonRootNode;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer.transform.LibraryTransformer;
import de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer.transform.TransformInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VersionInfo {
    private static VersionInfo INSTANCE;
    private JsonRootNode versionData;
    private List<OptionalLibrary> optionals = Lists.newArrayList();
    private TransformInfo[] transforms;
    private LibraryTransformer transformer;

    private VersionInfo()
    {
        this(VersionInfo.class.getResourceAsStream("/install_profile.json"));
    }
    private VersionInfo(InputStream installProfile)
    {
        //InputStream installProfile = getClass().getResourceAsStream("/install_profile.json");
        JdomParser parser = new JdomParser();
        transforms = new TransformInfo[0];
        try
        {
            versionData = parser.parse(new InputStreamReader(installProfile, Charsets.UTF_8));

            if (versionData.isArrayNode("optionals"))
            {
                for (JsonNode opt : versionData.getArrayNode("optionals"))
                {
                    OptionalLibrary o = new OptionalLibrary(opt);
                    if (!o.isValid())
                    {
                        // Make this more prominent to the packer?
                        System.out.println("Optional Library is invalid, must specify a name, artifact and maven");
                        continue;
                    }
                    optionals.add(o);
                }
            }

            LibraryTransformer tmp = new LibraryTransformer();
            transforms = tmp.read(versionData);
            transformer = transforms.length > 0 ? tmp : null;
        }
        catch (JsonNodeDoesNotMatchJsonNodeSelectorException ignore) { }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }

    public static void LazyInit(InputStream installProfile){
        if(INSTANCE == null){
            if (installProfile == null){
                INSTANCE = new VersionInfo();
            } else {
                INSTANCE = new VersionInfo(installProfile);
            }
        }
    }
    public static void LazyInit(){
        if(INSTANCE == null){
            INSTANCE = new VersionInfo();
        }
    }

    public static String getProfileName()
    {
        LazyInit();
        //return INSTANCE.versionData.getStringValue("install","profileName");
        return "Forge";
    }

    public static String getVersionTarget()
    {
        try {
            return INSTANCE.versionData.getStringValue("install", "target");
        }catch (JsonNodeDoesNotMatchJsonNodeSelectorException ignore){
            return INSTANCE.versionData.getStringValue("id");
        }
    }
    public static File getLibraryPath(File root)
    {
        String path = INSTANCE.versionData.getStringValue("install","path");
        String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split(path), String.class);
        File dest = root;
        Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split(split[0]);
        for (String part : subSplit)
        {
            dest = new File(dest, part);
        }
        dest = new File(new File(dest, split[1]), split[2]);
        String fileName = split[1]+"-"+split[2]+".jar";
        return new File(dest,fileName);
    }

    public static String getModListType()
    {
        return !INSTANCE.versionData.isStringValue("install", "modList") ? "" :
                INSTANCE.versionData.getStringValue("install", "modList");
    }

    public static String getVersion()
    {
        return INSTANCE.versionData.getStringValue("install","version");
    }

    public static String getWelcomeMessage()
    {
        return INSTANCE.versionData.getStringValue("install","welcome");
    }

    public static String getLogoFileName()
    {
        return INSTANCE.versionData.getStringValue("install","logo");
    }

    public static String getURLFileName()
    {
        if (!INSTANCE.versionData.isStringValue("install", "urlIcon"))
            return "/url.png";
        return INSTANCE.versionData.getStringValue("install", "urlIcon");
    }

    public static boolean getStripMetaInf()
    {
        try
        {
            return INSTANCE.versionData.getBooleanValue("install", "stripMeta");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static JsonNode getVersionInfo()
    {
        return INSTANCE.versionData.getNode("versionInfo");
    }

    public static File getMinecraftFile(File path)
    {
        return new File(new File(path, getMinecraftVersion()),getMinecraftVersion()+".jar");
    }
    public static String getContainedFile()
    {
        try {
            return INSTANCE.versionData.getStringValue("install", "filePath");
        }catch (JsonNodeDoesNotMatchJsonNodeSelectorException ignore){
            return "";
        }
    }
    public static void extractFile(File path) throws IOException
    {
        INSTANCE.doFileExtract(path);
    }

    private void doFileExtract(File path) throws IOException
    {
        if (Strings.isNullOrEmpty(getContainedFile())) return;
        InputStream inputStream = getClass().getResourceAsStream("/"+getContainedFile());
        OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
        ByteStreams.copy(inputStream, outputSupplier);
    }

    public static String overriddenMinecraftVersion = null;
    public static String getMinecraftVersion()
    {
        if(overriddenMinecraftVersion == null) {
            return INSTANCE.versionData.getStringValue("install", "minecraft");
        }else{
            return overriddenMinecraftVersion;
        }
    }

    public static String getMirrorListURL()
    {
        return INSTANCE.versionData.getStringValue("install","mirrorList");
    }

    public static boolean hasMirrors()
    {
        return INSTANCE.versionData.isStringValue("install","mirrorList");
    }

    public static boolean hideClient()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideClient") &&
                INSTANCE.versionData.getBooleanValue("install", "hideClient");
    }

    public static boolean hideServer()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideServer") &&
                INSTANCE.versionData.getBooleanValue("install", "hideServer");
    }

    public static boolean hideExtract()
    {
        return INSTANCE.versionData.isBooleanValue("install", "hideExtract") &&
                INSTANCE.versionData.getBooleanValue("install", "hideExtract");
    }

    public static boolean isInheritedJson()
    {
        return INSTANCE.versionData.isStringValue("versionInfo", "inheritsFrom") &&
                INSTANCE.versionData.isStringValue("versionInfo", "jar");
    }

    public static boolean hasOptionals()
    {
        return getOptionals().size() > 0;
    }

    public static List<OptionalLibrary> getOptionals()
    {
        return INSTANCE.optionals;
    }

    public static List<LibraryInfo> getLibraries(String marker, Predicate<String> filter)
    {
        List<LibraryInfo> ret = Lists.newArrayList();

        List<JsonNode> iter;
        try {
            iter = INSTANCE.versionData.getArrayNode("versionInfo", "libraries");
        }catch (Exception ignore){
            iter = INSTANCE.versionData.getArrayNode("libraries");
        }

        for (JsonNode node : iter)
            ret.add(new LibraryInfo(node, marker));

        for (OptionalLibrary opt : getOptionals())
        {
            LibraryInfo info = new LibraryInfo(opt, marker);
            info.setEnabled(filter.test(opt.getArtifact()));
            ret.add(info);
        }

        return ret;
    }

    public static boolean needsMCDownload(String side)
    {
        for (TransformInfo ti : INSTANCE.transforms)
        {
            if (ti.validSide(side) && ("{minecraft_server_jar}".equals(ti.input) || "{minecraft_jar}".equals(ti.input)))
                return true;
        }
        return false;
    }

    public static List<TransformInfo> getTransforms(String side)
    {
        List<TransformInfo> ret = new ArrayList<TransformInfo>();
        for (TransformInfo ti : INSTANCE.transforms)
            if (ti.validSide(side))
                ret.add(ti);
        return ret;
    }

    public static LibraryTransformer getTransformer()
    {
        return INSTANCE.transformer;
    }
}
