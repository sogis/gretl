package ch.so.agi.gretl.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import ch.ehi.basics.logging.EhiLogger;
import ch.so.agi.gretl.logging.Ehi2GretlAdapter;
import ch.so.agi.gretl.logging.LogEnvironment;

public class GretlPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        LogEnvironment.initGradleIntegrated();
        Ehi2GretlAdapter.init();
        
    }

}
