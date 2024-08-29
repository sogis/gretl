package ch.so.agi.gretl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Utility class for running integration tests.
 * Handles execution of test jobs either via Gradle or a runtime image.
 */
public class IntegrationTestUtil {
    private static final String newline = System.lineSeparator();
    private static final String TESTTYPE = System.getProperty("GRETL_TESTTYPE");
    private static final String TESTTYPE_JAR = "jar";
    private static final String TESTTYPE_IMAGE = "image";
    private static final String GRETL_PROJECT_ABS_PATH = System.getProperty("GRETL_PROJECT_ABS_PATH");
    private static final String ROOT_PROJECT_ABS_PATH = System.getProperty("ROOT_PROJECT_ABS_PATH");

    static {
        if (TESTTYPE == null || TESTTYPE.isEmpty()) {
            throw new RuntimeException("Environment variable GRETL_TESTTYPE not present or not set to valid value");
        }

        if (GRETL_PROJECT_ABS_PATH == null || GRETL_PROJECT_ABS_PATH.isEmpty()) {
            throw new RuntimeException("Environment variable GRETL_PROJECT_ABS_PATH not present or not set to valid value");
        }
    }

    /**
     * Runs a job located at the specified job path.
     *
     * @param jobPath the path to the job to run
     * @throws Exception if an error occurs during job execution
     */
    public static void runJob(String jobPath) throws Exception {
        runJob(jobPath, null);
    }

    /**
     * Runs a job located at the specified job path with the given Gradle variables.
     *
     * @param jobPath the path to the job to run
     * @param variables an array of Gradle variables to pass to the job
     * @throws Exception if an error occurs during job execution
     */
    public static void runJob(String jobPath, GradleVariable[] variables) throws Exception {
        int ret = runJob(jobPath, variables, new StringBuffer(), new StringBuffer());
        assertThat(ret, is(0));
    }

    /**
     * Builds the option string for the given Gradle variables.
     *
     * @param variables an array of Gradle variables
     * @return the option string for the variables
     */
    private static String buildOptionString(GradleVariable[] variables) {
        String varText = "";
        if (variables != null && variables.length > 0) {
            StringBuffer buf = new StringBuffer();
            for (GradleVariable var: variables){
                buf.append(" ");
                buf.append(var.buildOptionString());
            }
            buf.append(" -s "); // TODO: expose
            varText = buf.toString();
        }
        return varText;
    }

    /**
     * Runs a job located at the specified job path with the given Gradle variables and collects the standard output and error.
     *
     * @param jobPath the path to the job to run
     * @param variables an array of Gradle variables to pass to the job
     * @param stderr a StringBuffer to collect the standard error
     * @param stdout a StringBuffer to collect the standard output
     * @return the exit value of the process running the job
     * @throws Exception if an error occurs during job execution
     */
    public static int runJob(String jobPath, GradleVariable[] variables, StringBuffer stderr, StringBuffer stdout) throws Exception {
        if (stderr == null) {
            throw new IllegalArgumentException("stderr must not be null");
        }
        if (stdout == null) {
            throw new IllegalArgumentException("stdout must not be null");
        }

        String runtimeCommand = buildRuntimeCommand();
        String jobDirOption = buildJobDirOption(jobPath);
        String varsString = buildOptionString(variables);

        String command = String.format("%s %s %s", runtimeCommand, jobDirOption, varsString);

        System.out.println("command:" + command);
        Process p = Runtime.getRuntime().exec(command);

        appendProcessOutputToStdStreams(p, stderr, stdout);

        p.waitFor();

        System.out.printf("Here is the standard output of the command [%s]:\n%n", command);
        System.out.print(stdout);
        System.out.printf("Here is the standard error of the command [%s] (if any):\n%n", command);
        System.out.print(stderr);

        return p.exitValue();
    }

    /**
     * Appends the output of the process to the provided standard error and standard output buffers.
     *
     * @param p the process whose output is to be collected
     * @param stderr a StringBuffer to collect the standard error
     * @param stdout a StringBuffer to collect the standard output
     */
    private static void appendProcessOutputToStdStreams(Process p, StringBuffer stderr, StringBuffer stdout){
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        new Thread() {
            public void run() {
                try {
                    String s;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                        if(stdout!=null) {
                            stdout.append(s);
                            stdout.append(newline);
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

        // read any errors from the attempted command
        new Thread() {
            public void run() {
                try {
                    String s;
                    while ((s = stdError.readLine()) != null) {
                        stderr.append(s);
                        stderr.append(newline);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Builds the runtime command to execute the job based on the test type.
     *
     * @return the runtime command
     */
    public static String buildRuntimeCommand() {
        String runtimeCommand = null;

        Path projectAbsPath = Paths.get(ROOT_PROJECT_ABS_PATH);
        Path subProjectAbsPath = Paths.get(GRETL_PROJECT_ABS_PATH);

        if(TESTTYPE_JAR.equalsIgnoreCase(TESTTYPE)) { //needs call to gradle(w) with init.gradle
            String tool="gradlew";
            if(System.getProperty("os.name").contains("Windows")){
                tool="gradlew.bat";
            }

            String initPath = subProjectAbsPath.resolve("src/integrationTest/jobs/init.gradle").toString();            
            String toolWithPath = projectAbsPath.resolve(tool).toString();
            runtimeCommand = String.format("%s --init-script %s", toolWithPath, initPath);
        }
        else if (TESTTYPE_IMAGE.equalsIgnoreCase(TESTTYPE)){
            runtimeCommand = projectAbsPath.resolve("runtimeImage/start-gretl.sh").toString();
        }

        return runtimeCommand;
    }

    /**
     * Builds the job directory option for the specified job path based on the test type.
     *
     * @param relativeJobPath the relative path to the job
     * @return the job directory option
     */
    public static String buildJobDirOption(String relativeJobPath){
        String buildJobDirOption = null;

        String absoluteJobPath = Paths.get(GRETL_PROJECT_ABS_PATH).resolve(relativeJobPath).toString();
        System.out.println(absoluteJobPath);

        if(TESTTYPE_JAR.equalsIgnoreCase(TESTTYPE)) {
            buildJobDirOption = "--project-dir " + absoluteJobPath;
        }
        else if (TESTTYPE_IMAGE.equalsIgnoreCase(TESTTYPE)){
            buildJobDirOption = "--job_directory " + absoluteJobPath;
        }

        return buildJobDirOption;
    }
}
