package hores;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Hores {

    final static boolean TEST = true;
    // final static boolean TEST = false;

    static final CucarachaDb c;

    static {
        final String f;
        if (TEST) {
            f = "hores_test.properties";
        } else {
            f = "hores.properties";
        }
        c = new CucarachaDb(new File(f));
    }

    public static void main(String[] args) throws ParseException {

        if (TEST) {

            c.removeDatabase();

            fitxaManual("08/08/2018 07:40:00");
            fitxaManual("08/08/2018 15:00:00");
            fitxaManual("09/08/2018 07:40:00");
            fitxaManual("09/08/2018 15:00:00");

            fitxaManual("08/10/2018 07:40:00");
            fitxaManual("08/10/2018 17:35:00");
            fitxaManual("09/10/2018 07:40:00");
            fitxaManual("09/10/2018 17:35:00");
            fitxaManual("10/10/2018 07:40:00");
            fitxaManual("10/10/2018 17:35:00");
            fitxaManual("11/10/2018 07:40:00");
            fitxaManual("11/10/2018 17:35:00");
            fitxaManual("12/10/2018 07:40:00");
            fitxaManual("12/10/2018 15:00:00");

            fitxaManual("15/10/2018 07:40:00");
            fitxaManual("15/10/2018 17:35:00");
            fitxaManual("16/10/2018 07:40:00");
            fitxaManual("16/10/2018 17:35:00");

            fitxaManual("28/10/2018 08:00:00");

            fitxaManual("29/10/2018 08:00:00");
            fitxaManual("30/10/2018 15:00:00");
            fitxaManual("30/10/2018 08:00:00");
            fitxaManual("29/10/2018 18:00:00");
            fitxaManual("29/10/2018 08:13:00");

            fitxaManual("22/10/2018 07:40:00");
            fitxaManual("22/10/2018 17:30:00");
            fitxaManual("26/10/2018 08:13:00");
            fitxaManual("27/10/2018 08:13:00");

            fitxaManual("29/11/2018 07:40:00");
            fitxaManual("29/11/2018 18:37:30");
        }

        fitxa();
        report();
    }

    static final SimpleDateFormat diaSdf = new SimpleDateFormat("dd/MM/yyyy");
    static final SimpleDateFormat horaSdf = new SimpleDateFormat("HH.mm.ss");
    static final SimpleDateFormat manualSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static String formataSegons(long secs) {
        long hores = secs / 60 / 60;
        long minuts = secs / 60 % 60;
        long segons = secs % 60 % 60;
        if (secs == 0L) {
            return "";
        }
        return String.format("%02d:%02d:%02d", hores, minuts, segons);
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
    public static void report() throws ParseException {

        List<DiaImputat> imputacions = reportaImputacions();

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

    /**
     * @return obté les imputacions de l'store, ordenades ascendentment
     */
    protected static List<DiaImputat> reportaImputacions() throws ParseException {
        c.beginTransaction();
        try {

            /*
             * carrega les imputacions d'store, tot ordenat asc
             */
            TreeMap<Date, List<Date>> map = new TreeMap<>();
            {
                Map<String, String> all = c.find(null, null);

                for (Entry<String, String> e : all.entrySet()) {
                    Date dia = diaSdf.parse(e.getKey());
                    String[] is = c.get(e.getKey()).split("\\,\\s*");
                    List<Date> imputacions = new ArrayList<>();
                    for (String i : is) {
                        Date imputacio = horaSdf.parse(i);
                        imputacions.add(imputacio);
                    }
                    Collections.sort(imputacions);
                    map.put(dia, imputacions);
                }
            }

            /**
             * crea les entitats {@link DiaImputat}, en ordre asc
             */
            List<DiaImputat> r = new ArrayList<>();
            for (Entry<Date, List<Date>> e : map.entrySet()) {
                String dia = diaSdf.format(e.getKey());
                String entrada = null;
                String sortida = null;

                List<Date> fitxades = e.getValue();
                if (fitxades.size() >= 1) {
                    entrada = horaSdf.format(fitxades.get(0));
                    if (fitxades.size() > 1) {
                        sortida = horaSdf.format(fitxades.get(fitxades.size() - 1));
                    }
                }

                r.add(new DiaImputat(dia, entrada, sortida));
            }

            return r;

        } finally {
            c.rollback();
        }
    }

    /**
     * inserta el moment actual de la fitxada a l'store
     */
    public static void fitxa() {
        imputa(new Date());
    }

    /**
     * inserta el moment de la fitxada a l'store, de forma manual (per imputar a
     * posteriorio)
     */
    public static void fitxaManual(String date) throws ParseException {
        Date d = manualSdf.parse(date);
        imputa(d);
    }

    /**
     * inserta el moment donat de la fitxada a l'store
     */
    protected static void imputa(Date date) {
        c.beginTransaction();
        try {

            String key = diaSdf.format(date);
            String imputacio = horaSdf.format(date);

            String imputacionsStr;
            if (c.exist(key)) {
                imputacionsStr = c.get(key) + ", " + imputacio;
            } else {
                imputacionsStr = imputacio;
            }

            c.put(key, imputacionsStr);

            c.commit();
        } catch (Exception e) {
            c.rollback();
            throw new RuntimeException(e);
        }
    }

}
