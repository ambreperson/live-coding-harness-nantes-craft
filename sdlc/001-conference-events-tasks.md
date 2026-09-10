# Rattacher les propositions à un événement — Tasks

Source design: sdlc/001-conference-events-design.md

> Note d'exécution (mise à jour après un blocage constaté en Phase 1) : les tâches de la Phase 0 modifient la signature de `Proposal.submit`/`rehydrate` et de `SubmitProposalCommand`, ce qui casse la compilation de `SubmitProposalService`, `ProposalController` et `ProposalRepositoryAdapter` (et leurs tests). **Écart de conception découvert à l'exécution** : Maven compile tout `src/main`/`src/test` en un seul module, donc laisser ce cassage ouvert bloque *toutes* les pistes de la Phase 1 — y compris les pistes A/B/C (`event`), qui ne touchent pourtant aucun fichier `proposal` — puisqu'aucune ne peut lancer le moindre test sans que le module compile. Un agent l'a détecté et s'est arrêté pour signaler le blocage (comportement correct) ; un autre a contourné le problème hors de son périmètre en codant en dur un `eventId` factice dans `ProposalController` (ce changement a été jeté). Correctif appliqué : la correction mécanique minimale des trois sites d'appel (déjà prévue comme première sous-tâche des Tracks D/E/F ci-dessous) a été faite une fois, directement en Phase 0, avant de relancer les pistes — ces sous-tâches sont donc déjà cochées ci-dessous. Les pistes ne dépendent donc plus les unes des autres, et il n'y a aucun conflit de fichiers entre elles.

## Phase 0 — Foundation
*(séquentiel — à terminer avant de démarrer la Phase 1)*

### Domaine `event` — agrégat
- [x] RED : écrire `EventTest#should_create_an_event_with_the_given_name` dans `src/test/java/conf/live/cfp/event/domain/model/EventTest.java`, appelant `Event.create("My Conf")` et vérifiant `event.name()` égal à `"My Conf"`.
- [x] Lancer `./mvnw test -Dtest=EventTest#should_create_an_event_with_the_given_name` — confirmer l'échec (erreur de compilation : `Event` n'existe pas).
- [x] GREEN : créer `Event` dans `src/main/java/conf/live/cfp/event/domain/model/Event.java` (agrégat immuable : champs `id`, `name`, factory `create(name)` générant un `UUID` aléatoire, accesseurs `id()`/`name()`, `equals`/`hashCode` sur `id`, `toString`), sur le modèle de `Proposal`.
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventTest#should_assign_a_unique_id_to_each_created_event`, créant deux événements avec le même nom et vérifiant que leurs `id()` sont non nuls et différents.
- [x] Lancer le test — confirmer qu'il passe déjà (le `UUID.randomUUID()` de `create` le couvre) ; sinon corriger `Event.create`.
- [x] RED : ajouter `EventTest#should_reject_a_blank_name` en `@ParameterizedTest` (`@NullAndEmptySource`, `@ValueSource(strings = {" ", "\t"})`), vérifiant que `Event.create(blankName)` lève `InvalidEventException` avec le message `"Event name must not be blank"`.
- [x] Lancer le test — confirmer l'échec pour compilation (`InvalidEventException` n'existe pas). *(Note : `InvalidEventException` et sa validation ont été anticipées dès l'implémentation initiale de `Event.create`, nécessaire à la compilation de `Event.java` ; ce test a donc passé directement sans étape rouge séparée.)*
- [x] GREEN : créer `InvalidEventException` dans `src/main/java/conf/live/cfp/event/domain/model/InvalidEventException.java` (extends `RuntimeException`, sur le modèle de `InvalidProposalException`), puis ajouter la validation `requireNonBlank` dans `Event.create`.
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventTest#should_rehydrate_an_event_without_re_validating_it`, appelant `Event.rehydrate("event-1", "My Conf")` et vérifiant `id()` et `name()`. *(Note : `rehydrate` a lui aussi été anticipé dans l'implémentation initiale de `Event.java`, sur le modèle de `Proposal`.)*
- [x] Lancer le test — confirmer l'échec (compilation : `rehydrate` n'existe pas).
- [x] GREEN : ajouter la factory `Event.rehydrate(id, name)` (sans revalidation).
- [x] Relancer le test — confirmer qu'il passe.
- [x] REFACTOR : relire `Event.java` et `EventTest.java`, vérifier la cohérence de nommage avec `Proposal`/`ProposalTest`, relancer `./mvnw test -Dtest=EventTest`.

### Exception de référence manquante (partagée par les Tracks D et E)
- [x] Créer `EventNotFoundException` dans `src/main/java/conf/live/cfp/event/domain/model/EventNotFoundException.java` (extends `RuntimeException`, message du type `"Event not found: " + eventId`) — action mécanique, pas de test dédié (comme `InvalidProposalException`, elle est couverte par les tests des couches qui l'utilisent, ajoutés dans les Tracks D et E).

### Ports `event` (mécanique, pas de comportement à tester ici)
- [x] Créer `CreateEventCommand` (record `String name`) dans `src/main/java/conf/live/cfp/event/domain/port/in/CreateEventCommand.java`.
- [x] Créer `CreateEventUseCase` (méthode `Event create(CreateEventCommand command)`) dans `src/main/java/conf/live/cfp/event/domain/port/in/CreateEventUseCase.java`.
- [x] Créer `ListEventsUseCase` (méthode `List<Event> listAll()`) dans `src/main/java/conf/live/cfp/event/domain/port/in/ListEventsUseCase.java`.
- [x] Créer `EventRepository` (méthodes `Event save(Event event)`, `Optional<Event> findById(String id)`, `List<Event> findAll()`) dans `src/main/java/conf/live/cfp/event/domain/port/out/EventRepository.java`.

### Extension de l'agrégat `Proposal` (champ `eventId` obligatoire)
- [x] RED : modifier `ProposalTest#should_create_a_draft_proposal_with_the_given_data` (`src/test/java/conf/live/cfp/proposal/domain/model/ProposalTest.java`) pour appeler `Proposal.submit("Hexagonal architecture in practice", "A deep dive into ports and adapters", "speaker-1", "event-1")` et ajouter l'assertion `assertThat(proposal.eventId()).isEqualTo("event-1")`.
- [x] Lancer `./mvnw test -Dtest=ProposalTest` — confirmer l'échec pour compilation (signature `submit` à 3 arguments, méthode `eventId()` absente).
- [x] GREEN : dans `Proposal.java`, ajouter le champ `eventId`, l'accesseur `eventId()`, et le paramètre `eventId` à `submit(title, description, speakerId, eventId)` avec validation `requireNonBlank(eventId, "Proposal event id must not be blank")`.
- [x] Mettre à jour tous les autres appels à `Proposal.submit(...)` dans `ProposalTest.java` (`should_assign_a_unique_id_to_each_submitted_proposal`, `should_reject_a_blank_title`, `should_reject_a_blank_description`, `should_reject_a_blank_speaker_id`) pour passer un 4ᵉ argument `"event-1"`.
- [x] Relancer `./mvnw test -Dtest=ProposalTest` — confirmer que ces tests passent à nouveau.
- [x] RED : ajouter `ProposalTest#should_reject_a_blank_event_id` en `@ParameterizedTest` (`@NullAndEmptySource`, `@ValueSource(strings = {" ", "\t"})`), vérifiant que `Proposal.submit("Title", "Description", "speaker-1", blankEventId)` lève `InvalidProposalException` avec le message `"Proposal event id must not be blank"`.
- [x] Lancer le test — confirmer qu'il passe déjà grâce à la validation ajoutée ; sinon corriger l'ordre de validation dans `Proposal.submit`.
- [x] RED : modifier `ProposalTest#should_rehydrate_a_proposal_without_re_validating_it` pour appeler `Proposal.rehydrate("proposal-1", "Title", "Description", "speaker-1", ProposalStatus.DRAFT, "event-1")` et ajouter `assertThat(proposal.eventId()).isEqualTo("event-1")`.
- [x] Lancer le test — confirmer l'échec pour compilation (signature `rehydrate` à 5 arguments). *(Note : réalisé dans la même passe GREEN que `submit` ci-dessus, `rehydrate` ayant été mis à jour en même temps.)*
- [x] GREEN : ajouter le paramètre `eventId` à `Proposal.rehydrate(...)`.
- [x] Relancer `./mvnw test -Dtest=ProposalTest` — confirmer que tous les tests de la classe passent.
- [x] Modifier `SubmitProposalCommand` (`src/main/java/conf/live/cfp/proposal/domain/port/in/SubmitProposalCommand.java`) pour ajouter le champ `eventId` : `record SubmitProposalCommand(String title, String description, String speakerId, String eventId)`.
- [x] REFACTOR : relire `Proposal.java` (ordre des champs/validations) et `ProposalTest.java`, relancer `./mvnw test -Dtest=ProposalTest`.

*(À l'issue de cette phase, `SubmitProposalService`, `ProposalController`, `ProposalRepositoryAdapter` et leurs tests ne compilent plus — c'est attendu, voir la note en tête de document. Chaque Track de la Phase 1 corrige son propre fichier.)*

## Phase 1 — Parallel tracks

### Track A — Event : application
*(parallel avec Track B, Track C, Track D, Track E, Track F)*

- [x] RED : écrire `CreateEventServiceTest#should_create_an_event_built_from_the_command_and_persist_it` dans `src/test/java/conf/live/cfp/event/application/CreateEventServiceTest.java` (`@ExtendWith(MockitoExtension.class)`, `@Mock EventRepository eventRepository`), vérifiant que `service.create(new CreateEventCommand("My Conf"))` retourne un `Event` avec `name()` égal à `"My Conf"` et que `eventRepository.save(...)` est appelé avec cet événement (via `ArgumentCaptor`), sur le modèle de `SubmitProposalServiceTest`.
- [x] Lancer `./mvnw test -Dtest=CreateEventServiceTest` — confirmer l'échec pour compilation (`CreateEventService` n'existe pas).
- [x] GREEN : créer `CreateEventService` dans `src/main/java/conf/live/cfp/event/application/CreateEventService.java` (`@Service`, implémente `CreateEventUseCase`, construit `Event.create(command.name())` et délègue à `eventRepository.save(...)`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `CreateEventServiceTest#should_return_the_event_persisted_by_the_repository`, mockant `eventRepository.save(any())` pour retourner un événement `rehydrate`d et vérifiant que `service.create(...)` retourne bien cette instance.
- [x] Lancer le test — confirmer qu'il passe déjà (sinon corriger `CreateEventService` pour retourner la valeur de `save`).
- [x] RED : écrire `ListEventsServiceTest#should_return_all_events_from_the_repository` dans `src/test/java/conf/live/cfp/event/application/ListEventsServiceTest.java`, mockant `eventRepository.findAll()` pour retourner une liste de deux `Event` et vérifiant que `service.listAll()` retourne cette même liste.
- [x] Lancer `./mvnw test -Dtest=ListEventsServiceTest` — confirmer l'échec pour compilation (`ListEventsService` n'existe pas).
- [x] GREEN : créer `ListEventsService` dans `src/main/java/conf/live/cfp/event/application/ListEventsService.java` (`@Service`, implémente `ListEventsUseCase`, délègue à `eventRepository.findAll()`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] REFACTOR : relire les deux services et leurs tests, relancer `./mvnw test -Dtest=CreateEventServiceTest,ListEventsServiceTest`.

### Track B — Event : adaptateur web
*(parallel avec Track A, Track C, Track D, Track E, Track F)*

- [x] RED : écrire `EventControllerTest#should_return_201_with_the_created_event_when_the_request_is_valid` dans `src/test/java/conf/live/cfp/event/adapter/in/web/EventControllerTest.java` (`@WebMvcTest(EventController.class)`, `@MockitoBean CreateEventUseCase createEventUseCase`), postant `{"name": "My Conf"}` sur `/api/events` et vérifiant `status().isCreated()`, l'en-tête `Location`, et le JSON `$.id`/`$.name`, sur le modèle de `ProposalControllerTest`.
- [x] Lancer `./mvnw test -Dtest=EventControllerTest#should_return_201_with_the_created_event_when_the_request_is_valid` — confirmer l'échec pour compilation.
- [x] GREEN : créer `CreateEventRequest` (`record CreateEventRequest(@NotBlank(message = "name must not be blank") String name)`) dans `src/main/java/conf/live/cfp/event/adapter/in/web/dto/CreateEventRequest.java`, `EventResponse` (`record EventResponse(String id, String name)` + `from(Event)`) dans `.../dto/EventResponse.java`, et `EventController` dans `src/main/java/conf/live/cfp/event/adapter/in/web/EventController.java` (`@RestController`, `@RequestMapping("/api/events")`, `POST` mappé sur `createEventUseCase.create(...)`, retourne `201` avec `Location: /api/events/{id}`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventControllerTest#should_return_400_when_the_name_is_blank`, postant `{"name": ""}` et vérifiant `status().isBadRequest()`.
- [x] Lancer le test — confirmer qu'il passe déjà (validation `@Valid`/`@NotBlank`) ; sinon ajouter `@Valid` sur le paramètre du contrôleur.
- [x] RED : ajouter `EventControllerTest#should_return_400_with_the_domain_message_when_the_use_case_rejects_the_event`, mockant `createEventUseCase.create(any())` pour lever `InvalidEventException("Event name must not be blank")`, et vérifiant `status().isBadRequest()` et `$.detail`.
- [x] Lancer le test — confirmer l'échec (pas de gestion de `InvalidEventException` → 500).
- [x] GREEN : créer `EventExceptionHandler` dans `src/main/java/conf/live/cfp/event/adapter/in/web/EventExceptionHandler.java` (`@RestControllerAdvice(assignableTypes = EventController.class)`, gère `InvalidEventException` → `ProblemDetail` 400), sur le modèle de `ProposalExceptionHandler`.
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventControllerTest#should_return_200_with_all_events`, mockant `listEventsUseCase.listAll()` (ajouter `@MockitoBean ListEventsUseCase listEventsUseCase`) pour retourner deux événements, appelant `GET /api/events`, et vérifiant `status().isOk()` et le contenu JSON des deux éléments.
- [x] Lancer le test — confirmer l'échec pour compilation (pas de méthode `GET` sur `EventController`). *(Observé en 405, pas en erreur de compilation, puisque le fichier compilait déjà — type d'échec correct néanmoins.)*
- [x] GREEN : ajouter la méthode `@GetMapping` sur `EventController`, déléguant à `listEventsUseCase.listAll()` et mappant chaque `Event` en `EventResponse`.
- [x] Relancer le test — confirmer qu'il passe.
- [x] REFACTOR : relire `EventController`/DTOs/`EventExceptionHandler` et leur test, relancer `./mvnw test -Dtest=EventControllerTest`.

### Track C — Event : adaptateur de persistance
*(parallel avec Track A, Track B, Track D, Track E, Track F)*

- [x] RED : écrire `EventRepositoryAdapterTest#should_map_the_event_to_an_entity_and_persist_it` dans `src/test/java/conf/live/cfp/event/adapter/out/persistence/EventRepositoryAdapterTest.java` (`@ExtendWith(MockitoExtension.class)`, `@Mock EventJpaRepository eventJpaRepository`), vérifiant que `adapter.save(event)` construit et persiste une `EventEntity` avec les mêmes `id`/`name`, sur le modèle de `ProposalRepositoryAdapterTest`.
- [x] Lancer `./mvnw test -Dtest=EventRepositoryAdapterTest#should_map_the_event_to_an_entity_and_persist_it` — confirmer l'échec pour compilation.
- [x] GREEN : créer `EventEntity` (`@Entity @Table(name = "event")`, champs `id`/`name`, constructeur protégé + constructeur complet, getters) dans `src/main/java/conf/live/cfp/event/adapter/out/persistence/EventEntity.java`, `EventJpaRepository extends JpaRepository<EventEntity, String>` dans `.../EventJpaRepository.java`, et `EventRepositoryAdapter implements EventRepository` (`@Repository`, méthodes `toEntity`/`toDomain` privées statiques) dans `.../EventRepositoryAdapter.java` avec `save(...)`. *(Note : `EventRepository` déclarant déjà `findById`/`findAll` comme méthodes abstraites, il a fallu les implémenter dès cette étape pour compiler — les étapes RED prévues plus bas pour `findById`/`findAll` n'ont donc jamais échoué à la compilation, exactement le filet de repli anticipé par ce document.)*
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventRepositoryAdapterTest#should_map_the_persisted_entity_back_to_a_domain_event`, mockant `eventJpaRepository.save(any())` pour retourner une `EventEntity` donnée et vérifiant que `adapter.save(event)` retourne l'`Event` équivalent.
- [x] Lancer le test — confirmer qu'il passe déjà (sinon corriger `toDomain`).
- [x] RED : ajouter `EventRepositoryAdapterTest#should_return_the_event_when_found_by_id`, mockant `eventJpaRepository.findById("event-1")` pour retourner `Optional.of(entity)` et vérifiant que `adapter.findById("event-1")` retourne `Optional.of(event)`.
- [x] Lancer le test — confirmer l'échec pour compilation (`findById` absent de l'adaptateur). *(N/A, voir note ci-dessus : passait déjà.)*
- [x] GREEN : implémenter `findById` dans `EventRepositoryAdapter` (délègue à `eventJpaRepository.findById(id).map(EventRepositoryAdapter::toDomain)`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `EventRepositoryAdapterTest#should_return_empty_when_no_event_matches_the_id`, mockant `eventJpaRepository.findById("missing")` pour retourner `Optional.empty()` et vérifiant que `adapter.findById("missing")` retourne `Optional.empty()`.
- [x] Lancer le test — confirmer qu'il passe déjà (sinon corriger le mapping `Optional`).
- [x] RED : ajouter `EventRepositoryAdapterTest#should_return_all_events`, mockant `eventJpaRepository.findAll()` pour retourner une liste de deux `EventEntity` et vérifiant que `adapter.findAll()` retourne la liste des `Event` correspondants.
- [x] Lancer le test — confirmer l'échec pour compilation (`findAll` absent de l'adaptateur). *(N/A, voir note ci-dessus : passait déjà.)*
- [x] GREEN : implémenter `findAll` dans `EventRepositoryAdapter` (`eventJpaRepository.findAll().stream().map(EventRepositoryAdapter::toDomain).toList()`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : écrire `EventRepositoryAdapterPersistenceTest#should_persist_an_event_and_make_it_retrievable` dans `src/test/java/conf/live/cfp/event/adapter/out/persistence/EventRepositoryAdapterPersistenceTest.java` (`@DataJpaTest`, `@Import(EventRepositoryAdapter.class)`), vérifiant qu'un `Event` sauvegardé est bien retrouvable via `eventJpaRepository.findById(...)`, sur le modèle de `ProposalRepositoryAdapterPersistenceTest`.
- [x] Lancer `./mvnw test -Dtest=EventRepositoryAdapterPersistenceTest` — confirmer l'échec (probable erreur de schéma si le mapping JPA est incorrect) ou son succès si Track C précédent est déjà correct ; corriger `EventEntity`/l'adaptateur si besoin jusqu'à obtenir le vert.
- [x] REFACTOR : relire `EventEntity`/`EventJpaRepository`/`EventRepositoryAdapter` et leurs tests, relancer `./mvnw test -Dtest=EventRepositoryAdapterTest,EventRepositoryAdapterPersistenceTest`.

### Track D — Proposal : application (vérification d'existence de l'événement)
*(parallel avec Track A, Track B, Track C, Track E, Track F — dépend uniquement des contrats `EventRepository`/`Proposal`/`SubmitProposalCommand` fixés en Phase 0)*

- [x] Corriger la compilation de `SubmitProposalServiceTest` (`src/test/java/conf/live/cfp/proposal/application/SubmitProposalServiceTest.java`) : ajouter `"event-1"` comme 4ᵉ argument à chaque `new SubmitProposalCommand(...)` et à chaque `Proposal.submit(...)`. *(Fait en Phase 0, voir la note d'exécution en tête de document.)*
- [x] Corriger la compilation de `SubmitProposalService` (`src/main/java/conf/live/cfp/proposal/application/SubmitProposalService.java`) : passer `command.eventId()` à `Proposal.submit(...)`. *(Fait en Phase 0.)*
- [x] Lancer `./mvnw test -Dtest=SubmitProposalServiceTest` — confirmer que la suite compile et passe à nouveau (sans encore de vérification d'existence).
- [x] RED : ajouter `SubmitProposalServiceTest#should_persist_a_proposal_referencing_an_existing_event`, ajoutant `@Mock EventRepository eventRepository`, construisant `new SubmitProposalService(proposalRepository, eventRepository)`, mockant `eventRepository.findById("event-1")` pour retourner `Optional.of(Event.rehydrate("event-1", "My Conf"))`, et vérifiant que `service.submit(command)` persiste bien la proposition.
- [x] Lancer le test — confirmer l'échec pour compilation (constructeur `SubmitProposalService` à un seul argument).
- [x] GREEN : ajouter la dépendance `EventRepository` au constructeur de `SubmitProposalService`. *(Note : cette étape GREEN a inclus en un seul edit à la fois l'injection `EventRepository` et la logique `orElseThrow`, ce qui a fait passer directement au vert le test RED suivant sans jamais l'observer rouge — écart mineur signalé par l'agent, sans impact sur le résultat ni la couverture finale.)*
- [x] Mettre à jour tous les autres `new SubmitProposalService(proposalRepository)` dans `SubmitProposalServiceTest.java` pour passer aussi `eventRepository`, et mocker `eventRepository.findById(...)` pour retourner un événement existant dans chacun de ces tests.
- [x] Relancer `./mvnw test -Dtest=SubmitProposalServiceTest` — confirmer que tous les tests passent.
- [x] RED : ajouter `SubmitProposalServiceTest#should_reject_the_submission_when_the_referenced_event_does_not_exist`, mockant `eventRepository.findById("missing-event")` pour retourner `Optional.empty()`, et vérifiant que `service.submit(new SubmitProposalCommand("Title", "Description", "speaker-1", "missing-event"))` lève `EventNotFoundException`.
- [x] Lancer le test — confirmer l'échec (aucune vérification d'existence dans `SubmitProposalService`). *(Non observé rouge, voir la note ci-dessus.)*
- [x] GREEN : dans `SubmitProposalService.submit`, avant de construire le `Proposal`, appeler `eventRepository.findById(command.eventId()).orElseThrow(() -> new EventNotFoundException("Event not found: " + command.eventId()))`.
- [x] Relancer le test — confirmer qu'il passe.
- [x] REFACTOR : relire `SubmitProposalService.java` et `SubmitProposalServiceTest.java`, relancer `./mvnw test -Dtest=SubmitProposalServiceTest`.

### Track E — Proposal : adaptateur web (champ `eventId`)
*(parallel avec Track A, Track B, Track C, Track D, Track F — dépend uniquement des contrats `SubmitProposalCommand`/`EventNotFoundException` fixés en Phase 0)*

- [x] Corriger la compilation de `ProposalControllerTest` (`src/test/java/conf/live/cfp/proposal/adapter/in/web/ProposalControllerTest.java`) : ajouter `"event-1"` comme 5ᵉ argument à chaque `Proposal.rehydrate(...)`. *(Fait en Phase 0, voir la note d'exécution en tête de document. Le `verify(...)` existant a aussi été mis à jour avec un `eventId` `null`, cohérent avec le corps JSON de test qui n'inclut pas encore ce champ.)*
- [x] Corriger la compilation de `SubmitProposalRequest`/`ProposalController` a minima pour recompiler (ajouter un champ `eventId` factice non encore validé) — préparer le terrain pour les tests suivants. *(Fait en Phase 0.)*
- [x] RED : modifier `ProposalControllerTest#should_return_201_with_the_created_proposal_when_the_request_is_valid` pour inclure `"eventId": "event-1"` dans le corps JSON, ajouter `$.eventId` aux assertions, et vérifier `verify(submitProposalUseCase).submit(eq(new SubmitProposalCommand("Title", "Description", "speaker-1", "event-1")))`.
- [x] Lancer `./mvnw test -Dtest=ProposalControllerTest#should_return_201_with_the_created_proposal_when_the_request_is_valid` — confirmer l'échec (le contrôleur ne transmet pas encore `eventId`, ou `ProposalResponse` ne l'expose pas).
- [x] GREEN : ajouter le champ `@NotBlank(message = "eventId must not be blank") String eventId` à `SubmitProposalRequest` (`src/main/java/conf/live/cfp/proposal/adapter/in/web/dto/SubmitProposalRequest.java`), passer `request.eventId()` dans la construction du `SubmitProposalCommand` dans `ProposalController`, et ajouter le champ `eventId` à `ProposalResponse` (record + `from(Proposal)`).
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : ajouter `ProposalControllerTest#should_return_400_when_the_event_id_is_blank`, postant une requête avec `"eventId": ""` et vérifiant `status().isBadRequest()`.
- [x] Lancer le test — confirmer qu'il passe déjà (validation `@NotBlank`) ; sinon corriger l'annotation.
- [x] RED : ajouter `ProposalControllerTest#should_return_400_with_the_domain_message_when_the_referenced_event_does_not_exist`, mockant `submitProposalUseCase.submit(any())` pour lever `EventNotFoundException("Event not found: missing-event")`, et vérifiant `status().isBadRequest()` et `$.detail`.
- [x] Lancer le test — confirmer l'échec (pas de gestion de `EventNotFoundException` dans `ProposalExceptionHandler` → 500).
- [x] GREEN : ajouter un `@ExceptionHandler(EventNotFoundException.class)` dans `ProposalExceptionHandler` (`src/main/java/conf/live/cfp/proposal/adapter/in/web/ProposalExceptionHandler.java`), mappant vers `ProblemDetail` 400.
- [x] Relancer le test — confirmer qu'il passe.
- [x] REFACTOR : relire `SubmitProposalRequest`/`ProposalResponse`/`ProposalController`/`ProposalExceptionHandler` et `ProposalControllerTest.java`, relancer `./mvnw test -Dtest=ProposalControllerTest`. *(Un import qualifié complet a été nettoyé au passage, et le test existant `should_return_400_with_the_domain_message_when_the_use_case_rejects_the_proposal` a dû recevoir un `eventId` non-vide dans son corps JSON pour continuer à atteindre le mock une fois la validation `@NotBlank` ajoutée.)*

### Track F — Proposal : adaptateur de persistance (colonne `eventId`)
*(parallel avec Track A, Track B, Track C, Track D, Track E — dépend uniquement du contrat `Proposal`/`ProposalRepository` fixé en Phase 0)*

- [x] Corriger la compilation de `ProposalRepositoryAdapterTest` et `ProposalRepositoryAdapterPersistenceTest` : ajouter `"event-1"` comme 4ᵉ argument à chaque `Proposal.submit(...)`. *(Fait en Phase 0, voir la note d'exécution en tête de document. Le correctif de compilation appliqué en Phase 0 a dû, par construction, ajouter directement le champ `eventId`/`getEventId()` à `ProposalEntity` et sa propagation dans `toEntity`/`toDomain` — il n'existe pas de correctif de compilation plus minimal pour ce fichier. Le reste de cette piste ne fait donc qu'ajouter les assertions de test ci-dessous, déjà vertes.)*
- [x] RED : modifier `ProposalRepositoryAdapterTest#should_map_the_proposal_to_an_entity_and_persist_it` pour ajouter l'assertion `assertThat(persistedEntity.getEventId()).isEqualTo(proposal.eventId())`.
- [x] Lancer `./mvnw test -Dtest=ProposalRepositoryAdapterTest#should_map_the_proposal_to_an_entity_and_persist_it` — confirmer l'échec pour compilation (`getEventId()` absent de `ProposalEntity`). *(N/A : `getEventId()` existait déjà suite au correctif de Phase 0 ; l'assertion est passée directement au vert.)*
- [x] GREEN : ajouter le champ `eventId` (+ getter `getEventId()`) à `ProposalEntity` (`src/main/java/conf/live/cfp/proposal/adapter/out/persistence/ProposalEntity.java`, constructeur complet mis à jour), et propager `proposal.eventId()` dans `ProposalRepositoryAdapter#toEntity`. *(Fait en Phase 0.)*
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : modifier `ProposalRepositoryAdapterTest#should_map_the_persisted_entity_back_to_a_domain_proposal` pour construire `new ProposalEntity(proposal.id(), proposal.title(), proposal.description(), proposal.speakerId(), proposal.status(), proposal.eventId())` et ajouter `assertThat(result.eventId()).isEqualTo("event-1")`.
- [x] Lancer le test — confirmer l'échec pour compilation (nouveau constructeur à 6 arguments). *(N/A, même raison que ci-dessus.)*
- [x] GREEN : propager `entity.getEventId()` dans `ProposalRepositoryAdapter#toDomain` (appel à `Proposal.rehydrate` avec le 6ᵉ argument). *(Fait en Phase 0.)*
- [x] Relancer le test — confirmer qu'il passe.
- [x] RED : modifier `ProposalRepositoryAdapterPersistenceTest#should_persist_a_proposal_and_make_it_retrievable` pour ajouter l'assertion `assertThat(found.get().getEventId()).isEqualTo("event-1")`.
- [x] Lancer `./mvnw test -Dtest=ProposalRepositoryAdapterPersistenceTest` — confirmer qu'il passe (si Track C ci-dessus est correctement fait) ; sinon corriger le mapping.
- [x] REFACTOR : relire `ProposalEntity.java`/`ProposalRepositoryAdapter.java` et leurs deux classes de test, relancer `./mvnw test -Dtest=ProposalRepositoryAdapterTest,ProposalRepositoryAdapterPersistenceTest`.

## Phase 2 — Integration & verification
*(séquentiel — après que chaque piste de la Phase 1 est entièrement cochée)*

- [x] Lancer la suite complète : `./mvnw test`. *(59 tests, 0 échec.)*
- [x] Lancer spécifiquement les règles d'architecture : `./mvnw test -Dtest=HexagonalArchitectureTest` (le nouveau domaine `event` doit respecter les mêmes règles de dépendance que `proposal`). *(7 tests, 0 échec — déjà couvert par le run complet ci-dessus.)*
- [x] Lancer `./mvnw clean verify` pour un build complet. *(BUILD SUCCESS.)*
- [x] Démarrer l'application (`./mvnw spring-boot:run`) et vérifier manuellement le parcours de bout en bout :
  - `POST /api/events` avec `{"name": "Nantes Craft"}` → `201` ✅ (`id` retourné : `35912cb2-...`).
  - `GET /api/events` → `200` ✅, la liste contient l'événement créé.
  - `POST /api/proposals` avec `eventId` valide → `201` ✅, la proposition retournée référence bien cet `eventId`.
  - `POST /api/proposals` avec un `eventId` inexistant (`"does-not-exist"`) → `400` ✅ (`"Event not found: does-not-exist"`).
  - `POST /api/proposals` avec `eventId` vide → `400` ✅.
- [x] Mettre à jour `ARCHITECTURE.md`/`README.md`/`CLAUDE.md` si besoin pour mentionner le nouveau domaine `event` (via l'agent `documenter`), puisque `CLAUDE.md` indique actuellement que seul `proposal` existe. *(Fait — voir aussi le point d'attention ArchUnit signalé ci-dessous.)*

> **Point d'attention signalé par l'agent `documenter` (hors périmètre de cette feature, à traiter séparément)** : la règle ArchUnit `domains_must_not_reach_into_other_domains_internals` de `HexagonalArchitectureTest` ne détecte en réalité aucune violation — vérifié expérimentalement à la fois sur la dépendance croisée `proposal` → `event` introduite ici, et sur une violation volontaire injectée puis retirée. Il semble s'agir d'un bug de polarité dans le `ArchCondition` personnalisé (`noClasses().should(...)`), préexistant à cette feature. La documentation présente pourtant cette règle comme détectant activement les fuites inter-domaines dès qu'un second domaine existe — ce qui est désormais le cas. À corriger dans un travail dédié.
