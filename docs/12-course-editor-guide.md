# Course Editor Guide

A comprehensive guide for creating high-quality lessons on the InnoLab CTF Platform.

---

## 1. Lesson Structure Template

Every lesson should follow this structure:

### Required Sections

1. **Title** — Clear, specific topic (e.g., "SQL Injection Fundamentals")
2. **Theory** — Brief introduction (2-3 paragraphs)
3. **In-Depth Analysis** — Detailed explanation with:
   - Vulnerable code example (BAD)
   - Secure code example (GOOD)
   - Attack demonstrations
   - Prevention methods
4. **Real-World Incidents** — At least 3 references to actual breaches
5. **External References** — Links to further reading

### Optional Sections

- **Video URL** — Link to supplementary video content
- **Challenge IDs** — Related practice challenges to attempt

---

## 2. Writing Standards

### Language Guidelines

- Use **clear, instructional language**
- Write in **second person** ("You will learn...", "When an attacker...")
- Explain **WHY** something is vulnerable, not just **WHAT**
- Always provide **mitigation/prevention tips**

### Code Examples

Every lesson with code should include:

1. **Vulnerable (BAD) example** — Show what's wrong
2. **Secure (GOOD) example** — Show the fix
3. **Language class names** — Use proper CSS classes:
   ```html
   <pre><code class="language-php">...</code></pre>
   <pre><code class="language-python">...</code></pre>
   ```

### CSS Class Modifiers

Add `vulnerable` or `secure` to highlight code variants:
```html
<pre><code class="language-javascript vulnerable">...</code></pre>
<pre><code class="language-javascript secure">...</code></pre>
```

This triggers visual indicators (red/green borders + labels) in the lesson renderer.

### Supported Languages

| Tag | Language |
|-----|----------|
| `language-php` | PHP |
| `language-python` | Python |
| `language-sql` | SQL |
| `language-javascript` / `language-js` | JavaScript |
| `language-bash` / `language-sh` | Bash |
| `language-c` | C |
| `language-cpp` | C++ |
| `language-java` | Java |
| `language-ruby` | Ruby |
| `language-go` | Go |
| `language-rust` | Rust |
| `language-html` | HTML |
| `language-css` | CSS |
| `language-json` | JSON |
| `language-xml` | XML |
| `language-yaml` | YAML |

> The syntax highlighter supports ~200+ languages via Prism. Any language name not in this list will likely still work.

---

## 3. Lesson Content Format

Lesson content is stored as **HTML** and rendered through a sanitizer. The TipTap editor in the admin panel generates proper HTML. You can also write raw HTML.

### Supported Elements

- **Headings** — `<h2>`, `<h3>`, `<h4>` (auto-generates table of contents navigation)
- **Paragraphs** — `<p>`
- **Code blocks** — `<pre><code class="language-xxx">`
- **Lists** — `<ul>`, `<ol>`
- **Links** — `<a href="...">`
- **Callouts** — `<div class="callout callout-info/warning/tip">`

### Callout Types

```html
<div class="callout callout-info">
  General information callout
</div>

<div class="callout callout-warning">
  Warning / cautionary note
</div>

<div class="callout callout-tip">
  Pro tip or best practice
</div>
```

---

## 4. Real-World Incidents

Each lesson should reference **at least one** real-world security incident.

For each incident, include:
- **Date** of the incident
- **Company/Organization** affected
- **Impact** — what was compromised
- **Root Cause** — how the vulnerability was exploited
- **Lesson Learned** — how it could have been prevented

---

## 5. Challenge Linking

Link related practice challenges to each lesson via the **Challenge IDs** field. Use the challenge ID (e.g., `web-101-1234567890`) to connect the lesson to specific CTF challenges that reinforce the concepts.

---

## 6. Example Lesson — SQL Injection

```html
<h2>SQL Injection Fundamentals</h2>

<p>SQL Injection (SQLi) is one of the most common web vulnerabilities... You will learn how attackers exploit unsanitized input...</p>

<h3>In-Depth Analysis</h3>

<h4>Vulnerable Example</h4>
<pre><code class="language-php vulnerable">
$query = "SELECT * FROM users WHERE username = '" . $_POST['username'] . "'";
$result = mysqli_query($conn, $query);
</code></pre>

<h4>Secure Example</h4>
<pre><code class="language-php secure">
$stmt = $conn->prepare("SELECT * FROM users WHERE username = ?");
$stmt->bind_param("s", $_POST['username']);
$stmt->execute();
</code></pre>

<h3>Real-World Incidents</h3>
<ul>
  <li><strong>2017 Equifax Breach:</strong> SQL injection in Apache Struts led to exposure of 147M records...</li>
  <li><strong>2019 Capital One:</strong> SSRF combined with SQL injection in a WAF bypass led to exposure of 100M+ records...</li>
</ul>

<div class="callout callout-tip">
  <strong>Best Practice:</strong> Always use parameterized queries as the primary defense...
</div>
```

---

## 7. Using the TipTap Editor

The admin panel uses **TipTap** (a rich text editor built on ProseMirror) for lesson content:

- **Bold**, *Italic*, ~~Strikethrough~~
- **Headings** — dropdown levels H2-H4
- **Code blocks** — inserts `<pre><code>` with language selection
- **Lists** — bullet and numbered
- **Links** — click to add/edit URLs
- **Callouts** — predefined info/warning/tip blocks

TipTap maintains clean HTML output compatible with the lesson renderer.

---

## 8. API Fields Reference

When creating lessons programmatically or needing to know exact field names:

| Backend Field | Type | Max Length | Purpose |
|---------------|------|------------|---------|
| `title` | String | 200 | Lesson title |
| `content` | TEXT | unlimited | Lesson body (HTML) |
| `detailedExplanation` | TEXT | unlimited | In-depth analysis section |
| `videoUrl` | String | 500 | Optional video link |
| `orderIndex` | Integer | — | Display ordering |
| `challengeIds` | List<String> | — | Related challenge IDs |
| `codeExamplesJson` | List<String> | — | Code example JSON strings |
| `realWorldIncidents` | List<String> | — | Real incident descriptions |
| `externalReferences` | List<String> | — | External reference URLs |
