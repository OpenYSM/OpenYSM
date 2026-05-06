package com.elfmcys.yesstevemodel.resource.models;

import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.elfmcys.yesstevemodel.util.data.StringPair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;

import java.util.List;

public class Metadata {
    private final String name;

    private final String tips;

    private final StringPair license;

    private final List<AuthorInfo> authors;

    //"home": "https://modrinth.com/mod/yes-steve-model",
    //"donate": "https://afdian.com/a/ysmmod"
    private final OrderedStringMap<String, String> link;

    public Metadata(String name, String tips, StringPair license, AuthorInfo[] authors, OrderedStringMap<String, String> link) {
        this.name = name;
        this.tips = tips;
        this.license = license;
        this.authors = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(authors));
        this.link = link;
    }

    public String getName() {
        return this.name;
    }

    public String getTips() {
        return this.tips;
    }

    public StringPair getLicense() {
        return this.license;
    }

    public List<AuthorInfo> getAuthors() {
        return this.authors;
    }

    public OrderedStringMap<String, String> getLink() {
        return this.link;
    }
}
