package io.github.sanyavertolet.edukate.common.checks;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubmissionContext {
    private String submissionId;
    private String problemId;
    private String problemText;
    private List<String> problemImageUrls;
    private List<String> submissionImageUrls;
}
