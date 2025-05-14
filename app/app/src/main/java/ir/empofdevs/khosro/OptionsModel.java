package ir.empofdevs.khosro;

import java.util.HashMap;

public class OptionsModel {
    private int img;
    private String name;
    private String url;
    private HashMap<String, Class<?>> parameters;

    public OptionsModel(int img, String name, String url, HashMap<String, Class<?>> parameters) {
        this.img = img;
        this.name = name;
        this.url = url;
        this.parameters = parameters;
    }

    public int getImage() { return this.img; }
    public String getName() { return this.name; }
    public String getUrl() { return this.url; }
    public HashMap<String, Class<?>> getParameters() { return this.parameters; }
}
