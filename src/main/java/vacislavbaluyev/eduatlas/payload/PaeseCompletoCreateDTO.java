package vacislavbaluyev.eduatlas.payload;

import vacislavbaluyev.eduatlas.entities.TipoScala;

public record PaeseCompletoCreateDTO(
        String nome,
        Integer anniScuolaObbligatoria,

        String votoMassimo,
        String votoMinimo,
        String votoSufficienza,
        TipoScala scalaTipo,

        Integer durataBaseAnni,
        Integer creditiPerAnno,
        String livelloEQF
) {
}
