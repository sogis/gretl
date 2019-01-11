package ch.so.agi.gretl.logging;

import java.util.ArrayList;

import org.gradle.internal.logging.events.OutputEvent;
import org.gradle.internal.logging.events.OutputEventListener;

public class GradleCollector implements OutputEventListener {
    private ArrayList<OutputEvent> events=new ArrayList<OutputEvent>();

    @Override
    public void onOutput(OutputEvent event) {
        events.add(event);
        
    }

    public OutputEvent getEvent(int i) {
        return events.get(i);
    }

    public void clear() {
        events.clear();
        
    }
    
}
