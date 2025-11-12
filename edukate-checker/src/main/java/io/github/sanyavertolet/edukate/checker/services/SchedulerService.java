package io.github.sanyavertolet.edukate.checker.services;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class SchedulerService {

    public Mono<ResponseEntity<Void>> schedule(Supplier<Mono<Void>> supplier) {
        supplier.get().subscribeOn(Schedulers.boundedElastic()).subscribe();
        return Mono.just(ResponseEntity.accepted().build());
    }
}
