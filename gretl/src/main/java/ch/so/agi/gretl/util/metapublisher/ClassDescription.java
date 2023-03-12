package ch.so.agi.gretl.util.metapublisher;

import java.util.List;

public class ClassDescription {
    private String name;
    private String title;
    // TODO Man koennte sich ueberlegen, ob man den Inhalt bereits hier in ein Span-Element einpackt. Damit das ganze wohlgeformt ist.  
    private String description;
    private boolean abstractClass;
    private String topicName;
    private String modelName;
    @Deprecated
    private String parentClass;
    private List<AttributeDescription> attributes;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTitle() {
        return title!=null?title:name;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isAbstractClass() {
        return abstractClass;
    }
    public void setAbstractClass(boolean abstractClass) {
        this.abstractClass = abstractClass;
    }
    public String getTopicName() {
        return topicName;
    }
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    public String getModelName() {
        return modelName;
    }
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    public String getParentClass() {
        return parentClass;
    }
    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }
    public List<AttributeDescription> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<AttributeDescription> attributes) {
        this.attributes = attributes;
    }
    public String getQualifiedName() {
        return modelName + "." + topicName + "." + name;
    }
}
