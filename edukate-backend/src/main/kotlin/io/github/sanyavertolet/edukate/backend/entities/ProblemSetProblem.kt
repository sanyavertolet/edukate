package io.github.sanyavertolet.edukate.backend.entities

import org.springframework.data.relational.core.mapping.Table

@Table("problem_set_problems") data class ProblemSetProblem(val problemSetId: Long, val problemId: Long, val position: Int)
