# Rattacher les propositions à un événement

## Problème / motivation
Aujourd'hui, une proposition de talk soumise via le CFP n'est liée à aucune conférence en particulier : le système suppose implicitement un seul événement. Or Ippon (ou toute organisation utilisant ce CFP) a vocation à gérer plusieurs conférences dans le temps (éditions successives, événements différents). Sans notion d'événement, il est impossible de savoir pour quelle conférence une proposition a été soumise, ni de séparer les propositions d'un événement de celles d'un autre.

## Utilisateurs
- **Organisateur** : crée les événements pour lesquels les propositions pourront être soumises, et consulte la liste des événements existants.
- **Intervenant (speaker)** : soumet sa proposition en la rattachant à l'événement pour lequel il candidate.

## Scope
- Création d'un événement, identifié par un nom.
- Consultation de la liste des événements existants.
- Rattachement d'une proposition à un événement au moment de sa soumission.

### Out of scope
- Modification ou suppression d'un événement une fois créé.
- Notion de statut d'un événement (ouvert / fermé aux soumissions), de dates ou de lieu.
- Gestion de rôles/permissions pour restreindre qui peut créer un événement — n'importe qui peut le faire pour l'instant, comme c'est déjà le cas pour la soumission de propositions.
- Rattachement à un événement d'une proposition déjà existante (migration des données historiques).
- Détail des propositions par événement (filtrage, tableau de bord) — seul le rattachement lui-même est couvert ici.

## Scénarios

### Créer un événement
Étant donné qu'un organisateur souhaite ouvrir un nouvel appel à propositions,
quand il crée un événement en lui donnant un nom,
alors l'événement est enregistré et devient disponible pour que des propositions y soient rattachées.

### Lister les événements
Étant donné que des événements ont été créés,
quand on consulte la liste des événements,
alors tous les événements créés sont retournés.

### Soumettre une proposition rattachée à un événement
Étant donné qu'un événement existe,
quand un intervenant soumet une proposition en indiquant cet événement,
alors la proposition est créée et rattachée à cet événement.

### Soumettre une proposition sans événement valide
Étant donné qu'aucun événement ne correspond à celui indiqué (événement inexistant ou absent de la demande),
quand un intervenant tente de soumettre une proposition,
alors la soumission est refusée, puisque le rattachement à un événement est obligatoire.

## Business rules & constraints
- Le rattachement d'une proposition à un événement est **obligatoire** : une proposition ne peut pas exister sans événement associé.
- Un événement est identifié au minimum par un nom.
- Aucune contrainte de rôle n'est imposée sur la création d'un événement à ce stade.

## Success criteria
- Un organisateur peut créer un événement et le retrouver dans la liste des événements.
- Une proposition soumise est toujours associée à un événement existant.
- Une tentative de soumission sans événement valide est rejetée.

## Open questions
- Le nom d'un événement doit-il être unique (deux événements ne peuvent pas porter le même nom) ? Assomption retenue pour l'instant : pas de contrainte d'unicité, à valider si besoin lors de la conception technique.
