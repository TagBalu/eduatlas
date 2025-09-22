package vacislavbaluyev.eduatlas.payload;

public record SistemaValutazioneDTO(
        Long id,
        Long paeseId,
        String nomePaese,
        String votoA,       // modifica
        String votoB,       // modifica
        String votoC,       // modifica
        String votoDE,      // modifica
        String votoF,       // modifica
        vacislavbaluyev.eduatlas.entities.TipoScala scalaTipo
) {}