package conf.live.cfp.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "conf.live.cfp")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.port..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("domain/model and domain/port must stay free of framework dependencies (domain purity)");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_jpa =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.port..")
                    .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                    .because("domain/model and domain/port must stay free of JPA dependencies (domain purity)");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_bean_validation =
            noClasses()
                    .that().resideInAnyPackage("..domain.model..", "..domain.port..")
                    .should().dependOnClassesThat().resideInAPackage("jakarta.validation..")
                    .because("domain/model and domain/port must stay free of Bean Validation dependencies (domain purity)");

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..")
                    .because("application depends only on domain ports, never on concrete adapters");

    @ArchTest
    static final ArchRule adapters_must_not_be_depended_on_by_domain_or_application =
            noClasses()
                    .that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..")
                    .because("dependencies must only point inward: domain and application never depend on adapters");

    private static final String ROOT_PACKAGE = "conf.live.cfp.";

    /**
     * Extracts the bounded-context domain name from a package residing under {@value #ROOT_PACKAGE}
     * (e.g. "conf.live.cfp.proposal.domain.model" -&gt; "proposal"). Returns null for classes that are
     * not part of any domain package (e.g. the application's root package, or this architecture test itself).
     */
    private static String domainOf(String packageName) {
        if (!packageName.startsWith(ROOT_PACKAGE)) {
            return null;
        }
        String rest = packageName.substring(ROOT_PACKAGE.length());
        int firstDot = rest.indexOf('.');
        String domain = firstDot >= 0 ? rest.substring(0, firstDot) : rest;
        return domain.isEmpty() || domain.equals("architecture") ? null : domain;
    }

    private static final ArchCondition<JavaClass> DEPEND_ON_ANOTHER_DOMAINS_MODEL_OR_ADAPTER =
            new ArchCondition<>("depend on another domain's model or adapter packages") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    String originDomain = domainOf(javaClass.getPackageName());
                    if (originDomain == null) {
                        return;
                    }
                    for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                        JavaClass targetClass = dependency.getTargetClass();
                        String targetPackage = targetClass.getPackageName();
                        String targetDomain = domainOf(targetPackage);
                        if (targetDomain == null || targetDomain.equals(originDomain)) {
                            continue;
                        }
                        boolean touchesModelOrAdapter =
                                targetPackage.contains(".domain.model") || targetPackage.contains(".adapter");
                        if (touchesModelOrAdapter) {
                            String message = String.format(
                                    "%s depends on %s, reaching into the '%s' domain's model or adapter package "
                                            + "instead of only its port.in",
                                    javaClass.getName(), targetClass.getName(), targetDomain);
                            // This condition is wrapped by noClasses(), which negates each event's polarity
                            // (ArchCondition#never()) before evaluating the rule. So the bad case we detect
                            // here must be reported as "satisfied" (matches the condition's description),
                            // not "violated" - .violated() here would get flipped to "satisfied" by the
                            // negation and the rule would never fail, which is exactly the bug this comment
                            // is guarding against.
                            events.add(SimpleConditionEvent.satisfied(javaClass, message));
                        }
                    }
                }
            };

    @ArchTest
    static final ArchRule domains_must_not_reach_into_other_domains_internals =
            noClasses()
                    .that().resideInAPackage("conf.live.cfp..")
                    .should(DEPEND_ON_ANOTHER_DOMAINS_MODEL_OR_ADAPTER)
                    .because("a domain may only be reached through another domain's port.in, never through its "
                            + "model or adapter packages directly (catches cross-domain leaks once a second domain exists)");

    @ArchTest
    static final ArchRule spring_mvc_controller_annotations_are_confined_to_the_web_adapter =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .or().areAnnotatedWith(RestControllerAdvice.class)
                    .should().resideInAPackage("..adapter.in.web..")
                    .because("@RestController/@RestControllerAdvice are the web adapter's concern for a given "
                            + "domain and must not appear anywhere else");

}
