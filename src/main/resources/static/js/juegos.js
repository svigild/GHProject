function showGameDetails(gameId) {
    $.ajax({
        url: '/juego/' + gameId,
        type: 'GET',
        success: function(data) {

            let screenshots = '';
            if (data.screenshots && data.screenshots.length > 0) {
                screenshots = `
    <div id="carouselScreenshots" class="carousel slide" data-ride="carousel">
        <div class="carousel-inner">
            ${data.screenshots.map((screen, index) => screen.image ? `
                <div class="carousel-item ${index === 0 ? 'active' : ''}">
                    <img src="${screen.image}" class="d-block w-100" alt="Screenshot">
                </div>` : '').join('')}
        </div>
        <a class="carousel-control-prev" href="#carouselScreenshots" role="button" data-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="sr-only">Previous</span>
        </a>
        <a class="carousel-control-next" href="#carouselScreenshots" role="button" data-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="sr-only">Next</span>
        </a>
    </div>`;
            }

            let description = data.description_raw ? data.description_raw : '';
            let releaseDate = data.released ? data.released : 'Fecha desconocida';
            let metacritic = data.metacritic ? data.metacritic : 'N/A';
            let platforms = data.platforms.map(p => `<p class="plataforma">${p.platform.name}</p>`).join('');

            let genres = data.genres.map(g => `<p class="genero">${g.name}</p>`).join('');
            console.log(data.genres)

            let developers = data.developers.map(d => d.name).join(', ');
            let publishers = data.publishers.map(p => p.name).join(', ');

            $('#gameDetails').html(`
                <div class="container">
                    <div class="row">
                        <div class="col-md-12 mb-3">
                            <img src="${data.background_image}" class="img-fluid" alt="Imagen del juego">
                        </div>
                        <div class="col-md-12">
                            <h3>${data.name} <span class="badge nota">${metacritic}</span></h3>
                            <p class="fecha-salida"> ${releaseDate}</p>
                            ${platforms}
                            ${genres}
                            <p>Desarrollado por: ${developers}</p>
                            <p>Publicado por: ${publishers}</p>
                            <p>${description}</p>
                        </div>
                        <div class="col-md-12">
                            <h5>Capturas de pantalla</h5>
                            <div>${screenshots}</div>
                        </div>
                    </div>
                </div>
            `);
            $('#gameModal').modal('show');
        },
        error: function(error) {
            console.error('Error al obtener los detalles del juego:', error);
        }
    });
}