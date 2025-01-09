package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class XslTransformerStep {
    private GretlLogger log;
    private String taskName;

    public XslTransformerStep() {
        this(null);
    }
    
    public XslTransformerStep(String taskName) {
        if (taskName == null) {
            this.taskName = XslTransformerStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    public void execute(File xslFile, File xmlFile, File outputDirectory, String fileExtension) throws IOException, SaxonApiException {
        log.lifecycle(String.format("Start XslTransformerStep(Name: %s XslFile: %s XmlFile: %s OutDirectory: %s FileExtension: %s)", taskName,
                xslFile.getAbsolutePath(), xmlFile.toString(), outputDirectory.toString(), fileExtension));

        Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(xslFile));
        
        XdmNode source = proc.newDocumentBuilder().build(new StreamSource(xmlFile));
        
        File outFile = Paths.get(outputDirectory.getAbsolutePath(), FilenameUtils.getBaseName(xmlFile.getName()) + "." + fileExtension).toFile();
        Serializer outFileSerializer = proc.newSerializer(outFile);
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(outFileSerializer);
        trans.transform();
        trans.close();
    }
    
    public void execute(String xslFileName, File xmlFile, File outputDirectory, String fileExtension) throws IOException, SaxonApiException { 
        String tmpDir = Files.createTempDirectory("xslttransformerstep").toFile().getAbsolutePath();
        File xslFile = new File(Paths.get(tmpDir, xslFileName).toFile().getAbsolutePath());
        InputStream xsltFileInputStream = XslTransformerStep.class.getResourceAsStream("/xslt/"+xslFileName); 
        Files.copy(xsltFileInputStream, xslFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        xsltFileInputStream.close();
        
        execute(xslFile, xmlFile, outputDirectory, fileExtension);
    }
}
