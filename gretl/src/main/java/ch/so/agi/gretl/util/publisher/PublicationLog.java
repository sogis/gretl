package ch.so.agi.gretl.util.publisher;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "Publication")
@JsonPropertyOrder({"dataIdent","published","publishedBaskets","publishedRegions"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicationLog {
    public PublicationLog() {
        super();
    }
    public PublicationLog(String dataIdent, java.util.Date published) {
        super();
        this.dataIdent = dataIdent;
        
        this.published = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(published);
    }
    private String dataIdent=null;//": "ch.so.afu.gewaesserschutz",
    private String published=null;// ": "2021-12-23T14:54:49.050062",
    private List<PublishedBasket> publishedBaskets=null;
    private List<PublishedRegion> publishedRegions=null;
    public String getDataIdent() {
        return dataIdent;
    }
    public void setDataIdent(String dataIdent) {
        this.dataIdent = dataIdent;
    }
    public String getPublished() {
        return published;
    }
    public void setPublished(String published) {
        this.published = published;
    }
    public List<PublishedBasket> getPublishedBaskets() {
        return publishedBaskets;
    }
    public void setPublishedBaskets(List<PublishedBasket> publishedBaskets) {
        this.publishedBaskets = publishedBaskets;
    }
    public void addPublishedBasket(PublishedBasket basket) {
        if(publishedBaskets==null) {
            publishedBaskets=new ArrayList<PublishedBasket>();
        }
        publishedBaskets.add(basket);
    }
    public List<PublishedRegion> getPublishedRegions() {
        return publishedRegions;
    }
    public void setPublishedRegions(List<PublishedRegion> publishedRegions) {
        this.publishedRegions = publishedRegions;
    }
    public void addPublishedRegion(PublishedRegion region) {
        if(publishedRegions==null) {
            publishedRegions=new ArrayList<PublishedRegion>();
        }
        publishedRegions.add(region);
    }
}
