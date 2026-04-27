# 📋 SPECIFICATION: Registro Integral y Historial Médico

> **Spec ID:** `SPEC-005`
> **Author:** Desarrollador
> **Date:** 2026-04-25
> **Status:** [x] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** recepcionista de la clínica "Huellas Sanas",
**I need** registrar una nueva mascota y su primera cita en un solo paso transaccional, y luego registrar el diagnóstico al completar la consulta,
**So that** nunca queden mascotas sin cita (datos huérfanos) y el historial médico de cada paciente esté siempre disponible y unificado para consulta rápida.

---

## 2. Hard Business Rules

### Bloque A — Registro Integral (Mascota + Primera Cita en transacción JDBC)

| Rule ID  | Rule Description                                                                                                                         | Error Behavior                                                                         |
|----------|------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `BR-001` | Todos los campos obligatorios de la mascota deben cumplir las reglas de SPEC-003 (nombre, especie, dueño activo)                         | Re-lanza `PetException` correspondiente                                                |
| `BR-002` | Todos los campos obligatorios de la cita deben cumplir las reglas de SPEC-004 (horarios, veterinario, conflicto de agenda)               | Re-lanza `AppointmentException` correspondiente                                        |
| `BR-003` | Si la inserción de la cita falla por cualquier causa (controlada o excepción), la inserción de la mascota debe revertirse con `rollback` | La mascota NO debe persistirse si la cita no pudo crearse — no datos huérfanos         |
| `BR-004` | Si la inserción de la mascota falla, no debe intentarse la inserción de la cita                                                          | Throw `RegistroIntegralException("Falló el registro de la mascota: " + causa)`         |
| `BR-005` | Toda la operación debe ejecutarse dentro de una sola transacción JDBC (`setAutoCommit(false)` → `commit()` / `rollback()`)              | (Control interno — garantía transaccional)                                             |

### Bloque B — Historial Médico (medical_records)

| Rule ID  | Rule Description                                                                                                        | Error Behavior                                                                       |
|----------|-------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `BR-006` | El `appointment_id` debe existir en `appointments` con `status = DONE`                                                  | Throw `MedicalRecordException("Solo se puede registrar historial en citas completadas")` |
| `BR-007` | El diagnóstico (`diagnosis`) no puede ser nulo ni vacío                                                                 | Throw `MedicalRecordException("El diagnóstico es obligatorio")`                      |
| `BR-008` | Solo puede existir un `medical_record` por cita (`appointment_id` UNIQUE)                                               | Throw `MedicalRecordException("Esta cita ya tiene un registro médico asociado")`     |
| `BR-009` | El `pet_id` y `veterinarian_id` del registro deben coincidir con los de la cita asociada                                | Throw `MedicalRecordException("Los datos de la cita no coinciden con el registro")`  |

### 2.1 Calculation Rules

No aplican cálculos numéricos en este spec. La lógica central es transaccional.

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Registro integral exitoso (mascota + cita)
> **Validates:** `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`

```gherkin
Given un cliente con id=1 activo existe
  And un veterinario con id=2 activo existe sin conflicto de agenda
  And un PetDTO válido y un AppointmentDTO válido sin conflictos
When  integralService.registerPetWithAppointment(petDto, appointmentDto) es llamado
Then  se inserta la mascota en simulacro.pets con active=true
  And se inserta la cita en simulacro.appointments con status=PENDING
  And ambas inserciones ocurren con commit() en la misma transacción
  And se retorna un objeto con petId y appointmentId generados
```

---

### Scenario 2 — Rollback: la cita falla por conflicto de agenda
> **Validates:** `BR-003`, `BR-005`

```gherkin
Given un veterinario con id=2 ya tiene una cita de "2026-06-01 10:00" a "2026-06-01 11:00"
  And un PetDTO válido
  And un AppointmentDTO con horario "2026-06-01 10:30" a "2026-06-01 11:30" para el mismo veterinario
When  integralService.registerPetWithAppointment(petDto, appointmentDto) es llamado
Then  se lanza AppointmentException con mensaje "El veterinario ya tiene una cita en ese horario"
  And la mascota NO está en simulacro.pets (rollback ejecutado)
  And el total de registros en pets es igual al que había antes de la llamada
```

---

### Scenario 3 — Rollback: la mascota falla por dueño inactivo
> **Validates:** `BR-001`, `BR-004`

```gherkin
Given un PetDTO con userId=99 que NO existe en la BD
  And un AppointmentDTO válido
When  integralService.registerPetWithAppointment(petDto, appointmentDto) es llamado
Then  se lanza PetException con mensaje "El dueño especificado no existe o está inactivo"
  And NO se inserta ningún registro en pets ni en appointments
```

---

### Scenario 4 — Registro de historial médico exitoso
> **Validates:** `BR-006`, `BR-007`, `BR-008`, `BR-009`

```gherkin
Given una cita con id=5 existe con status=DONE, pet_id=3, veterinarian_id=1
  And NO existe ningún medical_record para appointment_id=5
  And un MedicalRecordDTO válido: appointmentId=5, petId=3, veterinarianId=1,
      diagnosis="Otitis externa leve", treatment="Limpieza y gotas antibióticas"
When  medicalRecordService.create(dto) es llamado
Then  el registro es persistido en simulacro.medical_records
  And medical_records.diagnosis equals "Otitis externa leve"
  And medical_records.appointment_id equals 5
```

---

### Scenario 5 — Diagnóstico nulo o vacío
> **Validates:** `BR-007`

```gherkin
Given un MedicalRecordDTO donde diagnosis es null o ""
When  medicalRecordService.create(dto) es llamado
Then  se lanza MedicalRecordException con mensaje "El diagnóstico es obligatorio"
```

---

### Scenario 6 — Cita no completada (status != DONE)
> **Validates:** `BR-006`

```gherkin
Given una cita con id=8 existe con status=CONFIRMED
  And un MedicalRecordDTO con appointmentId=8
When  medicalRecordService.create(dto) es llamado
Then  se lanza MedicalRecordException con mensaje "Solo se puede registrar historial en citas completadas"
```

---

### Scenario 7 — Registro médico duplicado para la misma cita
> **Validates:** `BR-008`

```gherkin
Given ya existe un medical_record para appointment_id=5
  And un nuevo MedicalRecordDTO con appointmentId=5
When  medicalRecordService.create(dto) es llamado
Then  se lanza MedicalRecordException con mensaje "Esta cita ya tiene un registro médico asociado"
  And NO se inserta ningún registro duplicado
```

---

### Scenario 8 — Consultar historial completo de una mascota
> **Validates:** `BR-006` (solo citas DONE tienen historial)

```gherkin
Given la mascota con id=3 tiene 3 citas DONE con sus respectivos medical_records
  And tiene 1 cita CONFIRMED sin medical_record
When  medicalRecordService.findByPet(3) es llamado
Then  el resultado contiene exactamente 3 registros médicos
  And cada registro incluye diagnosis, treatment, vaccines_applied y recorded_at
  And el resultado está ordenado por recorded_at DESC
```

---

### Scenario 9 — Datos de cita no coinciden con el registro médico
> **Validates:** `BR-009`

```gherkin
Given una cita con id=10 tiene pet_id=3 y veterinarian_id=1
  And un MedicalRecordDTO con appointmentId=10, petId=99, veterinarianId=1
When  medicalRecordService.create(dto) es llamado
Then  se lanza MedicalRecordException con mensaje "Los datos de la cita no coinciden con el registro"
```

---

### Scenario 10 — Rollback por excepción inesperada en la cita (RuntimeException)
> **Validates:** `BR-003`, `BR-005`

```gherkin
Given el AppointmentRepository lanza RuntimeException("Fallo de BD") al intentar insertar
  And el PetDTO es válido y se insertaría correctamente
When  integralService.registerPetWithAppointment(petDto, appointmentDto) es llamado
Then  se ejecuta rollback() en la conexión JDBC
  And la mascota NO persiste en la BD
  And se lanza RegistroIntegralException wrapping la causa original
```

---

## 4. UI Requirements

### Panel: Registro Integral

| Element Type  | Name/Label              | Bound to Field     | Action/Validation                           |
|---------------|-------------------------|--------------------|---------------------------------------------|
| Input Text    | Nombre mascota          | `petName`          | Required (BR-001)                           |
| Input Text    | Especie                 | `species`          | Required (BR-001)                           |
| ComboBox      | Dueño (CLIENT)          | `userId`           | Lista CLIENTs activos (BR-001)              |
| ComboBox      | Veterinario             | `veterinarianId`   | Lista vets activos (BR-002)                 |
| Date Picker   | Fecha y hora inicio     | `startTime`        | No pasado, required (BR-002)                |
| Date Picker   | Fecha y hora fin        | `endTime`          | > startTime (BR-002)                        |
| Button        | Registrar todo          | N/A                | Calls `controller.registerPetWithAppointment()` |

### Panel: Registro de Historial Médico

| Element Type  | Name/Label              | Bound to Field       | Action/Validation                           |
|---------------|-------------------------|----------------------|---------------------------------------------|
| ComboBox      | Cita completada         | `appointmentId`      | Solo citas con status=DONE (BR-006)         |
| Text Area     | Diagnóstico             | `diagnosis`          | Required (BR-007)                           |
| Text Area     | Tratamiento             | `treatment`          | Opcional                                    |
| Input Text    | Vacunas aplicadas       | `vaccinesApplied`    | Opcional                                    |
| Text Area     | Notas clínicas          | `notes`              | Opcional                                    |
| Button        | Guardar registro        | N/A                  | Calls `controller.createMedicalRecord()`    |

### Panel: Historial por Mascota

| Element Type  | Name/Label              | Bound to Field           | Action/Validation                         |
|---------------|-------------------------|--------------------------|-------------------------------------------|
| ComboBox      | Mascota                 | `petId`                  | Lista de mascotas activas                 |
| Data Table    | Historial médico        | `List<MedicalRecord>`    | Ordenado por fecha DESC                   |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer      | Class Name                        | Key Responsibility                                                                     |
|------------|-----------------------------------|----------------------------------------------------------------------------------------|
| Exception  | `RegistroIntegralException.java`  | Fallo en la operación transaccional mascota + cita                                     |
| Exception  | `MedicalRecordException.java`     | Violación de reglas de historial médico                                                |
| Model      | `MedicalRecord.java`              | id, appointmentId, petId, veterinarianId, diagnosis, treatment, notes, vaccinesApplied, recordedAt |
| Repository | `MedicalRecordRepository.java`    | `save`, `findByPetId`, `findByAppointmentId`, `existsByAppointmentId`                  |
| Service    | `IntegralRegistrationService.java`| Transacción JDBC: inserta Pet → inserta Appointment → commit / rollback                |
| Service    | `MedicalRecordService.java`       | Validaciones BR-006..009 + delegación a MedicalRecordRepository                        |
| Controller | `IntegralController.java`         | `registerPetWithAppointment(petDto, appointmentDto)`                                   |
| Controller | `MedicalRecordController.java`    | `createMedicalRecord(dto)`, `findHistoryByPet(petId)`                                  |
| Test       | `IntegralRegistrationServiceTest.java` | Scenarios 1, 2, 3, 10 (transaccionalidad y rollback)                              |
| Test       | `MedicalRecordServiceTest.java`   | Scenarios 4, 5, 6, 7, 8, 9                                                            |

### 5.2 SQL Queries

```sql
-- ── REGISTRO INTEGRAL (ejecutar dentro de setAutoCommit(false)) ──

-- 1. Insertar mascota
INSERT INTO simulacro.pets (user_id, name, species, breed, birth_date, active)
VALUES (?, ?, ?, ?, ?, true);
-- → obtener pet.id generado con getGeneratedKeys()

-- 2. Insertar cita (usa pet.id obtenido en el paso anterior)
INSERT INTO simulacro.appointments (user_id, veterinarian_id, pet_id, start_time, end_time, status, notes)
VALUES (?, ?, ?, ?, ?, 'PENDING'::status_t, ?);

-- ── HISTORIAL MÉDICO ──

-- Verificar status de la cita antes de insertar (BR-006)
SELECT status, pet_id, veterinarian_id
FROM simulacro.appointments WHERE id = ?;

-- Verificar que no existe registro previo (BR-008)
SELECT COUNT(*) FROM simulacro.medical_records WHERE appointment_id = ?;

-- Insertar registro médico
INSERT INTO simulacro.medical_records
    (appointment_id, pet_id, veterinarian_id, diagnosis, treatment, notes, vaccines_applied)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- Consultar historial completo de una mascota (ordenado)
SELECT mr.id, mr.diagnosis, mr.treatment, mr.notes, mr.vaccines_applied, mr.recorded_at,
       a.start_time, a.end_time,
       vu.name AS vet_name, v.speciality
FROM simulacro.medical_records mr
JOIN simulacro.appointments a    ON a.id  = mr.appointment_id
JOIN simulacro.veterinarians v   ON v.id  = mr.veterinarian_id
JOIN simulacro.users vu          ON vu.id = v.user_id
WHERE mr.pet_id = ?
ORDER BY mr.recorded_at DESC;
```

### 5.3 Patrón Transaccional Obligatorio

```java
// IntegralRegistrationService.registerPetWithAppointment()
Connection conn = ConnectionFactory.getConnection();
try {
    conn.setAutoCommit(false);

    long petId = petRepository.save(petDto, conn);         // usa misma conexión
    long apptId = appointmentRepository.save(apptDto.withPetId(petId), conn);

    conn.commit();
    return new RegistroIntegralResult(petId, apptId);

} catch (AppointmentException | PetException e) {
    conn.rollback();
    throw e;                                               // re-lanza la excepción de negocio
} catch (Exception e) {
    conn.rollback();
    throw new RegistroIntegralException("Error inesperado: " + e.getMessage(), e);
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

### 5.4 Dependencies

- [x] Tabla `simulacro.pets`
- [x] Tabla `simulacro.appointments`
- [x] Tabla `simulacro.medical_records` con `appointment_id UNIQUE`
- [x] `SPEC-003` — PetService/PetRepository para validaciones de mascota
- [x] `SPEC-004` — AppointmentService/AppointmentRepository para validaciones de cita

---

## 6. Out of Scope

- Adjuntar archivos (radiografías, análisis) al historial
- Envío automático del historial al dueño por email
- Historial compartido entre clínicas

---

## 7. Approval

| Role          | Name | Date | ✓   |
|---------------|------|------|-----|
| Tech Lead     |      |      | [ ] |
| Product Owner |      |      | [ ] |
