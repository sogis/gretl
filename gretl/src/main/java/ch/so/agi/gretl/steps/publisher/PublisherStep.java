package ch.so.agi.gretl.steps.publisher;

import java.util.Objects;

/**
 * Skeleton entry point for the modular Publisher implementation.
 */
public class PublisherStep {
    private final PublisherStepParameters parameters;

    public PublisherStep(PublisherStepParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    public PublisherStepParameters getParameters() {
        return parameters;
    }
}
