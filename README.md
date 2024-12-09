# GameHub
Proyecto final del Grado Superior en Desarrollo de Aplicaciones Multiplataforma

## Como arrancar el proyecto

1. Arrancar el contenedor de SQL de Docker
   ![image](https://github.com/user-attachments/assets/42a36233-5a80-4a4f-aea0-e560083e721a)

2. Configurar la conexión a la base de datos en el archivo application.properties
   ![image](https://github.com/user-attachments/assets/83490f16-65e4-4685-9b20-f5abf2790730)

3. Arrancar el proyecto en IntelliJ Idea y acceder a la URL de localhost:9090


## Caracteristicas

- Sistema de login y registro
- Sistema de amistad entre usuarios
- Búsqueda y visualización de datos sobre diferentes videojuegos mediante la API de RAWG.
- Biblioteca para cada usuario con los juegos que posee
- Sección de noticias sobre videojuegos a través de una API
- Foros donde los usuarios pueden compartir sus experiencias y dudas sobre los videojuegos (Próximamente)

## Tecnologías utilizadas

La base de datos en SQL, el backend en Spring Boot y el frontend en Thymeleaf.

## APIs utilizadas

- API de videojuegos: RAWG - https://rawg.io/
- API de noticias: NewsAPI - https://newsapi.org/

## Arquitectura utilizada

He decidido utilizar el Modelo Vista Controlador (MVC) que es un patrón que separa la aplicación en tres partes: **Modelo** (datos), **Vista** (interfaz) y **Controlador** (gestiona la interacción).  

He decidido utilizarlo en mi proyecto para mantener una estructura modular y facilitar futuras actualizaciones o cambios en la interfaz sin afectar la lógica de negocio.

## Capturas de pantalla
![image](https://github.com/user-attachments/assets/c9833f51-a1dd-4a4a-9e95-3bd5d2afad64)
![image (1)](https://github.com/user-attachments/assets/94e7a66c-4e33-411d-b790-3c70bae36d23)
![image (2)](https://github.com/user-attachments/assets/7a4dbfb1-e1a3-434d-a726-b07f33993eb3)
![image (3)](https://github.com/user-attachments/assets/acf99fd0-1ad3-4d7d-b702-94e831b227b7)
![image (4)](https://github.com/user-attachments/assets/ab78c769-b19b-41b1-b7fe-7a09e7a5940f)

