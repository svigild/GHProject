/**
 * Funcion para eliminar un juego de la biblioteca del usuario.
 * @param juegoId
 */
function eliminarJuego(event, juegoId) {
    event.preventDefault(); // Evitar el submit por defecto del formulario

    $.ajax({
        url: '/biblioteca/eliminar/' + juegoId,
        type: 'DELETE',

        success: function(response) {
            alert('El juego se eliminó correctamente.');
            location.reload(); // Recargar la página después de eliminar el juego
        },

        error: function(xhr, status, error) {
            alert('Error al intentar eliminar el juego: ' + error);
        }
    });
}

var cronometros = {};

function cronometroJuego(event, juegoId) {
    var btn = document.getElementById('cronometro-btn-' + juegoId);
    if (btn) {
        btn.innerHTML = '<i class="fas fa-stopwatch"></i> Parar';
    }
    var tiempoTotalElem = document.getElementById('tiempo-total-' + juegoId);

    if (!cronometros[juegoId]) {
        // Iniciar cronómetro
        cronometros[juegoId] = {
            startTime: new Date(),
            intervalId: setInterval(function () {
                var tiempoJugado = new Date() - cronometros[juegoId].startTime;
                tiempoTotalElem.innerText = new Date(tiempoJugado).toISOString().substr(11, 8);
            }, 1000)
        };
        btn.innerHTML = '<i class="fas fa-stopwatch"></i> Parar';

        // Realizar solicitud para iniciar el tiempo jugado en el servidor
        fetch('/api/tiempojugado/iniciar?usuarioId=' + usuarioId + '&juegoId=' + juegoId, {
            method: 'POST',
        })
            .then(response => response.json())
            .then(data => {
                cronometros[juegoId].id = data.id;  // Almacenar el ID devuelto por el servidor
            })
            .catch(error => {
                console.error('Error al iniciar el tiempo jugado:', error);
            });

    } else {
        // Parar cronómetro y actualizar el tiempo jugado
        clearInterval(cronometros[juegoId].intervalId);
        var tiempoJugado = new Date() - cronometros[juegoId].startTime;

        // Actualizar el tiempo total jugado en el servidor
        fetch('/api/tiempojugado/terminar/' + cronometros[juegoId].id, {
            method: 'POST',
        })
            .then(response => response.json())
            .then(data => {
                tiempoTotalElem.innerText = new Date(data.tiempoTotal).toISOString().substr(11, 8);
            })
            .catch(error => {
                console.error('Error al terminar el tiempo jugado:', error);
            });

        delete cronometros[juegoId];
        btn.innerHTML = '<i class="fas fa-stopwatch"></i> Iniciar';
    }
}
