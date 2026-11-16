<div align="center">

  <!-- 1. LOGO DE NEXUSCLASS -->
  <a href="TU_LINK_DEL_SITIO_WEB" target="_blank">
    <img src="Recursos/logo.png" alt="NexusClass Logo" width="150" />
  </a>

  <br><br>

  <!-- 2. TÍTULO DEL PROYECTO -->
  <h1>NexusClass App Movil (Android)</h1>

  <p align="center">
    <b>Solución Móvil Inteligente, Tu mejor aliado en la U! Donde cuentas con:</b>
    <br>
    
*   **Carga de Horario PDF:** El estudiante sube el PDF descargado de Moodle. La API extrae y procesa automáticamente las asignaturas, días, horas y salones.
*   **Dashboard Diario:** Vista en vivo adaptada al día actual que resalta en pantalla grande la "Clase Actual" y cuál es la "Próxima Clase" a asistir.

*   **Control de Tareas:** Módulo CRUD completo para registrar, editar, listar y eliminar tareas o exámenes vinculados específicamente a cada asignatura.
  </p>

  <!-- 3. ENLACES DIRECTOS (APK Y WEB) -->
  <p align="center">
    <a href="https://drive.google.com/drive/folders/1_NrRbXYCXTXUAO9Mib1DWM-AI-Fta8Zp?usp=sharing" target="_blank">
      <img src="https://img.shields.io/badge/📱_Descargar_APK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK desde Drive">
    </a>
    <a href="https://nexusclassweb.netlify.app" target="_blank">
      <img src="https://img.shields.io/badge/🌐_Visitar_Plataforma_Web-2196F3?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Visitar Sitio Web">
    </a>
  </p>
</div>

---

## ⚙️ Arquitectura y Stack Tecnológico

<div align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=kotlin,android,nodejs,express,mongodb&theme=light" alt="Tech Stack Principal" />
  </a>
</div>
<br>

El proyecto está construido bajo una arquitectura moderna dividida en las siguientes capas:

### 📱 Capa Android
*   **Lenguaje y UI:** Kotlin, Jetpack Compose + Material 3
*   **Arquitectura y Estado:** MVVM, ViewModel + StateFlow
*   **Navegación:** Navigation Compose
*   **Peticiones de Red:** Retrofit + OkHttp
*   **Tareas en Segundo Plano:** WorkManager (alertas)

### 🛠️ Capa API
*   **Entorno y Framework:** Node.js, Express
*   **Procesamiento y Utilidades:** Mongoose, multer (manejo de archivos), pdf-parse, luxon (gestión de zonas horarias)

### 🗄️ Base de Datos y Hosting
*   **Base de datos:** MongoDB Atlas 
*   **Hosting de la API:** Render 

---

## 🚀 Instalación 

### Requisitos Previos
- Android Api 32+ (v12 o superior)
- Conexion a Internet
- Cuenta en la Corporación Universitaria Autónoma Del Cauca  **(Obligatorio)**

### Creditos
1. Desarrollado por:
   ```bash
   Marcos David Mejia Escobar
