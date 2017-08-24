package de.beckerdd.bennet.minecraft.technicserver.minecraftforge.installer;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import argo.jdom.JsonNode;

public class LibraryInfo
{
    private Artifact artifact;
    private List<String> checksums;
    private boolean side = false;
    private boolean enabled = true;
    private String  url = DownloadUtils.LIBRARIES_URL;

    public LibraryInfo(JsonNode node, String marker)
    {
        artifact = new Artifact(node.getStringValue("name"));

        if (node.isArrayNode("checksums"))
        {
            checksums = Lists.newArrayList(Lists.transform(node.getArrayNode("checksums"), new Function<JsonNode, String>()
            {
                @Override
                public String apply(JsonNode node)
                {
                    return node.getText();
                }
            }));
        }
        side = node.isBooleanValue(marker) && node.getBooleanValue(marker);

        if (MirrorData.INSTANCE.hasMirrors() && node.isStringValue("url"))
            url = MirrorData.INSTANCE.getMirrorURL();
        else if (node.isStringValue("url"))
            url = node.getStringValue("url") + "/";
    }

    public LibraryInfo(OptionalLibrary lib, String marker)
    {
        artifact = new Artifact(lib.getArtifact());
        side = (lib.isServer() && "serverreq".equals(marker)) ||
                    (lib.isClient() && "clientreq".equals(marker));
        url = lib.getMaven();
    }

    public Artifact     getArtifact()   { return artifact;  }
    public List<String> getChecksums()  { return checksums; }
    public boolean      isCorrectSide() { return side;      }
    public boolean      isEnabled()     { return enabled;   }
    public void setEnabled(boolean v)   {
        enabled = v;      }
    public String       getURL()        { return url;       }
}