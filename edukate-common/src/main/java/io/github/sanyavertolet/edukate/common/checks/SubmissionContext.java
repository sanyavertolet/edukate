package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class SubmissionContext {
    private String submissionId;
    private String problemId;
    private String problemText;
    private List<String> problemImageUrls;
    private List<String> submissionImageUrls;
}
