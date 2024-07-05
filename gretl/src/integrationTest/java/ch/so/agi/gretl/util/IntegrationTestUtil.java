package ch.so.agi.gretl.util;

import org.gradle.testkit.runner.GradleRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class IntegrationTestUtil {

    public static GradleRunner getGradleRunner(File projectDirectory, String taskName) throws IOException{
        return getGradleRunner(projectDirectory, taskName, null);
    }

    public static GradleRunner getGradleRunner(File projectDirectory, String taskName, GradleVariable[] variables) throws IOException{
        List<String> arguments = getRunnerArguments(taskName, variables);
        return GradleRunner.create()
                .withProjectDir(projectDirectory)
                .withPluginClasspath(getPluginClassPaths())
                .withArguments(arguments)
                .forwardOutput();
    }

    private static List<String> getRunnerArguments(String taskName, GradleVariable[] variables){
        List<String> arguments = new ArrayList<>();
        arguments.add("--init-script");
        arguments.add(IntegrationTestUtil.getPathToInitScript());
        arguments.add(taskName);
        if(variables != null){
            for(GradleVariable variable: variables){
                arguments.add(variable.buildOptionString());
            }
        }
        return arguments;
    }

    private static ArrayList<File> getPluginClassPaths() throws IOException {
        ArrayList<File> classpath = new ArrayList<>();
        File classpathFile = new File(System.getProperty("user.dir"),"build/integrationTest/resources/pluginClassPath.txt");
        List<String> lines = Files.readAllLines(classpathFile.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            classpath.add(new File(line));
        }
        return classpath;
    }

    public static String getPathToInitScript(){
        return new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/init.gradle").getAbsolutePath();
    }
}
