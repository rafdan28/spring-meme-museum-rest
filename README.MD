# UNIVERSITA' DEGLI STUDI DI NAPOLI FEDERICO II

# WEB TECHNOLOGIES

## Author

    Raffaele D'Anna N97000455

# Meme Museum – Spring Boot Backend

<p align="center">
  <!-- Logo per tema chiaro -->
  <img src="src/main/resources/static/image/logo.png#gh-light-mode-only" alt="Meme Museum Logo" width="200"/>
  <!-- Logo per tema scuro -->
  <img src="src/main/resources/static/image/logo-dark.png#gh-dark-mode-only" alt="Meme Museum Logo Dark" width="200"/>
</p>

**Meme Museum Backend** è il servizio REST che gestisce la piattaforma di condivisione, scoperta e interazione con i meme.  
Sviluppato con [**Spring Boot**](https://spring.io/projects/spring-boot) e documentato tramite [**OpenAPI/Swagger**](https://swagger.io/), fornisce API sicure e performanti integrate con un database [**PostgreSQL**](https://www.postgresql.org/) e storage immagini con [**SeaweedFS**](https://github.com/seaweedfs/seaweedfs).

---

## 🚀 Funzionalità principali

- **Autenticazione e autorizzazione JWT**:
  - Registrazione e login
  - Recupero credenziali con invio email
  - Ruoli utente (`USER`, `ADMIN`) con restrizioni d’accesso
- **Gestione meme**:
  - Upload immagini (SeaweedFS)
  - Votazione (upvote/downvote)
  - Commenti con CRUD e moderazione autore
  - Modifica e cancellazione meme
- **Meme del giorno**:
  - Selezione giornaliera basata su algoritmo di rotazione e punteggio
  - Storico consultabile con paginazione
- **Gestione tag**:
  - Ricerca e filtraggio meme
- **Sicurezza**:
  - Protezione da accessi non autorizzati
  - Validazione input
- **Documentazione interattiva API**:
  - Accessibile su `/swagger-ui.html`

---

## 🛠️ Tecnologie utilizzate

- **Backend:** Spring Boot 3, Spring Security, Spring Data JPA, MapStruct
- **Database:** PostgreSQL
- **Storage immagini:** SeaweedFS
- **Documentazione API:** OpenAPI/Swagger
- **Autenticazione:** JWT
- **Containerizzazione:** Docker, Docker Compose
- **Email Service:** JavaMailSender (SMTP / MailTrap per test)
- **Testing API:** Postman, Playwright (E2E in integrazione con frontend)

---

## 📂 Struttura del progetto

```plaintext

openapi/
├── meme-api.yaml # Specifiche OpenAPI riguardo l'entità meme
├── user-api.yaml # Specifiche OpenAPI riguardo l'entità user

infrastructure/
├── # Infrastruttura conteinerizzata: Database e Storage Immagini

src/
├── main/
│   ├── java/com/springmememuseumrest/
│   │   ├── config/        # Configurazione, JWT e Spring Security
│   │   │   ├── exception/        # Eccezioni personalizzate
│   │   ├── controller/    # Controller REST
│   │   ├── entity/        # Entity JPA
│   │   ├── mapper/        # MapStruct mapper
│   │   ├── repository/    # Repository JPA
│   │   ├── scheduler/     # Schedulatore di scelta meme del giorno
│   │   ├── service/       # Servizi applicativi
│   ├── resources/
│       ├── static/        # File statici (logo, index, css, ecc.)
│       ├── application.properties
└── test/
    └── java/              # Test unitari e integrazione
```

## ⚙️ Installazione e avvio

### 1️⃣ Clona il repository

```bash
git clone https://github.com/rafdan28/spring-meme-museum-rest.git
cd spring-meme-museum-rest
```

### 2️⃣ Configura il database e lo storage

- Assicurati di avere **Docker** installato.
- Avvia PostgreSQL e SeaweedFS tramite Docker Compose:

```bash
docker compose up -d
```

### 3️⃣ Genera le classi dai file OpenAPI

Il progetto utilizza le specifiche openapi/\*.yaml per generare automaticamente le API client/server.

Esegui:

```bash
./mvnw clean generate-sources
```

Questo comando genera le interfacce e i modelli necessari all’interno della directory target/generated-sources.

### 4️⃣ Configura le variabili di ambiente (es. SMTP, JWT secret, Postgres, etc.)

- Rinomina **application.properties.dummy** in **application.properties**
- Aggiorna **application.properties**

### 5️⃣ Avvia il backend

```bash
./mvnw spring-boot:run
```

## 🧪 Testing

Il backend può essere testato con:

- **Postman** per test manuali
- **Playwright** (in combinazione con il frontend) per E2E
- **JUnit** e **Mockito** per test unitari

## 🧪 Documentazione API

Dopo aver avviato il backend, puoi verificare il corretto funzionamento accedendo alla pagina di benvenuto:

```bash
http://localhost:8080
```

La documentazione interattiva delle API è disponibile all’indirizzo:

```bash
http://localhost:8080/swagger-ui.html
```
