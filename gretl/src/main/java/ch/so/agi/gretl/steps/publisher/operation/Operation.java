package ch.so.agi.gretl.steps.publisher.operation;

/**
 * Common contract for publisher operations.
 */
public interface Operation {
    default String getHumanReadableName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the result detail for the success log line.
     *
     * <p>{@code OpSequenceRunner} combines this with {@link #getHumanReadableName()}
     * as {@code "&lt;name&gt;: &lt;detail&gt;"}. Implementations must therefore describe
     * the completed work without repeating their human-readable name. This method is
     * called only after {@link #execute()} completed successfully.</p>
     */
    String getSuccessLogDetail();

    /** Executes this single-use operation with the inputs supplied at construction time. */
    void execute() throws Exception;
}
