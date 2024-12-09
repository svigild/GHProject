// Función para cargar una imagen de fondo aleatoria de videojuegos
async function setBackgroundImage() {
    try {
        const response = await fetch('https://api.unsplash.com/photos/random?query=video-games&client_id=xMnbI9NTApKmjT8q7fiCrzAOSfNJDywKEHMPwUCMOUo');
        const data = await response.json();
        const imageUrl = data.urls.regular;
        document.body.style.backgroundImage = `url(${imageUrl})`;
    } catch (error) {
        console.error('Error fetching background image:', error);
    }
}
setBackgroundImage();

// Validar formulario
function validateForm() {
    const form = document.querySelector('form');
    const inputs = form.querySelectorAll('input');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.checkValidity()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });

    return isValid;
}