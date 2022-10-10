package ch.so.agi.gretl.util.publisher;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonPropertyOrder({"region","publishedBaskets"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublishedRegion {
    public PublishedRegion() {
        super();
    }
    public PublishedRegion(String region) {
        super();
        this.region = region;
    }
    private String region=null;
    private List<PublishedBasket> publishedBaskets=null;
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
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
}
