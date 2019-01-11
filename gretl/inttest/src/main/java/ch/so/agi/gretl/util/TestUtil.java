package ch.so.agi.gretl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestUtil {
    private static final String newline=System.getProperty("line.separator");

    private static final String TESTTYPE = System.getProperty("GRETL_TESTTYPE");
    private static final String TESTTYPE_JAR = "jar";
    private static final String TESTTYPE_IMAGE = "image";

    private static final String INTTESTFOLDER_ABS_PATH = System.getProperty("GRETL_INTTESTFOLDER_ABS_PATH");

    static{
        if (TESTTYPE == null || TESTTYPE.length() == 0){
            throw new RuntimeException("Environment variable GRETL_TESTTYPE not present or not set to valid value");
        }

        if (INTTESTFOLDER_ABS_PATH == null || INTTESTFOLDER_ABS_PATH.length() == 0){
            throw new RuntimeException("Environment variable GRETL_INTTESTFOLDER_ABS_PATH not present or not set to valid value");
        }
    }

    public static void runJob(String jobPath) throws Exception {
        runJob(jobPath, null);
    }

    public static void runJob(String jobPath, GradleVariable[] variables) throws Exception {
        int ret=runJob(jobPath,variables,new StringBuffer(),new StringBuffer());
        assertThat(ret, is(0));
    }

    private static String buildOptionString(GradleVariable[] variables) {
        String varText = "";
        if(variables != null && variables.length > 0){
            StringBuffer buf = new StringBuffer();
            for(GradleVariable var: variables){
                buf.append(" ");
                buf.append(var.buildOptionString());
            }
            varText = buf.toString();
        }
        return varText;
    }

    public static int runJob(String jobPath, GradleVariable[] variables, StringBuffer stderr, StringBuffer stdout) throws Exception {
        if(stderr==null) {
            throw new IllegalArgumentException("stderr must not be null");
        }
        if(stdout==null) {
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

        System.out.println(String.format("Here is the standard output of the command [%s]:\n", command));
        System.out.print(stdout);
        System.out.println(String.format("Here is the standard error of the command [%s] (if any):\n", command));
        System.out.print(stderr);

        return p.exitValue();
    }

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


    public static String buildRuntimeCommand(){
        String runtimeCommand = null;

        Path repoRootDir = Paths.get(INTTESTFOLDER_ABS_PATH).getParent().getParent();

        if(TESTTYPE_JAR.equalsIgnoreCase(TESTTYPE)) { //needs call to gradle(w) with init.gradle
            String tool="gradlew";
            if(System.getProperty("os.name").contains("Windows")){
                tool="gradlew.bat";
            }

            String toolWithPath = repoRootDir.resolve(tool).toString();
            runtimeCommand = String.format("%s --init-script ../init.gradle", toolWithPath);
        }
        else if (TESTTYPE_IMAGE.equalsIgnoreCase(TESTTYPE)){
            runtimeCommand = repoRootDir.resolve("runtimeImage/start-gretl.sh").toString();
        }

        return runtimeCommand;
    }

    public static String buildJobDirOption(String relativeJobPath){
        String buildJobDirOption = null;

        String absoluteJobPath = Paths.get(INTTESTFOLDER_ABS_PATH).resolve(relativeJobPath).toString();

        if(TESTTYPE_JAR.equalsIgnoreCase(TESTTYPE)) {
            buildJobDirOption = "--project-dir " + absoluteJobPath;
        }
        else if (TESTTYPE_IMAGE.equalsIgnoreCase(TESTTYPE)){
            buildJobDirOption = "--job_directory " + absoluteJobPath;
        }

        return buildJobDirOption;
    }
}
