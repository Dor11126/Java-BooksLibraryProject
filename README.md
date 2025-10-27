
# BooksLibraryFull

A multi-module Java project that demonstrates a simple **books library** system with a TCP server, a JavaFX desktop client, and an in-memory **LRU cache** to speed up reads. The system persists data to a JSON file and exposes a minimal JSON-over-sockets protocol.

---

## 1) What this project does

- **Server (TCP, JSON line protocol)** listens on `localhost:34567`.  
- **Client (JavaFX app)** lets you view, add, get, and delete books from the server.  
- **Caching (LRU)** accelerates repeated `get` operations.  
- **Storage** is a simple JSON file (`books_store.json`).

**Typical flow:**  
Client → sends JSON Request → Server → Service (Cache → File DAO) → returns JSON Response → Client table updates.

---

## 2) Module layout (Maven reactor)

```
BooksLibraryFull/                   (parent POM)
├─ AlgorithmModule/                 (cache interfaces & implementations)
├─ AppModule/                       (domain + DAO + services)
├─ NetworkModule/                   (TCP server + controller)
└─ UIClientApp/                     (JavaFX desktop client)
```

### AlgorithmModule
- `IAlgoCache<K,V>` — cache interface.
- `LRUAlgoCacheImpl<K,V>` — O(1) LRU cache using a hash map + linked list.
- (Tests) `AlgoCacheTest`

### AppModule
- Domain model: `Book` (fields: `id`, `title`, `author`, `publishYear`).
- DAO: `DaoFileImpl<T>` — JSON-backed file DAO using Gson.
- Service: `BookService` — read-through/write-through cache orchestration.
- (Tests) `BookServiceTest`

### NetworkModule
- `Server` — TCP server on port **34567**.
- `BookController` — maps actions to service calls.
- DTOs: `Request<T>`, `Response<T>` (Gson-serializable).

### UIClientApp
- `LibraryApp` — JavaFX UI:
  - **Welcome screen** (optional background image + “Enter” button).
  - Main screen with **TableView** and controls:
    - **Get All**, **Get**, **Add**, **Clear**, **Delete** (Delete button styled red).
  - **Duplicate ID protection**: shows a **warning dialog** and **blocks the add** if the ID already exists in the current table data.

---

## 3) JSON line protocol

Each request is a single JSON line, each response is one JSON line back.

**Request** (examples):
```json
{ "action": "getAll", "body": null }
{ "action": "get",    "body": { "id": 1 } }
{ "action": "add",    "body": { "id": 5, "title": "T", "author": "A", "publishYear": 2024 } }
{ "action": "delete", "body": { "id": 5 } }
```

**Response** (examples):
```json
{ "success": true, "message": "OK", "data": [ { "id": 1, "title": "…", "author": "…", "publishYear": 2020 } ] }
{ "success": true, "message": "OK", "data": { "id": 1, "title": "…", "author": "…", "publishYear": 2020 } }
{ "success": false, "message": "Not found", "data": null }
```

---

## 4) Build

**Requirements**
- JDK **17+** (tested up to JDK 24).
- Maven **3.8+**.
- JavaFX SDK **17+** (if you run the UI from the IDE without using Maven JavaFX plugins).

**From the project root:**
```bash
mvn clean install
```

This builds all modules and runs existing unit tests for `AlgorithmModule` and `AppModule`.

---

## 5) Run the server

### Option A — via Maven (recommended)
From the **project root**:
```bash
mvn -pl NetworkModule -am \
  org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=com.dor_cohen.network.server.Server
```
> `-pl NetworkModule -am` builds what’s needed and runs the server main class.

### Option B — via IDE
- Open **NetworkModule** main class: `com.dor_cohen.network.server.Server`.
- Run configuration: standard Java application run.

When running, you should see:
```
📡 Server started on port 34567 — waiting for clients...
```

---

## 6) Run the JavaFX client (UI)

### Option A — via IDE with JavaFX SDK

**Main class:** `com.dor_cohen.ui.LibraryApp`

**VM options (adjust the path to your JavaFX SDK):**
```
--module-path "C:\path\to\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml
```
(Optional) to silence a warning on JDK 22+:
```
--enable-native-access=javafx.graphics
```

**Background image on the welcome screen (optional):**
- The code points to an absolute path like:
  `file:D:/.../BooksLibraryFull/BooksLibraryFull/library.jpg`
- Replace it with a valid path on your machine **or** move the image into `UIClientApp/src/main/resources/` and load it from the classpath.

> Make sure the **server is running first**, then start the UI.

### Option B — via Maven JavaFX plugin
If you add `javafx-maven-plugin` to `UIClientApp/pom.xml`, you can run:
```bash
mvn -pl UIClientApp javafx:run
```
(Plugin setup is not included in this repo by default.)

---

## 7) UI features & usage

- **Welcome screen**: background image + “Enter” button to open the main UI.
- **Main screen** (TableView of books):
  - **Get All**: fetches all books from the server.
  - **Get**: if a row is selected → fetch that ID; otherwise reads the ID from the textbox.
  - **Add**: creates a new book from the text fields.
    - **Duplicate ID guard** → pops a **Warning** and **does not send** the request if the ID already exists in the current table.
  - **Clear**: clears the table **only in the UI** (no server delete).
  - **Delete**: deletes the selected ID (or typed ID). Button is styled **red**.

---

## 8) How caching works (LRU)

- The server constructs:
  - `dao = DaoFileImpl<Book>("books_store.json", TypeToken<Map<Long,Book>>(){}.getType())`
  - `cache = new LRUAlgoCacheImpl<Long,Book>(100)`
  - `service = new BookService(dao, cache)`
- **Read-through**: `BookService.findBook(id)` checks the cache first; on **miss**, loads from DAO, then inserts into cache.
- **Write-through**: `saveBook(b)` writes to DAO and updates the cache.
- **Eviction**: when the capacity (100) is exceeded, the **least recently used** entry is evicted.
- Time complexity: O(1) `get`/`put` via hash map + doubly-linked list.

**Why it matters**: repeated `get` calls for the same ID are **fast** and do not re-hit the file each time.

---

## 9) Tests

- **AlgorithmModule** → `AlgoCacheTest` (3 tests).  
  Run:
  ```bash
  mvn -pl AlgorithmModule test
  ```

- **AppModule** → `BookServiceTest` (1 test).  
  Run:
  ```bash
  mvn -pl AppModule test
  ```

You can also run `mvn test` at the root to execute all tests.

---

## 10) Troubleshooting

- **`Address already in use: bind`**
  - The server port `34567` is already used. Close the previous server or change the port in `Server` constructor.

- **`JavaFX runtime components are missing` / `Module javafx.controls not found`**
  - Ensure the JavaFX SDK path is correct in **VM options**:
    ```
    --module-path "C:\path\to\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml
    ```
  - Make sure your JDK version matches the JavaFX SDK build.

- **`Connection refused: connect` in the client**
  - Start the **server** first, then the UI.
  - Verify firewall/antivirus isn’t blocking localhost TCP.

- **Duplicate ID appears to overwrite**
  - The client has a **pre-check** and shows a warning dialog. If you still see overwrites, refresh with **Get All** and confirm your server-side `add` enforces uniqueness.

---

## 11) Clean-up & version control tips

- It’s safe to delete all Maven `target/` folders – they are build outputs.
- The `.idea/` folder is IDE metadata (safe to delete but you’ll lose local IDE settings).  
- Suggested `.gitignore` entries:
  ```gitignore
  **/target/
  .idea/
  *.iml
  .DS_Store
  ```

---

## 12) Roadmap ideas (optional)

- Use a **thread pool** on the server (`ExecutorService`) instead of `new Thread(...)` per connection.
- Move request routing out of `Server` into a dedicated **RequestHandler**.
- Split the JavaFX UI into multiple classes (controller, views) to better follow MVC and SRP.
- Add more cache strategies (e.g., **FIFO**, **LFU**) + tests.
- Package JavaFX with the app (Maven plugin or jlink) to avoid manual module-path setup.

---

## 13) Credits

- Java, Maven, JavaFX, Gson.
- Implemented by **Dor Cohen**.
