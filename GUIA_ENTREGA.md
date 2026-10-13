# Guía paso a paso — Lab 2: CI/CD del proyecto Banco (entrega 13/oct/2026)

Esta guía cubre lo que **no se puede hacer desde el código** (cuentas, secretos, clics en las plataformas). Los archivos ya están en el proyecto: `pom.xml`, `Dockerfile`, `.github/workflows/build.yml`, pruebas en `src/test`.

Reemplace `<USUARIO>`, `<REPO>`, `<PROJECT_KEY>` por sus valores reales.

## 1. Subir el proyecto a GitHub
```bash
cd Lab2026banco-main
git init -b main
git add .
git commit -m "Proyecto banco con CI/CD"
git remote add origin https://github.com/<USUARIO>/<REPO>.git
git push -u origin main
```
(El primer push disparará el pipeline y puede fallar en `sonar`/`docker` hasta configurar los secretos y los identificadores de SonarCloud: es normal.)

## 2. SonarCloud
1. https://sonarcloud.io → *Log in* con GitHub → **Analyze new project** → elegir el repo → *Set up*.
2. *Administration → Analysis Method*: **desactivar Automatic Analysis**.
3. *My Account → Security → Generate Token* → copiarlo.
4. GitHub → repo → *Settings → Secrets and variables → Actions → New repository secret*: `SONAR_TOKEN`.
5. *Information* (menú izquierdo): copiar **Project Key** y **Organization Key** y ponerlos en `pom.xml`:
   ```xml
   <sonar.projectKey>PROJECT_KEY</sonar.projectKey>
   <sonar.organization>ORGANIZATION_KEY</sonar.organization>
   ```
6. *Information → Badges*: copiar los Markdown y reemplazar los `<PROJECT_KEY>` del README.

## 3. Docker Hub
1. https://hub.docker.com → crear cuenta → **Create repository** `lab12026p` (público).
2. *Account Settings → Security → New Access Token* (mejor que usar la contraseña).
3. Secrets en GitHub: `DOCKER_USERNAME` y `DOCKER_PASSWORD` (el token).

## 4. Base de datos MySQL en la nube
El backend necesita MySQL y Render no ofrece MySQL gratis. Opciones gratuitas: **Aiven for MySQL** (plan free), TiDB Cloud Serverless o Railway. Cree la base `lab12026p` y anote host, puerto, usuario y contraseña. Con ellos arme:
`DB_URL=jdbc:mysql://HOST:PORT/lab12026p?sslMode=REQUIRED&serverTimezone=UTC`

## 5. Render (proveedor cloud)
1. https://render.com → *New → Web Service* → **Existing Image** → `docker.io/<DOCKER_USERNAME>/lab12026p:latest` (la imagen debe existir: haga primero un push a `main` con Docker Hub configurado, o súbala una vez a mano).
2. *Environment*: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
3. *Settings → Deploy Hook*: copiar la URL → secret `RENDER_DEPLOY_HOOK_URL`.
4. URL pública del servicio (ej. `https://lab12026p.onrender.com`) → secret `RENDER_APP_URL`.
5. En GitHub, *Settings → Environments → New environment* llamado `production` (el job `deploy` lo usa).

## 6. Pull requests con trunk-based (evidencia exigida)
```bash
git checkout main && git pull
git checkout -b feature/mejora-readme        # rama corta
# ... haga un cambio pequeño (p. ej. en README.md o un test)
git add . && git commit -m "feat: mejora documentacion"
git push -u origin feature/mejora-readme
```
En GitHub: **Compare & pull request** → verificar los checks (Unit tests, SonarCloud, Build) → **Merge pull request**. Al hacer merge a `main` se ejecuta `docker` y `deploy`. Repita con **al menos otra rama** (ej. `feature/segundo-cambio`). El workflow también acepta pushes a `mi-rama` y permite ejecución manual (`workflow_dispatch`); los demás pushes deben usar `main`, `feature/**` o `fix/**`.

## 7. Pruebas en la nube (adjuntar capturas)
Reemplace `URL` por la de Render:
```bash
curl -X POST URL/api/customers -H "Content-Type: application/json" \
  -d '{"firstName":"Ana","lastName":"Gomez","accountNumber":"111111111","balance":1000.0}'
curl -X POST URL/api/customers -H "Content-Type: application/json" \
  -d '{"firstName":"Beto","lastName":"Perez","accountNumber":"222222222","balance":500.0}'
curl URL/api/customers
curl -X POST URL/api/transactions -H "Content-Type: application/json" \
  -d '{"senderAccountNumber":"111111111","receiverAccountNumber":"222222222","amount":100}'
curl URL/api/transactions/111111111
```
(Nota: en el plan gratuito de Render la primera petición puede tardar ~1 min por "cold start".)

## 8. Snyk (opcional del PDF)
https://snyk.io → entrar con GitHub → *Add project* → repo → copiar el badge Markdown al README.

## 9. Checklist de evidencias para anexar
- [ ] Captura de Actions con los 5 jobs en verde en `main`
- [ ] Captura de Docker Hub con la imagen publicada
- [ ] Captura del servicio en Render + variables (sin mostrar contraseñas)
- [ ] Capturas de al menos 2 Pull Requests (checks verdes y merge a `main`)
- [ ] Captura del grafo de ramas (GitKraken o `git log --graph --oneline --all`)
- [ ] Captura de SonarCloud (Quality Gate + cobertura)
- [ ] Capturas de las pruebas `curl`/Postman contra la URL de Render
- [ ] README con insignias en el repo
