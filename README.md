# Runner Service

Microservicio encargado de **validar, ejecutar, formatear y lintéar snippets de código PrintScript**.  
Forma parte del sistema **Snippet Searcher**, junto con:

-  `snippet-service`: gestiona snippets, tests y configuraciones.
-  `authorization-service`: maneja autenticación y permisos de usuarios.
- ️ `runner-service` (este): procesa los snippets usando la librería `printscript`.

---

## Propósito

El **runner-service** no guarda datos: su responsabilidad es **procesar y devolver resultados**.  
Se comunica con el `snippet-service` para obtener el código fuente y devolver los resultados de validación o ejecución.

### Funcionalidades principales

-  **Validación sintáctica y semántica** de snippets (posición exacta de errores).
-  **Ejecución de código** (modo simple o interactivo).
-  **Formateo** de código según reglas definidas.
-  **Análisis estático (lint)** para verificar buenas prácticas.
-  **Ejecución de tests asociados** a snippets.
-  **Re-evaluación batch** cuando cambia la versión del parser o las reglas.

---


