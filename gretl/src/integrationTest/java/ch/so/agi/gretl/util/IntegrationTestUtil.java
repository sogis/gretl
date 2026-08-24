package ch.so.agi.gretl.util;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class IntegrationTestUtil {

    private static final String newline=System.getProperty("line.separator");
    private static final String TEST_TYPE = System.getProperty("GRETL_TESTTYPE");
    private static final String GRETL_PROJECT_ABSOLUTE_PATH = System.getProperty("GRETL_PROJECT_ABS_PATH");
    private static final String ROOT_PROJECT_ABSOLUTE_PATH = System.getProperty("ROOT_PROJECT_ABS_PATH");
    private static final String INTEGRATION_LOG_LEVEL = System.getProperty("GRETL_INTEGRATION_LOG_LEVEL",
            "LIFECYCLE");

    public static void executeTestRunner(File projectDirectory) throws IOException {
        executeTestRunner(projectDirectory, null, null);
    }

    public static void executeTestRunner(File projectDirectory, GradleVariable[] variables) throws IOException {
        executeTestRunner(projectDirectory, variables, null);
    }

    public static void executeTestRunner(File projectDirectory, GradleVariable[] variables, String taskName) throws IOException {
        if (TestType.IMAGE.equals(TEST_TYPE)) {
            executeDockerRunCommand(projectDirectory, variables);
        } else if(TestType.JAR.equals(TEST_TYPE)) {
            System.out.print(executeGradleRunner(projectDirectory, taskName, variables, INTEGRATION_LOG_LEVEL));
        } else {
            throw new GretlException("Unknown test type: " + TEST_TYPE);
        }
    }

    /** Runs a TestKit job at the requested Gradle log level and returns its complete output. */
    public static String executeTestRunnerAndCaptureOutput(File projectDirectory, GradleVariable[] variables, String taskName,
            String logLevel) throws IOException {
        if (!TestType.JAR.equals(TEST_TYPE)) {
            throw new GretlException("Capturing Gradle output requires GRETL_TESTTYPE=JAR");
        }
        return executeGradleRunner(projectDirectory, taskName, variables, logLevel);
    }

    private static String executeGradleRunner(File projectDirectory, String taskName, GradleVariable[] variables,
            String logLevel) throws IOException {
        List<String> arguments = getRunnerArguments(taskName, variables, logLevel);
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDirectory)
                .withArguments(arguments)
                .build();
        return result.getOutput();
        //TaskOutcome outcome = Objects.requireNonNull(result.task(":" + taskName)).getOutcome();
    }

    private static void executeDockerRunCommand(File projectDirectory, GradleVariable[] variables) {
        String absolutePath = projectDirectory.getAbsolutePath();
        String jobPath = projectDirectory.getPath();

        String dockerRunCommand = getDockerRunCommand(jobPath, variables);
        StringBuffer stdError = new StringBuffer();
        StringBuffer stdOut = new StringBuffer();

        System.out.printf("Absolute Path: %s%n", absolutePath);
        System.out.print(stdOut);
        try {
            Process process = Runtime.getRuntime().exec(dockerRunCommand);
            appendProcessOutputToStdStreams(process, stdError, stdOut);
            process.waitFor();
            logDockerRunOutput(dockerRunCommand, stdError, stdOut);
            int result = process.exitValue();
            assertEquals(0, result);
        } catch (IOException | InterruptedException e) {
            throw new GretlException("Error while executing docker run command: " + dockerRunCommand, e);
        }
    }

    private static void logDockerRunOutput(String dockerRunCommand, StringBuffer stdError, StringBuffer stdOut) {
        System.out.printf("Here is the standard output of the command [%s]:\n%n", dockerRunCommand);
        System.out.print(stdOut);
        System.out.printf("Here is the standard error of the command [%s] (if any):\n%n", dockerRunCommand);
        System.out.print(stdError);
    }

    private static String getDockerRunCommand(String jobPath, GradleVariable[] variables) {
        String pathToRunCommandExecutionFile = Paths.get(ROOT_PROJECT_ABSOLUTE_PATH).resolve("runtimeImage/start-gretl.sh").toString();
        String jobDirectoryOption = buildJobDirectoryOptionString(jobPath);
        String gradleVariablesString = buildOptionString(variables);

        String logLevelOption = getLogLevelOption(INTEGRATION_LOG_LEVEL);
        return String.format("%s %s%s %s", pathToRunCommandExecutionFile, jobDirectoryOption, logLevelOption,
                gradleVariablesString);
    }

    private static String buildOptionString(GradleVariable[] variables) {
        if (variables == null || variables.length == 0) {
            return "";
        }
        StringBuilder optionString = new StringBuilder();
        for (GradleVariable variable : variables) {
            optionString.append(variable.buildOptionString()).append(" ");
        }
        return optionString.toString().trim();
    }

    public static String buildJobDirectoryOptionString(String relativeJobPath) {
        return "--job_directory " + Paths.get(GRETL_PROJECT_ABSOLUTE_PATH).resolve(relativeJobPath).toString();
    }

    private static List<String> getRunnerArguments(String taskName, GradleVariable[] variables, String logLevel) {
        List<String> arguments = new ArrayList<>();
        String logLevelOption = getLogLevelOption(logLevel);
        if (!logLevelOption.isEmpty()) {
            arguments.add(logLevelOption.trim());
        }
        arguments.add("--init-script");
        arguments.add(IntegrationTestUtil.getPathToInitScript());
        if (taskName != null) {
            arguments.add(taskName);
        }
        arguments.add("--rerun-tasks");
        arguments.add("-s");
        if (variables != null) {
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

    public static String getPathToInitScript() {
        return new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/init.gradle").getAbsolutePath();
    }

    static String getLogLevelOption(String logLevel) {
        if ("INFO".equalsIgnoreCase(logLevel)) {
            return " --info";
        }
        if ("DEBUG".equalsIgnoreCase(logLevel)) {
            return " --debug";
        }
        return "";
    }

    private static void appendProcessOutputToStdStreams(Process p, StringBuffer stderr, StringBuffer stdout) {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        new Thread(() -> {
            try {
                String s;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                    if(stdout != null) {
                        stdout.append(s);
                        stdout.append(newline);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                String s;
                while ((s = stdError.readLine()) != null) {
                    stderr.append(s);
                    stderr.append(newline);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
