package hores;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HoresDsl {

    public static void main(String[] args) throws ParseException {
        HoresDsl h = new HoresDsl();

        h.r("10/12/2018", "07:45:00", "17:35:00");
        h.r("11/12/2018", "07:50:00", "17:45:00");
        h.r("12/12/2018", "07:45:00", "17:35:00");
        h.r("13/12/2018", "07:50:00", "17:45:00");
        h.r("14/12/2018", "07:45:00", "15:00:00");

        h.r("17/12/2018", "07:45:00", "19:00:00");

        h.r("01/01/2019", "07:45:00", "19:00:00");

        h.report();
    }

    static final SimpleDateFormat diaSdf = new SimpleDateFormat("dd/MM/yyyy");
    static final SimpleDateFormat horaSdf = new SimpleDateFormat("HH:mm:ss");

    List<DiaImputat> imputacions = new ArrayList<>();

    public HoresDsl r(String dia, String entrada, String sortida) {
        this.imputacions.add(new DiaImputat(dia, entrada, sortida));
        return this;
    }

    public static class DiaImputat {

        final String dia;
        final String entrada;
        final String sortida;

        public DiaImputat(String dia, String entrada, String sortida) {
            super();
            this.dia = dia;
            this.entrada = entrada;
            this.sortida = sortida;
        }

        public boolean isDivendres() {
            String nomDia = getNomDia();
            return "divendres".equals(nomDia);
        }

        public boolean isIntensiva() {
            String mes;
            try {
                mes = new SimpleDateFormat("MM", new Locale("ca", "ES")).format(diaSdf.parse(this.dia));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return mes.equals("07") || mes.equals("08");
        }

        /**
         * @return el nom del dia, en català (p.ex. "dimarts")
         */
        public String getNomDia() {
            try {
                return new SimpleDateFormat("EEEE", new Locale("ca", "ES")).format(diaSdf.parse(this.dia));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return el temps treballat, amb descansos corregits, i en millisecs
         */
        public long segonsTreballats() {
            if (entrada == null || sortida == null) {
                return 0L;
            }
            long secs;
            try {
                long entrada = horaSdf.parse(this.entrada).getTime();
                long sortida = horaSdf.parse(this.sortida).getTime();
                secs = (sortida - entrada) / 1_000L;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (isIntensiva()) {
                if (isDivendres()) {
                    secs -= 60 * 45;
                } else {
                    secs -= 60 * 30;
                }
            } else {
                if (isDivendres()) {
                    secs -= 60 * 45;
                } else {
                    secs -= 60 * (15 + 45);
                }
            }
            return secs;
        }

        /**
         * @return {@link #segonsTreballats()} en format 00:00:00
         */
        public String tempsTreballat() {
            return formataSegons(segonsTreballats());
        }

        /**
         * @return el time en millisecs del dia (sense hora)
         */
        public long getDiaTime() {
            try {
                return diaSdf.parse(dia).getTime();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return el # de mes (1=gener, 2=febrer, ...)
         */
        public int getNumMes() {
            final SimpleDateFormat numMesSdf = new SimpleDateFormat("MM");
            Date date;
            try {
                date = diaSdf.parse(dia);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return Integer.parseInt(numMesSdf.format(date));
        }

        /**
         * @return la línia del dia, en format report
         */
        @Override
        public String toString() {

            String s = String.format("%-10s %-10s    %8s => %-8s   %6s   %8s", //
                    getNomDia(), dia, //
                    entrada == null ? "" : entrada, //
                    sortida == null ? "" : sortida, //
                    "", // segonsTreballats(),
                    tempsTreballat());

            if (isDivendres()) {
                s += " (v)";
            }
            if (isIntensiva()) {
                s += " (*)";
            }
            return s;
        }
    }

    static String formataSegons(long secs) {
        long hores = secs / 60 / 60;
        long minuts = secs / 60 % 60;
        long segons = secs % 60 % 60;
        if (secs == 0L) {
            return "";
        }
        return String.format("%02d:%02d:%02d", hores, minuts, segons);
    }

    static Map<String, Integer> m = new LinkedHashMap<>();
    static {
        m.put("dilluns", 0);
        m.put("dimarts", 1);
        m.put("dimecres", 2);
        m.put("dijous", 3);
        m.put("divendres", 4);
        m.put("dissabte", 5);
        m.put("diumenge", 6);
    }

    /**
     * printa el report de imputacions, amb descansos corregits, i amb sumatoris
     * setmanals i mensuals, amb una fina detecció de salts de setmanes i mesos.
     */
    public void report() throws ParseException {

        DiaImputat lastImp = null;
        long segonsSetmanalsAcum = 0L;
        long segonsMensualsAcum = 0L;

        for (DiaImputat imp : imputacions) {

            if (lastImp != null) {
                /*
                 * detecta canvi de setmana: (1) per nom de dia es detecta un ordre invers (2)
                 * si entre imputacions hi ha més d'una setmana
                 */
                long t1 = lastImp.getDiaTime();
                long t2 = imp.getDiaTime();
                boolean canviSetmana;
                canviSetmana = m.get(lastImp.getNomDia()) > m.get(imp.getNomDia());
                canviSetmana = canviSetmana || t2 - t1 > 7 * 24L * 60L * 60L * 1000L;

                if (canviSetmana) {
                    System.out.println("------------------------------- total setmana: "
                            + formataSegons(segonsSetmanalsAcum) + " -------------");
                    segonsSetmanalsAcum = 0L;
                }

                /*
                 * detecta canvi de mes
                 */
                if (lastImp.getNumMes() != imp.getNumMes()) {
                    System.out.println("=============================== total mensual: "
                            + formataSegons(segonsMensualsAcum) + " =============");
                    segonsMensualsAcum = 0L;
                }
            }

            System.out.println(imp);

            long segonsTreballats = imp.segonsTreballats();
            segonsSetmanalsAcum += segonsTreballats;
            segonsMensualsAcum += segonsTreballats;

            lastImp = imp;
        }

        System.out.println("----------------------------- parcial setmana: " + formataSegons(segonsSetmanalsAcum)
                + " -------------");
        System.out.println("============================= parcial mensual: " + formataSegons(segonsMensualsAcum)
                + " =============");
    }
}
