# 📋 SPECIFICATION: Gestión de Citas Médicas (Appointments)

> **Spec ID:** `SPEC-004`
> **Author:** Desarrollador
> **Date:** 2026-04-25
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** recepcionista o ADMIN de la clínica "Huellas Sanas",
**I need** agendar, confirmar, cancelar y consultar citas médicas entre mascotas y veterinarios,
**So that** se eliminen los conflictos de agenda (doble reserva) y el equipo siempre tenga visibilidad del estado de cada cita sin recurrir a agendas de papel.

---

## 2. Hard Business Rules

| Rule ID  | Rule Description                                                                                                       | Error Behavior                                                                          |
|----------|------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `BR-001` | `start_time` no puede ser nulo                                                                                         | Throw `AppointmentException("La hora de inicio es obligatoria")`                        |
| `BR-002` | `end_time` debe ser estrictamente mayor que `start_time`                                                               | Throw `AppointmentException("La hora de fin debe ser posterior a la hora de inicio")`   |
| `BR-003` | `start_time` no puede ser una fecha/hora en el pasado                                                                  | Throw `AppointmentException("No se puede agendar una cita en el pasado")`               |
| `BR-004` | El veterinario (`veterinarian_id`) debe existir y corresponder a un user con `active = true`                           | Throw `AppointmentException("El veterinario especificado no está disponible")`          |
| `BR-005` | La mascota (`pet_id`) debe existir con `active = true`                                                                 | Throw `AppointmentException("La mascota especificada no existe o está inactiva")`       |
| `BR-006` | El veterinario NO puede tener otra cita cuyo intervalo se solape con el nuevo `[start_time, end_time]` — **regla crítica anti conflicto de agenda** | Throw `AppointmentException("El veterinario ya tiene una cita en ese horario")`  |
| `BR-007` | Una cita solo puede cancelarse si su `status` es `PENDING` o `CONFIRMED`                                               | Throw `AppointmentException("Solo se pueden cancelar citas pendientes o confirmadas")`  |
| `BR-008` | Una cita solo puede marcarse como `DONE` si su `status` es `CONFIRMED`                                                 | Throw `AppointmentException("Solo se pueden completar citas confirmadas")`              |
| `BR-009` | El usuario (`user_id`) debe existir con `active = true`                                                                | Throw `AppointmentException("El cliente especificado no existe o está inactivo")`       |

### 2.1 Calculation Rules

| Calc ID    | Description                         | Formula                                                                           |
|------------|-------------------------------------|-----------------------------------------------------------------------------------|
| `CALC-001` | Detección de solapamiento de horario | `existingStart < newEnd AND existingEnd > newStart` para el mismo `veterinarian_id`|

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Cita agendada exitosamente
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-006`, `BR-009`

```gherkin
Given un veterinario con id=1 activo existe
  And una mascota con id=2 activa existe
  And un cliente con id=3 activo existe
  And el veterinario NO tiene citas entre "2026-05-10 09:00" y "2026-05-10 10:00"
When  appointmentService.schedule(dto con los datos anteriores) es llamado
Then  la cita es persistida con status=PENDING
  And appointment.getVeterinarianId() equals 1
  And el id generado es retornado
```

---

### Scenario 2 — start_time nulo
> **Validates:** `BR-001`

```gherkin
Given un AppointmentDTO donde startTime es null
When  appointmentService.schedule(dto) es llamado
Then  se lanza AppointmentException con mensaje "La hora de inicio es obligatoria"
```

---

### Scenario 3 — end_time anterior o igual a start_time
> **Validates:** `BR-002`

```gherkin
Given un AppointmentDTO donde startTime="2026-05-10 10:00" y endTime="2026-05-10 09:00"
When  appointmentService.schedule(dto) es llamado
Then  se lanza AppointmentException con mensaje "La hora de fin debe ser posterior a la hora de inicio"
```

---

### Scenario 4 — Cita en el pasado
> **Validates:** `BR-003`

```gherkin
Given un AppointmentDTO donde startTime es ayer a las 09:00
When  appointmentService.schedule(dto) es llamado
Then  se lanza AppointmentException con mensaje "No se puede agendar una cita en el pasado"
```

---

### Scenario 5 — Conflicto de agenda (solape exacto) — REGLA CRÍTICA
> **Validates:** `BR-006`, `CALC-001`

```gherkin
Given el veterinario con id=1 ya tiene una cita de "2026-05-10 09:00" a "2026-05-10 10:00"
  And un nuevo AppointmentDTO para el veterinario id=1 de "2026-05-10 09:30" a "2026-05-10 10:30"
When  appointmentService.schedule(dto) es llamado
Then  se lanza AppointmentException con mensaje "El veterinario ya tiene una cita en ese horario"
  And NO se inserta ningún registro nuevo en appointments
```

---

### Scenario 6 — Conflicto de agenda (cita contenida dentro)
> **Validates:** `BR-006`, `CALC-001`

```gherkin
Given el veterinario con id=1 ya tiene una cita de "2026-05-10 08:00" a "2026-05-10 12:00"
  And un nuevo DTO de "2026-05-10 09:00" a "2026-05-10 10:00" para el mismo veterinario
When  appointmentService.schedule(dto) es llamado
Then  se lanza AppointmentException con mensaje "El veterinario ya tiene una cita en ese horario"
```

---

### Scenario 7 — Sin conflicto: cita inmediatamente después de otra (valor límite)
> **Validates:** `BR-006`, `CALC-001`

```gherkin
Given el veterinario con id=1 tiene una cita de "2026-05-10 09:00" a "2026-05-10 10:00"
  And un nuevo DTO de "2026-05-10 10:00" a "2026-05-10 11:00" para el mismo veterinario
When  appointmentService.schedule(dto) es llamado
Then  la operación completa exitosamente (contigua pero sin solape)
```

---

### Scenario 8 — Cancelar cita con status CONFIRMED
> **Validates:** `BR-007`

```gherkin
Given una cita con id=10 existe con status=CONFIRMED
When  appointmentService.cancel(10) es llamado
Then  appointments.status para id=10 equals CANCELLED
```

---

### Scenario 9 — Cancelar cita ya completada (estado inválido)
> **Validates:** `BR-007`

```gherkin
Given una cita con id=11 existe con status=DONE
When  appointmentService.cancel(11) es llamado
Then  se lanza AppointmentException con mensaje "Solo se pueden cancelar citas pendientes o confirmadas"
  And appointments.status para id=11 sigue siendo DONE
```

---

### Scenario 10 — Confirmar → Completar cita
> **Validates:** `BR-008`

```gherkin
Given una cita con id=12 existe con status=CONFIRMED
When  appointmentService.markAsDone(12) es llamado
Then  appointments.status para id=12 equals DONE
```

---

## 4. UI Requirements

| Element Type  | Name/Label              | Bound to Field      | Action/Validation                              |
|---------------|-------------------------|---------------------|------------------------------------------------|
| Date Picker   | Fecha y hora inicio     | `startTime`         | Required, no pasado (BR-001, BR-003)           |
| Date Picker   | Fecha y hora fin        | `endTime`           | > startTime (BR-002)                           |
| ComboBox      | Veterinario             | `veterinarianId`    | Lista de veterinarios activos (BR-004)         |
| ComboBox      | Mascota                 | `petId`             | Lista de mascotas activas del cliente (BR-005) |
| Text Area     | Notas                   | `notes`             | Opcional                                       |
| Button        | Agendar cita            | N/A                 | Calls `controller.schedule()`                  |
| Button        | Cancelar cita           | N/A                 | Calls `controller.cancel(id)`                  |
| Button        | Marcar como completada  | N/A                 | Calls `controller.markAsDone(id)`              |
| Data Table    | Citas del día           | `List<Appointment>` | Filtra por fecha, muestra status con color     |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer      | Class Name                     | Key Responsibility                                                              |
|------------|--------------------------------|---------------------------------------------------------------------------------|
| Exception  | `AppointmentException.java`    | Violación de reglas de citas                                                    |
| Model      | `Appointment.java`             | id, userId, veterinarianId, petId, startTime, endTime, status, notes, createdAt |
| Repository | `AppointmentRepository.java`   | `save`, `findById`, `findByVetAndDateRange`, `updateStatus`, `findByDate`       |
| Service    | `AppointmentService.java`      | Validaciones BR-001..009 + detección de solape CALC-001                         |
| Controller | `AppointmentController.java`   | `schedule(dto)`, `cancel(id)`, `confirm(id)`, `markAsDone(id)`, `findByDate`   |
| Test       | `AppointmentServiceTest.java`  | Todos los scenarios anteriores (especialmente BR-006)                           |

### 5.2 SQL Queries

```sql
-- Insertar cita
INSERT INTO simulacro.appointments (user_id, veterinarian_id, pet_id, start_time, end_time, status, notes)
VALUES (?, ?, ?, ?, ?, 'PENDING'::status_t, ?);

-- Detectar solapamiento para un veterinario (BR-006 / CALC-001)
SELECT COUNT(*) FROM simulacro.appointments
WHERE veterinarian_id = ?
  AND status NOT IN ('CANCELLED')
  AND start_time < ?     -- newEndTime
  AND end_time   > ?;    -- newStartTime

-- Buscar cita por id
SELECT id, user_id, veterinarian_id, pet_id, start_time, end_time, status, notes, created_at
FROM simulacro.appointments WHERE id = ?;

-- Cambiar status de una cita
UPDATE simulacro.appointments SET status = ?::status_t WHERE id = ?;

-- Buscar citas de un día (vista diaria)
SELECT a.id, a.start_time, a.end_time, a.status, a.notes,
       u.name AS client_name, p.name AS pet_name,
       vu.name AS vet_name
FROM simulacro.appointments a
JOIN simulacro.users u  ON u.id  = a.user_id
JOIN simulacro.pets  p  ON p.id  = a.pet_id
JOIN simulacro.veterinarians v  ON v.id  = a.veterinarian_id
JOIN simulacro.users vu ON vu.id = v.user_id
WHERE DATE(a.start_time) = ?
ORDER BY a.start_time;
```

### 5.3 Dependencies

- [x] Tabla `simulacro.appointments` con `UNIQUE(veterinarian_id, start_time)` y `CHECK(end_time > start_time)`
- [x] Tabla `simulacro.users` y `simulacro.veterinarians`
- [x] Tabla `simulacro.pets`
- [x] `SPEC-002` — UserRepository para verificar veterinario (BR-004) y cliente (BR-009)
- [x] `SPEC-003` — PetRepository para verificar mascota (BR-005)

---

## 6. Out of Scope

- Notificaciones por email/SMS al dueño
- Reprogramación automática de citas canceladas
- Vista de calendario gráfico (solo tabla de citas por día)

---

## 7. Approval

| Role          | Name | Date | ✓   |
|---------------|------|------|-----|
| Tech Lead     |      |      | [ ] |
| Product Owner |      |      | [ ] |
