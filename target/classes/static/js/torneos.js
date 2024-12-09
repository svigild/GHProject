let timeoutId;

document.addEventListener('DOMContentLoaded', function() {
    const buscarJuegoInput = document.getElementById('buscarJuego');
    if (buscarJuegoInput) {
        buscarJuegoInput.addEventListener('input', function(e) {
            clearTimeout(timeoutId);
            const query = e.target.value;

            if (query.length < 3) {
                document.getElementById('resultadosBusqueda').innerHTML = '';
                return;
            }

            timeoutId = setTimeout(() => {
                buscarJuegos(query);
            }, 300);
        });
    }
});

function buscarJuegos(query) {
    fetch(`/buscarJuegosTorneos?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            const resultados = document.getElementById('resultadosBusqueda');
            resultados.innerHTML = '';
            data.forEach(juego => {
                const item = document.createElement('a');
                item.href = '#';
                item.className = 'list-group-item list-group-item-action';
                item.innerHTML = `<img src="${juego.background_image}" alt="${juego.name}" style="width: 50px; height: 50px; object-fit: cover;"> ${juego.name}`;
                item.onclick = function(e) {
                    e.preventDefault();
                    seleccionarJuego(juego);
                };
                resultados.appendChild(item);
            });
        })
        .catch(error => console.error('Error:', error));
}

function seleccionarJuego(juego) {
    document.getElementById('juegoSeleccionado').value = juego.name;
    document.getElementById('juegoId').value = juego.id;
    document.getElementById('buscarJuego').value = juego.name;
    document.getElementById('resultadosBusqueda').innerHTML = '';

    // Mostrar la información del juego seleccionado
    const juegoInfo = document.getElementById('juegoSeleccionadoInfo');
    const juegoImagen = document.getElementById('juegoImagen');
    const juegoNombre = document.getElementById('juegoNombre');
    const juegoDescripcion = document.getElementById('juegoDescripcion');

    juegoImagen.src = juego.background_image;
    juegoNombre.textContent = juego.name;

    let description = juego.description_raw ? juego.description_raw : '';
    juegoDescripcion.textContent = description.length > 200 ? description.substring(0, 200) + '...' : description;

    // Actualizar el campo oculto con la URL de la imagen
    document.getElementById('imagenTorneo').value = juego.background_image;  // Establecer la URL de la imagen del juego

    juegoInfo.style.display = 'block';
}

// Muestra la imagen seleccionada al crear el torneo
document.addEventListener('DOMContentLoaded', function() {
    const imagenTorneoInput = document.getElementById('imagenTorneo');
    if (imagenTorneoInput) {
        imagenTorneoInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            const previewImagen = document.getElementById('previewImagen');

            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewImagen.src = e.target.result;
                    previewImagen.style.display = 'block';
                }
                reader.readAsDataURL(file);
            } else {
                previewImagen.style.display = 'none';
            }
        });
    }
});

function eliminarTorneo(event, id) {
    event.preventDefault();
    if (confirm('¿Estás seguro de que quieres eliminar este torneo?')) {
        fetch(`/torneos/eliminar/${id}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            }
        }).then(response => {
            if (response.ok) {
                window.location.href = '/torneos';
            } else {
                alert('Error al eliminar el torneo');
            }
        });
    }
}


// validar formulario al crear un torneo
function validateForm() {
    const nombre = document.getElementById('nombreTorneo').value;
    const descripcion = document.getElementById('descripcionTorneo').value;
    const fechaInicio = document.getElementById('fechaInicio').value;
    const fechaFin = document.getElementById('fechaFin').value;

    // Resetear el mensaje de alerta
    document.getElementById('alertContainer').style.display = 'none';

    // Validaciones
    if (!nombre || !descripcion || !fechaInicio || !fechaFin) {
        showAlert("Todos los campos son obligatorios.");
        return false; // Evitar el envío del formulario
    }

    if (new Date(fechaInicio) >= new Date(fechaFin)) {
        showAlert("La fecha de inicio debe ser anterior a la fecha de fin.");
        return false; // Evitar el envío del formulario
    }

    return true; // Permitir el envío del formulario
}

// Función para mostrar alertas
function showAlert(message) {
    const alertContainer = document.getElementById('alertContainer');
    const alertMessage = document.getElementById('alertMessage');

    alertMessage.textContent = message;
    alertContainer.style.display = 'block';
}