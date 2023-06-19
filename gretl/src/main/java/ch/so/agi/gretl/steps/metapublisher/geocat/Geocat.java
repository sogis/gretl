package ch.so.agi.gretl.steps.metapublisher.geocat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;

import ch.interlis.iom.IomObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ch.so.agi.gretl.steps.metapublisher.geocat.model.ThemePublicationGC;

public class Geocat {
    private static final String TEMPLATE_FILENAME = "geocat_template.xml";
    private static final String GEOCAT_DIR_NAME = "geocat";
    private static final String SHARED_DIR_NAME = "shared";

    private static Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
    }
    
    public static void export(IomObject iomObj, File templateFile, Path geocatFile) throws IOException, TemplateException {
        cfg.setDirectoryForTemplateLoading(templateFile.getParentFile());
        
        Template tpl = cfg.getTemplate(templateFile.getName());
        
        try(FileWriter writer = new FileWriter(geocatFile.toFile())) {
            ThemePublicationGC tpCat = new ThemePublicationGC(iomObj);            
            tpl.process(tpCat, writer);
        }
    }
}
