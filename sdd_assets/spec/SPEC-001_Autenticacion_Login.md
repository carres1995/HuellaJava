# 📋 SPECIFICATION: Autenticación y Login de Usuarios

> **Spec ID:** `SPEC-001`
> **Author:** Desarrollador
> **Date:** 2026-04-25
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** miembro del personal de la clínica "Huellas Sanas" (ADMIN, VETERINARIAN o CLIENT),
**I need** autenticarme con email y contraseña para acceder al sistema,
**So that** solo el personal autorizado pueda operar el sistema y cada rol acceda únicamente a las funciones que le corresponden.

---

## 2. Hard Business Rules

| Rule ID  | Rule Description                                                                                  | Error Behavior                                                        |
|----------|---------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `BR-001` | El email no puede ser nulo ni vacío                                                               | Throw `AuthException("El email es obligatorio")`                      |
| `BR-002` | La contraseña no puede ser nula ni vacía                                                          | Throw `AuthException("La contraseña es obligatoria")`                 |
| `BR-003` | El email debe existir en la tabla `users`                                                         | Throw `AuthException("Credenciales incorrectas")`                     |
| `BR-004` | El hash SHA-256 de la contraseña ingresada debe coincidir con el almacenado en `users.password`   | Throw `AuthException("Credenciales incorrectas")`                     |
| `BR-005` | El usuario debe tener `active = true`                                                             | Throw `AuthException("Usuario inactivo. Contacte al administrador.")` |
| `BR-006` | El sistema debe devolver el `role_t` del usuario autenticado para control de acceso posterior     | (No error — parte del happy path)                                     |

### 2.1 Calculation Rules

| Calc ID    | Description                   | Formula                                          |
|------------|-------------------------------|--------------------------------------------------|
| `CALC-001` | Hash de contraseña para login | `SHA-256(plainPassword) → hex string de 64 chars`|

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Login exitoso como ADMIN
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`, `CALC-001`

```gherkin
Given un usuario con email "admin@huellassanas.com" y role ADMIN existe en la BD con active=true
  And su password almacenado es el SHA-256 de "admin123"
When  el servicio authService.login("admin@huellassanas.com", "admin123") es llamado
Then  la operación retorna el objeto User correctamente
  And user.getRole() equals ADMIN
  And user.isActive() equals true
```

---

### Scenario 2 — Email nulo o vacío
> **Validates:** `BR-001`

```gherkin
Given un email con valor null o ""
When  authService.login(null, "cualquierPassword") es llamado
Then  se lanza AuthException con mensaje "El email es obligatorio"
```

---

### Scenario 3 — Contraseña nula o vacía
> **Validates:** `BR-002`

```gherkin
Given una contraseña con valor null o ""
When  authService.login("admin@huellassanas.com", null) es llamado
Then  se lanza AuthException con mensaje "La contraseña es obligatoria"
```

---

### Scenario 4 — Email no registrado en la BD
> **Validates:** `BR-003`

```gherkin
Given un email "noexiste@test.com" que NO existe en la tabla users
When  authService.login("noexiste@test.com", "cualquier") es llamado
Then  se lanza AuthException con mensaje "Credenciales incorrectas"
```

---

### Scenario 5 — Contraseña incorrecta
> **Validates:** `BR-004`, `CALC-001`

```gherkin
Given un usuario con email "vet@huellassanas.com" existe en la BD
  And su password almacenado es el SHA-256 de "correcta123"
When  authService.login("vet@huellassanas.com", "incorrecta999") es llamado
Then  se lanza AuthException con mensaje "Credenciales incorrectas"
```

---

### Scenario 6 — Usuario inactivo
> **Validates:** `BR-005`

```gherkin
Given un usuario con email "inactivo@test.com" existe en la BD con active=false
  And su password es correcto
When  authService.login("inactivo@test.com", "pass123") es llamado
Then  se lanza AuthException con mensaje "Usuario inactivo. Contacte al administrador."
```

---

### Scenario 7 — Verificación del hash SHA-256
> **Validates:** `CALC-001`

```gherkin
Given la contraseña en texto plano "miPassword"
When  PasswordUtil.hash("miPassword") es llamado
Then  el resultado es exactamente "f4b75c2b4f7c3e2d1a9b8c0e6d5f3a2b..." (64 chars hex)
  And el resultado NO contiene la cadena "miPassword"
```

---

### Scenario 8 — Login exitoso como VETERINARIAN
> **Validates:** `BR-003`, `BR-004`, `BR-006`

```gherkin
Given un usuario con role VETERINARIAN existe en la BD con active=true
When  authService.login(emailVet, passwordVet) es llamado
Then  user.getRole() equals VETERINARIAN
```

---

## 4. UI Requirements

| Element Type  | Name/Label         | Bound to Field | Action/Validation              |
|---------------|--------------------|----------------|--------------------------------|
| Input Text    | Email              | `email`        | Required (BR-001)              |
| Input Password| Contraseña         | `password`     | Required (BR-002)              |
| Button        | Iniciar sesión     | N/A            | Calls `controller.login()`     |
| Label (error) | Mensaje de error   | N/A            | Muestra AuthException.message  |

> Aplica tanto para vista Consola como para vista Swing/JavaFX (mismo controller).

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer      | Class Name                  | Key Responsibility                                         |
|------------|-----------------------------|------------------------------------------------------------|
| Exception  | `AuthException.java`        | Violación de reglas de autenticación                       |
| Util       | `PasswordUtil.java`         | `hash(String plain): String` — SHA-256 hex                 |
| Model      | `User.java`                 | id, name, password(hash), email, phone, address, active, createdAt, role |
| Repository | `UserRepository.java`       | `findByEmail(String email): Optional<User>`                |
| Service    | `AuthService.java`          | `login(String email, String password): User`               |
| Controller | `AuthController.java`       | Recibe input de vista, llama AuthService, retorna User/error|
| Test       | `AuthServiceTest.java`      | Todos los scenarios anteriores                             |

### 5.2 SQL Queries

```sql
-- Buscar usuario por email para login
SELECT id, name, password, email, phone, address, active, created_at, role
FROM simulacro.users
WHERE email = ?;
```

### 5.3 Dependencies

- [x] Tabla `simulacro.users` debe existir con columna `password CHAR(64)`
- [ ] `SPEC-002` (Gestión de Usuarios) — para crear usuarios con hash

---

## 6. Out of Scope

- Gestión de sesiones / tokens JWT — el sistema es de escritorio
- Recuperación de contraseña olvidada
- Bloqueo por intentos fallidos

---

## 7. Approval

| Role          | Name | Date | ✓   |
|---------------|------|------|-----|
| Tech Lead     |      |      | [ ] |
| Product Owner |      |      | [ ] |
