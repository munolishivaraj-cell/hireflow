# Deploying the free demo: Render + Aiven

The app runs on a **Render free web service** (Docker). MySQL, Valkey (Redis-compatible) and Kafka are
**Aiven free-tier services**. The `aiven` Spring profile switches the app to TLS connections and reads
every secret from environment variables, so no credentials go into Git.

Things to know about free tiers:

| | Limit | What it means for the demo |
|---|---|---|
| Render free | 512 MB RAM, 0.1 CPU; sleeps after 15 min without traffic | The first visit after a quiet period takes about 1–2 minutes while it wakes up. The image is tuned to about 310 MB. |
| Aiven free Kafka | 5 topics, 2 partitions each; powers off after 24 h without traffic | Create the 2 topics yourself (below). If the demo sat unused, power the service back on in the console before showing it. |
| Aiven free MySQL / Valkey | Small, single node; can power off when idle | Same as above: check they're **Running** before a demo. |

## 1. Aiven: collect the connection details

Open your project in the Aiven console. For each service, open **Overview → Connection information**.

**MySQL**
- `DB_URL` = `jdbc:mysql://<Host>:<Port>/defaultdb?sslMode=REQUIRED`
- `DB_USERNAME` = `avnadmin`
- `DB_PASSWORD` = the service password

**Valkey (Redis)**
- `REDIS_URL` = the **Service URI**. It starts with `rediss://` (two s's, meaning TLS).

**Kafka**
1. Under **Topics**, create two topics, each with **2 partitions**:
   - `hireflow.hiring-events`
   - `hireflow.hiring-events.DLT`
2. Pick an authentication method:
   - **SASL (preferred)**: if **Advanced configuration** offers `kafka_authentication_methods.sasl`, enable it. Then under Connection information choose **SASL**. Note that the SASL port differs from the certificate port.
     - `KAFKA_BOOTSTRAP_SERVERS` = `<Host>:<SASL port>`
     - `KAFKA_USERNAME` = `avnadmin`
     - `KAFKA_PASSWORD` = the password shown for SASL
     - `KAFKA_CA_CERT` = the full text of the **CA certificate**, including the `-----BEGIN CERTIFICATE-----` and `-----END CERTIFICATE-----` lines
   - **Client certificate (if SASL isn't available on the free plan)**:
     - `KAFKA_BOOTSTRAP_SERVERS` = `<Host>:<Port>`
     - `KAFKA_CA_CERT` = the CA certificate text
     - `KAFKA_ACCESS_CERT` = the **Access certificate** text
     - `KAFKA_ACCESS_KEY` = the **Access key** text
     - Set `SPRING_PROFILES_ACTIVE` to `aiven,aiven-cert,demo`.

## 2. Push the code to GitHub

Render deploys from a Git repository. The code needs to be on GitHub first, either in this repository
(with HireFlow in the `hireflow/` folder) or in a repository of its own.

## 3. Render: create the web service

In the Render dashboard, go to **New → Web Service** and connect the GitHub repository. Then set:

| Setting | Value |
|---|---|
| Root Directory | `hireflow` (leave empty if HireFlow is the repository root) |
| Language / Runtime | Docker |
| Instance type | **Free** |
| Health Check Path | `/actuator/health/liveness` |

Add these **environment variables**. Paste certificate text as-is; Render keeps multi-line values.

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `aiven,demo` (`demo` seeds fictional jobs and candidates on first start) |
| `KAFKA_PARTITIONS` | `2` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | from MySQL |
| `REDIS_URL` | from Valkey |
| `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_USERNAME`, `KAFKA_PASSWORD`, `KAFKA_CA_CERT` | from Kafka |

If HireFlow is the root of its own repository, `render.yaml` sets all of this up through
**New → Blueprint**. Render still asks you for each secret.

Click **Deploy**. The first build takes a few minutes. When the log shows `Started HireFlowApplication`,
open `https://<your-service>.onrender.com`. It redirects to the Swagger UI.

## 4. Check it works

1. `GET /api/jobs` shows the seeded demo jobs.
2. `POST /api/applications` with a seeded job and candidate. A few seconds later,
   `GET /api/applications/{id}` returns a `matchScore` (this proves Kafka works end to end).
3. `GET /api/jobs/{id}/pipeline` returns the stage counts (served from Valkey on repeat calls).

## Troubleshooting

| Log message | Fix |
|---|---|
| `Access denied for user` / `Communications link failure` | Check `DB_URL` host, port and password, and that the MySQL service is Running. |
| `SaslAuthenticationException` | Wrong SASL port or password, or SASL isn't enabled. Use the client-certificate option. |
| `PKIX path building failed` / SSL handshake errors | `KAFKA_CA_CERT` is incomplete. Paste the whole certificate, including the BEGIN/END lines. |
| `UnknownTopicOrPartitionException` | Create the two topics listed in step 1. |
| Render says the service exceeded memory | Lower `-XX:MaxRAMPercentage` in the Dockerfile, for example to `55`. |

## Public demo etiquette

The API is open: anyone with the link can create jobs and candidates. The seeded data is fictional,
and the API description asks visitors not to enter real personal data. Applying is rate limited per
candidate. To reset the demo, drop and recreate the `defaultdb` tables (or the MySQL service), then
redeploy. The `demo` profile seeds again when there are no jobs.
