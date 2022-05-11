package dev.westernpine.site.transformers;

import com.google.gson.Gson;
import dev.westernpine.site.Site;
import spark.ResponseTransformer;

import static dev.westernpine.site.Site.gson;

public class JsonTransformer implements ResponseTransformer {

    @Override
    public String render(Object o) throws Exception {
        return gson.toJson(o);
    }
}
