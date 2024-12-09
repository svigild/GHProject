function eliminarTorneo(event, id) {
    event.preventDefault();
    if (confirm('¿Estás seguro de que quieres eliminar este torneo?')) {
        fetch(`/admin/torneos/eliminar/${id}`, { // Asegúrate de que la ruta sea correcta
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            }
        }).then(response => {
            if (response.ok) {
                window.location.href = '/admin'; // Redirige al panel de administración
            } else {
                alert('Error al eliminar el torneo');
            }
        });
    }
}