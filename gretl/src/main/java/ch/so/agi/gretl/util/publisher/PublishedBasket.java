package ch.so.agi.gretl.util.publisher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"model","topic","basket"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublishedBasket {
    private String model;
    private String topic;
    private String basket;
    public PublishedBasket() {
        super();
    }
    public PublishedBasket(String model, String topic, String basket) {
        super();
        this.model = model;
        this.topic = topic;
        this.basket = basket;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getBasket() {
        return basket;
    }
    public void setBasket(String basket) {
        this.basket = basket;
    }
}
