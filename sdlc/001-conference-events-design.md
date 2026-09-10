# Rattacher les propositions à un événement — Conception technique

Source spec : sdlc/001-conference-events.md

## Domain model changes

- **Nouveau domaine `event`**, suivant la même structure hexagonale que `proposal` (`domain/model`, `domain/port/in|out`, `application`, `adapter/in/web`, `adapter/out/persistence`).
  - Agrégat `Event` (immutable, id `UUID` en `String`, `name`). Factory `Event.create(name)` qui lève `InvalidEventException` si `name` est vide/blanc (règle métier : « un événement est identifié au minimum par un nom »). Factory `Event.rehydrate(id, name)` sans revalidation, comme pour `Proposal`.
  - `InvalidEventException` (domain exception, sans dépendance framework), sur le modèle de `InvalidProposalException`.
- **Modification de l'agrégat `Proposal`** : ajout du champ `eventId`.
  - `Proposal.submit(title, description, speakerId, eventId)` valide que `eventId` n'est pas vide/blanc (règle structurelle : « le rattachement à un événement est obligatoire »), au même titre que les autres champs.
  - `Proposal.rehydrate(...)` prend aussi `eventId`, sans revalidation.
  - **Important** : l'agrégat `Proposal` valide uniquement que `eventId` est structurellement présent. Il ne peut pas vérifier que l'événement référencé *existe réellement* — cette vérification nécessite d'interroger un autre agrégat, ce qui n'appartient pas au domaine mais à la couche application (voir plus bas).
- **Nouvelle exception `EventNotFoundException`** (dans `event/domain/model`) : levée quand un `eventId` fourni ne correspond à aucun événement existant. Distincte de `InvalidEventException` (qui sanctionne une donnée de création d'événement invalide) et de `InvalidProposalException` (qui sanctionne une donnée de proposition structurellement invalide) : celle-ci sanctionne une référence à une ressource inexistante.

## Ports

### In (nouveaux/modifiés use cases)
- `event/domain/port/in/CreateEventUseCase` + `CreateEventCommand(name)` — nouveau.
- `event/domain/port/in/ListEventsUseCase` — nouveau (pas de commande, retourne `List<Event>`).
- `proposal/domain/port/in/SubmitProposalCommand` — modifié : ajoute `eventId`. `SubmitProposalUseCase` inchangé dans sa forme (même méthode `submit`), mais son contrat métier inclut désormais la vérification d'existence de l'événement.

### Out (nouveaux/modifiés besoins de persistance)
- `event/domain/port/out/EventRepository` — nouveau : `Event save(Event event)`, `Optional<Event> findById(String id)`, `List<Event> findAll()`.
- `proposal/domain/port/out/ProposalRepository` — inchangé dans sa forme (`save`), la persistance du nouveau champ `eventId` est un détail d'adaptateur.

## Phase 0 — Fondation

Séquentiel, à faire avant toute parallélisation :

1. `event/domain/model/Event` + `InvalidEventException` + `EventNotFoundException`.
2. `event/domain/port/in/CreateEventCommand`, `CreateEventUseCase`, `ListEventsUseCase`.
3. `event/domain/port/out/EventRepository`.
4. Modification de `proposal/domain/model/Proposal` (ajout `eventId`, validation non-blanc) et de `proposal/domain/port/in/SubmitProposalCommand` (ajout `eventId`).

Une fois ces contrats fixés, aucune piste du Phase 1 n'a besoin de revenir les modifier.

## Phase 1 — Pistes parallèles

### Track A — Event : application
Implémente `CreateEventUseCase` (`CreateEventService`) et `ListEventsUseCase` (`ListEventsService`) contre `EventRepository`. Tests Mockito, sans dépendre de l'adaptateur de persistance réel.

### Track B — Event : adaptateur web
`EventController` (`POST /api/events`, `GET /api/events`), DTOs `CreateEventRequest`/`EventResponse`, et un `@RestControllerAdvice(assignableTypes = EventController.class)` mappant `InvalidEventException` en `ProblemDetail` 400. Tests `@WebMvcTest` avec les use cases mockés via `@MockitoBean`.

### Track C — Event : adaptateur de persistance
`EventEntity` (`id`, `name`), `EventJpaRepository extends JpaRepository<EventEntity, String>`, `EventRepositoryAdapter implements EventRepository`. Test de mapping Mockito + `@DataJpaTest` pour un round-trip réel contre H2.

### Track D — Proposal : application (vérification d'existence de l'événement)
`SubmitProposalService` dépend désormais aussi de `EventRepository` (en plus de `ProposalRepository`) : avant de construire l'agrégat `Proposal`, vérifie que `eventRepository.findById(command.eventId())` existe ; sinon lève `EventNotFoundException`. Tests Mockito mockant les deux ports — ne dépend d'aucune implémentation réelle des Tracks A/B/C.

### Track E — Proposal : adaptateur web (champ `eventId`)
`SubmitProposalRequest` gagne un champ `eventId` (`@NotBlank`). Le `@RestControllerAdvice` existant (scopé à `ProposalController`) gagne un handler pour `EventNotFoundException` → `ProblemDetail` 400 (« l'événement référencé n'existe pas »). Tests `@WebMvcTest` avec `SubmitProposalUseCase` mocké.

### Track F — Proposal : adaptateur de persistance (colonne `eventId`)
`ProposalEntity` gagne une colonne `eventId` (simple `String`, pas de relation JPA — cohérent avec le besoin exprimé de rattachement « simple »). `ProposalRepositoryAdapter` propage le champ dans `toEntity`/`toDomain`. Test de mapping Mockito + `@DataJpaTest`.

Les six pistes ne dépendent que des contrats du Phase 0 et peuvent être menées en parallèle par des sessions/personnes différentes.

## Phase 2 — Intégration & vérification

- Vérifier le câblage Spring (injection de `EventRepository` dans `SubmitProposalService`, résolution des beans `EventController`/`ProposalController`).
- Lancer la suite complète (`./mvnw test`).
- Parcours manuel de bout en bout couvrant les critères de succès de la spec :
  1. `POST /api/events` avec un nom → 201, l'événement apparaît ensuite dans `GET /api/events`.
  2. `POST /api/proposals` avec un `eventId` valide → 201, la proposition créée référence bien cet événement.
  3. `POST /api/proposals` avec un `eventId` inexistant (ou absent) → 400.

## Traceability

| Scénario / règle métier (spec) | Où c'est traité |
|---|---|
| Créer un événement | Track A (application), Track B (web), Track C (persistance) |
| Lister les événements | Track A (application), Track B (web), Track C (persistance) |
| Soumettre une proposition rattachée à un événement | Phase 0 (`Proposal.eventId`), Track D (vérification d'existence), Track E (web), Track F (persistance) |
| Soumettre une proposition sans événement valide → refusée | Phase 0 (`eventId` non-blanc obligatoire), Track D (`EventNotFoundException` si l'événement n'existe pas), Track E (mapping HTTP 400) |
| Un événement est identifié au minimum par un nom | Phase 0 (`Event.create` + `InvalidEventException`) |
| Aucune contrainte de rôle sur la création d'un événement | Aucun changement technique requis (pas de sécurité ajoutée) |

## Open technical questions

- La spec laisse ouverte l'unicité du nom d'un événement (assomption : pas de contrainte). Ce plan ne l'impose pas non plus : `EventRepository`/`EventEntity` n'ajoutent aucune contrainte d'unicité. À revoir ensemble si cette hypothèse doit être levée avant l'implémentation.
