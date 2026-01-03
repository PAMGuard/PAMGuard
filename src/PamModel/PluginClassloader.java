package PamModel;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassloader extends URLClassLoader {

    public PluginClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
	public void addURL(URL url) {
        super.addURL(url);
    }
}
