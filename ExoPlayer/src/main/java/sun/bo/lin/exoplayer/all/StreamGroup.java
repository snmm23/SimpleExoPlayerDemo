package sun.bo.lin.exoplayer.all;


public class StreamGroup {

    private String name;
    private String url;

    public StreamGroup(String name, String url){
        setName(name);
        setUrl(url);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
