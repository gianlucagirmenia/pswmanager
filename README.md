# 🔐 Password123 - Password Manager

Un password manager sicuro sviluppato in Java Spring Boot con cifratura AES-256.

## 🚀 Caratteristiche Principali
- ✅ Cifratura AES-256 delle password
- ✅ Master password protetta da hash SHA-256
- ✅ Import/Export (backup cifrato e CSV)
- ✅ Controllo password violate (HIBP integration)
- ✅ Interfaccia web responsive
- ✅ Categorizzazione e ricerca
- ✅ Sanitizzazione automatica database

## 🏗️ Architettura

### Struttura Principale

```
com.durdencorp.pswmanager/
├── rest/ # Controller (Web + API)
├── service/ # Business logic e servizi
│ ├── export/ # Servizi import/export
│ └── security/ # Controlli sicurezza
├── model/ # Entity JPA
├── repository/ # Repository Spring Data
├── dto/ # Data Transfer Objects
└── utils/ # Utility
```


### Classi Core
- **`PasswordEntry`** - Modello dati principale (password cifrate)
- **`PasswordEntryService`** - Logica di gestione password
- **`MasterPasswordEncryption`** - Cifratura/decifratura AES-256
- **`PasswordEntryController`** - API REST per le password
- **`WebController`** - Controller per le pagine web
- **`ExportController`/`ImportController`** - Gestione backup

## 🗄️ Database Schema

### Tabella `password_entry`
| Campo | Tipo | Descrizione |
|-------|------|-------------|
| id | BIGINT | Primary key |
| title | VARCHAR | Titolo credenziale |
| username | VARCHAR | Nome utente |
| encrypted_password | VARCHAR | Password cifrata (AES) |
| url | VARCHAR | URL associato |
| notes | TEXT | Note |
| category | VARCHAR | Categoria |
| created_at | TIMESTAMP | Data creazione |
| updated_at | TIMESTAMP | Data modifica |

### Tabella `app_config`
| Campo | Tipo | Descrizione |
|-------|------|-------------|
| id | BIGINT | Primary key |
| config_key | VARCHAR | Chiave configurazione |
| config_value | VARCHAR | Valore configurazione |

## 🌐 API Principali

### Autenticazione
- `GET/POST /login` - Login/logout
- `GET /logout` - Logout

### Gestione Password
- `GET /` - Lista password (paginata)
- `GET /new`, `GET /edit/{id}` - Form
- `POST /save` - Salva/aggiorna
- `GET /delete/{id}` - Elimina

### API REST (`/api/passwords`)
- `GET /api/passwords` - Lista tutte
- `POST /api/passwords` - Crea nuova
- `PUT /api/passwords/{id}` - Aggiorna
- `DELETE /api/passwords/{id}` - Elimina
- `GET /api/passwords/search` - Ricerca

### Import/Export
- `GET /api/export/encrypted` - Backup cifrato (.enc)
- `GET /api/export/csv` - Metadati CSV
- `POST /api/import/encrypted` - Importa backup
- `POST /api/import/csv` - Importa CSV

## 🔐 Sicurezza

### Flusso Cifratura
1. Master password → SHA-256 → salvata in `app_config`
2. Master password → chiave AES → sessione
3. Password utente + IV casuale → AES-256/CBC → Base64

### Controlli Implementati
- Validazione HIBP (Have I Been Pwned)
- Rate limiting tentativi login
- Sanitizzazione automatica dati
- Logging operazioni sensibili

## 🛠️ Tecnologie
- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA
- **Database**: H2 (in-memory)
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Cifratura**: AES-256/CBC/PKCS5Padding
- **Build**: Maven

## 🚀 Esecuzione

```bash
# Clona e compila
git clone https://github.com/gianlucagirmenia/pswmanager.git
cd pswmanager
mvn clean package

# Esegui
java -jar target/pswmanager-*.jar

# Accedi a: http://localhost:6969