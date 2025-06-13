package io.github.sanyavertolet.edukate.backend.configs;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.utils.SemVerUtils;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import reactor.util.function.Tuple3;

import java.util.Objects;

@Configuration
public class ProblemInsertionMongoEventListener extends AbstractMongoEventListener<Problem> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<Problem> event) {
        Problem entity = event.getSource();
        Document document = event.getDocument();
        Objects.requireNonNull(document);

        String problemId = entity.getId();

        Tuple3<Integer, Integer, Integer> tuple = SemVerUtils.parse(problemId);

        document.put(SemVerUtils.majorFieldName, tuple.getT1());
        document.put(SemVerUtils.minorFieldName, tuple.getT2());
        document.put(SemVerUtils.patchFieldName, tuple.getT3());
    }
}
