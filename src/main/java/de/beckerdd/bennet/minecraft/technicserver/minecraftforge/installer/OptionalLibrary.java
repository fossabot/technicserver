package de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer;

import argo.jdom.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class OptionalLibrary
{
    @SuppressWarnings("unused")
    private JsonNode node;

    private String name;
    private String artifact;
    private String maven;

    private boolean client = false;
    private boolean server = false;
    private boolean _default = true;
    private boolean inject = true;
    private String desc;
    private String url;

    public OptionalLibrary(JsonNode node)
    {
        this.node = node;
        name     = node.getStringValue("name");
        artifact = node.getStringValue("artifact");
        maven    = node.getStringValue("maven");
        client   = getBool(node, "client", true);
        server   = getBool(node, "server", true);
        _default = getBool(node, "default", true);
        inject   = getBool(node, "inject", true);
        if (node.isStringValue("desc"))
            desc = node.getStringValue("desc");
        if (node.isStringValue("url"))
            url = node.getStringValue("url");
    }

    private boolean getBool(JsonNode node, String name, boolean _def)
    {
        if (!node.isBooleanValue(name))
            return _def;
        return node.getBooleanValue(name);
    }

    public boolean isValid()
    {
        return name != null && artifact != null && maven != null;
    }

    public String getName()     { return name;     }
    public String getArtifact() { return artifact; }
    public String getMaven()    { return maven;    }
    public boolean isClient()   { return client;   }
    public boolean isServer()   { return server;   }
    public boolean getDefault() { return _default; }
    public boolean isInjected() { return inject;   }
    public String getDesc()     { return desc;     }
    public String getURL()      { return url;      }

    public static boolean saveModListJson(File root, File json, List<OptionalLibrary> libs, Predicate<String> filter)
    {
        List<String> artifacts = Lists.newArrayList();
        for (OptionalLibrary lib : libs)
        {
            if (filter.test(lib.getArtifact()))
                artifacts.add(lib.getArtifact());
        }

        if (artifacts.size() == 0)
            return true;

        File parent = json.getParentFile();
        if (!parent.exists())
            parent.mkdirs();

        System.out.println("Saving optional modlist to: " + json);

        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        buf.append("    \"repositoryRoot\": \"").append(root.getAbsolutePath().replace('\\', '/')).append("\",\n");
        buf.append("    \"modRef\": [\n");
        for (int x = 0; x < artifacts.size(); x++)
        {
            buf.append("        \"").append(artifacts.get(x)).append('"');
            if (x < artifacts.size() - 1)
                buf.append(',');
            buf.append('\n');
        }
        buf.append("    ]\n");
        buf.append("}\n");

        try
        {
            Files.write(buf.toString(), json, Charsets.UTF_8);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
