# Design.md - Documentación de Producción y Manual de Uso

## Visión General del Proyecto
**Colnect URL Builder** es una aplicación móvil nativa para Android desarrollada en Kotlin y Jetpack Compose. Está diseñada para facilitar la navegación, búsqueda y catalogación de numismática (monedas y coleccionables) en el portal internacional **Colnect.com**.

La aplicación combina un generador dinámico de URLs estructuradas con almacenamiento local desacoplado, actualización automática en segundo plano mediante `WorkManager`, e integración con un visor web (`WebView`) optimizado con inyecciones JavaScript para eliminar publicidad y elementos redundantes en dispositivos móviles.

---

## Módulos y Estructura de Archivos

Para garantizar máxima velocidad, orden y mantenibilidad en producción, la aplicación está organizada en archivos independientes por responsabilidad:

1. **`Config.kt`**: Centraliza todas las constantes configurables (URLs base de Colnect, repositorio GitHub, etiquetas de log y límites de tiempo).
2. **`Models.kt`**: Define los modelos de datos (`ColnectItem`, `UpdateInfo`) y estados sellados (`AppState`).
3. **`UpdateWorker.kt`**: Implementación de `CoroutineWorker` para la sincronización periódica en segundo plano cada 3 horas con verificación diferencial del archivo `version.txt`, validación de conteo y descarga selectiva de CSVs únicamente modificados.
4. **`ColnectViewModel.kt`**: ViewModel principal que gestiona el estado reactivo (`StateFlow`), lógica de filtrado numismático, sincronización de cookies/sesiones y parsing acelerado de CSVs.
5. **`Components.kt`**: Componentes de UI reutilizables (campos autocompletables, badges diagnósticos, diálogos de alerta y barra de acción).
6. **`Screens.kt`**: Pantallas principales de la aplicación (Pantalla de Carga, Diseñador de URL Principal y Pantalla WebView integrada).
7. **`ColnectUrlApp.kt`**: Contenedor Compose de nivel superior que gestiona las transiciones entre pantallas, gestos de deslizamiento y diálogos globales.
8. **`MainActivity.kt`**: Punto de entrada de la app en Android que configura el modo pantalla completa inmersivo y el servicio periódico de `WorkManager`.

---

## Manual de Uso para el Usuario Final

### 1. Inicio y Carga Inicial
* Al abrir la aplicación, se presenta una pantalla de bienvenida con un indicador de estado.
* La app carga localmente las bases de datos optimizadas de **Países**, **Valores Faciales**, **Composiciones/Materiales** y **Monedas Locales/Divisas**.
* Si la app se inicia por primera vez sin datos y sin conexión a internet, mostrará una pantalla de aviso con temporizador de reintento.

### 2. Generador Dinámico de URLs
* **Selección de Países:** Ingrese texto en el campo "País" para filtrar por coincidencia flexible de acentos y caracteres especiales (por ejemplo, buscar "mexico" encontrará "México").
* **Selección de Valor Facial:** Seleccione un valor de la lista precargada o escriba un número personalizado.
* **Año de Ceca / Emisión:** Introduzca el año numérico deseado (ej. `1982`).
* **Diámetro (mm):** Ingrese el diámetro en milímetros (ej. `23.5`).
* **Material / Composición:** Seleccione de la lista limpia de composiciones numismáticas (ej. `Plata`, `Bronce`, `Cupro-Níquel`).
* **Moneda / Divisa:** Seleccione la unidad monetaria correspondiente (ej. `Peso Mexicano`).

### 3. Generación y Navegación
* Al presionar **"Generar URL Colnect"**, la app valida la estructura del enlace generado.
* Si hay conexión a internet activa, se abrirá automáticamente el visor **WebView** integrado.
* Si no hay conexión, se presentará un diálogo informativo advirtiendo la falta de red pero permitiendo copiar el enlace.

### 5. Historial de Búsquedas y Reutilización de URLs
* **Botón de Historial:** Ubicado en la barra superior de herramientas (icono de reloj/historial). Muestra una insignia indicadora cuando existen búsquedas previas.
* **Gestión de URLs:** En el diálogo emergente del Historial, el usuario puede revisar las búsquedas generadas anteriormente con sus etiquetas descriptivas, copiarlas al portapapeles, abrirlas en Colnect o eliminar elementos individuales/limpiar todo el historial.

### 6. Alternador Dinámico de Tema (Modo Claro / Modo Oscuro OLED)
* **Conmutador en la Barra de Herramientas:** Un icono contextual (sol/luna) en la parte superior permite alternar instantáneamente entre el tema claro de alto contraste y el tema oscuro OLED negro puro.
* **Persistencia:** La preferencia de tema se almacena localmente y se aplica de forma automática al iniciar la aplicación.

### 7. Visualización de URL en Tiempo Real
* **Muestra No Editable:** Encima del botón de acción principal, un bloque de texto no editable muestra la URL de Colnect construida exactamente en tiempo real a medida que el usuario escribe en cualquier campo.
* **Copia Rápida:** Incluye un botón dedicado de un toque para copiar la URL construida al portapapeles al instante.

---

## Registro de Eventos (Logging System)

La aplicación implementa un sistema unificado de trazabilidad e información en consola para auditoría en tiempo de ejecución:
* Todas las llamadas a funciones clave en `ViewModel`, `WorkManager` y componentes UI quedan registradas bajo el tag `ColnectApp` en `Logcat`.
* Formato de registro: `[Clase::Metodo] Called with params: ...`
* Esto permite depurar el rendimiento en producción sin afectar la experiencia del usuario.

---

## Repositorio de Datos GitHub (`colnect-data`)

Para mantener actualizada la aplicación sin necesidad de publicar APKs constantemente, los datos de catálogo se sincronizan desde el repositorio público:
**`https://github.com/gabrielschvartz/colnect-data`**

### Estructura del Repositorio Remoto:
* `version.txt`: Archivo de control que contiene las versiones y recuentos esperados:
  ```text
  VERSION = 1.0.0
  PAISES = 247
  DENOMINACIONES = 2065
  MATERIALES = 132
  VALORES_FACIALES = 1250
  ```
* `paises.csv`: Lista de países en formato `Nombre;FragmentoURL` o `FragmentoURL,Nombre`.
* `denominaciones.csv`: Lista de monedas locales / divisas.
* `materiales.csv`: Lista de composiciones numismáticas.
* `valores_faciales.csv`: Lista de valores faciales catalogados.

### Instrucciones para Mantener el Repositorio Actualizado:
1. Al agregar o actualizar un archivo CSV en GitHub, incremente o actualice únicamente el recuento de registros de ese campo en `version.txt` (ej. cambiar `PAISES = 248`).
2. El servicio `UpdateWorker` de la app consultará este archivo cada 3 horas (utilizando un parámetro cache-buster `?t=timestamp`).
3. La aplicación comparará la línea `VERSION=` y cada línea individual. Si solo cambió `PAISES`, la app descargará **únicamente `paises.csv`**, conservando intactos los demás archivos sin realizar descargas innecesarias.

---

## Compatibilidad Responsiva para Tablets y Exportación Web (Capacitor)

La aplicación está completamente adaptada para funcionar en dispositivos móviles, tablets de pantalla ancha y como PWA / Aplicación Web empaquetada mediante **Capacitor**:
* **Contenedor Responsivo (`BoxWithConstraints`):** Ajusta automáticamente el ancho del formulario y las vistas intermedias (`maxWidth = 750.dp`) centrándolas en pantallas de tabletas o navegadores web para evitar estiramientos desproporcionados.
* **Layouts Adaptativos:** Se adapta fluido a cambios de orientación (Vertical y Horizontal) y estados de teclado virtual.
* **Inyección y Navegación Web:** El visor WebView está desacoplado para ser empaquetado en contenedores híbridos (Capacitor Browser Plugin / WebViews) sin perder soporte para cookies y sesión.

---

## Resolución de Firma y Compilación Local (Keystore)

Al clonar o exportar el proyecto a un entorno local, el archivo `debug.keystore` se encuentra ignorado en `.gitignore` por seguridad, pero el repositorio incluye `debug.keystore.base64`.
* **Auto-restauración en Gradle:** `build.gradle.kts` (raíz) decodifica automáticamente `debug.keystore.base64` en `debug.keystore` si no existe en la carpeta raíz al compilar.
* **Restauración manual (en Windows PowerShell):**
  ```powershell
  [System.IO.File]::WriteAllBytes("debug.keystore", [System.Convert]::FromBase64String((Get-Content "debug.keystore.base64" -Raw).Trim()))
  ```
* **Restauración manual (en Símbolo del Sistema / CMD):**
  ```cmd
  certutil -decode debug.keystore.base64 debug.keystore
  ```

---

## Mejoras y Correcciones Sugeridas para Futuras Versiones

1. **Almacenamiento en Room Database (Opcional para Grandes Volúmenes):**
   * *Diagnóstico:* Actualmente la app utiliza parsing acelerado en memoria sobre CSVs locales con excelentes tiempos de respuesta (inferiores a 50ms).
   * *Recomendación:* Si la base de datos supera los 50.000 registros en el futuro, se sugiere migrar a una base de datos SQLite indexada con **Room**.

2. **Modo Oscuro Personalizado:**
   * *Diagnóstico:* La interfaz actual utiliza un tema claro de alto contraste optimizado para legibilidad.
   * *Recomendación:* Incorporar una opción en la barra de herramientas para alternar dinámicamente entre tema claro y tema oscuro OLED.

3. **Exportación de Búsquedas e Historial:**
   * *Recomendación:* Añadir una pestaña de "Favoritos / Historial de Búsquedas" para guardar enlaces frecuentes o colecciones personales.
