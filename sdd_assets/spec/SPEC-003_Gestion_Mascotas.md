# 📋 SPECIFICATION: Gestión de Mascotas (Pets)

> **Spec ID:** `SPEC-003`
> **Author:** Desarrollador
> **Date:** 2026-04-25
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** recepcionista o ADMIN de la clínica "Huellas Sanas",
**I need** registrar, consultar y desactivar mascotas asociadas a un dueño (CLIENT),
**So that** la clínica tenga un registro unificado de pacientes que permita consultar su historial médico rápidamente y contactar al dueño en cualquier momento.

---

## 2. Hard Business Rules

| Rule ID  | Rule Description                                                                                     | Error Behavior                                                            |
|----------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| `BR-001` | El nombre de la mascota no puede ser nulo ni vacío                                                   | Throw `PetException("El nombre de la mascota es obligatorio")`            |
| `BR-002` | La especie no puede ser nula ni vacía                                                                | Throw `PetException("La especie es obligatoria")`                         |
| `BR-003` | El `user_id` (dueño) debe existir en `simulacro.users` con rol CLIENT y `active = true`             | Throw `PetException("El dueño especificado no existe o está inactivo")`   |
| `BR-004` | No se puede eliminar una mascota físicamente — solo desactivar (`active = false`)                    | Throw `PetException("No se permite borrar mascotas, use desactivar")`     |
| `BR-005` | La fecha de nacimiento, si se provee, no puede ser una fecha futura                                  | Throw `PetException("La fecha de nacimiento no puede ser futura")`        |
| `BR-006` | Un dueño puede tener múltiples mascotas activas (sin límite definido)                                | (No error — solo validar que el dueño sea válido)                         |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Registro exitoso de mascota
> **Validates:** `BR-001`, `BR-002`, `BR-003`

```gherkin
Given un usuario con id=1 existe en la BD con role=CLIENT y active=true
  And un PetDTO válido: name="Firulais", species="Perro", userId=1
When  petService.create(dto) es llamado
Then  la mascota es persistida en simulacro.pets con active=true
  And pet.getUserId() equals 1
  And el id generado es retornado
```

---

### Scenario 2 — Nombre de mascota nulo o vacío
> **Validates:** `BR-001`

```gherkin
Given un PetDTO donde name es null o ""
When  petService.create(dto) es llamado
Then  se lanza PetException con mensaje "El nombre de la mascota es obligatorio"
```

---

### Scenario 3 — Especie nula o vacía
> **Validates:** `BR-002`

```gherkin
Given un PetDTO donde species es null o ""
When  petService.create(dto) es llamado
Then  se lanza PetException con mensaje "La especie es obligatoria"
```

---

### Scenario 4 — Dueño inexistente
> **Validates:** `BR-003`

```gherkin
Given un user_id=999 que NO existe en simulacro.users
  And un PetDTO válido con userId=999
When  petService.create(dto) es llamado
Then  se lanza PetException con mensaje "El dueño especificado no existe o está inactivo"
  And NO se inserta ningún registro en pets
```

---

### Scenario 5 — Dueño inactivo
> **Validates:** `BR-003`

```gherkin
Given un usuario con id=3 existe en la BD con role=CLIENT y active=false
  And un PetDTO válido con userId=3
When  petService.create(dto) es llamado
Then  se lanza PetException con mensaje "El dueño especificado no existe o está inactivo"
```

---

### Scenario 6 — Fecha de nacimiento futura
> **Validates:** `BR-005`

```gherkin
Given un PetDTO con birthDate = LocalDate.now().plusDays(1)
When  petService.create(dto) es llamado
Then  se lanza PetException con mensaje "La fecha de nacimiento no puede ser futura"
```

---

### Scenario 7 — Desactivar mascota (borrado lógico)
> **Validates:** `BR-004`

```gherkin
Given una mascota con id=7 existe en la BD con active=true
When  petService.deactivate(7) es llamado
Then  pets.active para id=7 equals false
  And el registro sigue existiendo en la BD
```

---

### Scenario 8 — Consultar mascotas activas de un dueño
> **Validates:** `BR-003`, `BR-004`

```gherkin
Given el usuario con id=2 tiene 3 mascotas: 2 activas y 1 inactiva
When  petService.findActiveByOwner(2) es llamado
Then  el resultado contiene exactamente 2 mascotas
  And ninguna de las retornadas tiene active=false
```

---

### Scenario 9 — Fecha de nacimiento en el día actual (valor límite válido)
> **Validates:** `BR-005`

```gherkin
Given un PetDTO con birthDate = LocalDate.now() (hoy)
When  petService.create(dto) es llamado
Then  la operación completa exitosamente (fecha de hoy es válida)
```

---

### Scenario 10 — Un dueño registra múltiples mascotas
> **Validates:** `BR-006`

```gherkin
Given un usuario CLIENT con id=1 ya tiene 2 mascotas activas
When  petService.create(nuevoPetDTO con userId=1) es llamado
Then  la nueva mascota es persistida correctamente
  And el usuario ahora tiene 3 mascotas activas en la BD
```

---

## 4. UI Requirements

| Element Type  | Name/Label              | Bound to Field  | Action/Validation                       |
|---------------|-------------------------|-----------------|-----------------------------------------|
| Input Text    | Nombre mascota          | `name`          | Required (BR-001)                       |
| Input Text    | Especie                 | `species`       | Required (BR-002)                       |
| Input Text    | Raza                    | `breed`         | Opcional                                |
| Date Picker   | Fecha de nacimiento     | `birthDate`     | No futura (BR-005), opcional            |
| ComboBox      | Dueño                   | `userId`        | Lista de CLIENTs activos (BR-003)       |
| Button        | Registrar mascota       | N/A             | Calls `controller.createPet()`          |
| Data Table    | Mascotas del dueño      | `List<Pet>`     | Filtra por dueño seleccionado           |
| Button        | Desactivar              | N/A             | Calls `controller.deactivatePet(id)`    |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer      | Class Name               | Key Responsibility                                              |
|------------|--------------------------|-----------------------------------------------------------------|
| Exception  | `PetException.java`      | Violación de reglas de negocio de mascota                       |
| Model      | `Pet.java`               | id, userId, name, species, breed, birthDate, active, createdAt  |
| Repository | `PetRepository.java`     | `save`, `findById`, `findActiveByOwnerId`, `updateActive`       |
| Service    | `PetService.java`        | Validaciones BR-001..006 + verificación de dueño en UserRepo    |
| Controller | `PetController.java`     | `createPet(dto)`, `deactivate(id)`, `findByOwner(userId)`       |
| Test       | `PetServiceTest.java`    | Todos los scenarios anteriores                                  |

### 5.2 SQL Queries

```sql
-- Insertar mascota
INSERT INTO simulacro.pets (user_id, name, species, breed, birth_date, active)
VALUES (?, ?, ?, ?, ?, true);

-- Buscar por id
SELECT id, user_id, name, species, breed, birth_date, active, created_at
FROM simulacro.pets WHERE id = ?;

-- Buscar mascotas activas de un dueño
SELECT id, user_id, name, species, breed, birth_date, active, created_at
FROM simulacro.pets
WHERE user_id = ? AND active = true;

-- Verificar que el dueño existe, es CLIENT y está activo
SELECT COUNT(*) FROM simulacro.users
WHERE id = ? AND role = 'CLIENT' AND active = true;

-- Desactivar mascota
UPDATE simulacro.pets SET active = false WHERE id = ?;
```

### 5.3 Dependencies

- [x] Tabla `simulacro.pets` debe existir
- [x] Tabla `simulacro.users` debe existir
- [x] `SPEC-002` — UserRepository disponible para verificar dueño (BR-003)

---

## 6. Out of Scope

- Upload de foto de la mascota
- Registro de peso / talla (se maneja en historial médico — SPEC-005)
- Transferencia de mascota entre dueños

---

## 7. Approval

| Role          | Name | Date | ✓   |
|---------------|------|------|-----|
| Tech Lead     |      |      | [ ] |
| Product Owner |      |      | [ ] |
