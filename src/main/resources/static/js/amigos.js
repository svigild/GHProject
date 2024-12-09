function enviarSolicitudAmistad() {
    var email = document.getElementById('email').value; // Obtén el email del campo

    // Obtén el token CSRF desde el formulario oculto o el meta tag
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajax({
        url: '/enviarSolicitudAmistad',
        type: 'POST',
        data: {
            email: email
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken); // Agregar el token CSRF en el encabezado
        },
        success: function(response) {
            alert("Solicitud de amistad enviada con éxito."); // Mensaje de éxito
            location.reload(); // Recargar la página
        },
        error: function(xhr) {
            alert(xhr.responseText); // Mostrar el mensaje de error recibido del servidor
        }
    });
}

function cargarDetallesAmigo(amigoId) {
    $.ajax({
        url: '/amigos/' + amigoId + '/detalles',
        type: 'GET',
        success: function (data) {
            // Formato de año de nacimiento
            const anioNacimiento = data.anioNacimiento.split('T')[0]; // Solo la fecha sin tiempo

            // Crear las "píldoras" para el email y año de nacimiento
            const infoHtml = `
                <img src="${data.fotoPerfil}" class="mb-3" alt="Foto de perfil" height="100" width="100">
                <h3>${data.nombre} ${data.apellidos}</h3>
                <p class="badge bg-secondary detalles-amigo">${data.email}</p>
                <p class="badge bg-secondary detalles-amigo">${anioNacimiento}</p>
            `;

            // Comenzar a construir la sección de juegos
            let juegosHtml = '';
            if (data.juegos && data.juegos.length > 0) {
                juegosHtml = data.juegos.map(juego => `
                    <div class="card game-card mx-2" style="flex: 0 0 auto; width: 200px;">
                        <img src="${juego.foto}" class="card-img-top" alt="${juego.nombre}">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">${juego.nombre}</h5>
                            <p class="card-text fecha-salida">${juego.fechaSalida}</p>
                            <p class="card-text">
                                ${juego.plataformas && juego.plataformas.length > 0 ? juego.plataformas.map(platform => `
                                    <span class="plataforma">${platform}</span>
                                `).join('') : 'No disponibles'}
                            </p>
                        </div>
                    </div>
                `).join('');
            } else {
                juegosHtml = `<p>${data.nombre} no ha añadido juegos a su biblioteca aún.</p>`;
            }

            // Actualizar el modal con la información del amigo y juegos
            $('#detallesAmigo').html(infoHtml);
            $('#carouselJuegos').html(juegosHtml);

            // Mostrar el modal después de cargar los detalles
            $('#amigoModal').modal('show');  // Mostrar el modal
        },
        error: function (error) {
            console.error('Error al cargar los detalles del amigo:', error);
        }
    });
}

function eliminarAmigo(amigoId) {
    // Confirmar la eliminación
    if (confirm('¿Estás seguro de que deseas eliminar a este amigo?')) {
        $.ajax({
            url: '/eliminarAmigo/' + amigoId,
            type: 'POST',
            success: function() {
                // Eliminar el amigo de la lista
                location.reload(); // Recargar la página para actualizar la lista de amigos
            },
            error: function(error) {
                console.error('Error al eliminar el amigo:', error);
                alert('No se pudo eliminar al amigo. Inténtalo de nuevo.');
            }
        });
    }
}
/*
function abrirChat(amigoId) {

    //Establezco la conexion
    document.addEventListener("DOMContentLoaded", function() {
        conectarWS(); // Establecer la conexión WebSocket
    });

    const chatFriendName = document.getElementById("chatFriendName");

    // Establecer el ID del amigo directamente
    console.log("Amigo ID:", amigoId);

    // Aquí podrías agregar lógica para obtener el nombre del amigo de otra manera si es necesario.
    // Por ahora, puedes asignar el amigoId como nombre, o dejarlo vacío.
    chatFriendName.textContent = `Chat con ID: ${amigoId}`; // Placeholder, puedes cambiar esto según tu lógica.

    $('#chatModal').modal('show'); // Muestra el modal del chat

    // Almacena el ID del destinatario en el campo oculto para usarlo al enviar mensajes
    document.getElementById('chatInput').setAttribute('data-amigo-id', amigoId);

}

function enviarMensaje() {
    const txtMensaje = document.getElementById("chatInput").value; // Obtiene el contenido del mensaje
    const remitenteId = document.getElementById("currentUserId").value; // Obtiene el ID del usuario actual
    const destinatarioId = document.getElementById('chatInput').getAttribute('data-amigo-id'); // Obtiene el ID del amigo

    // Validar que el mensaje no esté vacío
    if (!txtMensaje.trim()) {
        alert("El mensaje no puede estar vacío.");
        return;
    }

    // Verificar que stompCliente no sea nulo antes de publicar
    if (stompCliente) {
        stompCliente.publish({
            destination: '/app/envio', // Asegúrate de que la ruta sea correcta
            body: JSON.stringify({
                contenido: txtMensaje,
                remitenteId: remitenteId,
                destinatarioId: destinatarioId
            })
        });
        document.getElementById("chatInput").value = ''; // Limpia el campo de texto
    } else {
        console.error("No se ha establecido la conexión WebSocket.");
        alert("Error: no se pudo enviar el mensaje, la conexión WebSocket no está activa.");
    }
}

// 1. Definición de la variable para el cliente STOMP
let stompCliente = null;

// 2. Conexión WebSocket y configuración STOMP
const onConnectSocket = () => {
    console.log("Conectado al WebSocket."); // Mensaje de depuración
    stompCliente.subscribe('/tema/mensajes', (mensaje) => {
        mostrarMensaje(mensaje.body); // Maneja el mensaje recibido
    });
};

const onWebSocketClose = () => {
    if (stompCliente != null) {
        stompCliente.deactivate();
    }
};

function conectarWS() {
    var socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Conectado: ' + frame);
        stompClient.subscribe('/topic/mensajes', function(mensaje) {
            mostrarMensaje(JSON.parse(mensaje.body));
        });
    }, function(error) {
        console.error('Error de conexión:', error);
    });
}

// 3. Conexión al cargar el DOM
document.addEventListener("DOMContentLoaded", function() {
    conectarWS(); // Establecer la conexión WebSocket al cargar la página
});

function aceptarSolicitud(solicitudId) {
    // Obtener el token CSRF desde el meta tag
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajax({
        url: '/aceptarSolicitud',
        type: 'POST',
        data: { solicitudId: solicitudId },
        beforeSend: function (xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken); // Añadir el token CSRF al header
        },
        success: function(response) {
            location.reload(); // Recargar la página tras aceptar la solicitud
        },
        error: function(xhr) {
            alert('Error al aceptar la solicitud.');
        }
    });
}

 */


function rechazarSolicitud(solicitudId) {
    // Obtener el token CSRF desde el meta tag
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajax({
        url: '/rechazarSolicitud',
        type: 'POST',
        data: { solicitudId: solicitudId },
        beforeSend: function (xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken); // Añadir el token CSRF al header
        },
        success: function(response) {
            location.reload(); // Recargar la página tras rechazar la solicitud
        },
        error: function(xhr) {
            alert('Error al rechazar la solicitud.');
        }
    });
}

