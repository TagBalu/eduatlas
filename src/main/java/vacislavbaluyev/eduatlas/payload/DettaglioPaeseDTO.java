package vacislavbaluyev.eduatlas.payload;


import vacislavbaluyev.eduatlas.entities.Paese;
import vacislavbaluyev.eduatlas.entities.SistemaUniversitario;
import vacislavbaluyev.eduatlas.entities.SistemaValutazione;
import vacislavbaluyev.eduatlas.entities.TipoScala;

public record DettaglioPaeseDTO(String nome,
                                Integer anniScuolaObbligatoria,
                                String votoA,
                                String votoB,
                                String votoC,
                                String votoDE,
                                String votoF,
                                TipoScala scalaTipo,
                                Integer durataBaseanni,
                                Integer creditiPerAnno,
                                String livelloEQF)
{

    public static DettaglioPaeseDTO fromEntities(
            Paese paese,
            SistemaValutazione sistemaVal,
            SistemaUniversitario sistemaUni
    ){
        return new DettaglioPaeseDTO(
                paese.getNome(),
                paese.getAnniSculaObbligaroia(),
                sistemaVal.getVotoA(),
                sistemaVal.getVotoB(),
                sistemaVal.getVotoC(),
                sistemaVal.getVotoDE(),
                sistemaVal.getVotoF(),
                sistemaVal.getScalaTipo(),
                sistemaUni.getDurataBaseAnni(),
                sistemaUni.getCreditiPerAnno(),
                sistemaUni.getLivelloEQF()

        );
    }
}
