package hores;

public class Port {

    // Para decidir la categoría de una escalada Strava multiplica la longitud de la
    // escalada (en metros) con los grados de la escalada. Si ese número es mayor
    // que 8000 entonces es una subida categorizada.
    //
    // Cat 4 > 8000
    // Cat 3 > 16000
    // Cat 2 > 32000
    // Cat 1 > 48000
    // FC (Hors Categorie) > 64000
    //
    // 1 milla = 1609.34 metros
    //
    public static void main(String[] args) {

        // System.out.println(1.81 * 1609.34 * 5);
        // cate(1.81 * 1609.34 * 5);
        //
        // cate(3360 * 7);
        // System.out.println((242.0 / 3360) * 100);
        //
        cate(3360 * (242.0 / 3360) * 100);
        cate(1360 * (101.0 / 1360) * 100);

        cate(242.0 * 100);
        cate(101.0 * 100);

    }

    static void cate(double coef) {
        String r = "-";
        if (coef > 8000) {
            r = "4";
        }
        if (coef > 16000) {
            r = "3";
        }
        if (coef > 32000) {
            r = "2";
        }
        if (coef > 48000) {
            r = "1";
        }
        System.out.println(coef + " => Cat. " + r);
    }

}
