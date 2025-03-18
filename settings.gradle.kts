import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.gradle.ReckonExtension

rootProject.name = "edukate"

plugins {
    id("org.ajoberstar.reckon.settings") version "0.19.1"
}

extensions.configure<ReckonExtension> {
    setDefaultInferredScope(Scope.MINOR.name)
    stages("rc", "final")
    setScopeCalc(calcScopeFromProp())
    setStageCalc(calcStageFromProp())
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("edukate-backend")
include("edukate-common")
include("edukate-gateway")
include("edukate-auth")
