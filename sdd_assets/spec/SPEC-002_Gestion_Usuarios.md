# 📋 SPECIFICATION: Gestión de Usuarios (Clientes y Veterinarios)

> **Spec ID:** `SPEC-002`
> **Author:** Desarrollador
> **Date:** 2026-04-25
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** ADMIN del sistema de la clínica "Huellas Sanas",
**I need** crear, consultar, activar y desactivar usuarios (clientes y veterinarios),
**So that** la clínica mantenga actualizado su directorio de dueños de mascotas y veterinarios disponibles, con datos siempre consistentes entre ambas plataformas.

---

## 2. Hard Business Rules

| Rule ID  | Rule Description                                                                                              | Error Behavior                                                              |
|----------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `BR-001` | El nombre no puede ser nulo ni vacío                                                                          | Throw `UserException("El nombre es obligatorio")`                           |
| `BR-002` | El email no puede ser nulo, vacío, ni tener formato inválido (debe contener `@` y dominio)                    | Throw `UserException("El email no es válido")`                              |
| `BR-003` | El email debe ser único en la tabla `users`                                                                   | Throw `UserException("Ya existe un usuario con ese email")`                 |
| `BR-004` | El teléfono no puede ser nulo ni vacío                                                                        | Throw `UserException("El teléfono es obligatorio")`                         |
| `BR-005` | La contraseña en texto plano no puede ser nula ni vacía y debe tener mínimo 8 caracteres                      | Throw `UserException("La contraseña debe tener al menos 8 caracteres")`     |
| `BR-006` | El rol debe ser un valor válido de `role_t`: CLIENT, VETERINARIAN o ADMIN                                     | Throw `UserException("Rol no válido")`                                      |
| `BR-007` | Si el rol es VETERINARIAN, la especialidad no puede ser nula ni vacía                                         | Throw `UserException("La especialidad es obligatoria para veterinarios")`   |
| `BR-008` | No se puede eliminar (borrado físico) un usuario — solo desactivar (`active = false`)                         | Throw `UserException("No se permite borrar usuarios, use desactivar")`      |
| `BR-009` | La contraseña debe almacenarse como hash SHA-256 (nunca texto plano)                                          | (Control interno — nunca exponer como error de usuario)                     |

### 2.1 Calculation Rules

| Calc ID    | Description                     | Formula                              |
|------------|---------------------------------|--------------------------------------|
| `CALC-001` | Hash contraseña antes de guardar | `PasswordUtil.hash(plainPassword)`   |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Registro exitoso de un CLIENT
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-006`, `CALC-001`

```gherkin
Given un UserDTO válido con name="Juan Pérez", email="juan@test.com",
      password="segura123", phone="3001234567", role=CLIENT
  And el email "juan@test.com" NO existe en la BD
When  userService.create(dto) es llamado
Then  el usuario es persistido en simulacro.users
  And users.password almacena el hash SHA-256 de "segura123", NO el texto plano
  And users.active equals true
  And el id generado es retornado
```

---

### Scenario 2 — Nombre nulo o vacío
> **Validates:** `BR-001`

```gherkin
Given un UserDTO donde name es null o ""
When  userService.create(dto) es llamado
Then  se lanza UserException con mensaje "El nombre es obligatorio"
```

---

### Scenario 3 — Email con formato inválido
> **Validates:** `BR-002`

```gherkin
Given un UserDTO donde email es "correo-sin-arroba"
When  userService.create(dto) es llamado
Then  se lanza UserException con mensaje "El email no es válido"
```

---

### Scenario 4 — Email duplicado
> **Validates:** `BR-003`

```gherkin
Given un usuario con email "existente@test.com" ya registrado en la BD
  And un nuevo UserDTO con el mismo email "existente@test.com"
When  userService.create(dto) es llamado
Then  se lanza UserException con mensaje "Ya existe un usuario con ese email"
  And NO se inserta ningún registro nuevo
```

---

### Scenario 5 — Contraseña con menos de 8 caracteres
> **Validates:** `BR-005`

```gherkin
Given un UserDTO donde password es "abc" (3 chars)
When  userService.create(dto) es llamado
Then  se lanza UserException con mensaje "La contraseña debe tener al menos 8 caracteres"
```

---

### Scenario 6 — Registro de VETERINARIAN sin especialidad
> **Validates:** `BR-007`

```gherkin
Given un UserDTO con role=VETERINARIAN y speciality null o ""
When  userService.create(dto) es llamado
Then  se lanza UserException con mensaje "La especialidad es obligatoria para veterinarios"
```

---

### Scenario 7 — Happy Path: Registro exitoso de VETERINARIAN
> **Validates:** `BR-006`, `BR-007`

```gherkin
Given un UserDTO válido con role=VETERINARIAN y speciality="Cirugía"
  And el email no existe en la BD
When  userService.create(dto) es llamado
Then  se inserta en simulacro.users con role=VETERINARIAN
  And se inserta en simulacro.veterinarians con el user_id recién creado y speciality="Cirugía"
  And ambas inserciones ocurren en la misma transacción JDBC
```

---

### Scenario 8 — Desactivar usuario (borrado lógico)
> **Validates:** `BR-008`

```gherkin
Given un usuario con id=5 existe en la BD con active=true
When  userService.deactivate(5) es llamado
Then  users.active para id=5 equals false
  And el registro sigue existiendo en la BD (no borrado físico)
```

---

### Scenario 9 — Contraseña almacenada como hash (nunca texto plano)
> **Validates:** `BR-009`, `CALC-001`

```gherkin
Given un UserDTO válido con password="miPassword123"
When  userService.create(dto) es llamado
Then  el valor en users.password tiene exactamente 64 caracteres
  And el valor NO contiene la cadena "miPassword123"
```

---

### Scenario 10 — Consultar todos los veterinarios activos
> **Validates:** `BR-008`

```gherkin
Given existen 3 veterinarios en la BD: 2 activos y 1 inactivo
When  userService.findActiveVeterinarians() es llamado
Then  el resultado contiene exactamente 2 veterinarios
  And ninguno de los retornados tiene active=false
```

---

## 4. UI Requirements

| Element Type  | Name/Label           | Bound to Field   | Action/Validation                 |
|---------------|----------------------|------------------|-----------------------------------|
| Input Text    | Nombre               | `name`           | Required (BR-001)                 |
| Input Text    | Email                | `email`          | Required + formato (BR-002)       |
| Input Password| Contraseña           | `password`       | Min 8 chars (BR-005)              |
| Input Text    | Teléfono             | `phone`          | Required (BR-004)                 |
| Input Text    | Dirección            | `address`        | Opcional                          |
| ComboBox      | Rol                  | `role`           | CLIENT / VETERINARIAN (BR-006)    |
| Input Text    | Especialidad         | `speciality`     | Visible y requerido si rol=VET    |
| Button        | Guardar              | N/A              | Calls `controller.createUser()`   |
| Data Table    | Lista de usuarios    | `List<User>`     | Muestra todos los usuarios activos|

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer      | Class Name                  | Key Responsibility                                                      |
|------------|-----------------------------|-------------------------------------------------------------------------|
| Exception  | `UserException.java`        | Violación de reglas de negocio de usuario                               |
| Model      | `User.java`                 | id, name, password, email, phone, address, active, createdAt, role      |
| Model      | `Veterinarian.java`         | Extiende/compone User: id, userId, speciality                           |
| Repository | `UserRepository.java`       | `save`, `findById`, `findByEmail`, `findAll`, `updateActive`            |
| Repository | `VeterinarianRepository.java`| `save(vet)`, `findByUserId`, `findAllActive`                           |
| Service    | `UserService.java`          | Validaciones BR-001..009 + hash + transacción para VETERINARIAN         |
| Controller | `UserController.java`       | `createUser(dto)`, `deactivate(id)`, `findAll()`, `findVeterinarians()` |
| Test       | `UserServiceTest.java`      | Todos los scenarios anteriores                                          |

### 5.2 SQL Queries

```sql
-- Insertar usuario
INSERT INTO simulacro.users (name, password, email, phone, address, active, role)
VALUES (?, ?, ?, ?, ?, true, ?::role_t);

-- Insertar veterinario (en la misma transacción que el usuario)
INSERT INTO simulacro.veterinarians (user_id, speciality)
VALUES (?, ?);

-- Buscar por email (duplicate check BR-003)
SELECT COUNT(*) FROM simulacro.users WHERE email = ?;

-- Buscar por id
SELECT id, name, password, email, phone, address, active, created_at, role
FROM simulacro.users WHERE id = ?;

-- Buscar todos los veterinarios activos
SELECT u.id, u.name, u.email, u.phone, u.address, u.active, u.created_at, u.role,
       v.id AS vet_id, v.speciality
FROM simulacro.users u
JOIN simulacro.veterinarians v ON v.user_id = u.id
WHERE u.active = true;

-- Desactivar usuario (borrado lógico)
UPDATE simulacro.users SET active = false WHERE id = ?;
```

### 5.3 Dependencies

- [x] Tabla `simulacro.users` debe existir
- [x] Tabla `simulacro.veterinarians` debe existir
- [x] `SPEC-001` — `PasswordUtil.hash()` generado en AuthService
- [x] ENUM `role_t` debe existir en la BD

---

## 6. Out of Scope

- Edición de email (campo inmutable por diseño)
- Gestión de permisos granulares por acción
- Upload de foto de perfil

---

## 7. Approval

| Role          | Name | Date | ✓   |
|---------------|------|------|-----|
| Tech Lead     |      |      | [ ] |
| Product Owner |      |      | [ ] |
